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

        String s = "";
        try {
            String currentLine;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((currentLine = reader.readLine()) != null) {
                s += currentLine + "\n";
            }
            reader.close();
            return s;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }



}
