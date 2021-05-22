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
    private Map<String, PMultiValueMetric> multiValueMetricsMap = new HashMap<>();

    /** Check if index mapping exists in ElasticSearch */
    private boolean mappingCreated = false;


    private PMetricRegistry(String name) {
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
        System.out.println("INFO:  PMetricRegistry: new metric " + metric.getName() + " registered in: " + registryName);
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

    public PMetric getMetric(String metricName) { return metricsMap.get(metricName); }

    public List<PMultiValueMetric> getMultiValueMetricsList() {
        return new ArrayList<>(multiValueMetricsMap.values());
    }

    public boolean isMappingCreated() {
        return mappingCreated;
    }

    public void setMappingCreated(boolean mappingCreated) {
        this.mappingCreated = mappingCreated;
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
                System.out.println("WARN:  PMetricRegistry: Metric " + m.getName() + " cannot be scraped, it contains no time-series points.");
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
            for (PMultiValueMetric mvm :
                    r.multiValueMetricsMap.values()) {
                sb.append("[").append(r.name).append("] ").append(mvm.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Reset all metrics in registry. This will not unregister metric from registry, but it will clear all time series
     * in the metric. No worries, they will be automatically added back in next iteration when you fill the metrics
     * again. The timestamp will be reset to 0, so you have to set it again when metric is collected.
     * One more thing: never reset a metric if you intend to use it as a Counter!
     */
    public void resetMetrics() {
        for (PMetric m : metricsMap.values()) {
            m.clear();
            m.setTimestamp(0);
        }
        for (PMultiValueMetric m : multiValueMetricsMap.values()) {
            m.clear();
            m.setTimestamp(0);
        }
    }


    public static void registerMultiValueMetric(String registryName, PMultiValueMetric metric) {
        PMetricRegistry r = registriesMap.getOrDefault(registryName, new PMetricRegistry(registryName));
        r.multiValueMetricsMap.put(metric.getName(), metric);
        registriesMap.put(registryName, r);
        System.out.println("INFO:  PMetricRegistry: new multi_value_metric " + metric.getName() + " registered in: " + registryName);
    }


}
