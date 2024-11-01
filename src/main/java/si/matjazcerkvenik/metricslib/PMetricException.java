package si.matjazcerkvenik.metricslib;

public class PMetricException extends RuntimeException {

    public PMetricException(String errorMessage) {
        super(errorMessage);
    }

    public PMetricException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

}
