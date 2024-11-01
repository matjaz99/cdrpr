package si.matjazcerkvenik.metricslib;

import java.util.Arrays;

public class PTimeSeries {

    private String[] labelValues;
    private Double value;

    // TODO if you make countValue and sumValue, you can get two values fields for the same metric

    public String[] getLabelValues() {
        return labelValues;
    }

    public void setLabelValues(String[] labelValues) {
        this.labelValues = labelValues;
    }

    public Double getValue() {
        return value;
    }

    public void set(double value) {
        this.value = value;
    }

    public void inc() {
        if (value == null) value = (double) 0;
        value += 1.0;
    }

    public void inc(double d) {
        if (value == null) value = Double.valueOf(0);
        value += d;
    }

    /**
     * Return true if this TS contains given label value (like filtering)
     * @param labelValue
     */
    public boolean containsLabelValue(String labelValue) {
        for (int i = 0; i < labelValues.length; i++) {
            if (labelValues[i].equals(labelValue)) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "{" +
                "labelValues=" + Arrays.toString(labelValues) +
                ", value=" + value +
                '}';
    }
}
