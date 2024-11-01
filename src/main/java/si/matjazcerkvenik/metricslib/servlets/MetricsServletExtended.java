package si.matjazcerkvenik.metricslib.servlets;

import io.prometheus.client.exporter.MetricsServlet;
import si.matjazcerkvenik.metricslib.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static si.matjazcerkvenik.metricslib.MetricsLib.*;

public class MetricsServletExtended extends MetricsServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        PromExporter.metricslib_servlet_requests_total.labels("/metrics").inc();

        for (PMetricRegistry reg : PMetricRegistry.getRegistries()) {
            for (PMetric m : reg.getMetricsList()) {
                PromExporter.metricslib_metrics_total.labels(reg.getName(), m.getName()).set(m.getTimeSeriesSize());
            }
            for (PMultiValueMetric m : reg.getMultiValueMetricsList()) {
                PromExporter.metricslib_metrics_total.labels(reg.getName(), m.getName()).set(1);
            }
            if (PROM_METRICS_EXPORT_ENABLE) {
                if (isPrometheusExportRegistryAllowed(reg.getName())) reg.collectPrometheusMetrics(reg.getName());
            }
        }
        super.doGet(req, resp);
    }

    private static boolean isPrometheusExportRegistryAllowed(String registry) {

        for (int i = 0; i < PROM_EXCLUDE_REGISTRY.length; i++) {
            if (PROM_EXCLUDE_REGISTRY[i].equals("_all")) return false;
            if (PROM_EXCLUDE_REGISTRY[i].equals(registry)) return false;
            if (PROM_EXCLUDE_REGISTRY[i].contains("*")) {
                String[] arr = PROM_EXCLUDE_REGISTRY[i].split("\\*");
                if (registry.startsWith(arr[0])) return false;
            }
        }

        for (int i = 0; i < PROM_INCLUDE_REGISTRY.length; i++) {
            if (PROM_INCLUDE_REGISTRY[i].equals("_all")) return true;
            if (PROM_INCLUDE_REGISTRY[i].equals(registry)) return true;
            if (PROM_INCLUDE_REGISTRY[i].contains("*")) {
                String[] arr = PROM_INCLUDE_REGISTRY[i].split("\\*");
                if (registry.startsWith(arr[0])) return true;
            }
        }
        return false;

    }

}
