package si.iskratel.cdrparser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.metricslib.FileClient;
import si.iskratel.simulator.Utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class CdrToCsv {

    private static Logger logger = LoggerFactory.getLogger(CdrToCsv.class);

    public static String CDR_INPUT_DIR = "cdr_input_dir";
    public static String CDR_OUTPUT_DIR = "cdr_output_dir";

    public static Properties releaseCausesProps;

    public static void main(String[] args) throws Exception {

        releaseCausesProps = new Properties();
        try {
            releaseCausesProps.load(new FileInputStream("call_release_causes.properties"));
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
        }

        File inputDir = new File(CDR_INPUT_DIR);

        File[] files = inputDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getAbsolutePath().endsWith(".si2");
            }
        });

        if (files.length == 0) logger.info("No CDR files found in " + CDR_INPUT_DIR + "/" + inputDir.getName());

        for (File f : files) {

            logger.info("Reading file: " + f.getAbsolutePath());

            List<CdrBean> cdrList = CdrParser.parse(f);
            logger.info("CDR contains " + cdrList.size() + " records");

            StringBuilder cdrCsv = new StringBuilder();
            cdrCsv.append("id,sequence,callType,ownerNumber,callingNumber,calledNumber,cdrTimeBeforeRinging,cdrRingingTimeBeforeAnsw,")
                    .append("duration,cause,causeString,callReleasingSide,startTime,endTime,")
                    .append("inTrunkId,inTrunkGroupId,outTrunkId,outTrunkGroupId,inTrunkGroupName,outTrunkGroupName,")
                    .append("nodeId,timestamp").append("\n");

            for (int i = 0; i < cdrList.size(); i++) {

                CdrBean cdrBean = cdrList.get(i);
                cdrBean.setNodeId("test_node");
                cdrCsv.append(putToStringBuilder(cdrBean));

            }

            String csvFileName = f.getAbsolutePath().replace(".si2", ".csv").replace(CDR_INPUT_DIR, CDR_OUTPUT_DIR);
            FileClient.writeToFile(csvFileName, cdrCsv.toString());

        }

    }

    private static String putToStringBuilder(CdrBean cdrBean) {

        StringBuilder sb = new StringBuilder();

        sb.append(cdrBean.getId()).append(",");
        sb.append(cdrBean.getSequence()).append(",");
        sb.append(cdrBean.getCallType()).append(",");
        sb.append(cdrBean.getOwnerNumber()).append(",");
        sb.append(cdrBean.getCallingNumber()).append(",");
        sb.append(cdrBean.getCalledNumber()).append(",");
        sb.append(cdrBean.getCdrTimeBeforeRinging()).append(",");
        sb.append(cdrBean.getCdrRingingTimeBeforeAnsw()).append(",");
        sb.append(cdrBean.getDuration()).append(",");
        sb.append(cdrBean.getCause()).append(",");
        sb.append(releaseCausesProps.getOrDefault(cdrBean.getCause() + "", "unknown")).append(",");
        sb.append(cdrBean.getCallReleasingSide()).append(",");
        sb.append(Utils.toDateString(cdrBean.getStartTime())).append(",");
        sb.append(Utils.toDateString(cdrBean.getEndTime())).append(",");
//        sb.append("\"cacType\":\"").append(cdrBean.getCacType()).append("\",");
//        sb.append("\"cacPrefix\":\"").append(cdrBean.getCacPrefix()).append("\",");
//        sb.append("\"cacNumber\":\"").append(cdrBean.getCacNumber()).append("\",");
        sb.append(cdrBean.getInTrunkId()).append(",");
        sb.append(cdrBean.getInTrunkGroupId()).append(",");
        sb.append(cdrBean.getOutTrunkId()).append(",");
        sb.append(cdrBean.getOutTrunkGroupId()).append(",");
        sb.append(cdrBean.getInTrunkGroupNameIE144()).append(",");
        sb.append(cdrBean.getOutTrunkGroupNameIE145()).append(",");
//        sb.append("\"servId\":\"").append(cdrBean.getServId()).append("\",");
//        sb.append("\"servIdOrig\":\"").append(cdrBean.getServIdOrig()).append("\",");
//        sb.append("\"servIdTerm\":\"").append(cdrBean.getServIdTerm()).append("\",");
//        sb.append("\"ctxCall\":\"").append(cdrBean.getCtxCall()).append("\",");
//        sb.append("\"ctxCallingNumber\":\"").append(cdrBean.getCtxCallingNumber()).append("\",");
//        sb.append("\"ctxCalledNumber\":\"").append(cdrBean.getCtxCalledNumber()).append("\",");
//        sb.append("\"bgidOrig\":\"").append(cdrBean.getBgidOrig()).append("\",");
//        sb.append("\"bgidTerm\":\"").append(cdrBean.getBgidTerm()).append("\",");
        sb.append(cdrBean.getNodeId()).append(",");
        sb.append(cdrBean.getStartTime().getTime());
        sb.append("\n");

        return sb.toString();
    }

}
