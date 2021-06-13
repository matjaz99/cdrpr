package si.iskratel.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

public class FileCleaner implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(FileCleaner.class);

    @Override
    public void run() {

        while (true) {

            File dir = new File(XmlParser.XML_PARSER_OUTPUT_DIR);

            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getAbsolutePath().endsWith(".xml");
                }
            });

            if (files.length > 0) {
                long now = new Date().getTime();

                for (int i = 0; i < files.length; i++) {
                    long diff = now - files[i].lastModified();

                    if (diff > XmlParser.XML_FILES_RETENTION_HOURS * 3600 * 1000) {
                        files[i].delete();
                        logger.info("Delete old file: " + files[i].getAbsolutePath());
                    }
                }
            }

            try {
                Thread.sleep(1 * 3600 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }
}
