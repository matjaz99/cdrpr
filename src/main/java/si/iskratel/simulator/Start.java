package si.iskratel.simulator;


import si.iskratel.cdr.parser.*;
import si.iskratel.metricslib.MetricsLib;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Start {

    public static String HOSTNAME = "localhost0";
    public static int BULK_SIZE = 100;
    public static int SEND_INTERVAL_SEC = 60;
    public static int SIMULATOR_NUM_OF_THREADS = 1;
    public static boolean DEBUG_ENABLED = false;
    public static int RETRIES = 3;
    public static String ES_HOST;
    public static int ES_PORT;
    public static boolean ES_AUTO_CREATE_INDEX;
    public static String PG_URL;
    public static String PG_USER;
    public static String PG_PASS;
    public static boolean PG_CREATE_TABLES_ON_START = true;
    public static boolean EXIT_AT_THE_END = false;
    public static String SIMULATOR_MODE;
    public static String SIMULATOR_STORAGE_TYPE;
    public static String SIMULATOR_NODEID;
    public static int SIMULATOR_CALL_DELAY = 10;
    public static int SIMULATOR_CALL_REASON = 0;
    public static int SIMULATOR_ANUM_START = 0;
    public static int SIMULATOR_ANUM_RANGE = 0;
    public static int SIMULATOR_BNUM_START = 0;
    public static int SIMULATOR_BNUM_RANGE = 0;
    public static boolean SIMULATOR_MINIMUM_DATA = false;
    public static boolean ENABLE_PROMETHEUS_METRICS = false;
    public static boolean ENABLE_DUMP_TO_FILE = false;

    public static long totalCount = 0;
    public static long badCdrRecordExceptionCount = 0;

    private static LinkedBlockingQueue<CdrBean> queue = new LinkedBlockingQueue();
    public static boolean running = true;

    public static List<CdrSimulatorThread> simulatorThreads = new ArrayList<>();

    public static Properties releaseCausesProps;

    public static void main(String[] args) throws Exception {

        Runtime.getRuntime().addShutdownHook(new TheShutdownHook());

        String testPgUrl = "jdbc:postgresql://elasticvm:5432/cdraggs";

        Map<String, String> getenv = System.getenv();
        SIMULATOR_NUM_OF_THREADS = Integer.parseInt(getenv.getOrDefault("CDRPR_THREADS", "48"));
        SIMULATOR_CALL_DELAY = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_DELAY", "25"));
        SIMULATOR_CALL_REASON = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_CALL_REASON", "0"));
        SIMULATOR_ANUM_START = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_ANUM_START", "100000000"));
        SIMULATOR_ANUM_RANGE = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_ANUM_RANGE", "99999999"));
        SIMULATOR_BNUM_START = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_BNUM_START", "800000000"));
        SIMULATOR_BNUM_RANGE = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_BNUM_RANGE", "99999999"));
        SIMULATOR_MINIMUM_DATA = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_SIMULATOR_MINIMUM_DATA", "false"));
        SIMULATOR_NODEID = getenv.getOrDefault("CDRPR_SIMULATOR_NODEID", "" +
                "Moscow, Ljubljana, Berlin, London, Paris, Moscow, Amsterdam, Belgrade, Madrid, " +
                "Paris, Berlin, Copenhagen, Madrid, Moscow, Rome, Zurich, Lisbon, Warsaw, Berlin, Helsinki, Prague, " +
                "Vienna, London, Paris, Budapest, Zagreb, Belgrade, Kiev, Moscow, Amsterdam, Brussels, London, Paris," +
                "Moscow, Oslo, Helsinki, Dublin, Sarajevo, Skopje");

        // possible values: STORE_ALL_CALLS, STORE_AGGREGATED_CALLS
        SIMULATOR_MODE = getenv.getOrDefault("CDRPR_SIMULATOR_MODE", "STORE_AGGREGATED_CALLS");
        // possible values: ELASTICSEARCH, POSTGRES
        SIMULATOR_STORAGE_TYPE = getenv.getOrDefault("CDRPR_SIMULATOR_STORAGE_TYPE", "ELASTICSEARCH");

        BULK_SIZE = Integer.parseInt(getenv.getOrDefault("CDRPR_BULK_SIZE", "50000"));
        SEND_INTERVAL_SEC = Integer.parseInt(getenv.getOrDefault("CDRPR_SEND_INTERVAL_SEC", "60"));
        DEBUG_ENABLED = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_DEBUG_ENABLED", "false"));
        RETRIES = Integer.parseInt(getenv.getOrDefault("CDRPR_RETRIES", "0"));
        ES_HOST = getenv.getOrDefault("CDRPR_ES_HOST", "elasticvm");
        ES_PORT = Integer.parseInt(getenv.getOrDefault("CDRPR_ES_PORT", "9200"));
        ES_AUTO_CREATE_INDEX = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_ES_AUTO_CREATE_INDEX", "true"));
        PG_URL = getenv.getOrDefault("CDRPR_PG_URL", testPgUrl);
        PG_USER = getenv.getOrDefault("CDRPR_PG_USER", "postgres");
        PG_PASS = getenv.getOrDefault("CDRPR_PG_PASS", "object00");
        PG_CREATE_TABLES_ON_START = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_PG_CREATE_TABLES_ON_START", "false"));
        EXIT_AT_THE_END = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_EXIT", "true"));
        ENABLE_PROMETHEUS_METRICS = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_ENABLE_PROMETHEUS_METRICS", "false"));
        ENABLE_DUMP_TO_FILE = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_DUMP_TO_FILE", "true"));

        try {
            HOSTNAME = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {}

        System.out.println("HOSTNAME: " + HOSTNAME);
        System.out.println("NUM_OF_THREADS: " + SIMULATOR_NUM_OF_THREADS);
        System.out.println("SIMULATOR_DELAY: " + SIMULATOR_CALL_DELAY);
        System.out.println("SEND_INTERVAL_SEC: " + SEND_INTERVAL_SEC);
        System.out.println("BULK_SIZE: " + BULK_SIZE);
        System.out.println("ES_HOST: " + ES_HOST);
        System.out.println("ES_PORT: " + ES_PORT);
        System.out.println("ES_AUTO_CREATE_INDEX: " + ES_AUTO_CREATE_INDEX);
        System.out.println("PG_URL: " + PG_URL);
        System.out.println("SIMULATOR_NODEID: " + SIMULATOR_NODEID);
        System.out.println("SIMULATOR_CALL_REASON: " + SIMULATOR_CALL_REASON);

        releaseCausesProps = new Properties();
        try {
            releaseCausesProps.load(new FileInputStream("call_release_causes.properties"));
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }

        MetricsLib.PROM_METRICS_EXPORT_ENABLE = ENABLE_PROMETHEUS_METRICS;
        MetricsLib.DUMP_TO_FILE_ENABLED = ENABLE_DUMP_TO_FILE;
        MetricsLib.DEFAULT_ES_HOST = ES_HOST;
        MetricsLib.DEFAULT_ES_PORT = ES_PORT;
        MetricsLib.ES_AUTO_CREATE_INDEX = ES_AUTO_CREATE_INDEX;
        MetricsLib.RETRIES = RETRIES;
        MetricsLib.init();
        PrometheusMetrics.defaultBulkSize.set(BULK_SIZE);
        PrometheusMetrics.maxQueueSize.set(200 * BULK_SIZE);

        for (int i = 1; i < SIMULATOR_NUM_OF_THREADS + 1; i++) {
            CdrSimulatorThread t = new CdrSimulatorThread(i);
            t.start();
            simulatorThreads.add(t);
            System.out.println("Simulator thread created: " + t.getThreadId());
        }

        StorageThread ct = new StorageThread();
        ct.start();

        Runnable aggregator = null;

        if (SIMULATOR_MODE.equalsIgnoreCase("STORE_ALL_CALLS")) {
            aggregator = new AllCallData(1);
        }
        if (SIMULATOR_MODE.equalsIgnoreCase("STORE_AGGREGATED_CALLS")) {
            aggregator = new AggregatedCalls(1);
        }

        Thread t = new Thread(aggregator);
        t.start();

        XmlSimulatorThread xst = new XmlSimulatorThread();
        xst.start();

    }



    public static void debug(String s) {
        if (DEBUG_ENABLED) System.out.println(s);
    }

    public static synchronized void addCdr(CdrBean cdrBean) {
        queue.add(cdrBean);
    }

    public static int getQueueSize() {
        return queue.size();
    }

    public static synchronized CdrBean pollCdr() {
        return queue.poll();
    }

    public static String getRandomNodeId() {
        String[] a = SIMULATOR_NODEID.split(",");
        Random r = new Random();
        return a[r.nextInt(a.length)].trim();
    }

}
