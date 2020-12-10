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

    /**
     * Return only metrics that suite selected criteria. Map of labelNames and concrete labelValues.
     * Eg. Get all metrics where node=abc and cause=Busy.
     * Other labels will be aggregated (summed-up).
     * @return metric with one time-series point
     */
    public PMetric FILTER(String[] labels, String[] values) {

        if (labels.length > labelNames.length) throw new PMetricException("Length mismatch");

        PMetric m = PMetric.build()
                .setName(name + "_filter")
                .setHelp("filter sum")
                .setLabelNames(labels);

        for (PTimeSeries ts : timeSeries.values()) {
            // if all labels match, then increment
            int[] res = new int[labels.length];
            for (int i = 0; i < labels.length; i++) {
                if (ts.containsLabelValue(values[i])) res[i] = 1;
            }
//            System.out.println("res: " + Arrays.toString(res));
            // if sum of result array equals length, then all labels matched
            int sum = 0;
            for (int i = 0; i < res.length; i++) {
                sum += res[i];
            }
            if (sum == res.length) {
                m.setLabelValues(values).inc(ts.getValue());
            }
        }

        return m;
    }


    /**
     * Summarize all metrics which match the labels. This comes handy to reduce the number of labels.
     * @param labels
     * @return metric with many time-series points according to given label names
     */
    public PMetric AGGREGATE(String[] labels) {

        PMetric m = PMetric.build()
                .setName(name + "_aggregate")
                .setHelp("filter sum")
                .setLabelNames(labels);

        int[] lblPosition = new int[labels.length];
        for (int i = 0; i < labels.length; i++) {
            for (int j = 0; j < labelNames.length; j++) {
                if (labels[i].equals(labelNames[j])) {
                    lblPosition[i] = j;
                }
            }
        }
        System.out.println("lblPosition: " + Arrays.toString(lblPosition));

        for (PTimeSeries ts : timeSeries.values()) {

            String[] arr = new String[lblPosition.length];
            for (int i = 0; i < lblPosition.length; i++) {
                arr[i] = ts.getLabelValues()[lblPosition[i]];
            }

            System.out.println("arr: " + Arrays.toString(arr));

            m.setLabelValues(arr).inc(ts.getValue());

        }

        return m;
    }

    public boolean containsLabelName(String labelName) {
        for (int i = 0; i < labelNames.length; i++) {
            if (labelNames[i].equals(labelName)) return true;
        }
        return false;
    }


    @Override
    public String toString() {
        return "metric_name=" + name + ", help=" + help + ", labels=" + Arrays.toString(labelNames);
    }

    public String toStringDetail() {
        String s = "PMetric[" + "timestamp=" + timestamp + ", metric_name=" + name+ ", labels=" + Arrays.toString(labelNames) + ", timeseries=\n";
        for (Map.Entry<String, PTimeSeries> entry : timeSeries.entrySet()) {
            s += "\t" + entry.getValue().toString() + "\n";
        }
        return s + "]";
    }

}
