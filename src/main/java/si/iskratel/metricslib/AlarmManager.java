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
        if (activeAlarmsList.containsKey(alarm.getAlarmId())) return;
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

        if (alarm.getTimestamp() == 0) alarm.setTimestamp(System.currentTimeMillis());

        OkHttpClient httpClient = new OkHttpClient();
        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

        String body = toJsonString(alarm);
        System.out.println("push alarm: " + body);

        Request request = new Request.Builder()
                .url(MetricsLib.ALARM_DESTINATION)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_API_VERSION)
                .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                .build();

        try {

            Response response = httpClient.newCall(request).execute();
            System.out.println("INFO:  AlarmManager: sending: " + alarm.getAlarmName() + "; responseCode=" + response.code());
            boolean success = response.isSuccessful();
            int responseCode = response.code();
            String responseText = response.body().string();
            response.close();

        } catch (Exception e) {
            System.out.println("ERROR: AlarmManager[0]: Could not send alarm. " + e.getMessage());
        }

        PromExporter.metricslib_alarms_sent_total.inc();

    }

    public static String toJsonString(Alarm alarm) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return "[" + mapper.writeValueAsString(alarm) + "]";
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toJsonStringAllAlarms() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(activeAlarmsList.values());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
