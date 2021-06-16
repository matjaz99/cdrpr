package si.iskratel.cdrparser;

import org.apache.commons.io.IOUtils;
import org.apache.kafka.common.protocol.types.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.iskratel.cdr.manager.BadCdrRecordException;
import si.iskratel.cdr.parser.*;
import si.iskratel.metricslib.*;
import si.iskratel.simulator.Start;
import si.iskratel.simulator.Utils;
import si.iskratel.xml.FileCleaner;
import si.iskratel.xml.XmlParser;
import si.iskratel.xml.model.MeasCollecFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.*;

public class CdrParser {

    private static Logger logger = LoggerFactory.getLogger(CdrParser.class);

    public static String CDR_INPUT_DIR = "cdr_input_dir";
    public static String CDR_OUTPUT_DIR = "cdr_output_dir";

    public static Properties releaseCausesProps;

    public static PMetric cdr_metric = PMetric.build()
            .setName("pmon_cdrparser_metric")
            .setHelp("adfasf")
            .setLabelNames("status")
            .register("pmon_internal");


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

            File dir = new File(CDR_INPUT_DIR);

            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getAbsolutePath().endsWith(".si2");
                }
            });

            if (files.length == 0) logger.info("No CDR files found in " + CDR_INPUT_DIR);

            for (File f : files) {

                logger.info("Reading file: " + f.getAbsolutePath());

                List<CdrBean> cdrList = parse(f);

                String cdrJson = "";
                int count = 0;
                for (int i = 0; i < cdrList.size(); i++) {
                    cdrJson += putToStringBuilder(cdrList.get(i));
                    count++;
                    if (count % 1000 == 0) {
                        es.sendBulkPost(cdrJson);
                        count = 0;
                        cdrJson = "";
                    }
                    if (i == cdrList.size() - 1) {
                        es.sendBulkPost(cdrJson);
                    }
                }

                // move processed file
                String absPath = f.getAbsolutePath();
                absPath = absPath.replace(CDR_INPUT_DIR, CDR_OUTPUT_DIR);
                logger.info("Moving file to new location: " + absPath);
                f.renameTo(new File(absPath));


            } // END foreach file

            if (files.length > 0) logger.info("Processed CDR files: " + files.length);

            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

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
                cdr_metric.setLabelValues("Success").inc();
                Start.totalCount++;
                logger.debug(cdrBean.toString());
            } catch (BadCdrRecordException e) {
                Start.badCdrRecordExceptionCount++;
                PpdrBean ppdrBean = cbc.parseBinaryPpdr(dr);
            } catch (Exception e) {
                e.printStackTrace();
                cdr_metric.setLabelValues("Fail").inc();
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

}
