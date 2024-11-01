package si.matjazcerkvenik.metricslib.util;

import java.util.HashMap;
import java.util.Map;

public class StateLog {

    private static Map<String, String> stateLog = new HashMap<>();

    public static void addToStateLog(String item, String state) {
        stateLog.put(item, state);
    }

    public static void removeFromStateLog(String item) {
        stateLog.remove(item);
    }

    public static Map<String, String> getStateLog() {
        return stateLog;
    }
}
