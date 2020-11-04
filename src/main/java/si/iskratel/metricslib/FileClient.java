package si.iskratel.metricslib;


import java.io.*;

public class FileClient {

    public static void dumpToFile(EsClient esClient, PMetric metric) {
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
            System.err.println("ERROR: FileNotFoundException: file not found: " + file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }



}
