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
    public static String ES_URL;
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

    public static long totalCount = 0;
    public static long badCdrRecordExceptionCount = 0;
    public static long startTime = 0;
    public static long endTime = 0;

    private static LinkedBlockingQueue<CdrBean> queue = new LinkedBlockingQueue();
    public static boolean running = true;

    public static List<CdrSimulatorThread> simulatorThreads = new ArrayList<>();

    public static Properties releaseCausesProps;

    public static void main(String[] args) throws Exception {

        Runtime.getRuntime().addShutdownHook(new TheShutdownHook());

//        String testUrl = "http://mcrk-docker-1:9200/cdrs/_bulk?pretty";
//        String testUrl = "http://mcrk-docker-1:9200/cdraggs/_bulk?pretty";
//        String testUrl = "http://pgcentos:9200/cdraggs/_bulk?pretty";
        String testUrl = "http://elasticvm:9200/cdraggs/_bulk?pretty";
//        String testUrl = "http://centosvm:9200/cdr_aggs/_bulk?pretty";
        String testPgUrl = "jdbc:postgresql://elasticvm:5432/cdraggs";

        Map<String, String> getenv = System.getenv();
        SIMULATOR_NUM_OF_THREADS = Integer.parseInt(getenv.getOrDefault("CDRPR_THREADS", "32"));
        SIMULATOR_CALL_DELAY = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_DELAY", "20"));
        SIMULATOR_CALL_REASON = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_CALL_REASON", "0"));
        SIMULATOR_ANUM_START = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_ANUM_START", "100000000"));
        SIMULATOR_ANUM_RANGE = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_ANUM_RANGE", "99999999"));
        SIMULATOR_BNUM_START = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_BNUM_START", "800000000"));
        SIMULATOR_BNUM_RANGE = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_BNUM_RANGE", "99999999"));
        SIMULATOR_MINIMUM_DATA = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_SIMULATOR_MINIMUM_DATA", "false"));
        SIMULATOR_NODEID = getenv.getOrDefault("CDRPR_SIMULATOR_NODEID", "" +
                "Moscow, Ljubljana, Berlin, London, Paris, Moscow, Amsterdam, Belgrade, Madrid, " +
                "Paris, Berlin, Copenhagen, Madrid, Moscow, Rome, Zurich, Lisbon, Warsaw, Berlin, Helsinki, Prague, " +
                "Vienna, London, Paris, Budapest, Zagreb, Belgrade, Kiev, Moscow, Amsterdam, Brussels, London, Paris");

        // possible values: STORE_ALL_CALLS, STORE_AGGREGATED_CALLS
        SIMULATOR_MODE = getenv.getOrDefault("CDRPR_SIMULATOR_MODE", "STORE_AGGREGATED_CALLS");
        // possible values: ELASTICSEARCH, POSTGRES
        SIMULATOR_STORAGE_TYPE = getenv.getOrDefault("CDRPR_SIMULATOR_STORAGE_TYPE", "POSTGRES");

        BULK_SIZE = Integer.parseInt(getenv.getOrDefault("CDRPR_BULK_SIZE", "8000"));
        SEND_INTERVAL_SEC = Integer.parseInt(getenv.getOrDefault("CDRPR_SEND_INTERVAL_SEC", "60000"));
        DEBUG_ENABLED = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_DEBUG_ENABLED", "false"));
        ES_URL = getenv.getOrDefault("CDRPR_ES_URL", testUrl);
        PG_URL = getenv.getOrDefault("CDRPR_PG_URL", testPgUrl);
        PG_USER = getenv.getOrDefault("CDRPR_PG_USER", "postgres");
        PG_PASS = getenv.getOrDefault("CDRPR_PG_PASS", "object00");
        PG_CREATE_TABLES_ON_START = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_PG_CREATE_TABLES_ON_START", "false"));
        EXIT_AT_THE_END = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_EXIT", "true"));
        ENABLE_PROMETHEUS_METRICS = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_ENABLE_PROMETHEUS_METRICS", "true"));

        try {
            HOSTNAME = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {}

        System.out.println("HOSTNAME: " + HOSTNAME);
        System.out.println("NUM_OF_THREADS: " + SIMULATOR_NUM_OF_THREADS);
        System.out.println("BULK_SIZE: " + BULK_SIZE);
        System.out.println("ES_URL: " + ES_URL);
        System.out.println("PG_URL: " + PG_URL);
        System.out.println("SIMULATOR_NODEID: " + SIMULATOR_NODEID);
        System.out.println("SIMULATOR_DELAY: " + SIMULATOR_CALL_DELAY);
        System.out.println("SIMULATOR_CALL_REASON: " + SIMULATOR_CALL_REASON);

        releaseCausesProps = new Properties();
        try {
            releaseCausesProps.load(new FileInputStream("call_release_causes.properties"));
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }

        MetricsLib.init();
        MetricsLib.ENABLE_PROMETHEUS_METRICS = ENABLE_PROMETHEUS_METRICS;
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

    }



    public static void debug(String s) {
        if (DEBUG_ENABLED) System.out.println(s);
    }

    public static synchronized void addCdr(CdrBean cdrBean) {
        if (queue.size() > 200 * BULK_SIZE) queue.poll();
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
