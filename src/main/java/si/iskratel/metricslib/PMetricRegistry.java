package si.iskratel.metricslib;

import io.prometheus.client.Gauge;

import java.util.*;

/**
 * This class contains a (static) list of all registries. Each registry has a name and
 * and a list of metrics. Each metric contains a list of time-series points (labels and value).
 * Special case is "default" registry, which is always automatically created at startup.
 */
public class PMetricRegistry {

    private static Map<String, PMetricRegistry> registriesMap = new HashMap<>();

    private String name;
    private Map<String, PMetric> metricsMap = new HashMap<>();
    private Map<String, Gauge> promMetricsMap = new HashMap<>();


    public PMetricRegistry(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    /**
     * This method creates new registry if it does not exist yet and adds a new metric to the list of metrics.
     * @param registryName
     * @param metric
     */
    public static void registerMetric(String registryName, PMetric metric) {
        PMetricRegistry r = registriesMap.getOrDefault(registryName, new PMetricRegistry(registryName));
        r.metricsMap.put(metric.getName(), metric);
        registriesMap.put(registryName, r);
    }

    public static List<PMetricRegistry> getRegistries() {
        return new ArrayList<>(registriesMap.values());
    }

    public static PMetricRegistry getDefaultRegistry() {
        return registriesMap.get("default");
    }

    public static PMetricRegistry getRegistry(String registryName) {
        return registriesMap.get(registryName);
    }

    public List<PMetric> getMetricsList() {
        return new ArrayList<>(metricsMap.values());
    }

    /**
     * Convert all metrics in given registry to Prometheus format. Prometheus metrics are available
     * at /metrics endpoint.
     * @param registryName
     */
    public void collectPrometheusMetrics(String registryName) {
        PMetricRegistry r = registriesMap.get(registryName);
        for (PMetric m : r.metricsMap.values()) {
            if (m.getTimeSeriesSize() == 0) {
                System.out.println("WARN: Metric " + m.getName() + " contains no time-series points. It will be ignored.");
                continue;
            }
            Gauge g = promMetricsMap.get(m.getName());
            if (g == null) g = Gauge.build().name(m.getName()).labelNames(m.getLabelNames()).help(m.getHelp()).register();
            for (PTimeSeries ts : m.getTimeSeries()) {
                g.labels(ts.getLabelValues()).set(ts.getValue());
            }
            promMetricsMap.put(m.getName(), g);
        }
    }

    public static void dumpMetrics() {

        System.out.println("Metrics map size: " + registriesMap.get("default").metricsMap.size());
        for (Map.Entry<String, PMetric> entry : registriesMap.get("default").metricsMap.entrySet()) {
            System.out.println("TimeSeries size: " + entry.getValue().getTimeSeriesSize());
            System.out.println(entry.getValue().toString());
        }

    }

    public static String describeMetrics() {
        StringBuilder sb = new StringBuilder();
        for (PMetricRegistry r : registriesMap.values()) {
            for (PMetric m : r.metricsMap.values()) {
                sb.append("[").append(r.name).append("] ").append(m.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public void clearTimeSeriesInMetrics(Long timestamp) {
        for (PMetric m : metricsMap.values()) {
            m.clear();
            if (timestamp != null) m.setTimestamp(timestamp);
        }
    }


}
