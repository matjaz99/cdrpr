package si.iskratel.simulator;

import si.iskratel.metricslib.PMetric;

public class TestMetricsThread extends Thread {

    public static PMetric test_metric = PMetric.build()
            .setName("test_metric")
            .setHelp("Metric")
            .setLabelNames("node", "cause", "trafficType");

    public static void main(String... args) {
        fillMetrics();
//        xml_metric.MULTIPLY(2);
        System.out.println(test_metric.toStringDetail());

        String[] l2 = {"node", "trafficType"};
        PMetric m2 = test_metric.AGGREGATE(l2);
        System.out.println(m2.toStringDetail());

        String[] l = {"node"};
        String[] v = {"A"};
        PMetric m = m2.FILTER(l, v);
        System.out.println(m.toStringDetail());




    }


    public static void fillMetrics() {
        test_metric.setLabelValues("A", "Answered", "INC").inc();
        test_metric.setLabelValues("A", "Answered", "OUT").inc();
        test_metric.setLabelValues("A", "Busy", "INC").inc();
        test_metric.setLabelValues("B", "Answered", "INC").inc();
        test_metric.setLabelValues("B", "Answered", "INC").inc();
        test_metric.setLabelValues("B", "No reply", "INC").inc();
//        test_metric.setLabelValues("C", "Answered", "OUT").inc();
//        test_metric.setLabelValues("C", "Busy", "INC").inc();
//        test_metric.setLabelValues("C", "No reply", "INC").inc();

    }

    @Override
    public void run() {




        while (true) {

            try {
                Thread.sleep(1 * 60 * 1000);
            } catch (InterruptedException e) {
            }



        }

    }
}
