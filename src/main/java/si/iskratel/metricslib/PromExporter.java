package si.iskratel.metricslib;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

public class PromExporter {

    // a place for internal metrics

    public static final Counter metricslib_hello_requests_total = Counter.build()
            .name("metricslib_hello_requests_total")
            .help("Number of hello requests served.")
            .register();
    public static final Gauge metricslib_metrics_total = Gauge.build()
            .name("metricslib_metrics_total")
            .labelNames("registry", "metric")
            .help("Number of time-series in each metric in registry")
            .register();
    public static final Counter metricslib_attempted_requests_total = Counter.build()
            .name("metricslib_attempted_requests_total")
            .labelNames("client", "endpoint")
            .help("Number of attempts to do bulk insert.")
            .register();
    public static final Counter metricslib_failed_requests_total = Counter.build()
            .name("metricslib_failed_requests_total")
            .labelNames("client", "endpoint", "error")
            .help("Number of failed attempts to do bulk insert.")
            .register();
    public static final Counter metricslib_dump_to_file_total = Counter.build()
            .name("metricslib_dump_to_file_total")
            .labelNames("client")
            .help("Number of files being dumped")
            .register();
    public static final Counter metricslib_dump_files_uploads_total = Counter.build()
            .name("metricslib_dump_files_uploads_total")
            .labelNames("client")
            .help("Number of successful file uploads.")
            .register();
    public static final Histogram metricslib_bulk_request_time = Histogram.build()
            .buckets(1, 5, 10, 30, 50)
            .name("metricslib_bulk_request_time")
            .labelNames("client", "endpoint", "method")
            .help("my first histogram")
            .register();

}
