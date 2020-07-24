package si.iskratel.metricslib;

import io.prometheus.client.Gauge;

import java.util.*;

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
        registriesMap.put(metric.getName(), r);
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

    public void convertAllMetricsToPrometheusMetrics() {
        for (PMetricRegistry r : registriesMap.values()) {
            convertToPrometheusMetrics(r.name);
        }
    }

    public void convertToPrometheusMetrics(String registry) {
        PMetricRegistry r = registriesMap.get(registry);
        for (PMetric m : r.metricsMap.values()) {
            Gauge g = promMetricsMap.get(m.getName());
            if (g == null) g = Gauge.build().name(m.getName()).labelNames(m.getLabelNames()).help(m.getHelp()).register();
            for (PTimeSeries ts : m.getTimeSeries()) {
                g.labels(ts.getLabelValues()).set(ts.getValue());
            }
            promMetricsMap.put(m.getName(), g);
        }
    }

    public static void dumpAllMetrics() {

        System.out.println("Metrics map size: " + registriesMap.get("default").metricsMap.size());
        for (Map.Entry<String, PMetric> entry : registriesMap.get("default").metricsMap.entrySet()) {
            System.out.println("TimeSeries size: " + entry.getValue().getTimeSeriesSize());
            System.out.println(entry.getValue().toString());
        }


    }

    public static String describeAllMetrics() {
        StringBuilder sb = new StringBuilder();
        for (PMetricRegistry r : registriesMap.values()) {
            for (PMetric m : r.metricsMap.values()) {
                sb.append("[").append(r.name).append("] metric_name=").append(m.getName())
                        .append(", help=").append(m.getHelp()).append(", labels=").append(Arrays.toString(m.getLabelNames())).append("\n");
            }
        }
        System.out.println(sb.toString());
        return sb.toString();
    }


}
