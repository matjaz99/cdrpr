package si.iskratel.cdr;


import org.apache.commons.io.IOUtils;
import si.iskratel.cdr.manager.BadCdrRecordException;
import si.iskratel.cdr.parser.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Test {

    public static String DIRECTORY;
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

    public static LinkedBlockingQueue<CdrBean> queue = new LinkedBlockingQueue();
    public static boolean running = true;

    public static List<EsClientThread2> threads = new ArrayList<>();
    public static List<CdrSimulatorThread> simulatorThreadThreads = new ArrayList<>();

    public static Properties releaseCausesProps;

    public static void main(String[] args) throws Exception {

        Runtime.getRuntime().addShutdownHook(new MyShutdownHook());

//        String testDir = "C:\\Users\\cerkvenik\\Documents\\CDRs\\experimental\\03";
        String testDir = "/Users/matjaz/Developer/cdr-files/samples/15M";
//        String testUrl = "http://mcrk-docker-1:9200/cdrs/_bulk?pretty";
        String testUrl = "http://pgcentos:9200/cdrs/_bulk?pretty";
//        String testUrl = "http://centosvm:9200/cdrs/_bulk?pretty";

        Map<String, String> getenv = System.getenv();
        DIRECTORY = getenv.getOrDefault("CDRPR_DIRECTORY", testDir);
        NUM_OF_THREADS = Integer.parseInt(getenv.getOrDefault("CDRPR_THREADS", "32"));
        BULK_SIZE = Integer.parseInt(getenv.getOrDefault("CDRPR_BULK_SIZE", "10000"));
        DEBUG_ENABLED = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_DEBUG_ENABLED", "false"));
        ES_URL = getenv.getOrDefault("CDRPR_ES_URL", testUrl);
        EXIT = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_EXIT", "true"));
        SIMULATOR_MODE = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_SIMULATOR_MODE", "true"));
        SIMULATOR_NODEID = getenv.getOrDefault("CDRPR_SIMULATOR_NODEID", "Ljubljana");
        SIMULATOR_DELAY = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_DELAY", "100"));
        SIMULATOR_CALL_REASON = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_CALL_REASON", "0"));
        SIMULATOR_ANUM_START = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_ANUM_START", "1000000"));
        SIMULATOR_ANUM_RANGE = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_ANUM_RANGE", "999999"));
        SIMULATOR_BNUM_START = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_BNUM_START", "8000000"));
        SIMULATOR_BNUM_RANGE = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_BNUM_RANGE", "999999"));

        System.out.println("NUM_OF_THREADS: " + NUM_OF_THREADS);
        System.out.println("BULK_SIZE: " + BULK_SIZE);
        System.out.println("CDR_DIRECTORY: " + DIRECTORY);
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

        // run simulator only; will not parse files if true!
        if (SIMULATOR_MODE) runSimulator();

        File dir = new File(DIRECTORY);
        File[] files = dir.listFiles();

        for (int i = 1; i < NUM_OF_THREADS + 1; i++) {
            threads.add(new EsClientThread2(i, "1001"));
        }

        int j = 0;
        for (int i = 0; i < files.length; i++) {
            threads.get(j).addFile(files[i]);
            j++;
            if (j == NUM_OF_THREADS) {
                j = 0;
            }
        }

        startTime = System.currentTimeMillis();

        for (int i = 0; i < NUM_OF_THREADS; i++) {
            threads.get(i).start();
            Thread.sleep(100);
        }

//        Thread t = new Thread(new EsClientThread());
//        t.start();

//        for (int i = 0; i < files.length; i++) {
//            parse(files[i]);
//        }

        while (true) {
            boolean stillRunning = false;
            for (EsClientThread2 t : threads) {
                stillRunning = stillRunning || t.isRunning();
            }
            if (stillRunning) {
                Thread.sleep(100);
            } else {
                break;
            }
        }

        long totalCdrCount = 0;
        long totalBadCdrCount = 0;
        int totalPostCount = 0;
        int totalResendCount = 0;
        for (EsClientThread2 t : threads) {
            totalCdrCount += t.getTotalCdrCount();
            totalBadCdrCount += t.getBadCdrRecordExceptionCount();
            totalPostCount += t.getPostCount();
            totalResendCount += t.getResendCount();
        }

        endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;

        System.out.println("--- Main process ended ---");
        System.out.println("Threads: " + NUM_OF_THREADS);
        System.out.println("Bulk size: " + BULK_SIZE);
        System.out.println("Directory: " + DIRECTORY);
        System.out.println("Files in dir: " + files.length);
        System.out.println("Records count: " + totalCdrCount);
        System.out.println("Bad records count: " + totalBadCdrCount);
        System.out.println("Total processing time: " + processingTime);
        System.out.println("Rate: " + (totalCdrCount * 1.0 / processingTime / 1.0 * 1000));
        System.out.println("Post requests count: " + totalPostCount);
        System.out.println("Resend count: " + totalResendCount);

        if (!EXIT) {
            while (true) {
                // do not exit
                Thread.sleep(1000);
            }
        }

    }

    public static void parse(File f) throws Exception {

        FileInputStream is = new FileInputStream(f);
//        ByteArrayInputStream bais = new ByteArrayInputStream(is.readAllBytes()); // requires Java 9!!!
        byte[] bytes = IOUtils.toByteArray(is);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        List<DataRecord> list = CDRReader.readDataRecords(bais);
        debug("records in file: " + list.size());

        for (DataRecord dr : list) {
            debug(dr.toString());
            CdrBeanCreator cbc = new CdrBeanCreator() {
                @Override
                public void setSpecificBeanValues(CdrObject cdrObj, CdrBean cdrBean) {

                }
            };
            try {
                CdrBean cdrBean = cbc.parseBinaryCdr(dr.getDataRecordBytes(), null);
                queue.put(cdrBean);
                totalCount++;
                if (BULK_SIZE == 1) {
                    ElasticHttpClient.sendOkhttpPost(cdrBean);
                } else {
//                    ElasticHttpClient.sendBulkPost(cdrBean);
                }
                debug(cdrBean.toString());
            } catch (BadCdrRecordException e) {
                badCdrRecordExceptionCount++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void debug(String s) {
        if (DEBUG_ENABLED) System.out.println(s);
    }

    public static void runSimulator() {

        for (int i = 1; i < NUM_OF_THREADS + 1; i++) {
            CdrSimulatorThread t = new CdrSimulatorThread(i);
            t.start();
            simulatorThreadThreads.add(t);
            System.out.println("Simulator thread created: " + t.getThreadId());
            try {
                Thread.sleep(1234);
            } catch (InterruptedException e) {

            }
        }

        StorageThread ct = new StorageThread();
        ct.start();

        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }

        System.exit(0);
    }

}
