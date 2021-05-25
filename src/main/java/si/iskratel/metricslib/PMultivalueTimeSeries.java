package si.iskratel.metricslib;

import java.util.HashMap;
import java.util.Map;

public class PMultivalueTimeSeries {

    private Map<String, String> labelsMap = new HashMap<>();
    private Map<String, Double> valuesMap = new HashMap<>();

    public PMultivalueTimeSeries addLabel(String key, String value) {
        if (labelsMap == null) labelsMap = new HashMap<>();
        labelsMap.put(key, value);
        return this;
    }

    public PMultivalueTimeSeries addValue(String key, double value) {
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

    @Override
    public String toString() {
        String lbls = "";
        for (String s : labelsMap.keySet()) {
            lbls += s + ", ";
        }
        return "labels=[" + lbls.substring(0, lbls.length() - 2) + "]";
    }

    public String toStringDetail() {
        String s = "data=\n";
        s += "\tlabels=" + labelsMap + "\n";
        s += "\tvalues=" + valuesMap + "\n";
        return s + ", lblSize=" + (labelsMap.size() + valuesMap.size());
    }

}
