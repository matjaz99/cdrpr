package si.iskratel.cdr;

import java.util.HashMap;
import java.util.Map;

public class PMetric {

    private long timestamp = System.currentTimeMillis();
    private String name;
    private String[] labelNames;
    private Map<String, PTimeSeries> timeSeries = new HashMap<>();

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public static PMetric build() {
        PMetric m = new PMetric();
        return m;
    }

    public PMetric setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public PMetric setLabelNames(String... labels) {
        labelNames = labels;
        return this;
    }

    public PTimeSeries setLabelValues(String... values) {
        String tsId = "";
        for (int i = 0; i < values.length; i++) {
            tsId += values[i];
        }
        PTimeSeries ts = timeSeries.getOrDefault(tsId, new PTimeSeries());
        ts.setLabelValues(values);
        timeSeries.put(tsId, ts);
        return ts;
    }

    public PMetric register() {
        PMetricRegistry.registerMetric(this);
        return this;
    }

    public void clear() {
        timeSeries.clear();
    }

    public int getTimeSeriesSize() {
        return timeSeries.size();
    }

    @Override
    public String toString() {
        String s = "CdrMetric{" + "timestamp=" + timestamp + ", name=" + name + ", timeseries={\n";
        for (Map.Entry<String, PTimeSeries> entry : timeSeries.entrySet()) {
            s += "\t" + entry.getValue().toString() + "\n";
        }
        return s + "}]";
    }

    public String toJsonString() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, PTimeSeries> entry : timeSeries.entrySet()) {
            sb.append("{ \"index\":{} }\n");
            sb.append("{");
            sb.append("\"name\":\"").append(name).append("\",");
            for (int i = 0; i < labelNames.length; i++) {
                sb.append("\"").append(labelNames[i]).append("\":\"").append(entry.getValue().getLabelValues()[i]).append("\",");
            }
            sb.append("\"value\":").append(entry.getValue().getValue()).append(",");
            sb.append("\"timestamp\":").append(timestamp);
            sb.append("}\n");
        }

        return sb.toString();
    }
}
