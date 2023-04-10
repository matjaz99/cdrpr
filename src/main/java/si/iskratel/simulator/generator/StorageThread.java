package si.iskratel.simulator.generator;

import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.simulator.SimulatorMetrics;
import si.iskratel.simulator.Start;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class StorageThread extends Thread {

    /** Main list which contains generated CDRs (CdrBeans) */
    private static LinkedBlockingQueue<CdrBean> queue = new LinkedBlockingQueue();

    private static Map<String, Long> callsInProgress = new HashMap<>();

    public void run() {

        while (true) {

            SimulatorMetrics.queueSize.set(queue.size());
            clearMap();

            try {
                sleep(5000);
            } catch (InterruptedException e) {
            }

        }

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


    public static synchronized void addCall(String aNum, long endTime) {
        callsInProgress.put(aNum, endTime);
    }

    public static boolean contains(String aNum) {
        if (callsInProgress.containsKey(aNum)) {
            return true;
        }
        return false;
    }

    public static int getNumberOfCallsInProgress() {
        return callsInProgress.size();
    }

    private static synchronized void clearMap() {
        // clean subscribers that are not in the call anymore
        long now = System.currentTimeMillis();
        try {
            Iterator it = callsInProgress.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Long> pair = (Map.Entry) it.next();
                if (pair.getValue() < now) {
                    SimulatorMetrics.callsInProgressRemoved.inc();
                    it.remove(); // avoids a ConcurrentModificationException
                }
            }
            SimulatorMetrics.callsInProgress.set(callsInProgress.size());
            System.out.println("calls in progress: " + callsInProgress.size());
        } catch (Exception e) {
            System.err.println("StorageThread: Error cleaning table callsInProgress");
        }
    }

}
