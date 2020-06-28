package si.iskratel.cdr;

import java.util.HashMap;
import java.util.Map;

public class PMetricRegistry {

    private static Map<String, PMetric> cdrMetricsMap = new HashMap<>();

    public static void registerMetric(PMetric metric) {
        cdrMetricsMap.put(metric.getName(), metric);
    }

    public static void dumpAllMetrics() {

        System.out.println("Metrics map size: " + cdrMetricsMap.size());
        for (Map.Entry<String, PMetric> entry : cdrMetricsMap.entrySet()) {
            System.out.println("TimeSeries size: " + entry.getValue().getTimeSeriesSize());
            System.out.println(entry.getValue().toString());
        }

    }


}
