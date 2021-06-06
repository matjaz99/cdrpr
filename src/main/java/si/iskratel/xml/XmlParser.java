package si.iskratel.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.iskratel.metricslib.*;
import si.iskratel.xml.model.MeasCollecFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class XmlParser {

    private static Logger logger = LoggerFactory.getLogger(XmlParser.class);

    public static int XML_PARSER_INTERVAL_SECONDS = 60;
    public static String XML_PARSER_INPUT_DIR = "xml_input_dir";
    public static String XML_PARSER_OUTPUT_DIR = "xml_processed_dir";
    public static int XML_FILES_RETENTION_HOURS = 168;

    public static PMetric xmlMetric = PMetric.build()
            .setName("pm_xml_metric")
            .setHelp("Xml based metric")
            .setLabelNames("nodeId", "elementType", "measurementType", "statisticGroup", "measName")
            .register("xml_metrics");

    public static PMultiValueMetric xmlMultiValueMetric = PMultiValueMetric.build()
            .setName("pm_xml_multivalue_metric")
            .setHelp("Xml based multivalue metric")
            .register("xml_multivalue_metrics");

    public static void main(String[] args) throws Exception {

        String xProps = System.getProperty("xmlViewer.configurationFile", "xml_viewer.properties");
        System.out.println(xProps);

        Properties properties = new Properties();
        properties.load(new FileInputStream(xProps));
        MetricsLib.init(properties);

        XML_PARSER_INTERVAL_SECONDS = Integer.parseInt((String) properties.getOrDefault("xmlviewer.parser.interval.seconds", "300"));
        XML_PARSER_INPUT_DIR = (String) properties.getOrDefault("xmlviewer.parser.input.dir", "xml_input_dir");
        XML_PARSER_OUTPUT_DIR = (String) properties.getOrDefault("xmlviewer.parser.output.dir", "xml_processed_dir");
        XML_FILES_RETENTION_HOURS = Integer.parseInt((String) properties.getOrDefault("xmlviewer.file.retention.hours", "0"));

        EsClient es = new EsClient(properties.getProperty("metricslib.elasticsearch.default.schema"),
                properties.getProperty("metricslib.elasticsearch.default.host"),
                Integer.parseInt(properties.getProperty("metricslib.elasticsearch.default.port")));

        if (XML_FILES_RETENTION_HOURS > 0) {
            FileCleaner fc = new FileCleaner();
            Thread t = new Thread(fc);
            t.setName("FileCleanerThread");
            t.start();
        }


        while (true) {

            File dir = new File(XML_PARSER_INPUT_DIR);

            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getAbsolutePath().endsWith(".xml");
                }
            });

            if (files.length == 0) logger.info("No XML files found in " + XML_PARSER_INPUT_DIR);

            for (File f : files) {

                PMetricRegistry.getRegistry("xml_metrics").resetMetrics();
                PMetricRegistry.getRegistry("xml_multivalue_metrics").resetMetrics();

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

                            String measurementType = mi.getMeasInfoId();
                            Date date = mi.getGranPeriod().getEndTime().toGregorianCalendar().getTime();

                            List<String> measTypes = mi.getMeasTypes(); // measurement names
                            String[] mArray = new String[measTypes.size()];
                            mArray = measTypes.toArray(mArray);

                            String statisticGroup = mi.getMeasValue().get(0).getMeasObjLdn();
                            List<String> values = mi.getMeasValue().get(0).getMeasResults(); // measurement values
                            String[] vArray = new String[values.size()];
                            vArray = values.toArray(vArray);

                            PMultivalueTimeSeries mvts = new PMultivalueTimeSeries();

                            for (int i = 0; i < mArray.length; i++) {
                                xmlMetric.setLabelValues(nodeId, elementType, measurementType, statisticGroup, mArray[i]).set(Double.parseDouble(vArray[i]));
//                                xmlMetric.setTimestamp(date.getTime());
                                mvts.addLabel("nodeId", nodeId)
                                        .addLabel("elementType", elementType)
                                        .addLabel("measurementType", measurementType)
                                        .addLabel("statisticGroup", statisticGroup)
                                        .addValue(mArray[i], Double.parseDouble(vArray[i]));
                            }

                            xmlMultiValueMetric.addMultiValueTimeSeries(mvts);
//                            xmlMultiValueMetric.setTimestamp(date.getTime());

                        }

                    }

                } catch (JAXBException e) {
                    logger.error("parse(): JAXBException: " + e.getMessage());
                }

//                System.out.println(xmlMultiValueMetric.toString());
//                System.out.println(xmlMultiValueMetric.toStringDetail());
                es.sendBulkPost(xmlMetric);
                es.sendBulkPost(xmlMultiValueMetric);

                // move processed file
//                String absPath = f.getAbsolutePath();
//                absPath = absPath.replace(XML_PARSER_INPUT_DIR, XML_PARSER_OUTPUT_DIR);
//                logger.info("Moving file to new location: " + absPath);
//                f.renameTo(new File(absPath));

                appendText(f.getName());

            } // END foreach file


            try {
//                Thread.sleep(XML_PARSER_INTERVAL_SECONDS * 1000);
                Thread.sleep(new Random().nextInt(XML_PARSER_INTERVAL_SECONDS * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static void appendText(String line) {
        boolean append = true;
        try {
            FileWriter fw = new FileWriter("processed_history.txt", append);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(line + "\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
