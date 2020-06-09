package si.iskratel.cdr;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StorageThread extends Thread {

    private static Map<String, Long> callsInProgress = new HashMap<>();

    public void run() {

        while (true) {

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
        int removedCount = 0;
        try {
            Iterator it = callsInProgress.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Long> pair = (Map.Entry) it.next();
                if (pair.getValue() < now) {
                    removedCount++;
                    it.remove(); // avoids a ConcurrentModificationException
                }
            }
            System.out.println("StorageThread: removed=" + removedCount + ", callsInProgress.size=" + callsInProgress.size());
        } catch (Exception e) {
            System.err.println("StorageThread: Error cleaning table callsInProgress");
        }
    }

}
