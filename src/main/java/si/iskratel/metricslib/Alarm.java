package si.iskratel.metricslib;

public class Alarm {

    private int nodeId;
    private String alarmId;
    private long timestamp;
    private int alarmCode;
    private String alarmName;
    private int severity;
    private String severityString;
    private String sourceInfo;
    private String additionalInfo;

    public Alarm(int nodeId, int alarmCode, String alarmName, int severity, String sourceInfo, String additionalInfo) {
        this.nodeId = nodeId;
        this.alarmCode = alarmCode;
        this.alarmName = alarmName;
        this.severity = severity;
        this.sourceInfo = sourceInfo;
        this.additionalInfo = additionalInfo;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getAlarmId() {
        return alarmCode + alarmName + nodeId + sourceInfo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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
        return severityString;
    }

    public void setSeverityString(String severityString) {
        this.severityString = severityString;
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

    @Override
    public String toString() {
        return "Alarm{" +
                "nodeId=" + nodeId +
                ", alarmId='" + alarmId + '\'' +
                ", timestamp=" + timestamp +
                ", alarmCode=" + alarmCode +
                ", alarmName='" + alarmName + '\'' +
                ", severity=" + severity +
                ", severityString='" + severityString + '\'' +
                ", sourceInfo='" + sourceInfo + '\'' +
                ", additionalInfo='" + additionalInfo + '\'' +
                '}';
    }
}
