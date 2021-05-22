package si.iskratel.metricslib;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PMultiValueMetric {

    /*
    The idea is to have two types of metrics:
    1. single value metric - multiple labels (string) and only one value (double)
       eg. total_calls{node=node1,cause=answered} 1234
    2. multi value metric - only one label (string) and multiple values (double)
       In PG each row represents value for column name
       eg. total_calls{node=node1} answered=523, busy=134, noreply=32
     */

    private long timestamp = 0;
    private String name;
    private String help;
    private Map<String, String> labelsMap = new HashMap<>();
    private Map<String, Double> valuesMap = new HashMap<>();
    private String parentRegistry;

    public static PMultiValueMetric build() {
        PMultiValueMetric m = new PMultiValueMetric();
        return m;
    }

    public PMultiValueMetric setHelp(String help) {
        this.help = help;
        return this;
    }

    public String getHelp() {
        if (help == null) throw new PMetricException("Help missing");
        return help;
    }

    public PMultiValueMetric setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getParentRegistry() {
        return parentRegistry;
    }

    public PMultiValueMetric addLabel(String key, String value) {
        if (labelsMap == null) labelsMap = new HashMap<>();
        labelsMap.put(key, value);
        return this;
    }

    public PMultiValueMetric addValue(String key, double value) {
        if (valuesMap == null) valuesMap = new HashMap<>();
        valuesMap.put(key, value);
        return this;
    }

    public Map<String, String> getLabelsMap() {
        return labelsMap;
    }

    public Map<String, Double> getValuesMap() {
        return valuesMap;
    }

    public PMultiValueMetric register(String registryName) {
        PMetricRegistry.registerMultiValueMetric(registryName,this);
        this.parentRegistry = registryName;
        return this;
    }

    public void clear() {
        labelsMap.clear();
        valuesMap.clear();
    }

    @Override
    public String toString() {
        String lbls = "";
        for (String s : labelsMap.keySet()) {
            lbls += s + ", ";
        }
        return "metric_name=" + name + ", help=" + help + ", labels=[" + lbls.substring(0, lbls.length() - 2) + "]";
    }

    public String toStringDetail() {
        String s = "PMultiValueMetric[" + "timestamp=" + timestamp + ", metric_name=" + name + ", data=\n";
        s += "\tlabels=" + labelsMap + "\n";
        s += "\tvalues=" + valuesMap + "\n";
        return s + "], size=" + (labelsMap.size() + valuesMap.size());
    }

}
