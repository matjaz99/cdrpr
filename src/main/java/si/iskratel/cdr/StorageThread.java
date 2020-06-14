package si.iskratel.cdr;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StorageThread extends Thread {

    private static Map<String, Long> callsInProgress = new HashMap<>();

    public void run() {

        while (true) {

            PrometheusMetrics.queueSize.set(Start.getQueueSize());
            clearMap();

            try {
                sleep(2000);
            } catch (InterruptedException e) {
            }

        }

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

    private static synchronized void clearMap() {
        // clean subscribers that are not in the call anymore
        long now = System.currentTimeMillis();
        try {
            Iterator it = callsInProgress.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Long> pair = (Map.Entry) it.next();
                if (pair.getValue() < now) {
                    PrometheusMetrics.callsInProgressRemoved.inc();
                    it.remove(); // avoids a ConcurrentModificationException
                }
            }
            PrometheusMetrics.callsInProgress.set(callsInProgress.size());
            System.out.println("StorageThread: callsInProgress.size=" + callsInProgress.size());
        } catch (Exception e) {
            System.err.println("StorageThread: Error cleaning table callsInProgress");
        }
    }

}
