package si.iskratel.cdrparser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.metricslib.FileClient;
import si.iskratel.metricslib.KafkaClient;

import java.io.File;
import java.io.FileFilter;

public class CdrToKafka {

    private static Logger logger = LoggerFactory.getLogger(CdrToKafka.class);

    public static String CDR_INPUT_DIR = "cdr_input_dir";
    public static String CDR_OUTPUT_DIR = "cdr_output_dir";


    public static void main(String[] args) throws Exception {

        KafkaClient kafkaClient = new KafkaClient("mcrk-docker-1:9092");

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

            for (int i = 0; i < data.cdrList.size(); i++) {

                CdrBean cdrBean = data.cdrList.get(i);
                cdrBean.setNodeId("test_node");

                kafkaClient.sendMsg("cdr_topic", CdrParser.toCsv(cdrBean));

            }

        }

    }



}
