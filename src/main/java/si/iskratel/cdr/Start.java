package si.iskratel.cdr;


import si.iskratel.cdr.parser.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Start {

    public static int BULK_SIZE = 100;
    public static int NUM_OF_THREADS = 1;
    public static boolean DEBUG_ENABLED = false;
    public static String ES_URL;
    public static boolean EXIT = false;
    public static boolean SIMULATOR_MODE = false;
    public static String SIMULATOR_NODEID;
    public static int SIMULATOR_DELAY = 10;
    public static int SIMULATOR_CALL_REASON = 0;
    public static int SIMULATOR_ANUM_START = 0;
    public static int SIMULATOR_ANUM_RANGE = 0;
    public static int SIMULATOR_BNUM_START = 0;
    public static int SIMULATOR_BNUM_RANGE = 0;

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

//        String testDir = "C:\\Users\\cerkvenik\\Documents\\CDRs\\experimental\\03";
        String testDir = "/Users/matjaz/Developer/cdr-files/samples/15M";
//        String testUrl = "http://mcrk-docker-1:9200/cdrs/_bulk?pretty";
        String testUrl = "http://pgcentos:9200/cdrs/_bulk?pretty";
//        String testUrl = "http://centosvm:9200/cdrs/_bulk?pretty";

        Map<String, String> getenv = System.getenv();
        NUM_OF_THREADS = Integer.parseInt(getenv.getOrDefault("CDRPR_THREADS", "96"));
        BULK_SIZE = Integer.parseInt(getenv.getOrDefault("CDRPR_BULK_SIZE", "5000"));
        DEBUG_ENABLED = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_DEBUG_ENABLED", "false"));
        ES_URL = getenv.getOrDefault("CDRPR_ES_URL", testUrl);
        EXIT = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_EXIT", "true"));
        SIMULATOR_MODE = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_SIMULATOR_MODE", "true"));
        SIMULATOR_NODEID = getenv.getOrDefault("CDRPR_SIMULATOR_NODEID", "Ljubljana");
        SIMULATOR_DELAY = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_DELAY", "20"));
        SIMULATOR_CALL_REASON = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_CALL_REASON", "0"));
        SIMULATOR_ANUM_START = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_ANUM_START", "1000000"));
        SIMULATOR_ANUM_RANGE = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_ANUM_RANGE", "999999"));
        SIMULATOR_BNUM_START = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_BNUM_START", "8000000"));
        SIMULATOR_BNUM_RANGE = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_BNUM_RANGE", "999999"));

        System.out.println("NUM_OF_THREADS: " + NUM_OF_THREADS);
        System.out.println("BULK_SIZE: " + BULK_SIZE);
        System.out.println("ES_URL: " + ES_URL);
        System.out.println("SIMULATOR_MODE: " + SIMULATOR_MODE);
        System.out.println("SIMULATOR_NODEID: " + SIMULATOR_NODEID);
        System.out.println("SIMULATOR_DELAY: " + SIMULATOR_DELAY);
        System.out.println("SIMULATOR_CALL_REASON: " + SIMULATOR_CALL_REASON);

        releaseCausesProps = new Properties();
        try {
            releaseCausesProps.load(new FileInputStream("call_release_causes.properties"));
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }

        for (int i = 1; i < NUM_OF_THREADS + 1; i++) {
            CdrSimulatorThread t = new CdrSimulatorThread(i);
            t.start();
            simulatorThreads.add(t);
            System.out.println("Simulator thread created: " + t.getThreadId());
        }

        StorageThread ct = new StorageThread();
        ct.start();

        IPersistenceClient persistenceClient = new ElasticPersistenceClient(1);
        Thread t = new Thread(persistenceClient);
        t.start();

//        while (running) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//
//            }
//        }

        startTime = System.currentTimeMillis();

//        while (true) {
//            boolean stillRunning = false;
//            for (EsClientThread2 t : threads) {
//                stillRunning = stillRunning || t.isRunning();
//            }
//            if (stillRunning) {
//                Thread.sleep(100);
//            } else {
//                break;
//            }
//        }

//        long totalCdrCount = 0;
//        long totalBadCdrCount = 0;
//        int totalPostCount = 0;
//        int totalResendCount = 0;
//        for (EsClientThread2 t : threads) {
//            totalCdrCount += t.getTotalCdrCount();
//            totalBadCdrCount += t.getBadCdrRecordExceptionCount();
//            totalPostCount += t.getPostCount();
//            totalResendCount += t.getResendCount();
//        }

        endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;

//        System.out.println("--- Main process ended ---");
//        System.out.println("Threads: " + NUM_OF_THREADS);
//        System.out.println("Bulk size: " + BULK_SIZE);
//        System.out.println("Records count: " + totalCdrCount);
//        System.out.println("Bad records count: " + totalBadCdrCount);
//        System.out.println("Total processing time: " + processingTime);
//        System.out.println("Rate: " + (totalCdrCount * 1.0 / processingTime / 1.0 * 1000));
//        System.out.println("Post requests count: " + totalPostCount);
//        System.out.println("Resend count: " + totalResendCount);

        if (!EXIT) {
            while (true) {
                // do not exit
                Thread.sleep(1000);
            }
        }

    }



    public static void debug(String s) {
        if (DEBUG_ENABLED) System.out.println(s);
    }

    public static synchronized void addCdr(CdrBean cdrBean) {
        if (queue.size() > 10 * BULK_SIZE) queue.poll();
        queue.add(cdrBean);
    }

    public static int getQueueSize() {
        return queue.size();
    }

    public static synchronized CdrBean pollCdr() {
        return queue.poll();
    }

}
