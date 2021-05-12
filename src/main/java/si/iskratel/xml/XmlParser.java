package si.iskratel.xml;

import si.iskratel.metricslib.*;
import si.iskratel.xml.model.MeasCollecFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class XmlParser {

    public static PMetric xmlMetric = PMetric.build()
            .setName("pm_xml_metric")
            .setHelp("Xml based metric")
            .setLabelNames("nodeId", "elementType", "measurementType", "statisticGroup", "measName")
            .register("xml_metrics");

    public static PMultiValueMetric xmlMultiValueMetric = PMultiValueMetric.build()
            .setName("pm_xml_multivalue_metric")
            .setHelp("Xml based metric")
            .register("xml_multivalue_metrics");

    public static void main(String[] args) throws Exception {

        Properties properties = new Properties();
        properties.load(new FileInputStream("xml_parser.properties"));
        MetricsLib.init(properties);

        EsClient es = new EsClient(properties.getProperty("metricslib.elasticsearch.default.schema"),
                properties.getProperty("metricslib.elasticsearch.default.host"),
                Integer.parseInt(properties.getProperty("metricslib.elasticsearch.default.port")));

//        FileLoader fl = new FileLoader();
//        Thread t = new Thread(fl);
//        t.start();

        while (true) {

            File dir = new File("xml_dir");

            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getAbsolutePath().endsWith(".xml");
                }
            });

            for (File f : files) {

                PMetricRegistry.getRegistry("xml_metrics").resetMetrics();
                PMetricRegistry.getRegistry("xml_multivalue_metrics").resetMetrics();

                System.out.println(f.getAbsolutePath());
                MeasCollecFile mcf;
                try {

                    JAXBContext jaxbContext = JAXBContext.newInstance(MeasCollecFile.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    mcf = (MeasCollecFile) jaxbUnmarshaller.unmarshal(f);

//                    System.out.println("parse(): localDn=" + mcf.getFileHeader().getFileSender().getLocalDn()
//                            + " measInfo=" + mcf.getMeasData().get(0).getMeasInfo().get(0).getMeasInfoId()
//                            + " measTypes=" + mcf.getMeasData().get(0).getMeasInfo().get(0).getMeasTypes()
//                            + " measResults=" + mcf.getMeasData().get(0).getMeasInfo().get(0).getMeasValue().get(0).getMeasResults());

                    String elementType = mcf.getFileHeader().getFileSender().getElementType();
                    String nodeId = mcf.getFileHeader().getFileSender().getLocalDn();

                    for (MeasCollecFile.MeasData md : mcf.getMeasData()) {

                        for (MeasCollecFile.MeasData.MeasInfo mi : md.getMeasInfo()) {

                            String measurementType = mi.getMeasInfoId();
                            Date date = mi.getGranPeriod().getEndTime().toGregorianCalendar().getTime();

                            List<String> measTypes = mi.getMeasTypes(); // measurement names
                            String[] mArray = new String[measTypes.size()];
                            mArray = measTypes.toArray(mArray);

                            String statisticGroup = mi.getMeasValue().get(0).getMeasObjLdn();
                            List<String> values = mi.getMeasValue().get(0).getMeasResults(); // measurement values
                            String[] vArray = new String[values.size()];
                            vArray = values.toArray(vArray);
                            for (int i = 0; i < mArray.length; i++) {
                                xmlMetric.setLabelValues(nodeId, elementType, measurementType, statisticGroup, mArray[i]).set(Double.parseDouble(vArray[i]));
                                xmlMultiValueMetric.addLabel("nodeId", nodeId)
                                        .addLabel("elementType", elementType)
                                        .addLabel("measurementType", measurementType)
                                        .addLabel("statisticGroup", statisticGroup)
                                        .addValue(mArray[i], Double.parseDouble(vArray[i]));
                            }
                        }

                    }

                    es.sendBulkPost(xmlMetric);
                    es.sendBulkPost(xmlMultiValueMetric);

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
