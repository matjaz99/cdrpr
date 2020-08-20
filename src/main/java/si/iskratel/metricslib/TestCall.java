package si.iskratel.metricslib;

public class TestCall {
    public String node;
    public String cause;
    public int duration;

    public TestCall(String node, String cause, int duration) {
        this.node = node;
        this.cause = cause;
        this.duration = duration;
    }
}
