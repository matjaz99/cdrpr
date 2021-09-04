package si.iskratel.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.cdrparser.CdrData;
import si.iskratel.cdrparser.CdrParser;
import si.iskratel.metricslib.FileClient;

import java.io.File;
import java.io.FileFilter;

public class CdrToCsv {

    private static Logger logger = LoggerFactory.getLogger(CdrToCsv.class);

    public static String CDR_INPUT_DIR = "cdr_input_dir";
    public static String CDR_OUTPUT_DIR = "cdr_output_dir";


    public static void main(String[] args) throws Exception {

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

            CdrData data = CdrParser.parse(f);
            logger.info("CDR contains " + data.cdrList.size() + " records");

            StringBuilder cdrCsv = new StringBuilder();
            cdrCsv.append("id,sequence,callType,ownerNumber,callingNumber,calledNumber,cdrTimeBeforeRinging,cdrRingingTimeBeforeAnsw,")
                    .append("duration,cause,causeString,callReleasingSide,startTime,endTime,")
                    .append("inTrunkId,inTrunkGroupId,outTrunkId,outTrunkGroupId,inTrunkGroupName,outTrunkGroupName,")
                    .append("nodeId,timestamp").append("\n");

            for (int i = 0; i < data.cdrList.size(); i++) {

                CdrBean cdrBean = data.cdrList.get(i);
                cdrBean.setNodeId("test_node");
                cdrCsv.append(CdrParser.toCsv(cdrBean));

            }

            String csvFileName = f.getAbsolutePath().replace(".si2", ".csv").replace(CDR_INPUT_DIR, CDR_OUTPUT_DIR);
            FileClient.writeToFile(csvFileName, cdrCsv.toString());

        }

    }



}
