package si.iskratel.cdr;

import java.util.Arrays;

public class PTimeSeries {

    private String[] labelValues;
    private double value;

    public String[] getLabelValues() {
        return labelValues;
    }

    public void setLabelValues(String[] labelValues) {
        this.labelValues = labelValues;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void inc() {
        value += 1.0;
    }

    public void inc(double d) {
        value += d;
    }

    @Override
    public String toString() {
        return "CdrTimeSeries{" +
                "labelValues=" + Arrays.toString(labelValues) +
                ", value=" + value +
                '}';
    }
}
