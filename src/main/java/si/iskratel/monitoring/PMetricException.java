package si.iskratel.monitoring;

public class PMetricException extends Exception {

    public PMetricException(String errorMessage) {
        super(errorMessage);
    }

    public PMetricException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

}
