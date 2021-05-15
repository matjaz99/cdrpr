package si.iskratel.metricslib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AlarmManager {

    private static Logger logger = LoggerFactory.getLogger(AlarmManager.class);

    private static Map<String, Alarm> activeAlarmsList = new HashMap<>();

    public static Map<Integer, String> alarmSeveritiesMap = new HashMap<>();

    public static Properties alarmSeveritiesProperties = new Properties();

    static {
        try {
            alarmSeveritiesProperties.load(new FileInputStream("severities.properties"));
            for (Object o : alarmSeveritiesProperties.keySet()) {
                alarmSeveritiesMap.put(Integer.parseInt(String.valueOf(o)), alarmSeveritiesProperties.getProperty(String.valueOf(o)));
            }
            // TODO read from props
//            alarmSeveritiesMap.put(0, "Indeterminate");
//            alarmSeveritiesMap.put(1, "Critical");
//            alarmSeveritiesMap.put(2, "Major");
//            alarmSeveritiesMap.put(3, "Minor");
//            alarmSeveritiesMap.put(4, "Warning");
//            alarmSeveritiesMap.put(5, "Clear");
//            alarmSeveritiesMap.put(6, "Informational");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private AlarmManager() {

    }

    public static synchronized void raiseAlarm(Alarm alarm) {
        if (alarm.getTimestamp() == 0) alarm.setTimestamp(System.currentTimeMillis());
        if (alarm.getNotificationType().equalsIgnoreCase("alarm")) {
            if (activeAlarmsList.containsKey(alarm.getAlarmId())) return;
            activeAlarmsList.put(alarm.getAlarmId(), alarm);
        }
        String body = toJsonString(alarm);
        push(body, (alarm.getSeverity() == 5 ? "CLEAR" : "ALARM"));
        alarm.setTimestamp(0);
    }

    public static synchronized void clearAlarm(Alarm alarm) {
        alarm.setTimestamp(0);
        Alarm a = activeAlarmsList.remove(alarm.getAlarmId());
        if (a != null) {
            int sev = a.getSeverity();
            a.setSeverity(5);
            if (alarm.getTimestamp() == 0) alarm.setTimestamp(System.currentTimeMillis());
            String body = toJsonString(alarm);
            push(body, (alarm.getSeverity() == 5 ? "CLEAR" : "ALARM"));
            a.setSeverity(sev);
            a.setTimestamp(0);
        }
    }

    private static void push(String body, String severity) {

        if (MetricsLib.ALARM_DESTINATION == null) return;

        logger.info("push(): sending " + severity + ": " + body);

        OkHttpClient httpClient = new OkHttpClient();
        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

        try {

            Request request = new Request.Builder()
                    .url(MetricsLib.ALARM_DESTINATION)
                    .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_API_VERSION)
                    .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                    .build();

            Response response = httpClient.newCall(request).execute();
            logger.info("push(): responseCode=" + response.code());
            boolean success = response.isSuccessful();
            int responseCode = response.code();
            String responseText = response.body().string();
            response.close();

        } catch (Exception e) {
            logger.error("push(): Could not send alarm. " + e.getMessage());
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
