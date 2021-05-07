package si.iskratel.xml;

import si.iskratel.xml.model.MeasCollecFile;
import si.iskratel.xml.model.MeasCollecFile3;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileFilter;

public class FileLoader implements Runnable {

    @Override
    public void run() {

        while (true) {


            File dir = new File("xml_dir");

            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getAbsolutePath().endsWith(".xml");
                }
            });

            for (File f : files) {
                System.out.println(f.getAbsolutePath());
                MeasCollecFile mcf;
                try {

                    JAXBContext jaxbContext = JAXBContext.newInstance(MeasCollecFile.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    mcf = (MeasCollecFile) jaxbUnmarshaller.unmarshal(f);

                    System.out.println("parse(): " + mcf.toString());

                } catch (JAXBException e) {
                    System.out.println("parse(): JAXBException: " + e.getMessage());
                }
            }


            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
