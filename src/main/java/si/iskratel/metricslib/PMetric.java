package si.iskratel.metricslib;

import java.util.*;

public class PMetric {

    private long timestamp = 0;
    private String name;
    private String help;
    private String[] labelNames;
    private Map<String, PTimeSeries> timeSeries = new HashMap<>();
    // use this as value in case when metric has no labels
    private Double value;
    // the name of registry that this metric belongs to
    private String parentRegistry;

    public static PMetric build() {
        PMetric m = new PMetric();
        return m;
    }

    public PMetric setHelp(String help) {
        this.help = help;
        return this;
    }

    public String getHelp() {
        if (help == null) return "Help missing";
        return help;
    }

    public PMetric setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getParentRegistry() {
        return parentRegistry;
    }

    public PMetric setLabelNames(String... labels) {
        labelNames = labels;
        return this;
    }

    public String[] getLabelNames() {
        return labelNames;
    }

    public PTimeSeries setLabelValues(String... values) {
        if (labelNames.length != values.length) {
            System.out.println("WARN: Number of label names is different than number of label values.");
            return null;
        }
        String tsId = "";
        // ID of timeseries is concatenation of all label values
        for (int i = 0; i < values.length; i++) {
            tsId += values[i];
        }
        PTimeSeries ts = timeSeries.getOrDefault(tsId, new PTimeSeries());
        ts.setLabelValues(values);
        timeSeries.put(tsId, ts);
        return ts;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Double getValue() {
        return value;
    }

    public void set(double value) {
        this.value = value;
    }

    public void inc() {
        if (value == null) value = 0.0;
        value += 1.0;
    }

    public void inc(double d) {
        if (value == null) value = 0.0;
        value += d;
    }

    public PMetric register() {
        PMetricRegistry.registerMetric("default",this);
        this.parentRegistry = "default";
        return this;
    }

    public PMetric register(String registryName) {
        PMetricRegistry.registerMetric(registryName,this);
        this.parentRegistry = registryName;
        return this;
    }

    public void clear() {
        timeSeries.clear();
    }

    public int getTimeSeriesSize() {
        return timeSeries.size();
    }

    public List<PTimeSeries> getTimeSeries() {
        return new ArrayList<>(timeSeries.values());
    }

    @Override
    public String toString() {
        return "m_name=" + name + ", help=" + help + ", labels=" + Arrays.toString(labelNames);
    }

    public String toStringDetail() {
        String s = "PMetric[" + "timestamp=" + timestamp + ", m_name=" + name + ", timeseries=\n";
        for (Map.Entry<String, PTimeSeries> entry : timeSeries.entrySet()) {
            s += "\t" + entry.getValue().toString() + "\n";
        }
        return s + "]";
    }

}
