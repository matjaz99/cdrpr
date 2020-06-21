package si.iskratel.cdr;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CdrMetric {

    private long timestamp = System.currentTimeMillis();
    private String name;
    private String[] labelNames;
    private Map<String, CdrTimeSeries> timeSeries = new HashMap<>();


    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static CdrMetric build() {
        CdrMetric m = new CdrMetric();
        return m;
    }

    public CdrMetric setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public CdrMetric setLabelNames(String... labels) {
        labelNames = labels;
        return this;
    }

    public CdrTimeSeries setLabelValues(String... values) {
        String tsId = "";
        for (int i = 0; i < values.length; i++) {
            tsId += values[i];
        }
        CdrTimeSeries ts = timeSeries.getOrDefault(tsId, new CdrTimeSeries());
        ts.setLabelValues(values);
        timeSeries.put(tsId, ts);
        return ts;
    }

    public CdrMetric register() {
        CdrMetricRegistry.registerMetric(this);
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
        for (Map.Entry<String, CdrTimeSeries> entry : timeSeries.entrySet()) {
            s += "\t" + entry.getValue().toString() + "\n";
        }
        return s + "}]";
    }

    public String toJsonString() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, CdrTimeSeries> entry : timeSeries.entrySet()) {
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
