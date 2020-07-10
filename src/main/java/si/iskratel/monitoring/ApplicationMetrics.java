package si.iskratel.monitoring;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

public class ApplicationMetrics {

    // a place for internal metrics

    public static final Gauge metricslib_registry_size = Gauge.build()
            .name("metricslib_registry_size")
            .labelNames("registry", "metric")
            .help("Number time-series in each metric in registry")
            .register();
    public static final Counter elasticPostsSent = Counter.build()
            .name("metricslib_elastic_post_requests_total")
            .labelNames("threadId")
            .help("Number of POST requests.")
            .register();
    public static final Counter elasticPostsResent = Counter.build()
            .name("metricslib_elastic_post_requests_resend_total")
            .labelNames("threadId")
            .help("Number of resent POST requests.")
            .register();
    public static final Counter postgresBulkInsertCount = Counter.build()
            .name("metricslib_postgres_bulk_inserts_total")
            .labelNames("threadId")
            .help("Number of INSERT requests in Postgres.")
            .register();
    public static final Counter postgresExceptionsCount = Counter.build()
            .name("metricslib_postgres_exceptions_total")
            .labelNames("threadId")
            .help("Number of errors in postgres.")
            .register();

}
