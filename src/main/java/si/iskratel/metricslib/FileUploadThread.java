package si.iskratel.metricslib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;

/**
 * This thread periodically checks the dump directory, if there are any files waiting to be uploaded to ElasticSearch.
 */
public class FileUploadThread extends Thread {

    private Logger logger = LoggerFactory.getLogger(FileUploadThread.class);

    private EsClient esClient;

    public FileUploadThread(EsClient client) {
        esClient = client;
    }

    private Alarm alarm_files_dumped = new Alarm(5999999, "PMON is dumping data", 3, "Elasticsearch is not accepting data", "Data is dumping to file");

    @Override
    public void run() {

        while (true) {

            File dumpDir = new File(MetricsLib.DUMP_DIRECTORY);
            if (!dumpDir.exists()) {
                logger.warn("Dump directory does not exist: " + dumpDir.getAbsolutePath());
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

            logger.debug("Files to upload: " + bkpFiles.length);
            PromExporter.metricslib_files_waiting_for_upload.set(bkpFiles.length);

            if (bkpFiles.length == 0) AlarmManager.clearAlarm(alarm_files_dumped);
            if (bkpFiles.length > 50) AlarmManager.raiseAlarm(alarm_files_dumped);

            for (int i = 0; i < bkpFiles.length; i++) {
                String s = FileClient.readFile(bkpFiles[i]);
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
