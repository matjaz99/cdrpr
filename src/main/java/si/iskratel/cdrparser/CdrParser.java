package si.iskratel.cdrparser;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.iskratel.cdr.manager.BadCdrRecordException;
import si.iskratel.cdr.parser.*;
import si.iskratel.metricslib.*;
import si.iskratel.simulator.Start;
import si.iskratel.simulator.Utils;

import java.io.*;
import java.util.*;

public class CdrParser {

    private static Logger logger = LoggerFactory.getLogger(CdrParser.class);

    public static String CDR_INPUT_DIR = "cdr_input_dir";
    public static String CDR_OUTPUT_DIR = "cdr_output_dir";
    private static boolean runOnce = true;

    public static Properties releaseCausesProps;

    public static PMetric cdr_files_total = PMetric.build()
            .setName("cdrparser_processed_files_total")
            .setHelp("Number of processed files")
            .setLabelNames("status")
            .register();

    public static PMetric cdr_records_total = PMetric.build()
            .setName("cdrparser_records_total")
            .setHelp("Number of extracted records")
            .setLabelNames("fileType")
            .register();


    public static void main(String[] args) throws Exception {

        releaseCausesProps = new Properties();
        try {
            releaseCausesProps.load(new FileInputStream("call_release_causes.properties"));
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
        }

        String xProps = System.getProperty("cdrparser.configurationFile", "cdr_parser/cdr_parser.properties");
        Properties cdrProps = new Properties();
        try {
            cdrProps.load(new FileInputStream(xProps));
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
        }

        MetricsLib.init(cdrProps);

        EsClient es = new EsClient(cdrProps.getProperty("metricslib.elasticsearch.default.schema"),
                cdrProps.getProperty("metricslib.elasticsearch.default.host"),
                Integer.parseInt(cdrProps.getProperty("metricslib.elasticsearch.default.port")));

        while (!EsClient.ES_IS_READY) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

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
                        return pathname.isFile() && pathname.getAbsolutePath().endsWith(".si2");
                    }
                });

                if (files.length == 0) logger.info("No CDR files found in " + CDR_INPUT_DIR + "/" + nodeDir.getName());

                for (File f : files) {

                    logger.info("Reading file: " + f.getAbsolutePath());

                    List<CdrBean> cdrList = parse(f);
                    cdr_files_total.setLabelValues("Success").inc();
                    logger.info("CDR contains " + cdrList.size() + " records");

                    StringBuilder cdrJson = new StringBuilder();
                    int count = 0;
                    for (int i = 0; i < cdrList.size(); i++) {
                        CdrBean cdrBean = cdrList.get(i);
                        cdrBean.setNodeId(nodeDir.getName());
                        cdrJson.append(putToStringBuilder(cdrBean));
                        count++;
                        if (count % 9000 == 0) {
                            es.sendBulkPost(cdrJson.toString());
                            count = 0;
                            cdrJson = new StringBuilder();
                        }
                        if (i == cdrList.size() - 1) {
                            es.sendBulkPost(cdrJson.toString());
                            cdrJson = new StringBuilder();
                        }
                    }

                    // move processed file
                    // FIXME create new output node dir
//                    String absPath = f.getAbsolutePath();
//                    absPath = absPath.replace(CDR_INPUT_DIR, CDR_OUTPUT_DIR);
//                    logger.info("Moving file to new location: " + absPath);
//                    f.renameTo(new File(absPath));


                } // END foreach file

                if (files.length > 0) logger.info("Processed CDR files: " + files.length);

            } // END foreach directory

            if (runOnce) break;

            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } // END while true

        System.exit(0);

    }

    public static List<CdrBean> parse(File f) throws Exception {

        List<CdrBean> returnList = new ArrayList<>();

        FileInputStream is = new FileInputStream(f);
//        ByteArrayInputStream bais = new ByteArrayInputStream(is.readAllBytes()); // requires Java 9!!!
        byte[] bytes = IOUtils.toByteArray(is);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        List<DataRecord> list = CDRReader.readDataRecords(bais);
        logger.info("records in file: " + list.size());

        for (DataRecord dr : list) {
            logger.debug(dr.toString());
            CdrBeanCreator cbc = new CdrBeanCreator() {
                @Override
                public void setSpecificBeanValues(CdrObject cdrObj, CdrBean cdrBean) {

                }
            };
            try {
                CdrBean cdrBean = cbc.parseBinaryCdr(dr.getDataRecordBytes(), null);
                returnList.add(cdrBean);
                cdr_records_total.setLabelValues("CDR").inc();
                logger.debug(cdrBean.toString());
            } catch (BadCdrRecordException e) {
                PpdrBean ppdrBean = cbc.parseBinaryPpdr(dr);
                cdr_records_total.setLabelValues("PPDR").inc();
            } catch (Exception e) {
                logger.error("Exception: ", e);
                cdr_records_total.setLabelValues("Unknown").inc();
            }
        }

        return returnList;

    }

    private static String putToStringBuilder(CdrBean cdrBean) {

        StringBuilder sb = new StringBuilder();

        sb.append("{\"index\":{\"_index\":\"").append("cdr_index").append("\"}}\n");
        sb.append("{");
        sb.append("\"id\":\"").append(cdrBean.getId()).append("\",");
        sb.append("\"callId\":\"").append(cdrBean.getCallid()).append("\",");
        sb.append("\"sequence\":\"").append(cdrBean.getSequence()).append("\",");
        sb.append("\"callType\":\"").append(cdrBean.getCallType()).append("\",");
        sb.append("\"ownerNumber\":\"").append(cdrBean.getOwnerNumber()).append("\",");
        sb.append("\"callingNumber\":\"").append(cdrBean.getCallingNumber()).append("\",");
        sb.append("\"calledNumber\":\"").append(cdrBean.getCalledNumber()).append("\",");
        sb.append("\"cdrTimeBeforeRinging\":").append(cdrBean.getCdrTimeBeforeRinging()).append(",");
        sb.append("\"cdrRingingTimeBeforeAnsw\":").append(cdrBean.getCdrRingingTimeBeforeAnsw()).append(",");
        sb.append("\"duration\":").append(cdrBean.getDuration()).append(",");
        sb.append("\"cause\":").append(cdrBean.getCause()).append(",");
        sb.append("\"causeString\":\"").append(releaseCausesProps.getOrDefault(cdrBean.getCause() + "", "unknown")).append("\",");
        sb.append("\"callReleasingSide\":\"").append(cdrBean.getCallReleasingSide()).append("\",");
        sb.append("\"startTime\":\"").append(Utils.toDateString(cdrBean.getStartTime())).append("\",");
        sb.append("\"endTime\":\"").append(Utils.toDateString(cdrBean.getEndTime())).append("\",");
        sb.append("\"cacType\":\"").append(cdrBean.getCacType()).append("\",");
        sb.append("\"cacPrefix\":\"").append(cdrBean.getCacPrefix()).append("\",");
        sb.append("\"cacNumber\":\"").append(cdrBean.getCacNumber()).append("\",");
        sb.append("\"inTrunkId\":\"").append(cdrBean.getInTrunkId()).append("\",");
        sb.append("\"inTrunkGroupId\":\"").append(cdrBean.getInTrunkGroupId()).append("\",");
        sb.append("\"outTrunkId\":\"").append(cdrBean.getOutTrunkId()).append("\",");
        sb.append("\"outTrunkGroupId\":\"").append(cdrBean.getOutTrunkGroupId()).append("\",");
        sb.append("\"inTrunkGroupName\":\"").append(cdrBean.getInTrunkGroupNameIE144()).append("\",");
        sb.append("\"outTrunkGroupName\":\"").append(cdrBean.getOutTrunkGroupNameIE145()).append("\",");
        sb.append("\"servId\":\"").append(cdrBean.getServId()).append("\",");
        sb.append("\"servIdOrig\":\"").append(cdrBean.getServIdOrig()).append("\",");
        sb.append("\"servIdTerm\":\"").append(cdrBean.getServIdTerm()).append("\",");
        sb.append("\"ctxCall\":\"").append(cdrBean.getCtxCall()).append("\",");
        sb.append("\"ctxCallingNumber\":\"").append(cdrBean.getCtxCallingNumber()).append("\",");
        sb.append("\"ctxCalledNumber\":\"").append(cdrBean.getCtxCalledNumber()).append("\",");
        sb.append("\"bgidOrig\":\"").append(cdrBean.getBgidOrig()).append("\",");
        sb.append("\"bgidTerm\":\"").append(cdrBean.getBgidTerm()).append("\",");
        sb.append("\"nodeId\":\"").append(cdrBean.getNodeId()).append("\",");
        sb.append("\"@timestamp\":").append(cdrBean.getStartTime().getTime()).append("}\n");

        return sb.toString();
    }

    private static PMultiValueMetric toMultivalueMetric(CdrBean cdrBean) {

        PMultiValueMetric mv = PMultiValueMetric.build()
                .setName("cdr_multivalue_metric")
                .setHelp("cdr data")
                .register("cdr_index");

        PMultivalueTimeSeries mvts = new PMultivalueTimeSeries();
        mvts.addValue("id", cdrBean.getId());
        mvts.addValue("callId", cdrBean.getCallid());
        mvts.addValue("sequence", cdrBean.getSequence());
        mvts.addValue("callType", cdrBean.getCallType());
        mvts.addLabel("ownerNumber", cdrBean.getOwnerNumber());
        mvts.addLabel("callingNumber", cdrBean.getCallingNumber());
        mvts.addLabel("calledNumber", cdrBean.getCalledNumber());
        mvts.addValue("cdrTimeBeforeRinging", cdrBean.getCdrTimeBeforeRinging());
        mvts.addValue("cdrRingingTimeBeforeAnsw", cdrBean.getCdrRingingTimeBeforeAnsw());
        mvts.addValue("duration", cdrBean.getDuration());
        mvts.addValue("cause", cdrBean.getCause());
        mvts.addLabel("causeString", (String) releaseCausesProps.getOrDefault(cdrBean.getCause() + "", "unknown"));
        mvts.addValue("callReleasingSide", cdrBean.getCallReleasingSide());
        mvts.addLabel("startTime", Utils.toDateString(cdrBean.getStartTime()));
        mvts.addLabel("endTime", Utils.toDateString(cdrBean.getEndTime()));
        mvts.addValue("cacType", cdrBean.getCacType());
        mvts.addValue("cacPrefix", cdrBean.getCacPrefix());
        mvts.addValue("cacNumber", cdrBean.getCacNumber());
        mvts.addValue("inTrunkId", cdrBean.getInTrunkId());
        mvts.addValue("inTrunkGroupId", cdrBean.getInTrunkGroupId());
        mvts.addValue("outTrunkId", cdrBean.getOutTrunkId());
        mvts.addValue("outTrunkGroupId", cdrBean.getOutTrunkGroupId());
        mvts.addLabel("inTrunkGroupName", cdrBean.getInTrunkGroupNameIE144());
        mvts.addLabel("outTrunkGroupName", cdrBean.getOutTrunkGroupNameIE145());
        mvts.addValue("servId", cdrBean.getServId());
        mvts.addValue("servIdOrig", cdrBean.getServIdOrig());
        mvts.addValue("servIdTerm", cdrBean.getServIdTerm());
        mvts.addValue("ctxCall", cdrBean.getCtxCall());
        mvts.addLabel("ctxCallingNumber", cdrBean.getCtxCallingNumber());
        mvts.addLabel("ctxCalledNumber", cdrBean.getCtxCalledNumber());
        mvts.addValue("bgidOrig", cdrBean.getBgidOrig());
        mvts.addValue("bgidTerm", cdrBean.getBgidTerm());
        mvts.addLabel("nodeId", cdrBean.getNodeId());

        mv.setTimestamp(cdrBean.getStartTime().getTime());

        return mv;
    }

}
