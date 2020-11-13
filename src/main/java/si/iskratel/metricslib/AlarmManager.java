package si.iskratel.metricslib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.util.HashMap;
import java.util.Map;

public class AlarmManager {

    private static Map<String, Alarm> activeAlarmsList = new HashMap<>();

    public static void raiseAlarm(Alarm alarm) {
        raiseAlarm(alarm, true);
    }

    public static synchronized void raiseAlarm(Alarm alarm, boolean send) {
        activeAlarmsList.put(alarm.getAlarmId(), alarm);
        if (send) push(alarm);
    }

    public static synchronized void clearAlarm(Alarm alarm) {
        Alarm a = activeAlarmsList.remove(alarm.getAlarmId());
        if (a != null) {
            a.setSeverity(5);
            push(a);
        }
    }

    private static void push(Alarm alarm) {

        OkHttpClient httpClient = new OkHttpClient();
        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

        String body = toJsonString();
        System.out.println("JSON alarms: " + body);

        Request request = new Request.Builder()
                .url("http://192.168.1.222:9070/webhook")
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
                .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                .build();

        try {

            Response response = httpClient.newCall(request).execute();
            System.out.println("INFO:  AlarmManager: alarm " + alarm.getAlarmName() + " sent; responseCode=" + response.code());
            boolean success = response.isSuccessful();
            int responseCode = response.code();
            String responseText = response.body().string();
            response.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String toJsonString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(activeAlarmsList.values());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
