package si.iskratel.metricslib;


import java.io.*;

public class FileClient {

    public static void dumpToFile(EsClient esClient, PMetric metric) {
        try {
            FileWriter myWriter = new FileWriter(MetricsLib.DUMP_DIRECTORY + "es_" + metric.getName() + "_" + System.currentTimeMillis() + ".txt");
            myWriter.write(metric.toEsNdJsonBulkString());
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }



}
