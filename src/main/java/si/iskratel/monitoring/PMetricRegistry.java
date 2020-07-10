package si.iskratel.monitoring;

import io.prometheus.client.Gauge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PMetricRegistry {

    private static Map<String, PMetricRegistry> registriesMap = new HashMap<>();

    private String name;
    private Map<String, PMetric> metricsMap = new HashMap<>();
    private Map<String, Gauge> promMetricsMap = new HashMap<>();

    static {
        registriesMap.put("default", new PMetricRegistry("default"));
    }

    public PMetricRegistry(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static void registerMetric(PMetric metric) {
        registerMetric("default", metric);
    }

    public static void registerMetric(String registry, PMetric metric) {
        PMetricRegistry r = registriesMap.getOrDefault(registry, new PMetricRegistry(registry));
        r.metricsMap.put(metric.getName(), metric);
    }

    public static List<PMetricRegistry> getRegistries() {
        return new ArrayList<>(registriesMap.values());
    }

    public List<PMetric> getMetricsList() {
        return getMetricsList("default");
    }

    public List<PMetric> getMetricsList(String registry) {
        PMetricRegistry r = registriesMap.get(registry);
        return new ArrayList<>(r.metricsMap.values());
    }

    public static void exportToPrometheusMetrics() {
        exportToPrometheusMetrics("default");
    }

    public static void exportToPrometheusMetrics(String registry) {
        // TODO export from registry and remove from AggregatedCalls
        for (PMetric m : registriesMap.get(registry).metricsMap.values()){

        }
    }

    public static void dumpAllMetrics() {

        System.out.println("Metrics map size: " + registriesMap.get("default").metricsMap.size());
        for (Map.Entry<String, PMetric> entry : registriesMap.get("default").metricsMap.entrySet()) {
            System.out.println("TimeSeries size: " + entry.getValue().getTimeSeriesSize());
            System.out.println(entry.getValue().toString());
        }

    }


}
