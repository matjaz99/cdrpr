package si.iskratel.metricslib.test;

import io.prometheus.client.Gauge;
import si.iskratel.metricslib.EsClient;
import si.iskratel.metricslib.MetricsLib;
import si.iskratel.metricslib.PMetric;
import si.iskratel.metricslib.PMetricRegistry;

import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String... args) throws Exception {

//        testMultipleRegistries();
//        testMetrics();
//        aggregateCalls();

        String s = "12345678901";
        if (s.length() > 10) {
            s = s.substring(0, 10);

        }
        System.out.println(s);

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

        PMetric metric4 = PMetric.build().setName("test_metric_4").setHelp("Test 4").register();
        metric4.inc();
    }


    public static void testMetrics() throws Exception {

        MetricsLib.init();

        PMetric calls_by_cause = PMetric.build().setName("cdr_calls_by_cause").setHelp("Coutning calls")
                .setLabelNames("node", "cause", "direction").register();

//        for (int i = 0; i < cdrList.length; i++) {
//            calls_by_cause.setLabelValues(cdr.getNode(), cdr.getCause(), cdr.getDirection()).inc();
//        }

        calls_by_cause.setLabelValues("node1", "Answered", "inc").inc();
        calls_by_cause.setLabelValues("node1", "Answered", "inc").inc();
        calls_by_cause.setLabelValues("node1", "Answered", "inc").inc();
        calls_by_cause.setLabelValues("node1", "Busy", "inc").inc();
        calls_by_cause.setLabelValues("node1", "Busy", "inc").inc(2.45678);
        calls_by_cause.setLabelValues("node1", "Error", "inc").inc();
        calls_by_cause.setLabelValues("node1", "Trunk unavailable", "inc").inc();

        calls_by_cause.setLabelValues("node2", "Answered", "inc").inc();
        calls_by_cause.setLabelValues("node2", "Busy", "inc").inc();
        calls_by_cause.setLabelValues("node2", "Error", "inc").inc();
        calls_by_cause.setLabelValues("node2", "Trunk unavailable", "inc").inc();

        PMetric calls_by_trunk = PMetric.build().setName("cdr_calls_by_trunk").setHelp("Coutning calls")
                .setLabelNames("node", "trunkgroup", "direction").register();

//        for (int i = 0; i < cdrList.length; i++) {
//            calls_by_cause.setLabelValues(cdr.getNode(), cdr.getCause(), cdr.getDirection()).inc();
//        }

        calls_by_trunk.setLabelValues("node1", "TG1", "inc").inc();
        calls_by_trunk.setLabelValues("node1", "TG2", "out").inc();

        PMetric test_without_timeseries_points = PMetric.build().setName("test_without_timeseries_points").setHelp("Coutning calls")
                .setLabelNames("node", "trunkgroup", "direction").register();

        EsClient es = new EsClient("http", "mcrk-docker-1", 9200);
        es.sendBulkPost(calls_by_cause);
        es.sendBulkPost(test_without_timeseries_points);


    }



    private static List<TestCall> callsList = new ArrayList<>();

    public static void aggregateCalls() throws Exception {

        callsList.add(new TestCall("Skopje", "Answered", 500));
        callsList.add(new TestCall("Skopje", "Answered", 500));
//        callsList.add(new TestCall("Skopje", "Answered", 500));
//        callsList.add(new TestCall("Skopje", "Busy", 500));
//        callsList.add(new TestCall("Skopje", "Rejected", 500));
//        callsList.add(new TestCall("Ljubljana", "Answered", 500));
//        callsList.add(new TestCall("Ljubljana", "Busy", 0));
//        callsList.add(new TestCall("Ljubljana", "Busy", 0));
//        callsList.add(new TestCall("Ljubljana", "Rejected", 0));

        MetricsLib.init(9099);
//        EsClient e = new EsClient("http://mcrk-docker-1:9200/cdraggs/_bulk");
        EsClient e = new EsClient("http", "mcrk-docker-1", 9200);

        PMetric pmon_calls_by_cause = PMetric.build()
                .setName("pmon_calls_by_cause")
                .setHelp("Counting calls by cause")
                .setLabelNames("node", "cause")
                .register("pmon_cdr_calls_idx");

        PMetric pmon_calls_by_duration = PMetric.build()
                .setName("pmon_calls_by_duration")
                .setHelp("Total duration of all calls")
                .setLabelNames("node")
                .register("pmon_cdr_trunks_idx");

        for (TestCall c : callsList) {
            pmon_calls_by_cause.setLabelValues(c.node, c.cause).inc();
            pmon_calls_by_cause.setTimestamp(System.currentTimeMillis());

            pmon_calls_by_duration.setLabelValues(c.node).inc(c.duration);
            pmon_calls_by_duration.setTimestamp(System.currentTimeMillis());


        }
        e.sendBulkPost(PMetricRegistry.getRegistry("pmon_cdr_calls_idx"));


        PMetric pmon_xml_calls_by_duration = PMetric.build()
                .setName("pmon_xml_calls_by_duration")
                .setHelp("Total duration of all calls")
                .setLabelNames("node")
                .register("pmon_xml");

        PMetricRegistry.getRegistry("pmon_xml").resetMetrics();
        pmon_xml_calls_by_duration.setLabelValues("ime noda").set(123); // set value from xml metric



        // Varianta 1 - ES + prometheus
        PMetric pam_cdr_ftp_downloaded_files_count = PMetric.build()
                .setName("pam_cdr_ftp_downloaded_files_count")
                .setHelp("Total duration of all calls")
                .setLabelNames("nodeName")
                .register("pam_cdr_internal_metrics");
        e.sendBulkPost(pam_cdr_ftp_downloaded_files_count); // optional: insert metrics into ES

        // Varianta 2 - only prometheus
        Gauge gauge = Gauge.build()
                .name("pam_cdr_ftp_downloaded_files_count")
                .help("asdjkfhaksjf asdf as")
                .labelNames("nodeName")
                .register();



    }


    public static void testStandaloneMetric() {
        PMetric m = new PMetric();
        m.setName("test_metric");
        m.setHelp("description");
        m.setLabelNames("node", "cause", "direction");
        m.setTimestamp(System.currentTimeMillis());
        m.setLabelValues("London", "Answered", "Local").inc();
        m.setLabelValues("London", "Answered", "Local").inc();
        m.setLabelValues("Berlin", "Busy", "Transit").inc();

        EsClient e = new EsClient("http", "elasticvm", 9200);
        e.sendBulkPost(m);

    }



}


