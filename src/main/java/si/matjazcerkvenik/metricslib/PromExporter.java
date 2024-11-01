package si.matjazcerkvenik.metricslib;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

public class PromExporter {

    // a place for internal metrics

    public static final Counter metricslib_servlet_requests_total = Counter.build()
            .name("metricslib_servlet_requests_total")
            .labelNames("uri")
            .help("Number of requests served.")
            .register();
    public static final Gauge metricslib_up_time = Gauge.build()
            .name("metricslib_up_time")
            .help("Time when started")
            .register();
    public static final Gauge metricslib_metrics_total = Gauge.build()
            .name("metricslib_metrics_total")
            .labelNames("registry", "metric")
            .help("Number of time-series in each metric in registry")
            .register();
    public static final Counter metricslib_http_requests_total = Counter.build()
            .name("metricslib_http_requests_total")
            .labelNames("code", "method", "endpoint")
            .help("Number of attempts to do bulk insert.")
            .register();
    public static final Counter metricslib_dump_to_file_total = Counter.build()
            .name("metricslib_dump_to_file_total")
            .help("Number of files being dumped")
            .register();
    public static final Gauge metricslib_files_waiting_for_upload = Gauge.build()
            .name("metricslib_files_waiting_for_upload")
            .help("Number of files waiting to be uploaded")
            .register();
    public static final Counter metricslib_dump_files_uploads_total = Counter.build()
            .name("metricslib_dump_files_uploads_total")
            .labelNames("status")
            .help("Number of successful file uploads.")
            .register();
    public static final Counter metricslib_dropped_metrics_total = Counter.build()
            .name("metricslib_dropped_metrics_total")
            .help("Number of metrics that were dropped because they could not be written")
            .register();
    public static final Counter metricslib_alarms_sent_total = Counter.build()
            .name("metricslib_alarms_sent_total")
            .help("Number of alarms sent")
            .register();
    public static final Histogram metricslib_http_request_duration_seconds = Histogram.build()
            .buckets(0.05, 0.1, 0.3, 0.5, 1.0, 3.0, 5.0, 10.0, 30.0)
            .name("metricslib_http_request_duration_seconds")
            .labelNames("endpoint", "method", "metric")
            .help("Http request-response time")
            .register();

}
