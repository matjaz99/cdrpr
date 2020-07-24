package si.iskratel.metricslib;

public class Test {

    public static void main(String... args) throws Exception {

        MetricsLib.init();

        PMetric metric = PMetric.build().setName("test_metric").setHelp("Test").setLabelNames("label1").register();

        metric.setLabelValues("aaa").inc();
        metric.setLabelValues("bbb").inc(5.53);
        metric.setLabelValues("ccc").set(9.74);

        PMetric metric1 = PMetric.build().setName("test_reg2").setHelp("Test reg2").setLabelNames("label1").register("reg2");
        metric1.setLabelValues("aaa").inc();
        metric1.setLabelValues("bbb").inc(5.53);
        metric1.setLabelValues("ccc").set(9.74);

    }

}
