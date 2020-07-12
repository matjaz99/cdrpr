package si.iskratel.metricslib;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

public class PromExporter {

    // a place for internal metrics

    public static final Gauge prom_metricslib_registry_size = Gauge.build()
            .name("metricslib_registry_size")
            .labelNames("registry", "metric")
            .help("Number time-series in each metric in registry")
            .register();
    public static final Counter prom_elasticPostsSent = Counter.build()
            .name("metricslib_elastic_post_requests_total")
            .labelNames("threadId")
            .help("Number of POST requests.")
            .register();
    public static final Counter prom_elasticPostsResent = Counter.build()
            .name("metricslib_elastic_post_requests_resend_total")
            .labelNames("threadId")
            .help("Number of resent POST requests.")
            .register();
    public static final Counter prom_postgresBulkInsertCount = Counter.build()
            .name("metricslib_postgres_bulk_inserts_total")
            .labelNames("threadId")
            .help("Number of INSERT requests in Postgres.")
            .register();
    public static final Counter prom_postgresExceptionsCount = Counter.build()
            .name("metricslib_postgres_exceptions_total")
            .labelNames("threadId")
            .help("Number of errors in postgres.")
            .register();
    public static final Histogram prom_bulkSendHistogram = Histogram.build()
            .buckets(1, 5, 10, 30, 50)
            .name("metricslib_bulk_request_time")
            .labelNames("client", "method")
            .help("my first histogram")
            .register();

}
