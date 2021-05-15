package si.iskratel.metricslib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Alarm {

    private String alarmId;
    private String alarmSource;
    private long timestamp;
    private String dateTime;
    private int alarmCode = 7774777;
    private String alarmName = "Alarm incident";
    private int severity = 0;
    private String severityString = "Indeterminate";
    private String notificationType = "alarm";
    private String sourceInfo = "";
    private String additionalInfo = "";
    private int probableCause = 1024;
    private int eventType = 5;

    public Alarm() {}

    public Alarm(int alarmCode, String alarmName, int severity, String sourceInfo, String additionalInfo) {
        this.alarmCode = alarmCode;
        this.alarmName = alarmName;
        this.severity = severity;
        this.sourceInfo = sourceInfo;
        this.additionalInfo = additionalInfo;
    }

    public Alarm(int alarmCode, String alarmName, int severity, String sourceInfo, String additionalInfo, boolean isEvent) {
        this.alarmCode = alarmCode;
        this.alarmName = alarmName;
        this.severity = severity;
        this.sourceInfo = sourceInfo;
        this.additionalInfo = additionalInfo;
        if (isEvent) this.notificationType = "event";
    }

    public Alarm(int alarmCode, String alarmName, int severity, String sourceInfo, String additionalInfo, int probableCause, int eventType, boolean isEvent) {
        this.alarmCode = alarmCode;
        this.alarmName = alarmName;
        this.severity = severity;
        this.sourceInfo = sourceInfo;
        this.additionalInfo = additionalInfo;
        this.probableCause = probableCause;
        this.eventType = eventType;
        if (isEvent) this.notificationType = "event";
    }

    public String getAlarmId() {
        alarmId = MD5Checksum.getMd5Checksum(alarmCode + alarmName + sourceInfo);
        return alarmId;
    }

    public String getAlarmSource() {
        alarmSource = MetricsLib.METRICSLIB_HOSTNAME;
        return alarmSource;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDateTime() {
        Date date = new Date(timestamp);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        dateTime = dateFormat.format(date);
        return dateTime;
    }

    public int getAlarmCode() {
        return alarmCode;
    }

    public void setAlarmCode(int alarmCode) {
        this.alarmCode = alarmCode;
    }

    public String getAlarmName() {
        return alarmName;
    }

    public void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public String getSeverityString() {
        severityString = AlarmManager.alarmSeveritiesMap.getOrDefault(severity, "Indeterminate");
        return severityString;
    }

    public String getSourceInfo() {
        return sourceInfo;
    }

    public void setSourceInfo(String sourceInfo) {
        this.sourceInfo = sourceInfo;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public int getProbableCause() {
        return probableCause;
    }

    public void setProbableCause(int probableCause) {
        this.probableCause = probableCause;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "alarmId='" + getAlarmId() + '\'' +
                ", timestamp=" + timestamp +
                ", alarmCode=" + alarmCode +
                ", alarmName='" + alarmName + '\'' +
                ", severity=" + severity +
                ", severityString='" + severityString + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", sourceInfo='" + sourceInfo + '\'' +
                ", additionalInfo='" + additionalInfo + '\'' +
                ", probableCause=" + probableCause +
                ", eventType=" + eventType +
                '}';
    }
}
