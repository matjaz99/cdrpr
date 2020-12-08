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
//        if (help == null) return "Help missing";
        if (help == null) throw new PMetricException("Help missing");
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
            throw new PMetricException("Number of label names is different than number of label values");
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

    public synchronized List<PTimeSeries> getTimeSeries() {
        return new ArrayList<>(timeSeries.values());
    }

    public double getSUM(String labelName, String filterLabelValue) {
        double sum = 0.0;
        int lblPosition = -1;

        for (int i = 0; i < labelNames.length; i++) {
            if (labelNames[i].equals(labelName)) {
                lblPosition = i;
            }
        }

        for (PTimeSeries ts : timeSeries.values()) {

            if (ts.getLabelValues()[lblPosition].equals(filterLabelValue)) {
                sum += ts.getValue();
            }

        }

        return sum;
    }

//    public PMetric SUM(PMetric metric1, PMetric metric2) {
//        PMetric m = PMetric.build()
//                .setName(metric1.getName() + "#" + metric2.getName())
//                .setHelp("SUM of " + metric1.getName() + "+" + metric2.getName())
//                .setLabelNames(labelNames);
//
//        // merge labels
//
//    }

    /**
     * Multiply all values by factor. This method is also used for dividing. For example to divide value by 60,
     * simply multiply it with 1/60.
     * @param factor
     */
    public void MULTIPLY(double factor) {
        for (PTimeSeries t : timeSeries.values()) {
            t.set(t.getValue() * factor);
        }
        if (value != null) value = value * factor;
    }

    public PMetric filterSUM(String labels) {
        PMetric m = PMetric.build()
                .setName(name + "_filterSUM")
                .setHelp("filter sum")
                .setLabelNames(labels);

        int lblPosition = -1;
        for (int i = 0; i < labelNames.length; i++) {
            if (labelNames[i].equals(labels)) {
                lblPosition = i;
            }
        }


        for (PTimeSeries t : timeSeries.values()) {
//            if (t.getLabelValues()[lblPosition])
        }
        return m;
    }



    @Override
    public String toString() {
        return "metric_name=" + name + ", help=" + help + ", labels=" + Arrays.toString(labelNames);
    }

    public String toStringDetail() {
        String s = "PMetric[" + "timestamp=" + timestamp + ", metric_name=" + name + ", timeseries=\n";
        for (Map.Entry<String, PTimeSeries> entry : timeSeries.entrySet()) {
            s += "\t" + entry.getValue().toString() + "\n";
        }
        return s + "]";
    }

}
