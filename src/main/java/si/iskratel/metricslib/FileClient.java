package si.iskratel.metricslib;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * This thread periodically checks the dump directory, if there are any files waiting to be uploaded to ElasticSearch.
 */
public class FileClient extends Thread {

    private static Logger logger = LoggerFactory.getLogger(FileClient.class);

    private EsClient esClient;

    private static long count = 0;

    private Alarm alarm_files_dumped = new Alarm(3200010, "Database inaccessible", 3, "Elasticsearch is not accepting data", "Data is dumping to file");

    public FileClient(EsClient client) {
        esClient = client;
    }

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
            logger.error("dumpToFile(): IOException: " + e.getMessage());
        }
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
            logger.error("dumpToFile(): IOException: " + e.getMessage());
        }
    }

    public String readFile(File file) {

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
            logger.error("readFile(): FileNotFoundException: " + file.getName());
        } catch (IOException e) {
            logger.error("readFile(): IOException: ", e);
        }

        return null;

    }


    @Override
    public void run() {

        if (!new File(MetricsLib.DUMP_DIRECTORY).exists()) {
            logger.warn("Dump directory does not exist: " + MetricsLib.DUMP_DIRECTORY);
        }

        while (true) {

            File dumpDir = new File(MetricsLib.DUMP_DIRECTORY);
            if (!dumpDir.exists()) {
                try {
                    Thread.sleep(MetricsLib.UPLOAD_INTERVAL_SECONDS * 1000);
                } catch (InterruptedException e) {
                }
                continue;
            }
            File[] bkpFiles = dumpDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.getName().endsWith(".ndjson")) return true;
                    return false;
                }
            });

            PromExporter.metricslib_files_waiting_for_upload.set(bkpFiles.length);

            if (bkpFiles.length == 0) AlarmManager.clearAlarm(alarm_files_dumped);
            if (bkpFiles.length > 50) AlarmManager.raiseAlarm(alarm_files_dumped);

            for (int i = 0; i < bkpFiles.length; i++) {
                String s = readFile(bkpFiles[i]);
                boolean b = esClient.sendBulkPost(s);
                logger.info("Uploading file: " + bkpFiles[i].getName() + " [result=" + b + "]");
                if (b) {
                    bkpFiles[i].delete();
                    PromExporter.metricslib_dump_files_uploads_total.labels("success").inc();
                } else {
                    PromExporter.metricslib_dump_files_uploads_total.labels("failed").inc();
                    break;
                }
            }

            try {
                Thread.sleep(MetricsLib.UPLOAD_INTERVAL_SECONDS * 1000);
            } catch (InterruptedException e) {
            }

        }

    }


}
