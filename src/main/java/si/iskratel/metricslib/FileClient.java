package si.iskratel.metricslib;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.iskratel.metricslib.alarm.Alarm;
import si.iskratel.metricslib.alarm.AlarmManager;
import si.iskratel.metricslib.util.StateLog;

import java.io.*;

/**
 * This thread periodically checks the dump directory, if there are any files waiting to be uploaded to ElasticSearch.
 */
public class FileClient extends Thread {

    private static Logger logger = LoggerFactory.getLogger(FileClient.class);

    private EsClient esClient;

    private static long dump_count = 0;
    private static long export_count = 0;

    private Alarm alarm_files_dumped = new Alarm(3200010, "Database inaccessible", 3, "Elasticsearch temporarily not available", "Data is dumping to file");

    public FileClient(EsClient client) {
        esClient = client;
    }

    public static void dumpToFile(PMetric metric) {
        if (!MetricsLib.DUMP_TO_FILE_ENABLED) {
            logger.warn("Dumping is disabled. Metric will be dropped!!!");
            PromExporter.metricslib_dropped_metrics_total.inc();
            return;
        }
        writeDumpedDataToFile(metric.getName(), PMetricFormatter.toEsNdJsonString(metric));
    }

    public static void dumpToFile(PMultiValueMetric metric) {
        if (!MetricsLib.DUMP_TO_FILE_ENABLED) {
            logger.warn("Dumping is disabled. Metric will be dropped!!!");
            PromExporter.metricslib_dropped_metrics_total.inc();
            return;
        }
        writeDumpedDataToFile(metric.getName(), PMetricFormatter.toEsNdJsonString(metric));
    }

    /**
     * Only for writing dumped data.
     * @param metricName name of metric
     * @param data data content
     */
    private static void writeDumpedDataToFile(String metricName, String data) {
        try {
            String fileName = metricName + "_" + System.currentTimeMillis() + "_" + (dump_count++) + ".ndjson";
            logger.warn("Dumping to file: " + fileName);
            FileWriter myWriter = new FileWriter(MetricsLib.DUMP_DIRECTORY + fileName);
            myWriter.write(data);
            myWriter.close();
            PromExporter.metricslib_dump_to_file_total.inc();
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
        }
    }

    /**
     * Read file and return content as String.
     * @param file file
     * @return content
     */
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
            logger.error("FileNotFoundException: " + file.getName());
        } catch (IOException e) {
            logger.error("IOException: ", e);
        }

        return null;

    }

    /**
     * Generic write to file.
     * @param filename file name
     * @param text contents
     */
    public static void writeToFile(String filename, String text) {
        try {
            FileWriter myWriter = new FileWriter(filename);
            myWriter.write(text);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove any file.
     * @param filename file
     */
    public static void removeFile(String filename) {
        File file = new File(filename);
        if (file.exists()) file.delete();
    }

    public static void exportToCsv(PMetric metric) {

        if (metric.getTimestamp() == 0) metric.setTimestamp(System.currentTimeMillis());

        StringBuilder sb = new StringBuilder();

        sb.append("Timestamp,DateTime,");
        for (int i = 0; i < metric.getLabelNames().length; i++) {
            sb.append(metric.getLabelNames()[i]).append(",");
        }
        sb.append("count\n");

        for (PTimeSeries ts : metric.getTimeSeries()) {
            sb.append(metric.getTimestamp()).append(",").append("DATE").append(",");
            for (int i = 0; i < ts.getLabelValues().length; i++) {
                sb.append(ts.getLabelValues()[i]).append(",");
            }
            sb.append(ts.getValue()).append("\n");
        }

        try {
            logger.info("Exporting to file: " + metric.getName());
            FileWriter myWriter = new FileWriter(MetricsLib.EXPORT_DIRECTORY + metric.getName() + "_" + System.currentTimeMillis() + "_" + (export_count++) + ".csv");
            myWriter.write(sb.toString());
            myWriter.close();
            PromExporter.metricslib_dump_to_file_total.inc();
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
        }

    }


    @Override
    public void run() {

        if (!new File(MetricsLib.DUMP_DIRECTORY).exists()) {
            logger.warn("Dump directory does not exist: " + MetricsLib.DUMP_DIRECTORY);
            StateLog.addToStateLog("Dump directory", "Directory does not exist.");
            return;
        }

        while (true) {

            try {
                Thread.sleep(MetricsLib.UPLOAD_INTERVAL_SECONDS * 1000);
            } catch (InterruptedException e) {
            }

            if (!EsClient.ES_IS_READY) {
                logger.warn("Uploading postponed. ElasticSearch is not ready.");
                continue;
            }

            File dumpDir = new File(MetricsLib.DUMP_DIRECTORY);
            File[] dumpFiles = dumpDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.getName().endsWith(".ndjson")) return true;
                    return false;
                }
            });

            PromExporter.metricslib_files_waiting_for_upload.set(dumpFiles.length);

            if (dumpFiles.length == 0) AlarmManager.clearAlarm(alarm_files_dumped);
            if (dumpFiles.length > 50) AlarmManager.raiseAlarm(alarm_files_dumped);

            for (int i = 0; i < dumpFiles.length; i++) {
                String s = readFile(dumpFiles[i]);
                boolean b = esClient.sendBulkPost(s);
                logger.info("Uploading file: " + dumpFiles[i].getName() + " [result=" + b + "]");
                if (b) {
                    dumpFiles[i].delete();
                    PromExporter.metricslib_dump_files_uploads_total.labels("success").inc();
                } else {
                    PromExporter.metricslib_dump_files_uploads_total.labels("failed").inc();
                    break;
                }
            }

            if (dumpFiles.length > 0) logger.info("Finished uploading dumped files");

        }

    }


}
