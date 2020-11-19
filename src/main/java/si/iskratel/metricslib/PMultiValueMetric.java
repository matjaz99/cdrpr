package si.iskratel.metricslib;

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
    private Map<String, String> labelsMap;
    private Map<String, Double> valuesMap;
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

    public PMultiValueMetric addValue(String key, Double value) {
        if (valuesMap == null) valuesMap = new HashMap<>();
        valuesMap.put(key, value);
        return this;
    }

    public PMultiValueMetric register(String registryName) {
        PMetricRegistry.registerMultiValueMetric(registryName,this);
        this.parentRegistry = registryName;
        return this;
    }

    @Override
    public String toString() {
        return "PMultiValueMetric{" +
                "timestamp=" + timestamp +
                ", name='" + name + '\'' +
                ", help='" + help + '\'' +
                ", labelsMap=" + labelsMap +
                ", valuesMap=" + valuesMap +
                ", parentRegistry='" + parentRegistry + '\'' +
                '}';
    }
}
