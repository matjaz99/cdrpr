package si.iskratel.metricslib;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

public class PromExporter {

    // a place for internal metrics

    public static final Gauge prom_metricslib_metrics_total = Gauge.build()
            .name("metricslib_metrics_total")
            .labelNames("registry", "metric")
            .help("Number of time-series in each metric in registry")
            .register();
    public static final Counter prom_metricslib_attempted_requests_total = Counter.build()
            .name("metricslib_attempted_requests_total")
            .labelNames("client", "endpoint")
            .help("Number of attempts to do bulk insert.")
            .register();
    public static final Counter prom_metricslib_failed_requests_total = Counter.build()
            .name("metricslib_failed_requests_total")
            .labelNames("client", "endpoint", "error")
            .help("Number of failed attempts to do bulk insert.")
            .register();
    public static final Histogram prom_bulkSendHistogram = Histogram.build()
            .buckets(1, 5, 10, 30, 50)
            .name("metricslib_bulk_request_time")
            .labelNames("client", "endpoint", "method")
            .help("my first histogram")
            .register();

}
