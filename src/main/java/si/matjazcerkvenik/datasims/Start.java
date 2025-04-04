package si.matjazcerkvenik.datasims;


import si.matjazcerkvenik.metricslib.MetricsLib;
import si.matjazcerkvenik.datasims.cdrpr.simulator.*;
import si.matjazcerkvenik.datasims.cdrpr.simulator.generator.CdrNodeGeneratorThread;
import si.matjazcerkvenik.datasims.cdrpr.simulator.generator.StorageThread;
import si.matjazcerkvenik.datasims.cdrpr.simulator.generator.XmlSimulatorThread;

import java.util.*;

public class Start {


    public static boolean running = true;
    public static List<CdrNodeGeneratorThread> nodeSimulatorThreads = new ArrayList<>();



    public static void main(String[] args) throws Exception {

        Runtime.getRuntime().addShutdownHook(new TheShutdownHook());

        Props.initialize();
        Props.loadReleaseCauses();

        // start generator
        // store each record into ES
        // config via env vars
        if (Props.SIMULATOR_MODE.equalsIgnoreCase("GENERATE_CDR_AND_STORE_ALL_TO_ES")) {
            initMetricsLib();
            startCdrNodeGenerators();
            Thread t = new Thread(new AllGenCdrsToEs2(1));
            t.setName("aggregator");
            t.start();
        }

        // start generator
        // aggregate (PMetric)
        // store each record into ES
        // config via cdrpr.properties
        if (Props.SIMULATOR_MODE.equalsIgnoreCase("GENERATE_CDR_AGGREGATE_AND_STORE_TO_ES")) {
            // this use case is also known as CDRAGGS
            initMetricsLib();
            startCdrNodeGenerators();
            Thread t = new Thread(new AggCdrsToEs(1));
            t.setName("aggregator");
            t.start();
        }

        if (Props.SIMULATOR_MODE.equalsIgnoreCase("GENERATE_CDR_AGGREGATE_AND_STORE_TO_ES_BY_NODE")) {
            // this use case is also known as NODEAGGS and TGAGGS
            // TODO
            initMetricsLib();
            startCdrNodeGenerators();
            Thread t = new Thread(new AggCdrsToEsByNode(1));
            t.setName("aggregator");
            t.start();
        }

        if (Props.SIMULATOR_MODE.equalsIgnoreCase("SEQUENTIAL_RANDOM_DATA_TO_OS")) {
            // this use case is also known as SEQDATA
            initMetricsLib();
            SeqRandomDataToEs seqRandomDataToEs = new SeqRandomDataToEs();
            seqRandomDataToEs.generateData();
        }

        // start generator
        // store each record into Kafka topic
        if (Props.SIMULATOR_MODE.equalsIgnoreCase("GENERATE_CDR_AND_STORE_ALL_TO_KAFKA")) {
            initMetricsLib();
            startCdrNodeGenerators();
            Thread t = new Thread(new AllGenCdrsToKafka(1));
            t.setName("aggregator");
            t.start();
        }

        // parse cdr
        // convert to csv
        // config with properties file
        if (Props.SIMULATOR_MODE.equalsIgnoreCase("CDR_TO_CSV")) {
            CdrToCsv.main(null);
        }
        if (Props.SIMULATOR_MODE.equalsIgnoreCase("CDR_TO_ES")) {
            CdrToEs.main(new String[1]);
        }
        if (Props.SIMULATOR_MODE.equalsIgnoreCase("CDR_AGGS_TO_ES")) {
            CdrAggsToEs.main(null);
        }
        if (Props.SIMULATOR_MODE.equalsIgnoreCase("CDR_TO_KAFKA")) {
            CdrToKafka.main(new String[1]);
        }
        if (Props.SIMULATOR_MODE.equalsIgnoreCase("CDR_AGGS_TO_XML")) {
            CdrAggsToXml.main(new String[1]);
        }


    }


    public static void startCdrNodeGenerators() {

        String[] nodeArray = Props.SIMULATOR_NODEID.split(",");
        for (int i = 0; i < nodeArray.length; i++) {
            CdrNodeGeneratorThread t = new CdrNodeGeneratorThread(i, nodeArray[i].trim());
            t.setName("CdrNodeGeneratorThread");
            t.start();
            nodeSimulatorThreads.add(t);
            System.out.println("Simulator thread created: " + nodeArray[i].trim());
        }

        StorageThread ct = new StorageThread();
        ct.setName("Storage");
        ct.start();

        XmlSimulatorThread xst = new XmlSimulatorThread();
        xst.setName("XmlSimulatorThread");
        xst.start();
    }

    public static void initMetricsLib() throws Exception {
        MetricsLib.PROM_METRICS_EXPORT_ENABLE = Props.PROMETHEUS_ENABLE_METRICS;
        MetricsLib.DUMP_TO_FILE_ENABLED = Props.ENABLE_DUMP_TO_FILE;
        MetricsLib.ES_DEFAULT_SCHEMA = Props.ES_SCHEMA;
        MetricsLib.ES_BASIC_USER = Props.ES_BASIC_USER;
        MetricsLib.ES_BASIC_PASS = Props.ES_BASIC_PASS;
        MetricsLib.ES_DEFAULT_HOST = Props.ES_HOST;
        MetricsLib.ES_DEFAULT_PORT = Props.ES_PORT;
        MetricsLib.ES_AUTO_CREATE_INDEX = Props.ES_AUTO_CREATE_INDEX;
        MetricsLib.ES_NUMBER_OF_SHARDS = Props.ES_NUMBER_OF_SHARDS;
        MetricsLib.ES_NUMBER_OF_REPLICAS = Props.ES_NUMBER_OF_REPLICAS;
        MetricsLib.RETRIES = Props.RETRIES;
        MetricsLib.ALARM_DESTINATION = Props.ALARM_DESTINATION;
//        MetricsLib.EXPORT_ENABLED = true;
        MetricsLib.init();
    }



    public static String getRandomNodeId() {
        String[] a = Props.SIMULATOR_NODEID.split(",");
        Random r = new Random();
        return a[r.nextInt(a.length)].trim();
    }

}
