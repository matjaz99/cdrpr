package si.iskratel.metricslib;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class FileClient {

    private  static Logger logger = LoggerFactory.getLogger(FileClient.class);

    public static void dumpToFile(PMetric metric) {
        try {
            FileWriter myWriter = new FileWriter(MetricsLib.DUMP_DIRECTORY + metric.getName() + "_" + System.currentTimeMillis() + ".ndjson");
            myWriter.write(PMetricFormatter.toEsNdJsonString(metric));
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void dumpToFile(PMultiValueMetric metric) {
        try {
            FileWriter myWriter = new FileWriter(MetricsLib.DUMP_DIRECTORY + metric.getName() + "_" + System.currentTimeMillis() + ".ndjson");
            myWriter.write(PMetricFormatter.toEsNdJsonString(metric));
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
