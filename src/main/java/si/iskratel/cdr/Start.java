package si.iskratel.cdr;


import si.iskratel.cdr.parser.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Start {

    public static String HOSTNAME = "localhost0";
    public static int BULK_SIZE = 100;
    public static int SIMULATOR_NUM_OF_THREADS = 1;
    public static boolean DEBUG_ENABLED = false;
    public static String ES_URL;
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
//        String testUrl = "http://pgcentos:9200/cdraggs/_bulk?pretty";
        String testUrl = "http://elasticvm:9200/cdraggs/_bulk?pretty";
//        String testUrl = "http://centosvm:9200/cdr_aggs/_bulk?pretty";

        Map<String, String> getenv = System.getenv();
        SIMULATOR_NUM_OF_THREADS = Integer.parseInt(getenv.getOrDefault("CDRPR_THREADS", "64"));
        SIMULATOR_NODEID = getenv.getOrDefault("CDRPR_SIMULATOR_NODEID", "Moscow, Ljubljana, Berlin, " +
                "London, Moscow, Rome, " +
                "Paris, Berlin, Copenhagen, Madrid, Moscow, Rome, Zurich, Lisbon, Warsaw, Berlin, Helsinki, Prague, " +
                "Vienna, London, Paris, " +
                "Budapest, Zagreb, Belgrade, Kiev, Moscow, Amsterdam, Brussels, London, Paris");
        SIMULATOR_CALL_DELAY = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_DELAY", "20"));
        SIMULATOR_CALL_REASON = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_CALL_REASON", "0"));
        SIMULATOR_ANUM_START = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_ANUM_START", "10000000"));
        SIMULATOR_ANUM_RANGE = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_ANUM_RANGE", "9999999"));
        SIMULATOR_BNUM_START = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_BNUM_START", "80000000"));
        SIMULATOR_BNUM_RANGE = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_BNUM_RANGE", "9999999"));
        SIMULATOR_MINIMUM_DATA = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_SIMULATOR_MINIMUM_DATA", "false"));

        // possible values
//        SIMULATOR_MODE = getenv.getOrDefault("CDRPR_SIMULATOR_MODE", "STORE_ALL_CALLS");
        SIMULATOR_MODE = getenv.getOrDefault("CDRPR_SIMULATOR_MODE", "STORE_AGGREGATED_CALLS");
        SIMULATOR_STORAGE_TYPE = getenv.getOrDefault("CDRPR_SIMULATOR_STORAGE_TYPE", "ELASTICSEARCH");
//        SIMULATOR_STORAGE_TYPE = getenv.getOrDefault("CDRPR_SIMULATOR_STORAGE_TYPE", "POSTGRES");

        BULK_SIZE = Integer.parseInt(getenv.getOrDefault("CDRPR_BULK_SIZE", "8000"));
        DEBUG_ENABLED = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_DEBUG_ENABLED", "false"));
        ES_URL = getenv.getOrDefault("CDRPR_ES_URL", testUrl);
        EXIT_AT_THE_END = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_EXIT", "true"));

        try {
            HOSTNAME = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {}

        System.out.println("HOSTNAME: " + HOSTNAME);
        System.out.println("NUM_OF_THREADS: " + SIMULATOR_NUM_OF_THREADS);
        System.out.println("BULK_SIZE: " + BULK_SIZE);
        System.out.println("ES_URL: " + ES_URL);
        System.out.println("SIMULATOR_NODEID: " + SIMULATOR_NODEID);
        System.out.println("SIMULATOR_DELAY: " + SIMULATOR_CALL_DELAY);
        System.out.println("SIMULATOR_CALL_REASON: " + SIMULATOR_CALL_REASON);

        releaseCausesProps = new Properties();
        try {
            releaseCausesProps.load(new FileInputStream("call_release_causes.properties"));
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }

        PrometheusMetrics.startJetty();
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

        IPersistenceClient persistenceClient = null;

        if (SIMULATOR_STORAGE_TYPE.equalsIgnoreCase("ELASTICSEARCH")) {

            if (SIMULATOR_MODE.equalsIgnoreCase("STORE_ALL_CALLS")) {
                persistenceClient = new EsStoreAllCallsPersistenceClient(1);
            }
            if (SIMULATOR_MODE.equalsIgnoreCase("STORE_AGGREGATED_CALLS")) {
                persistenceClient = new EsStoreAggregatedCalls(1);
            }

        }

        if (SIMULATOR_STORAGE_TYPE.equalsIgnoreCase("POSTGRES")) {

            // TODO

        }

        Thread t = new Thread(persistenceClient);
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
