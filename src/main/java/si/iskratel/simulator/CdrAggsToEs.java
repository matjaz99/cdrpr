package si.iskratel.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.cdr.parser.PpdrBean;
import si.iskratel.cdrparser.CdrData;
import si.iskratel.cdrparser.CdrParser;
import si.iskratel.metricslib.*;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Parse CDR files and aggregate them into multi-value metrics and store to Elasticsearch
 */
public class CdrAggsToEs {

    private static Logger logger = LoggerFactory.getLogger(CdrAggsToEs.class);

    public static String CDR_INPUT_DIR = "cdr_input_dir";
    public static String CDR_OUTPUT_DIR = "cdr_output_dir";

    public static final String INDEX_CDRSTATS = "cdrstats";
    public static final String INDEX_CDRMETRICS = "cdrmetrics";

    public static PMetric cdr_files_total = PMetric.build()
            .setName("cdrparser_processed_files_total")
            .setHelp("Number of processed files")
            .setLabelNames("status")
            .register();

    // multivalue metrics
    public static PMultiValueMetric mv_cdr_seizures = PMultiValueMetric.build()
            .setName("cdr_node_seizures")
            .setHelp("Number of call seizures")
            .register(INDEX_CDRSTATS);

    public static PMultiValueMetric mv_cdr_active_calls = PMultiValueMetric.build()
            .setName("cdr_active_calls")
            .setHelp("Number of active calls")
            .register(INDEX_CDRSTATS);

    public static PMultiValueMetric mv_cdr_release_causes = PMultiValueMetric.build()
            .setName("cdr_release_causes")
            .setHelp("Number of calls by release cause")
            .register(INDEX_CDRSTATS);

    public static PMultiValueMetric mv_cdr_durations = PMultiValueMetric.build()
            .setName("cdr_durations")
            .setHelp("Duration of calls")
            .register(INDEX_CDRSTATS);

    // metrics
    public static PMetric m_cdr_node_seizures = PMetric.build()
            .setName("cdr_node_seizures")
            .setHelp("Number of call seizures")
            .setLabelNames("nodeName")
            .register(INDEX_CDRMETRICS);

    public static PMetric m_cdr_node_seizure_answers = PMetric.build()
            .setName("cdr_node_seizures")
            .setHelp("Number of call seizures that are answered")
            .setLabelNames("nodeName")
            .register(INDEX_CDRMETRICS);

    public static PMetric m_cdr_inctg_seizures = PMetric.build()
            .setName("cdr_inctg_node_seizures")
            .setHelp("Number of call seizures on incoming trunk group")
            .setLabelNames("nodeName", "trunkGroup")
            .register(INDEX_CDRMETRICS);

    public static PMetric m_cdr_outtg_seizures = PMetric.build()
            .setName("cdr_outtg_node_seizures")
            .setHelp("Number of call seizures on outgoing trunk group")
            .setLabelNames("nodeName", "trunkGroup")
            .register(INDEX_CDRMETRICS);

    public static PMetric m_cdr_node_active_calls = PMetric.build()
            .setName("cdr_node_active_calls")
            .setHelp("Number of active calls")
            .setLabelNames("nodeName")
            .register(INDEX_CDRMETRICS);

    public static PMetric m_cdr_node_release_causes = PMetric.build()
            .setName("cdr_node_release_causes")
            .setHelp("Number of calls by release cause")
            .setLabelNames("nodeName", "releaseCause")
            .register(INDEX_CDRMETRICS);

    public static PMetric m_cdr_inctg_release_causes = PMetric.build()
            .setName("cdr_inctg_release_causes")
            .setHelp("Number of calls by release cause on incoming trunk group")
            .setLabelNames("nodeName", "trunkGroup", "releaseCause")
            .register(INDEX_CDRMETRICS);

    public static PMetric m_cdr_outtg_release_causes = PMetric.build()
            .setName("cdr_outtg_release_causes")
            .setHelp("Number of calls by release cause on outgoing trunk group")
            .setLabelNames("nodeName", "trunkGroup", "releaseCause")
            .register(INDEX_CDRMETRICS);

    public static PMetric m_cdr_node_durations = PMetric.build()
            .setName("cdr_durations")
            .setHelp("Duration of calls")
            .setLabelNames("nodeName")
            .register(INDEX_CDRMETRICS);

    public static PMetric m_cdr_inctg_durations = PMetric.build()
            .setName("cdr_inctg_durations")
            .setHelp("Duration of calls on incoming trunk group")
            .setLabelNames("nodeName", "trunkGroup")
            .register(INDEX_CDRMETRICS);

    public static PMetric m_cdr_outtg_durations = PMetric.build()
            .setName("cdr_outtg_durations")
            .setHelp("Duration of calls on outgoing trunk group")
            .setLabelNames("nodeName", "trunkGroup")
            .register(INDEX_CDRMETRICS);

    private static Map<String, PMultivalueTimeSeries> seriesMap = new HashMap<>();

    public static void main(String[] args) throws Exception {

//        Props.EXIT_WHEN_DONE = true;
        Props.SIMULATOR_MOVE_FILES_WHEN_PROCESSED = false;

        String xProps = System.getProperty("cdrparser.configurationFile", "cdr_parser/cdr_parser.properties");
        Properties cdrProps = new Properties();
        try {
            cdrProps.load(new FileInputStream(xProps));
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
        }

        MetricsLib.init(cdrProps);
        EsClient es = MetricsLib.getClientInstance();

//        EsClient es = new EsClient(cdrProps.getProperty("metricslib.elasticsearch.default.schema"),
//                cdrProps.getProperty("metricslib.elasticsearch.default.host"),
//                Integer.parseInt(cdrProps.getProperty("metricslib.elasticsearch.default.port")));

//        while (!EsClient.ES_IS_READY) {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        if (Props.CLIENT_WAIT_UNTIL_READY) es.waitUntilElasticsearchIsReady();

        while (true) {

            File inputDir = new File(CDR_INPUT_DIR);

            File[] nodeDirectories = inputDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });

            for (File nodeDir : nodeDirectories) {

                File[] files = nodeDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isFile() && !pathname.getName().startsWith(".");// && pathname.getAbsolutePath().endsWith(".si2");
                    }
                });

                if (files.length == 0) logger.info("No CDR files found in " + CDR_INPUT_DIR + "/" + nodeDir.getName());

                for (File f : files) {

                    PMetricRegistry.getRegistry(INDEX_CDRSTATS).resetMetrics();
                    PMetricRegistry.getRegistry(INDEX_CDRMETRICS).resetMetrics();
                    seriesMap.clear();

                    logger.info("Reading file: " + f.getAbsolutePath());

                    long timestamp = getSamplingTimeFromFilename(f.getName()).getTime();
                    CdrData data = CdrParser.parse(f);
                    data.nodeName = nodeDir.getName();
                    cdr_files_total.setLabelValues("Success").inc();

                    PMultivalueTimeSeries mvts_node_statistics = new PMultivalueTimeSeries();
                    mvts_node_statistics
                            .addLabel("node.name", data.nodeName)
                            .addLabel("node.id", data.nodeName);
//                    PMultivalueTimeSeries mvts_node_release_causes = new PMultivalueTimeSeries();

                    for (int i = 0; i < data.cdrList.size(); i++) {
                        CdrBean cdrBean = data.cdrList.get(i);
                        cdrBean.setNodeId(data.nodeName);

                        if (cdrBean.getSequence() == 1 || cdrBean.getSequence() == 2) {

                            mvts_node_statistics.incValue("node.seizures", 1);

                            increaseTrunkGroupSeriesValue(cdrBean, "TrunkGroup.seizures", 1);
//                            if (cdrBean.getInTrunkGroupNameIE144() != null) {
//                                PMultivalueTimeSeries m2 = seriesMap.getOrDefault(cdrBean.getInTrunkGroupNameIE144() + "-inc",
//                                        new PMultivalueTimeSeries());
//                                m2.addLabel("node.name", cdrBean.getNodeId())
//                                        .addLabel("node.id", cdrBean.getNodeId())
//                                        .addLabel("trunkGroup.name", cdrBean.getInTrunkGroupNameIE144())
//                                        .addLabel("incTrunkGroup.id", Integer.toString(cdrBean.getInTrunkGroupId()))
//                                        .addLabel("incTrunkGroup.name", cdrBean.getInTrunkGroupNameIE144())
//                                        .incValue("incTrunkGroup.seizures", 1);
//                                seriesMap.put(cdrBean.getInTrunkGroupNameIE144() + "-inc", m2);
//                            }
//                            if (cdrBean.getOutTrunkGroupNameIE145() != null) {
//                                PMultivalueTimeSeries m3 = seriesMap.getOrDefault(cdrBean.getOutTrunkGroupNameIE145() + "-out",
//                                        new PMultivalueTimeSeries());
//                                m3.addLabel("node.name", cdrBean.getNodeId())
//                                        .addLabel("node.id", cdrBean.getNodeId())
//                                        .addLabel("trunkGroup.name", cdrBean.getOutTrunkGroupNameIE145())
//                                        .addLabel("outTrunkGroup.id", Integer.toString(cdrBean.getOutTrunkGroupId()))
//                                        .addLabel("outTrunkGroup.name", cdrBean.getOutTrunkGroupNameIE145())
//                                        .incValue("outTrunkGroup.seizures", 1);
//                                seriesMap.put(cdrBean.getOutTrunkGroupNameIE145() + "-out", m3);
//                            }

                            m_cdr_node_seizures.setLabelValues(data.nodeName).inc();
                            m_cdr_inctg_seizures.setLabelValues(data.nodeName, cdrBean.getInTrunkGroupNameIE144()).inc();
                            m_cdr_outtg_seizures.setLabelValues(data.nodeName, cdrBean.getOutTrunkGroupNameIE145()).inc();

                            if (cdrBean.getDuration() > 0) m_cdr_node_seizure_answers.setLabelValues(data.nodeName).inc();

                        }
                        if (cdrBean.getSequence() == 3) {
                            mvts_node_statistics.incValue("node.active_calls", 1);
                        }
                        if (cdrBean.getSequence() == 1 || cdrBean.getSequence() == 4 || cdrBean.getSequence() == 5) {
                            m_cdr_node_release_causes.setLabelValues(data.nodeName, cdrBean.getCauseString()).inc();
                            m_cdr_inctg_release_causes.setLabelValues(data.nodeName, cdrBean.getInTrunkGroupNameIE144(), cdrBean.getCauseString()).inc();
                            m_cdr_outtg_release_causes.setLabelValues(data.nodeName, cdrBean.getOutTrunkGroupNameIE145(), cdrBean.getCauseString()).inc();
                        }
                        if (cdrBean.getDuration() > 0) {
                            mvts_node_statistics.incValue("node.duration", cdrBean.getDuration());

                            increaseTrunkGroupSeriesValue(cdrBean, "TrunkGroup.duration", cdrBean.getDuration());
//                            if (cdrBean.getInTrunkGroupNameIE144() != null) {
//                                PMultivalueTimeSeries m2 = seriesMap.getOrDefault(cdrBean.getInTrunkGroupNameIE144() + "-inc",
//                                        new PMultivalueTimeSeries());
//                                m2.addLabel("node.name", cdrBean.getNodeId())
//                                        .addLabel("node.id", cdrBean.getNodeId())
//                                        .addLabel("trunkGroup.name", cdrBean.getInTrunkGroupNameIE144())
//                                        .addLabel("incTrunkGroup.id", Integer.toString(cdrBean.getInTrunkGroupId()))
//                                        .addLabel("incTrunkGroup.name", cdrBean.getInTrunkGroupNameIE144())
//                                        .addLabel("incTrunkGroup.direction", "incoming")
//                                        .incValue("incTrunkGroup.duration", cdrBean.getDuration());
//                                seriesMap.put(cdrBean.getInTrunkGroupNameIE144() + "-inc", m2);
//                            }
//                            if (cdrBean.getOutTrunkGroupNameIE145() != null) {
//                                PMultivalueTimeSeries m3 = seriesMap.getOrDefault(cdrBean.getOutTrunkGroupNameIE145() + "-out",
//                                        new PMultivalueTimeSeries());
//                                m3.addLabel("node.name", cdrBean.getNodeId())
//                                        .addLabel("node.id", cdrBean.getNodeId())
//                                        .addLabel("trunkGroup.name", cdrBean.getOutTrunkGroupNameIE145())
//                                        .addLabel("outTrunkGroup.id", Integer.toString(cdrBean.getOutTrunkGroupId()))
//                                        .addLabel("outTrunkGroup.name", cdrBean.getOutTrunkGroupNameIE145())
//                                        .addLabel("outTrunkGroup.direction", "outgoing")
//                                        .incValue("outTrunkGroup.duration", cdrBean.getDuration());
//                                seriesMap.put(cdrBean.getOutTrunkGroupNameIE145() + "-out", m3);
//                            }


                            m_cdr_node_durations.setLabelValues(data.nodeName).inc(cdrBean.getDuration());
                            m_cdr_inctg_durations.setLabelValues(data.nodeName, cdrBean.getInTrunkGroupNameIE144()).inc(cdrBean.getDuration());
                            m_cdr_outtg_durations.setLabelValues(data.nodeName, cdrBean.getOutTrunkGroupNameIE145()).inc(cdrBean.getDuration());
                        }

                        if (cdrBean.getCause() == 16) {
                            mvts_node_statistics.incValue("node.answered", 1);

                            increaseTrunkGroupSeriesValue(cdrBean, "TrunkGroup.answered", 1);
//                            if (cdrBean.getInTrunkGroupNameIE144() != null) {
//                                PMultivalueTimeSeries m2 = seriesMap.getOrDefault(cdrBean.getInTrunkGroupNameIE144() + "-inc",
//                                        new PMultivalueTimeSeries());
//                                m2.addLabel("node.name", cdrBean.getNodeId())
//                                        .addLabel("node.id", cdrBean.getNodeId())
//                                        .addLabel("trunkGroup.name", cdrBean.getInTrunkGroupNameIE144())
//                                        .addLabel("incTrunkGroup.id", Integer.toString(cdrBean.getInTrunkGroupId()))
//                                        .addLabel("incTrunkGroup.name", cdrBean.getInTrunkGroupNameIE144())
//                                        .addLabel("incTrunkGroup.direction", "incoming")
//                                        .incValue("incTrunkGroup.answered", 1);
//                                seriesMap.put(cdrBean.getInTrunkGroupNameIE144() + "-inc", m2);
//                            }
//                            if (cdrBean.getOutTrunkGroupNameIE145() != null) {
//                                PMultivalueTimeSeries m3 = seriesMap.getOrDefault(cdrBean.getOutTrunkGroupNameIE145() + "-out",
//                                        new PMultivalueTimeSeries());
//                                m3.addLabel("node.name", cdrBean.getNodeId())
//                                        .addLabel("node.id", cdrBean.getNodeId())
//                                        .addLabel("trunkGroup.name", cdrBean.getOutTrunkGroupNameIE145())
//                                        .addLabel("outTrunkGroup.id", Integer.toString(cdrBean.getOutTrunkGroupId()))
//                                        .addLabel("outTrunkGroup.name", cdrBean.getOutTrunkGroupNameIE145())
//                                        .addLabel("outTrunkGroup.direction", "outgoing")
//                                        .incValue("outTrunkGroup.answered", 1);
//                                seriesMap.put(cdrBean.getOutTrunkGroupNameIE145() + "-out", m3);
//                            }

                        }

                        if (cdrBean.getCause() == 17) {
                            mvts_node_statistics.incValue("node.busy", 1);

                            increaseTrunkGroupSeriesValue(cdrBean, "TrunkGroup.busy", 1);
//                            if (cdrBean.getInTrunkGroupNameIE144() != null) {
//                                PMultivalueTimeSeries m2 = seriesMap.getOrDefault(cdrBean.getInTrunkGroupNameIE144() + "-inc",
//                                        new PMultivalueTimeSeries());
//                                m2.addLabel("node.name", cdrBean.getNodeId())
//                                        .addLabel("node.id", cdrBean.getNodeId())
//                                        .addLabel("trunkGroup.name", cdrBean.getInTrunkGroupNameIE144())
//                                        .addLabel("incTrunkGroup.id", Integer.toString(cdrBean.getInTrunkGroupId()))
//                                        .addLabel("incTrunkGroup.name", cdrBean.getInTrunkGroupNameIE144())
//                                        .addLabel("incTrunkGroup.direction", "incoming")
//                                        .incValue("incTrunkGroup.busy", 1);
//                                seriesMap.put(cdrBean.getInTrunkGroupNameIE144() + "-inc", m2);
//                            }
//                            if (cdrBean.getOutTrunkGroupNameIE145() != null) {
//                                PMultivalueTimeSeries m3 = seriesMap.getOrDefault(cdrBean.getOutTrunkGroupNameIE145() + "-out",
//                                        new PMultivalueTimeSeries());
//                                m3.addLabel("node.name", cdrBean.getNodeId())
//                                        .addLabel("node.id", cdrBean.getNodeId())
//                                        .addLabel("trunkGroup.name", cdrBean.getOutTrunkGroupNameIE145())
//                                        .addLabel("outTrunkGroup.id", Integer.toString(cdrBean.getOutTrunkGroupId()))
//                                        .addLabel("outTrunkGroup.name", cdrBean.getOutTrunkGroupNameIE145())
//                                        .addLabel("outTrunkGroup.direction", "outgoing")
//                                        .incValue("outTrunkGroup.busy", 1);
//                                seriesMap.put(cdrBean.getOutTrunkGroupNameIE145() + "-out", m3);
//                            }

                        }

                        if (cdrBean.getCdrTimeBeforeRinging() != null) {
                            mvts_node_statistics.incValue("node.timeBeforeRinging", cdrBean.getCdrTimeBeforeRinging());
                        }

                        if (cdrBean.getCdrRingingTimeBeforeAnsw() != null) {
                            mvts_node_statistics.incValue("node.timeBeforeAnswer", cdrBean.getCdrRingingTimeBeforeAnsw());
                        }

                    }

                    for (int i = 0; i < data.ppdrList.size(); i++) {
                        PpdrBean ppdrBean = data.ppdrList.get(i);
                        PMultivalueTimeSeries m2 = seriesMap.get(ppdrBean.getTrunkGroupName() + "-inc");
                        if (m2 != null) {
                            m2.incValue("incTrunkGroup.allChanels", ppdrBean.getNumberOfAllTrunks())
                                    .incValue("incTrunkGroup.outOfserviceChanels", ppdrBean.getNumberOfOutOfServiceTrunks());
                            seriesMap.put(ppdrBean.getTrunkGroupName() + "-inc", m2);
                        }

                        PMultivalueTimeSeries m3 = seriesMap.get(ppdrBean.getTrunkGroupName() + "-out");
                        if (m3 != null) {
                            m3.incValue("outTrunkGroup.allChanels", ppdrBean.getNumberOfAllTrunks())
                                    .incValue("outTrunkGroup.outOfserviceChanels", ppdrBean.getNumberOfOutOfServiceTrunks());;
                            seriesMap.put(ppdrBean.getTrunkGroupName() + "-out", m3);
                        }

                    }


                    for (PMultivalueTimeSeries ts : seriesMap.values()) {
                        mv_cdr_seizures.addMultiValueTimeSeries(ts);
                    }
                    mv_cdr_seizures.addMultiValueTimeSeries(mvts_node_statistics);
                    mv_cdr_seizures.setTimestamp(timestamp);
////                    System.out.println(cdr_seizures.toStringDetail());
                    es.sendBulkPost(mv_cdr_seizures);

//                    mv_cdr_active_calls.addMultiValueTimeSeries(mvts_node_active_calls);
//                    mv_cdr_active_calls.setTimestamp(timestamp);
////                    System.out.println(cdr_active_calls.toStringDetail());
//                    es.sendBulkPost(mv_cdr_active_calls);

//                    mv_cdr_durations.addMultiValueTimeSeries(mvts_node_durations);
//                    mv_cdr_durations.setTimestamp(timestamp);
////                    System.out.println(cdr_durations.toStringDetail());
//                    es.sendBulkPost(mv_cdr_durations);

//                    mv_cdr_release_causes.addMultiValueTimeSeries(mvts_node_release_causes);
//                    mv_cdr_release_causes.setTimestamp(timestamp);
////                    System.out.println(cdr_release_causes.toStringDetail());
//                    es.sendBulkPost(mv_cdr_release_causes);

                    PMetricRegistry.getRegistry(INDEX_CDRMETRICS).setTimestamp(timestamp);

                    es.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDRMETRICS));


                    if (Props.SIMULATOR_MOVE_FILES_WHEN_PROCESSED) {
                        // create new output node dir
                        String nodeOutDir = nodeDir.getAbsolutePath();
                        nodeOutDir = nodeOutDir.replace(CDR_INPUT_DIR, CDR_OUTPUT_DIR);
                        File nodeOutDirFile = new File(nodeOutDir);
                        if (!nodeOutDirFile.exists()) {
                            logger.info("Creating new output directory: " + nodeOutDir);
                            Path outDir = Paths.get(nodeOutDir);
                            Files.createDirectories(outDir);
                        }

                        // move processed file
                        String absPath = f.getAbsolutePath();
                        absPath = absPath.replace(CDR_INPUT_DIR, CDR_OUTPUT_DIR);
                        logger.info("Moving file to new location: " + absPath);
                        Files.move(Paths.get(f.getAbsolutePath()), Paths.get(absPath), StandardCopyOption.REPLACE_EXISTING);
                    }


                } // END foreach file

                if (files.length > 0) logger.info("Processed CDR files: " + files.length);

            } // END foreach directory

            if (Props.SIMULATOR_EXIT_WHEN_DONE) break; // run only once

            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } // END while true

        System.exit(0);

    }


    public static Date getSamplingTimeFromFilename(String filename) {

        filename = filename.replace(".si2", "");

        int x = filename.length();
        String second = filename.substring(x-2, x);
        String minute = filename.substring(x-4, x-2);
        String hour = filename.substring(x-6, x-4);
        String day = filename.substring(x-8, x-6);
        String month = filename.substring(x-10, x-8);
        String year = filename.substring(x-14, x-10);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(year));
        cal.set(Calendar.MONTH, Integer.parseInt(month) - 1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
        cal.set(Calendar.MINUTE, Integer.parseInt(minute));
        cal.set(Calendar.SECOND, Integer.parseInt(second));
        cal.set(Calendar.MILLISECOND, 0);

        Date d = cal.getTime();
        logger.info("time-conversion: file: " + filename + "   -->    time: " + Utils.toDateString(d));
        return d;

    }

    private static void increaseTrunkGroupSeriesValue(CdrBean cdrBean, String key, double value) {
        if (cdrBean.getInTrunkGroupNameIE144() != null) {
            PMultivalueTimeSeries m2 = seriesMap.getOrDefault(cdrBean.getInTrunkGroupNameIE144() + "-inc",
                    new PMultivalueTimeSeries());
            m2.addLabel("node.name", cdrBean.getNodeId())
                    .addLabel("node.id", cdrBean.getNodeId())
                    .addLabel("trunkGroup.name", cdrBean.getInTrunkGroupNameIE144())
                    .addLabel("incTrunkGroup.id", Integer.toString(cdrBean.getInTrunkGroupId()))
                    .addLabel("incTrunkGroup.name", cdrBean.getInTrunkGroupNameIE144())
                    .incValue("inc" + key, value);
            seriesMap.put(cdrBean.getInTrunkGroupNameIE144() + "-inc", m2);
        }
        if (cdrBean.getOutTrunkGroupNameIE145() != null) {
            PMultivalueTimeSeries m3 = seriesMap.getOrDefault(cdrBean.getOutTrunkGroupNameIE145() + "-out",
                    new PMultivalueTimeSeries());
            m3.addLabel("node.name", cdrBean.getNodeId())
                    .addLabel("node.id", cdrBean.getNodeId())
                    .addLabel("trunkGroup.name", cdrBean.getOutTrunkGroupNameIE145())
                    .addLabel("outTrunkGroup.id", Integer.toString(cdrBean.getOutTrunkGroupId()))
                    .addLabel("outTrunkGroup.name", cdrBean.getOutTrunkGroupNameIE145())
                    .incValue("out" + key, value);
            seriesMap.put(cdrBean.getOutTrunkGroupNameIE145() + "-out", m3);
        }
    }

}
