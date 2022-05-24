package si.iskratel.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.cdr.parser.PpdrBean;
import si.iskratel.cdrparser.CdrData;
import si.iskratel.cdrparser.CdrParser;
import si.iskratel.metricslib.*;
import si.iskratel.simulator.model.xml.CdrAggs;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
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
public class CdrAggsToXml {

    private static Logger logger = LoggerFactory.getLogger(CdrAggsToXml.class);

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
    public static PMultiValueMetric mv_cdr_statistics = PMultiValueMetric.build()
            .setName("cdr_node_statistics")
            .setHelp("Current counters on node")
            .register(INDEX_CDRSTATS);


    private static Map<String, PMultivalueTimeSeries> tgMvSeriesMap = new HashMap<>();

    public static void main(String[] args) throws Exception {

        Props.SIMULATOR_EXIT_WHEN_DONE = true;
        Props.HANDLE_FILES_WHEN_PROCESSED = "nothing";

        logger.info("CdrAggsToItXml instantiated");

        String xProps = System.getProperty("cdrparser.configurationFile", "cdr_parser/cdr_parser.properties");
        Properties cdrProps = new Properties();
        try {
            cdrProps.load(new FileInputStream(xProps));
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
        }

        MetricsLib.init(cdrProps);

        while (true) {

            File inputDir = new File(CDR_INPUT_DIR);

            File[] nodeDirectories = inputDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });

            if (nodeDirectories.length == 0) {
                logger.info("no monitored nodes found");
            }

            for (File nodeDir : nodeDirectories) {

                File[] files = nodeDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isFile() && !pathname.getName().startsWith(".");// && pathname.getAbsolutePath().endsWith(".si2");
                    }
                });

                if (files.length == 0) logger.info("No CDR files found in " + CDR_INPUT_DIR + "/" + nodeDir.getName());

                int numberOfRecordsInCdrFile = 0;

                for (File cdrFile : files) {

                    PMetricRegistry.getRegistry(INDEX_CDRSTATS).resetMetrics();
//                    PMetricRegistry.getRegistry(INDEX_CDRMETRICS).resetMetrics();
                    tgMvSeriesMap.clear();

                    logger.info("Reading file: " + cdrFile.getAbsolutePath());

                    long timestamp = getSamplingTimeFromFilename(cdrFile.getName()).getTime();
                    CdrData data = CdrParser.parse(cdrFile);
                    data.nodeName = nodeDir.getName();
                    numberOfRecordsInCdrFile = data.cdrList.size();
                    cdr_files_total.setLabelValues("Success").inc();

                    PMultivalueTimeSeries mvts_node_statistics = new PMultivalueTimeSeries();
                    mvts_node_statistics
                            .addLabel("node.name", data.nodeName)
                            .addLabel("node.id", data.nodeName);

                    // analyze each record
                    for (int i = 0; i < data.cdrList.size(); i++) {
                        CdrBean cdrBean = data.cdrList.get(i);
                        cdrBean.setNodeId(data.nodeName);

                        // count how many seizures occurred in this interval
                        if (cdrBean.getSequence() == 1 || cdrBean.getSequence() == 2) {
                            mvts_node_statistics.incValue("node.seizures", 1);
                            increaseTrunkGroupSeriesValue(cdrBean, "TrunkGroup.seizures", 1);

                            // count seizures that have no release cause yet, but they have duration
                            // this indicates that the call was answered (used for calculating ASR in the same interval)
                            if (cdrBean.getDuration() > 0) {
                                mvts_node_statistics.incValue("node.seizuresWithAnswer", 1);
                                increaseTrunkGroupSeriesValue(cdrBean, "TrunkGroup.seizuresWithAnswer", 1);
                            }

                        }

                        // count how many calls are not finished yet in this interval
                        if (cdrBean.getSequence() == 2 || cdrBean.getSequence() == 3) {
                            mvts_node_statistics.incValue("node.active_calls", 1);
                        }

                        // count release causes from last cdr record
                        if (cdrBean.getSequence() == 1 || cdrBean.getSequence() == 4 || cdrBean.getSequence() == 5) {

                            switch (cdrBean.getCause()) {
                                case 16:
                                    mvts_node_statistics.incValue("node.answered", 1);
                                    increaseTrunkGroupSeriesValue(cdrBean, "TrunkGroup.answered", 1);
                                    break;
                                case 17:
                                    mvts_node_statistics.incValue("node.busy", 1);
                                    increaseTrunkGroupSeriesValue(cdrBean, "TrunkGroup.busy", 1);
                                    break;
                                case 19:
                                    mvts_node_statistics.incValue("node.noReply", 1);
                                    increaseTrunkGroupSeriesValue(cdrBean, "TrunkGroup.noReply", 1);
                                    break;
                                case 21:
                                    mvts_node_statistics.incValue("node.rejected", 1);
                                    increaseTrunkGroupSeriesValue(cdrBean, "TrunkGroup.rejected", 1);
                                    break;
                                default:
                                    mvts_node_statistics.incValue("node.other", 1);
                                    increaseTrunkGroupSeriesValue(cdrBean, "TrunkGroup.other", 1);
                            }

                            increaseReleaseCausesSeriesValue(cdrBean);

                        }

                        // summarize total call duration
                        if (cdrBean.getDuration() > 0) {
                            mvts_node_statistics.incValue("node.duration", cdrBean.getDuration());
                            increaseTrunkGroupSeriesValue(cdrBean, "TrunkGroup.duration", cdrBean.getDuration());
                        }

                        // summarize total call setup time
                        if (cdrBean.getCdrTimeBeforeRinging() != null) {
                            mvts_node_statistics.incValue("node.timeBeforeRinging", cdrBean.getCdrTimeBeforeRinging());
                            increaseTrunkGroupSeriesValue(cdrBean, "TrunkGroup.timeBeforeRinging", cdrBean.getDuration());
                        }

                        // summarize total time before answer
                        if (cdrBean.getCdrRingingTimeBeforeAnsw() != null) {
                            mvts_node_statistics.incValue("node.timeBeforeAnswer", cdrBean.getCdrRingingTimeBeforeAnsw());
                            increaseTrunkGroupSeriesValue(cdrBean, "TrunkGroup.timeBeforeAnswer", cdrBean.getDuration());
                        }

                    } // END analyze each record (foreach cdr bean)

                    // calculate traffic intensity and traffic volume
                    double d = mvts_node_statistics.getValuesMap().getOrDefault("node.duration", -1.0);
                    if (d != -1.0) {
                        d = d / 1000; // to seconds
                        double trInt = d / 900; // trInt: duration in sec / interval
                        double trVol = trInt * 0.25; // trVol: trInt * interval (h)
                        System.out.println("dur: " + d + ", trInt: " + trInt + ", trVol: " + trVol);
                        mvts_node_statistics.incValue("node.trafficIntensity", trInt);
                        mvts_node_statistics.incValue("node.trafficVolume", trVol);
                    }

                    // collect number of channels/trunks from ppdr records
                    for (int i = 0; i < data.ppdrList.size(); i++) {
                        PpdrBean ppdrBean = data.ppdrList.get(i);
                        PMultivalueTimeSeries m2 = tgMvSeriesMap.get("inc" + ppdrBean.getTrunkGroupName());
                        if (m2 != null) {
                            m2.incValue("incTrunkGroup.allChanels", ppdrBean.getNumberOfAllTrunks())
                                    .incValue("incTrunkGroup.outOfserviceChanels", ppdrBean.getNumberOfOutOfServiceTrunks());
                            tgMvSeriesMap.put("inc" + ppdrBean.getTrunkGroupName(), m2);
                        }

                        PMultivalueTimeSeries m3 = tgMvSeriesMap.get("out" + ppdrBean.getTrunkGroupName());
                        if (m3 != null) {
                            m3.incValue("outTrunkGroup.allChanels", ppdrBean.getNumberOfAllTrunks())
                                    .incValue("outTrunkGroup.outOfserviceChanels", ppdrBean.getNumberOfOutOfServiceTrunks());;
                            tgMvSeriesMap.put("out" + ppdrBean.getTrunkGroupName(), m3);
                        }

                    }

                    // merge time series into metric and prepare metric to be sent
                    for (PMultivalueTimeSeries ts : tgMvSeriesMap.values()) {
                        mv_cdr_statistics.addMultiValueTimeSeries(ts);
                    }
                    mv_cdr_statistics.addMultiValueTimeSeries(mvts_node_statistics);
                    mv_cdr_statistics.setTimestamp(timestamp);

                    System.out.println(mv_cdr_statistics.toStringDetail());

                    // TODO convert to XML
                    CdrAggs cdrAggs = new CdrAggs();
                    CdrAggs.Metadata metadata = new CdrAggs.Metadata();
                    CdrAggs.Statistics statistics = new CdrAggs.Statistics();
                    CdrAggs.Statistics.Node node = new CdrAggs.Statistics.Node();
                    CdrAggs.Statistics.TrunkGroups trunkGroups = new CdrAggs.Statistics.TrunkGroups();
                    List<CdrAggs.Statistics.TrunkGroups.TrunkGroup> trunkGroupList = new ArrayList<>();

                    // fill metadata
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(new Date());
                    metadata.setStartTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
                    metadata.setEndTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
                    metadata.setHostname(data.nodeName);
                    metadata.setNodeId(data.nodeName);
                    metadata.setFilename(cdrFile.getName());
                    metadata.setProductCategory("IE");

                    CdrAggs.Statistics.CallStats nodeCallStats = new CdrAggs.Statistics.CallStats();
                    nodeCallStats.setRecords(numberOfRecordsInCdrFile);
                    nodeCallStats.setSeizures(mvts_node_statistics.getValuesMap().getOrDefault("node.seizures", 0.0));
                    nodeCallStats.setSeizuresWithAnswer(mvts_node_statistics.getValuesMap().getOrDefault("node.seizuresWithAnswer", 0.0));
                    nodeCallStats.setActive(mvts_node_statistics.getValuesMap().getOrDefault("node.active_calls", 0.0));
                    nodeCallStats.setDuration(mvts_node_statistics.getValuesMap().getOrDefault("node.duration", 0.0));
                    nodeCallStats.setTrafficIntensity(mvts_node_statistics.getValuesMap().getOrDefault("node.trafficIntensity", 0.0));
                    nodeCallStats.setTrafficVolume(mvts_node_statistics.getValuesMap().getOrDefault("node.trafficVolume", 0.0));

                    for (PMultivalueTimeSeries ts : tgMvSeriesMap.values()) {
                        CdrAggs.Statistics.CallStats callStats = new CdrAggs.Statistics.CallStats();
                        callStats.setRecords(0);
                        callStats.setSeizures(ts.getValuesMap().getOrDefault("incTrunkGroup.seizures", 0.0));
                        callStats.setSeizuresWithAnswer(ts.getValuesMap().getOrDefault("incTrunkGroup.seizuresWithAnswer", 0.0));
                        callStats.setActive(ts.getValuesMap().getOrDefault("node.active_calls", 0.0));
                        callStats.setDuration(ts.getValuesMap().getOrDefault("incTrunkGroup.duration", 0.0));
                        callStats.setTrafficIntensity(mvts_node_statistics.getValuesMap().getOrDefault("incTrunkGroup.trafficIntensity", 0.0));
                        callStats.setTrafficVolume(mvts_node_statistics.getValuesMap().getOrDefault("incTrunkGroup.trafficVolume", 0.0));

                        CdrAggs.Statistics.TrunkGroups.TrunkGroup tg = new CdrAggs.Statistics.TrunkGroups.TrunkGroup();
                        tg.setId(ts.getLabelsMap().getOrDefault("incTrunkGroup.id", "-"));
                        tg.setName(ts.getLabelsMap().getOrDefault("incTrunkGroup.name", "-"));
                        tg.setDirection(ts.getLabelsMap().getOrDefault("incTrunkGroup.direction", "-"));
                        tg.setCallStats(callStats);

                        trunkGroupList.add(tg);
                    }

                    trunkGroups.setTrunkGroup(trunkGroupList);

                    node.setCallStats(nodeCallStats);
                    statistics.setNode(node);
                    statistics.setTrunkGroups(trunkGroups);
                    cdrAggs.setMetadata(metadata);
                    cdrAggs.setStatistics(statistics);

                    File xmlOutFile = new File("./dump/" + cdrFile.getName().split(".si2")[0] + ".xml");
                    JAXBContext jaxbContext = JAXBContext.newInstance(CdrAggs.class);
                    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                    jaxbMarshaller.marshal(cdrAggs, xmlOutFile);

//                    System.out.println(PMetricFormatter.toEsNdJsonString(mv_cdr_statistics));

//                    PMetricRegistry.getRegistry(INDEX_CDRMETRICS).setTimestamp(timestamp);
//                    es.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDRMETRICS));


                    // handle processed file
                    if (Props.HANDLE_FILES_WHEN_PROCESSED.equalsIgnoreCase("move")) {
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
                        String absPath = cdrFile.getAbsolutePath();
                        absPath = absPath.replace(CDR_INPUT_DIR, CDR_OUTPUT_DIR);
                        logger.info("Moving file to new location: " + absPath);
                        Files.move(Paths.get(cdrFile.getAbsolutePath()), Paths.get(absPath), StandardCopyOption.REPLACE_EXISTING);
                    } else if (Props.HANDLE_FILES_WHEN_PROCESSED.equalsIgnoreCase("delete")) {
                        cdrFile.delete();
                        logger.info("File deleted: " + cdrFile.getAbsolutePath());
                    } else {
                        // do nothing
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
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date d = cal.getTime();
        logger.info("time-conversion: file: " + filename + "   -->    time: " + Utils.toDateString(d));
        return d;

    }

    private static void increaseTrunkGroupSeriesValue(CdrBean cdrBean, String label, double value) {
        if (cdrBean.getInTrunkGroupNameIE144() != null) {
            PMultivalueTimeSeries m2 = tgMvSeriesMap.getOrDefault("inc" + cdrBean.getInTrunkGroupNameIE144(),
                    new PMultivalueTimeSeries());
            m2.addLabel("node.name", cdrBean.getNodeId())
                    .addLabel("node.id", cdrBean.getNodeId())
                    .addLabel("trunkGroup.name", cdrBean.getInTrunkGroupNameIE144())
                    .addLabel("trunkGroup.id", Integer.toString(cdrBean.getInTrunkGroupId()))
                    .addLabel("incTrunkGroup.id", Integer.toString(cdrBean.getInTrunkGroupId()))
                    .addLabel("incTrunkGroup.name", cdrBean.getInTrunkGroupNameIE144())
                    .incValue("inc" + label, value);
            tgMvSeriesMap.put("inc" + cdrBean.getInTrunkGroupNameIE144(), m2);
        }
        if (cdrBean.getOutTrunkGroupNameIE145() != null) {
            PMultivalueTimeSeries m3 = tgMvSeriesMap.getOrDefault("out" + cdrBean.getOutTrunkGroupNameIE145(),
                    new PMultivalueTimeSeries());
            m3.addLabel("node.name", cdrBean.getNodeId())
                    .addLabel("node.id", cdrBean.getNodeId())
                    .addLabel("trunkGroup.name", cdrBean.getOutTrunkGroupNameIE145())
                    .addLabel("trunkGroup.id", Integer.toString(cdrBean.getOutTrunkGroupId()))
                    .addLabel("outTrunkGroup.id", Integer.toString(cdrBean.getOutTrunkGroupId()))
                    .addLabel("outTrunkGroup.name", cdrBean.getOutTrunkGroupNameIE145())
                    .incValue("out" + label, value);
            tgMvSeriesMap.put("out" + cdrBean.getOutTrunkGroupNameIE145(), m3);
        }
    }

    private static void increaseReleaseCausesSeriesValue(CdrBean cdrBean) {
        int id = cdrBean.getCause();
        String name = cdrBean.getCauseString();

        if (id != 16 || id != 17 || id != 18 || id != 19 || id != 21 || id != 1 || id != 3 || id != 6
                || id != 25 || id != 27 || id != 31 || id != 34 || id != 23 || id != 127) {
            id = 0;
            name = "other";
        }

        PMultivalueTimeSeries m = tgMvSeriesMap.getOrDefault("relCause" + name, new PMultivalueTimeSeries());
        m.addLabel("node.name", cdrBean.getNodeId())
                .addLabel("node.id", cdrBean.getNodeId())
                .addLabel("releaseCause.name", name)
                .addLabel("releaseCause.id", Integer.toString(id))
                .incValue("releaseCause.count", 1);
        tgMvSeriesMap.put("relCause" + name, m);
    }

}
