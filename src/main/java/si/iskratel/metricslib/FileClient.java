package si.iskratel.metricslib;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class FileClient {

    private static Logger logger = LoggerFactory.getLogger(FileClient.class);

    private static int count = 1;

    public static void dumpToFile(PMetric metric) {
        if (!MetricsLib.DUMP_TO_FILE_ENABLED) {
            logger.warn("dumpToFile(): Dumping is disabled. Metric will be dropped!!!");
            PromExporter.metricslib_dropped_metrics_total.inc();
            return;
        }
        try {
            logger.info("dumpToFile(): Dumping to file: " + metric.getName());
            FileWriter myWriter = new FileWriter(MetricsLib.DUMP_DIRECTORY + metric.getName() + "_" + System.currentTimeMillis() + "_" + (count++) + ".ndjson");
            myWriter.write(PMetricFormatter.toEsNdJsonString(metric));
            myWriter.close();
            PromExporter.metricslib_dump_to_file_total.inc();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (count > 9999) count = 1;
    }

    public static void dumpToFile(PMultiValueMetric metric) {
        if (!MetricsLib.DUMP_TO_FILE_ENABLED) {
            logger.warn("dumpToFile(): Dumping is disabled. Metric will be dropped!!!");
            PromExporter.metricslib_dropped_metrics_total.inc();
            return;
        }
        try {
            logger.info("dumpToFile(): Dumping to file: " + metric.getName());
            FileWriter myWriter = new FileWriter(MetricsLib.DUMP_DIRECTORY + metric.getName() + "_" + System.currentTimeMillis() + "_" + (count++) + ".ndjson");
            myWriter.write(PMetricFormatter.toEsNdJsonString(metric));
            myWriter.close();
            PromExporter.metricslib_dump_to_file_total.inc();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (count > 9999) count = 1;
    }

    public static String readFile(File file) {

        StringBuilder sb = new StringBuilder();
        try {
            String currentLine;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((currentLine = reader.readLine()) != null) {
                sb.append(currentLine).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (FileNotFoundException e) {
            logger.error("File not found: " + file.getName());
        } catch (IOException e) {
            logger.error("IOException: ", e);
        }

        return null;

    }



}
