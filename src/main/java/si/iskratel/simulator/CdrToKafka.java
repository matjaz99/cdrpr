package si.iskratel.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.cdrparser.CdrData;
import si.iskratel.cdrparser.CdrParser;
import si.iskratel.metricslib.KafkaClient;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class CdrToKafka {

    private static Logger logger = LoggerFactory.getLogger(CdrToKafka.class);

    public static String CDR_INPUT_DIR = "cdr_input_dir";
    public static String CDR_OUTPUT_DIR = "cdr_output_dir";


    public static void main(String[] args) throws Exception {

        Props.EXIT_WHEN_DONE = false;
//        Props.KAFKA_BOOTSTRAP_SERVER = "centosvm:9092";

        KafkaClient kafkaClient = new KafkaClient(Props.KAFKA_BOOTSTRAP_SERVER);

        while (true) {

            File inputDir = new File(CDR_INPUT_DIR);

            File[] files = inputDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getAbsolutePath().endsWith(".si2");
                }
            });

            if (files.length == 0) logger.info("No CDR files found in " + inputDir.getName());

            for (File f : files) {

                logger.info("Reading file: " + f.getAbsolutePath());

                CdrData data = CdrParser.parse(f);
                logger.info("CDR contains " + data.cdrList.size() + " records");

                for (int i = 0; i < data.cdrList.size(); i++) {

                    CdrBean cdrBean = data.cdrList.get(i);
                    cdrBean.setNodeId("test_node");

                    kafkaClient.sendMsg("cdr_topic", CdrParser.toCsv(cdrBean).trim());

                }

                String absPath = f.getAbsolutePath();
                absPath = absPath.replace(CDR_INPUT_DIR, CDR_OUTPUT_DIR);
                logger.info("Moving file to new location: " + absPath);
                Files.move(Paths.get(f.getAbsolutePath()), Paths.get(absPath), StandardCopyOption.REPLACE_EXISTING);

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
