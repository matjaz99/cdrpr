package si.iskratel.metricslib;

public class Test {

    public static void main(String... args) throws Exception {

        testMultipleRegistries();

    }

    public static void testMultipleRegistries() throws Exception {
        MetricsLib.init();

        PMetric metric = PMetric.build().setName("test_metric_0").setHelp("Test 0").setLabelNames("label1").register();
        metric.setLabelValues("aaa").inc();
        metric.setLabelValues("bbb").inc(5.53);
        metric.setLabelValues("ccc").set(9.74);

        PMetric metric1 = PMetric.build().setName("test_metric_1").setHelp("Test 1").setLabelNames("label1").register();
        metric1.setLabelValues("aaa").inc();
        metric1.setLabelValues("aaa").inc();

        PMetric metric2 = PMetric.build().setName("test_metric_2").setHelp("Test 2").setLabelNames("label1").register("my-registry");
        metric2.setLabelValues("www").inc();
        metric2.setLabelValues("www").inc();
        metric2.setLabelValues("www").inc(0.5);
        metric2.setLabelValues("yyy").inc(5.53);
        metric2.setLabelValues("zzz").set(-2.1426);

        PMetric metric3 = PMetric.build().setName("test_metric_3").setHelp("Test 3").setLabelNames("label1").register("registry3");
        metric3.setLabelValues("ggg").inc(0.5);
        metric3.setLabelValues("ggg").set(0);
        metric3.setLabelValues("ggg").inc(0.0000000000001);
    }

}
