package si.iskratel.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.cdr.parser.PpdrBean;
import si.iskratel.cdrparser.CdrData;
import si.iskratel.cdrparser.CdrParser;
import si.iskratel.metricslib.*;
import si.iskratel.simulator.model.xml.CdrAggs;
import si.iskratel.simulator.model.xml.XmlFormatter;

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

//        Props.SIMULATOR_EXIT_WHEN_DONE = true;
//        Props.HANDLE_FILES_WHEN_PROCESSED = "nothing";

        logger.info("CdrAggsToXml instantiated");

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

                String nodeName = nodeDir.getName();
                String nodeId = nodeDir.getName();

                for (File cdrFile : files) {

                    PMetricRegistry.getRegistry(INDEX_CDRSTATS).resetMetrics();
                    tgMvSeriesMap.clear();

                    logger.info("Reading file: " + cdrFile.getAbsolutePath());

                    long timestamp = getSamplingTimeFromFilename(cdrFile.getName()).getTime();
                    CdrData data = CdrParser.parse(cdrFile);
                    data.nodeName = nodeDir.getName();
                    cdr_files_total.setLabelValues("Success").inc();

                    PMultivalueTimeSeries mvts_cdr_statistics = new PMultivalueTimeSeries();
                    mvts_cdr_statistics
                            .addLabel("node.name", data.nodeName)
                            .addLabel("node.id", data.nodeName)
                            .addLabel("metric", "nodeStats");

                    // analyze each record - start counting
                    for (int i = 0; i < data.cdrList.size(); i++) {
                        CdrBean cdrBean = data.cdrList.get(i);
                        cdrBean.setNodeId(data.nodeName);

                        mvts_cdr_statistics.incValue("node.records", 1);
                        increaseTrunkGroupSeriesValue(cdrBean, "trunkGroup.records", 1);

                        // count how many seizures occurred in this interval
                        if (cdrBean.getSequence() == 1 || cdrBean.getSequence() == 2) {
                            mvts_cdr_statistics.incValue("node.seizures", 1);
                            increaseTrunkGroupSeriesValue(cdrBean, "trunkGroup.seizures", 1);

                            // count seizures that have no release cause yet, but they have duration > 0
                            // this indicates that the call was answered (for calculating ASR inside the same interval)
                            if (cdrBean.getDuration() > 0) {
                                mvts_cdr_statistics.incValue("node.seizuresWithAnswer", 1);
                                increaseTrunkGroupSeriesValue(cdrBean, "trunkGroup.seizuresWithAnswer", 1);
                            }

                        }

                        // count active calls (started, but not finished yet in this interval)
                        if (cdrBean.getSequence() == 2 || cdrBean.getSequence() == 3) {
                            mvts_cdr_statistics.incValue("node.activeCalls", 1);
                            increaseTrunkGroupSeriesValue(cdrBean, "trunkGroup.activeCalls", 1);
                        }

                        // count release causes from last cdr record
                        if (cdrBean.getSequence() == 1 || cdrBean.getSequence() == 4 || cdrBean.getSequence() == 5) {

                            switch (cdrBean.getCause()) {
                                case 16:
                                    mvts_cdr_statistics.incValue("cause.answered", 1);
                                    increaseTrunkGroupSeriesValue(cdrBean, "cause.answered", 1);
                                    break;
                                case 17:
                                    mvts_cdr_statistics.incValue("cause.busy", 1);
                                    increaseTrunkGroupSeriesValue(cdrBean, "cause.busy", 1);
                                    break;
                                case 19:
                                    mvts_cdr_statistics.incValue("cause.noResponse", 1);
                                    increaseTrunkGroupSeriesValue(cdrBean, "cause.noResponse", 1);
                                    break;
                                case 21:
                                    mvts_cdr_statistics.incValue("cause.rejected", 1);
                                    increaseTrunkGroupSeriesValue(cdrBean, "cause.rejected", 1);
                                    break;
                                default:
                                    mvts_cdr_statistics.incValue("cause.other", 1);
                                    increaseTrunkGroupSeriesValue(cdrBean, "cause.other", 1);
                            }

                            // increaseReleaseCausesSeriesValue(cdrBean); // what does this do?

                        }

                        // summarize total call duration
                        if (cdrBean.getDuration() > 0) {
                            mvts_cdr_statistics.incValue("node.duration", cdrBean.getDuration());
                            increaseTrunkGroupSeriesValue(cdrBean, "trunkGroup.duration", cdrBean.getDuration());
                        }

                        // summarize total call setup time
                        if (cdrBean.getCdrTimeBeforeRinging() != null) {
                            mvts_cdr_statistics.incValue("node.timeBeforeRinging", cdrBean.getCdrTimeBeforeRinging());
                            increaseTrunkGroupSeriesValue(cdrBean, "trunkGroup.timeBeforeRinging", cdrBean.getDuration());
                        }

                        // summarize total time before answer
                        if (cdrBean.getCdrRingingTimeBeforeAnsw() != null) {
                            mvts_cdr_statistics.incValue("node.timeBeforeAnswer", cdrBean.getCdrRingingTimeBeforeAnsw());
                            increaseTrunkGroupSeriesValue(cdrBean, "trunkGroup.timeBeforeAnswer", cdrBean.getDuration());
                        }

                    } // END analyze each record (foreach cdr bean)

                    // calculate traffic intensity and traffic volume
                    double dur = mvts_cdr_statistics.getValuesMap().getOrDefault("node.duration", -1.0);
                    if (dur != -1.0) {
                        dur = dur / 1000; // to seconds
                        double trInt = dur / 900; // trInt: total duration in sec / interval
                        double trVol = trInt * 0.25; // trVol: trInt * interval (h)
                        System.out.println("dur: " + dur + " [sec], trInt: " + trInt + " [E], trVol: " + trVol + " [Eh]");
                        mvts_cdr_statistics.incValue("kpi.trafficIntensity", trInt);
                        mvts_cdr_statistics.incValue("kpi.trafficVolume", trVol);
                    }

                    // calculate ASR
                    double seiz = mvts_cdr_statistics.getValuesMap().getOrDefault("node.seizures", 0.0);
                    double seizAns = mvts_cdr_statistics.getValuesMap().getOrDefault("node.seizuresWithAnswer", 0.0);
                    if (seiz > 0.0) {
                        mvts_cdr_statistics.incValue("kpi.asr", seizAns/seiz * 1.0);
                    }

                    // do the same for trunk groups
                    for (PMultivalueTimeSeries ts : tgMvSeriesMap.values()) {
                        if (ts.getValuesMap().containsKey("trunkGroup.duration")) {
                            // calculate traffic intensity and traffic volume for each trunk group
                            double tgDur = ts.getValuesMap().getOrDefault("trunkGroup.duration", -1.0);
                            if (tgDur != -1.0) {
                                tgDur = tgDur / 1000; // to seconds
                                double trInt = tgDur / 900; // trInt: total duration in sec / interval
                                double trVol = trInt * 0.25; // trVol: trInt * interval (h)
                                System.out.println("dur: " + tgDur + " [sec], trInt: " + trInt + " [E], trVol: " + trVol + " [Eh]");
                                ts.incValue("kpi.trafficIntensity", trInt);
                                ts.incValue("kpi.trafficVolume", trVol);
                            }
                        }
                        if (ts.getValuesMap().containsKey("trunkGroup.seizures")) {
                            // calculate ASR
                            double tgSeiz = mvts_cdr_statistics.getValuesMap().getOrDefault("trunkGroup.seizures", 0.0);
                            double tgSeizAns = mvts_cdr_statistics.getValuesMap().getOrDefault("trunkGroup.seizuresWithAnswer", 0.0);
                            if (tgSeiz > 0.0) {
                                ts.incValue("kpi.asr", tgSeizAns/tgSeiz);
                            }
                        }
                    }

                    // collect number of channels/trunks from ppdr records
                    for (int i = 0; i < data.ppdrList.size(); i++) {
                        PpdrBean ppdrBean = data.ppdrList.get(i);
                        PMultivalueTimeSeries channels = new PMultivalueTimeSeries();
                        channels.addLabel("node.name", nodeName)
                                .addLabel("node.id", nodeId)
                                .addLabel("trunkGroup.name", ppdrBean.getTrunkGroupName())
                                .addLabel("trunkGroup.id", Integer.toString(ppdrBean.getTrunkGroupId()))
                                .addLabel("metric", "tgChannels")
                                .incValue("trunkGroup.allChannels", ppdrBean.getNumberOfAllTrunks())
                                .incValue("trunkGroup.outOfServiceChannels", ppdrBean.getNumberOfOutOfServiceTrunks());
                        tgMvSeriesMap.put(ppdrBean.getTrunkGroupName(), channels);

                    }

                    // merge time series into metric and prepare metric to be sent
                    for (PMultivalueTimeSeries ts : tgMvSeriesMap.values()) {
                        mv_cdr_statistics.addMultiValueTimeSeries(ts);
                    }
                    mv_cdr_statistics.addMultiValueTimeSeries(mvts_cdr_statistics);
                    mv_cdr_statistics.setTimestamp(timestamp);

                    System.out.println(mv_cdr_statistics.toStringDetail());

                    // convert to XML
                    CdrAggs cdrAggs = XmlFormatter.getCdrAggsRootElement(data, cdrFile.getName(), mv_cdr_statistics);

                    File xmlOutFile = new File("./dump/" + cdrFile.getName().split(".si2")[0] + ".xml");
                    JAXBContext jaxbContext = JAXBContext.newInstance(CdrAggs.class);
                    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                    jaxbMarshaller.marshal(cdrAggs, xmlOutFile);

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
                    .addLabel("trunkGroup.direction", "inc")
                    .addLabel("metric", "tgStats")
                    .incValue(label, value);
            tgMvSeriesMap.put("inc" + cdrBean.getInTrunkGroupNameIE144(), m2);
        }
        if (cdrBean.getOutTrunkGroupNameIE145() != null) {
            PMultivalueTimeSeries m3 = tgMvSeriesMap.getOrDefault("out" + cdrBean.getOutTrunkGroupNameIE145(),
                    new PMultivalueTimeSeries());
            m3.addLabel("node.name", cdrBean.getNodeId())
                    .addLabel("node.id", cdrBean.getNodeId())
                    .addLabel("trunkGroup.name", cdrBean.getOutTrunkGroupNameIE145())
                    .addLabel("trunkGroup.id", Integer.toString(cdrBean.getOutTrunkGroupId()))
                    .addLabel("trunkGroup.direction", "out")
                    .addLabel("metric", "tgStats")
                    .incValue(label, value);
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
