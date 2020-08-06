package si.iskratel.metricslib;

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
        this.value = new Double(value);
    }

    public void inc() {
        if (value == null) value = new Double(0);
        value += 1.0;
    }

    public void inc(double d) {
        if (value == null) value = new Double(0);
        value += d;
    }

    @Override
    public String toString() {
        return "{" +
                "labelValues=" + Arrays.toString(labelValues) +
                ", value=" + value +
                '}';
    }
}
