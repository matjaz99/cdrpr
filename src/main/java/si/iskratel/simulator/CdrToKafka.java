package si.iskratel.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.cdrparser.CdrData;
import si.iskratel.cdrparser.CdrParser;
import si.iskratel.metricslib.KafkaClient;

import java.io.File;
import java.io.FileFilter;

public class CdrToKafka {

    private static Logger logger = LoggerFactory.getLogger(CdrToKafka.class);

    public static String CDR_INPUT_DIR = "cdr_input_dir";
    public static String CDR_OUTPUT_DIR = "cdr_output_dir";


    public static void main(String[] args) throws Exception {

        Props.EXIT_WHEN_DONE = false;

        KafkaClient kafkaClient = new KafkaClient(Props.KAFKA_BOOTSTRAP_SERVER);

        File inputDir = new File(CDR_INPUT_DIR);

        while (true) {

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

                String absPath = f.getAbsolutePath();
                absPath = absPath.replace(CDR_INPUT_DIR, CDR_OUTPUT_DIR);
                logger.info("Moving file to new location: " + absPath);
                f.renameTo(new File(absPath));

            } // END foreach file

            if (files.length > 0) logger.info("Processed CDR files: " + files.length);

            if (Props.EXIT_WHEN_DONE) break; // run only once

            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        System.exit(0);

    }



}
