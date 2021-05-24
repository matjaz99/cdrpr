package si.iskratel.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static Logger logger = LoggerFactory.getLogger(XmlParser.class);

    public static int XML_PARSER_INTERVAL_SECONDS = 60;
    public static String XML_PARSER_INPUT_DIR = "xml_input_dir";
    public static String XML_PARSER_OUTPUT_DIR = "xml_processed_dir";

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
        properties.load(new FileInputStream("xml_viewer.properties"));
        MetricsLib.init(properties);

        XML_PARSER_INTERVAL_SECONDS = Integer.parseInt((String) properties.getOrDefault("xmlviewer.parser.interval.seconds", "300"));
        XML_PARSER_INPUT_DIR = (String) properties.getOrDefault("xmlviewer.parser.input.dir", "xml_input_dir");
        XML_PARSER_OUTPUT_DIR = (String) properties.getOrDefault("xmlviewer.parser.output.dir", "xml_processed_dir");

        EsClient es = new EsClient(properties.getProperty("metricslib.elasticsearch.default.schema"),
                properties.getProperty("metricslib.elasticsearch.default.host"),
                Integer.parseInt(properties.getProperty("metricslib.elasticsearch.default.port")));

//        FileLoader fl = new FileLoader();
//        Thread t = new Thread(fl);
//        t.start();

        while (true) {

            File dir = new File(XML_PARSER_INPUT_DIR);

            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getAbsolutePath().endsWith(".xml");
                }
            });

            if (files.length == 0) logger.info("No XML files found");

            for (File f : files) {

                PMetricRegistry.getRegistry("xml_metrics").resetMetrics();

                logger.info("Reading file: " + f.getAbsolutePath());
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

                            PMetricRegistry.getRegistry("xml_multivalue_metrics").resetMetrics();

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
//                                xmlMetric.setTimestamp(date.getTime());
                                xmlMultiValueMetric.addLabel("nodeId", nodeId)
                                        .addLabel("elementType", elementType)
                                        .addLabel("measurementType", measurementType)
                                        .addLabel("statisticGroup", statisticGroup)
                                        .addValue(mArray[i], Double.parseDouble(vArray[i]));
//                                xmlMultiValueMetric.setTimestamp(date.getTime());
                            }

                            es.sendBulkPost(xmlMultiValueMetric);

                        }

                    }

                } catch (JAXBException e) {
                    logger.error("parse(): JAXBException: " + e.getMessage());
                }

                es.sendBulkPost(xmlMetric);

                // move processed file
                String absPath = f.getAbsolutePath();
                System.out.println("Current location: " + absPath);
                absPath = absPath.replace(XML_PARSER_INPUT_DIR, XML_PARSER_OUTPUT_DIR);
                System.out.println("New location: " + absPath);
                f.renameTo(new File(absPath));

            } // END foreach file


            try {
                Thread.sleep(XML_PARSER_INTERVAL_SECONDS * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
