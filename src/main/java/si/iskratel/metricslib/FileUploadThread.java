package si.iskratel.metricslib;

import java.io.File;
import java.io.FileFilter;

/**
 * This thread periodically checks the dump directory, if there are any files waiting to be uploaded to ElasticSearch.
 */
public class FileUploadThread extends Thread {

    private EsClient esClient;

    public FileUploadThread(EsClient client) {
        esClient = client;
    }

    @Override
    public void run() {

        while (true) {

            File dumpDir = new File(MetricsLib.DUMP_DIRECTORY);
            if (!dumpDir.exists()) {
                System.out.println("WARN: dump directory does not exist: " + dumpDir.getAbsolutePath());
                continue;
            }
            File[] bkpFiles = dumpDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.getName().startsWith("es_")) return true;
                    return false;
                }
            });

            System.out.println("Uploading files: " + bkpFiles.length);

            for (int i = 0; i < bkpFiles.length; i++) {
                String s = FileClient.readFile(bkpFiles[i]);
                boolean b = esClient.sendBulkPost(s);
                System.out.println("sendBulkPost result=" + b + " for file: " + bkpFiles[i].getName());
                if (b) {
                    bkpFiles[i].delete();
                } else {
                    break;
                }
            }

            try {
                Thread.sleep(16 * 1000);
            } catch (InterruptedException e) {
            }

        }

    }
}
