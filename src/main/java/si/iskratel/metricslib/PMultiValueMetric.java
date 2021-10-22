package si.iskratel.metricslib;

import java.util.*;

public class PMultiValueMetric {

    /*
    The idea is to have two types of metrics:
    1. single value metric - multiple labels (string) and only one value (double)
       eg. total_calls{node=node1,cause=answered} 1234
    2. multi value metric - multiple labels (string) and multiple values (double)
       In PG each row represents value for column name
       eg. total_calls{node=node1,trunkGroup=tg1} answered=523, busy=134, noreply=32
     */

    private long timestamp = 0;
    private String name;
    private String help;
    // list of all multivalue series (labls+values)
    private List<PMultivalueTimeSeries> multivalueMetrics = new ArrayList<>();
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
        if (name == null) throw new PMetricException("Name missing");
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

    public void addMultiValueTimeSeries(PMultivalueTimeSeries m) {
        multivalueMetrics.add(m);
    }

    public List<PMultivalueTimeSeries> getMultivalueMetrics() {
        return multivalueMetrics;
    }

    public PMultiValueMetric register(String registryName) {
        PMetricRegistry.registerMultiValueMetric(registryName,this);
        this.parentRegistry = registryName;
        return this;
    }

    public void clear() {
//        labelsMap.clear();
//        valuesMap.clear();
        multivalueMetrics.clear();
    }

    @Override
    public String toString() {
        String s = "";
        for (PMultivalueTimeSeries mvts : multivalueMetrics) {
            s += "metric_name=" + name + ", help=" + help + ", " + mvts.toString() + "\n";
        }
        return s;
    }

    public String toStringDetail() {
        String s = "PMultiValueMetric[" + "timestamp=" + timestamp + ", metric_name=" + name + ", ";
        String s1 = "";
        for (PMultivalueTimeSeries mvts : multivalueMetrics) {
            s1 += mvts.toStringDetail() + "\n";
        }
        return s + s1 + "], size=" + multivalueMetrics.size();
    }

}
