package si.iskratel.cdrparser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.metricslib.EsClient;
import si.iskratel.metricslib.MetricsLib;
import si.iskratel.metricslib.PMetric;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class CdrToEs {

    private static Logger logger = LoggerFactory.getLogger(CdrToEs.class);

    public static String CDR_INPUT_DIR = "cdr_input_dir";
    public static String CDR_OUTPUT_DIR = "cdr_output_dir";
    private static boolean runOnce = true;

    public static PMetric cdr_files_total = PMetric.build()
            .setName("cdrparser_processed_files_total")
            .setHelp("Number of processed files")
            .setLabelNames("status")
            .register();

    public static void main(String[] args) throws Exception {

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

                    CdrData data = CdrParser.parse(f);
                    cdr_files_total.setLabelValues("Success").inc();
                    logger.info("CDR contains " + data.cdrList.size() + " records");

                    StringBuilder cdrJson = new StringBuilder();
                    int count = 0;
                    for (int i = 0; i < data.cdrList.size(); i++) {
                        CdrBean cdrBean = data.cdrList.get(i);
                        cdrBean.setNodeId(nodeDir.getName());
                        cdrJson.append(CdrParser.toEsNdjson("cdr_index", cdrBean));
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

}
