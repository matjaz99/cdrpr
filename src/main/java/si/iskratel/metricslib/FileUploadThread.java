package si.iskratel.metricslib;

import java.io.File;
import java.io.FileFilter;

public class FileUploadThread extends Thread {

    private EsClient esClient;

    public FileUploadThread(EsClient client) {
        esClient = client;
    }

    @Override
    public void run() {

        while (true) {

            File simDir = new File(MetricsLib.DUMP_DIRECTORY);
            File[] bkpFiles = simDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.getName().startsWith("es_")) return true;
                    return false;
                }
            });

            System.out.println("Uploading files: " + bkpFiles.length);

            for (int i = 0; i < bkpFiles.length; i++) {
                System.out.println("Reading file: " + bkpFiles[i].getAbsolutePath());
                String s = FileClient.readFile(bkpFiles[i]);
                System.out.println("Reading file complete: " + bkpFiles[i].getAbsolutePath());
                //System.out.println("From file: \n" + s);
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
