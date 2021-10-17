package si.iskratel.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.cdrparser.CdrData;
import si.iskratel.cdrparser.CdrParser;
import si.iskratel.metricslib.EsClient;
import si.iskratel.metricslib.MetricsLib;
import si.iskratel.metricslib.PMetric;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class CdrToEs {

    private static Logger logger = LoggerFactory.getLogger(CdrToEs.class);

    public static String CDR_INPUT_DIR = "cdr_input_dir";
    public static String CDR_OUTPUT_DIR = "cdr_output_dir";

    public static PMetric cdr_files_total = PMetric.build()
            .setName("cdrparser_processed_files_total")
            .setHelp("Number of processed files")
            .setLabelNames("status")
            .register();

    public static void main(String[] args) throws Exception {

//        Props.EXIT_WHEN_DONE = false;

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
                Thread.sleep(100);
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
                        return pathname.isFile() && !pathname.getName().startsWith(".");// && pathname.getAbsolutePath().endsWith(".si2");
                    }
                });

                if (files.length == 0) logger.info("No CDR files found in " + CDR_INPUT_DIR + "/" + nodeDir.getName());

                for (File f : files) {

                    logger.info("Reading file: " + f.getAbsolutePath());

                    CdrData data = CdrParser.parse(f);
                    cdr_files_total.setLabelValues("Success").inc();
                    logger.info("CDR contains " + data.cdrList.size() + " records");

                    StringBuilder cdrJson = new StringBuilder();
                    int count = 0;
                    for (int i = 0; i < data.cdrList.size(); i++) {
                        CdrBean cdrBean = data.cdrList.get(i);
                        cdrBean.setNodeId(nodeDir.getName());
                        cdrJson.append(CdrParser.toEsNdjsonShort("cdr_index", cdrBean));
                        count++;
                        if (count % 9000 == 0) {
                            es.sendBulkPost(cdrJson.toString());
                            count = 0;
                            cdrJson = new StringBuilder();
                        }
                        if (i == data.cdrList.size() - 1) {
                            es.sendBulkPost(cdrJson.toString());
                            cdrJson = new StringBuilder();
                        }
                    }



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


                } // END foreach file

                if (files.length > 0) logger.info("Processed CDR files: " + files.length);

            } // END foreach directory

            if (Props.EXIT_WHEN_DONE) break; // run only once

            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } // END while true

        System.exit(0);

    }

}
