package si.iskratel.metricslib;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;

public class MetricsLib {

    /** Just a version */
    public static String METRICSLIB_VERSION = "1.0";
    /** Hostname where MetricsLib is running */
    public static String METRICSLIB_HOSTNAME;
    /** Port for Jetty http server */
    public static int METRICSLIB_PORT = 9099;
    /** Path prefix in case if running behind proxy */
    public static String PATH_PREFIX = "/";
    /** True if MetricsLib is running in container */
    public static boolean METRICSLIB_IS_CONTAINERIZED = false;
    /** Enable exporting collected metrics in prometheus format on /metrics endpoint. Does not apply to MetricsLib internal metrics, they are exposed anyway. */
    public static boolean PROM_METRICS_EXPORT_ENABLE = false;
    /** Coma-separated list of registries to include in export. Special keyword: '_all'. Allows wildcard '*' for begins_with_. */
    public static String[] PROM_INCLUDE_REGISTRY = { "_all" };
    /** Coma-separated list of registries to exclude from export. Exclusion is checked before the inclusion! */
    public static String[] PROM_EXCLUDE_REGISTRY = { "" };
    /** Number of retries if sending fails */
    public static int RETRIES = 3;
    public static int RETRY_INTERVAL_MILLISECONDS = 1500;
    public static int BULK_SIZE = 50000;
    /** If still failing, then dump metrics to this directory */
    public static String DUMP_DIRECTORY = "dump/";
    /** Dump only if dumping is enabled */
    public static boolean DUMP_TO_FILE_ENABLED = false;
    /** Interval for uploading dumped files */
    public static int UPLOAD_INTERVAL_SECONDS = 125;
    public static String DEFAULT_ES_HOST = "localhost";
    public static int DEFAULT_ES_PORT = 9200;
    /** Choose whether or not you want index to be automatically created */
    public static boolean ES_AUTO_CREATE_INDEX = true;
    /** A separate thread for uploading files */
    public static FileUploadThread fut;

    private MetricsLib() { }

    static class HelloServlet extends HttpServlet {

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
            PromExporter.metricslib_servlet_requests_total.labels("/hello").inc();

            resp.getWriter().println("<h1>Iskratel MetricsLib v" + METRICSLIB_VERSION + "</h1>");

            resp.getWriter().println("<a href=\"http://" + METRICSLIB_HOSTNAME + ":" + METRICSLIB_PORT + "/metrics\">/metrics</a>");
            resp.getWriter().println("<a href=\"http://" + METRICSLIB_HOSTNAME + ":" + METRICSLIB_PORT + "/indices\">/indices</a>");

            resp.getWriter().println("<h3>Configuration</h3>");
            resp.getWriter().println("<pre>"
                    + "metricslib.hostname=" + METRICSLIB_HOSTNAME + "\n"
                    + "metricslib.port=" + METRICSLIB_PORT + "\n"
                    + "metricslib.isContainerized=" + METRICSLIB_IS_CONTAINERIZED + "\n"
                    + "metricslib.pathPrefix=" + PATH_PREFIX + "\n"
                    + "metricslib.prometheus.enable=" + PROM_METRICS_EXPORT_ENABLE + "\n"
                    + "metricslib.prometheus.include.registry=" + PROM_INCLUDE_REGISTRY + "\n"
                    + "metricslib.prometheus.exclude.registry=" + PROM_EXCLUDE_REGISTRY + "\n"
                    + "metricslib.client.retry=" + RETRIES + "\n"
                    + "metricslib.client.retry.interval.millis=" + RETRY_INTERVAL_MILLISECONDS + "\n"
                    + "metricslib.client.bulk.size=" + BULK_SIZE + "\n"
                    + "metricslib.client.dump.enabled=" + DUMP_TO_FILE_ENABLED + "\n"
                    + "metricslib.client.dump.directory=" + DUMP_DIRECTORY + "\n"
                    + "metricslib.upload.interval.seconds=" + UPLOAD_INTERVAL_SECONDS + "\n"
                    + "metricslib.client.elasticsearch.default.host=" + DEFAULT_ES_HOST + "\n"
                    + "metricslib.client.elasticsearch.default.port=" + DEFAULT_ES_PORT + "\n"
                    + "</pre>");

            resp.getWriter().println("<h3>Registries</h3>");
            resp.getWriter().println("<pre>");
            for (PMetricRegistry r : PMetricRegistry.getRegistries()) {
                resp.getWriter().println(r.getName());
            }
            resp.getWriter().println("</pre>");

            resp.getWriter().println("<h3>Metrics</h3>");
            resp.getWriter().println("<pre>" + PMetricRegistry.describeMetrics() + "</pre>");

        }
    }

    static class IndicesServlet extends HttpServlet {

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

            PromExporter.metricslib_servlet_requests_total.labels("/indices").inc();

            EsClient e = new EsClient(DEFAULT_ES_HOST, DEFAULT_ES_PORT);
            String s = e.sendGetIndices();

            resp.getWriter().println("<h1>Elasticsearch indices</h1>");
            resp.getWriter().println("<h3>" + DEFAULT_ES_HOST + ":" + DEFAULT_ES_PORT + "</h3>");
            resp.getWriter().println("<pre>" + s + "</pre>");

        }
    }

    static class MetricsServletExtended extends MetricsServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

            PromExporter.metricslib_servlet_requests_total.labels("/metrics").inc();

            for (PMetricRegistry reg : PMetricRegistry.getRegistries()) {
                for (PMetric m : reg.getMetricsList()) {
                    PromExporter.metricslib_metrics_total.labels(reg.getName(), m.getName()).set(m.getTimeSeriesSize());
                }
                if (PROM_METRICS_EXPORT_ENABLE) {
                    if (isPrometheusExportRegistryAllowed(reg.getName())) reg.collectPrometheusMetrics(reg.getName());
                }
            }
            super.doGet(req, resp);
        }
    }

    public static void init() throws Exception {
        init(METRICSLIB_PORT);
    }

    public static void init(int port) throws Exception {
        METRICSLIB_PORT = port;
        startJetty(port);
    }

    /**
     * Just give me the damn properties.
     * @param props
     * @throws Exception if properties contain invalid values
     */
    public static void init(Properties props) throws Exception {
        METRICSLIB_PORT = Integer.parseInt((String) props.getOrDefault("metricslib.jetty.port", "9099"));
        PATH_PREFIX = (String) props.getOrDefault("metricslib.pathPrefix", "/");
        if (PATH_PREFIX.length() > 0 && !PATH_PREFIX.endsWith("/")) PATH_PREFIX += "/";

        RETRIES = Integer.parseInt((String) props.getOrDefault("metricslib.client.retry.count", "3"));
        RETRY_INTERVAL_MILLISECONDS = Integer.parseInt((String) props.getOrDefault("metricslib.client.retry.interval.millis", "1500"));
        BULK_SIZE = Integer.parseInt((String) props.getOrDefault("metricslib.client.bulk.size", "50000"));
        String dd = (String) props.getOrDefault("metricslib.client.dump.directory", "");
        if (dd.length() > 0 && !dd.endsWith("/")) dd += "/";
        DUMP_DIRECTORY = dd;
        DUMP_TO_FILE_ENABLED = Boolean.parseBoolean((String) props.getOrDefault("metricslib.client.dump.enabled", "true"));
        UPLOAD_INTERVAL_SECONDS = Integer.parseInt((String) props.getOrDefault("metricslib.upload.interval.seconds", "16"));

        PROM_METRICS_EXPORT_ENABLE = Boolean.parseBoolean((String) props.getOrDefault("metricslib.prometheus.enable", "true"));
        String include = (String) props.getOrDefault("metricslib.prometheus.include.registry", "_all");
        if (include.length() == 0) include = "_all";
        PROM_INCLUDE_REGISTRY = include.split(",");
        String exclude = (String) props.getOrDefault("metricslib.prometheus.exclude.registry", "");
        PROM_EXCLUDE_REGISTRY = exclude.split(",");

        DEFAULT_ES_HOST = (String) props.getOrDefault("metricslib.client.elasticsearch.default.host", null);
        DEFAULT_ES_PORT = Integer.parseInt((String) props.getOrDefault("metricslib.client.elasticsearch.default.port", "0"));

        if (METRICSLIB_PORT > 0) startJetty(METRICSLIB_PORT);
    }

    private static void startJetty(int port) throws Exception {

        try {
            METRICSLIB_HOSTNAME = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            METRICSLIB_HOSTNAME = "localhost";
        }

        // check if running inside container
        try (Stream< String > stream =
                     Files.lines(Paths.get("/proc/1/cgroup"))) {
            System.out.println("Running in container: " + stream.anyMatch(line -> line.contains("/docker")));
            METRICSLIB_IS_CONTAINERIZED = true;
        } catch (IOException e) {
            METRICSLIB_IS_CONTAINERIZED = false;
        }

        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);
        HelloServlet hs = new HelloServlet();
//        context.addServlet(new ServletHolder(hs), "/");
        context.addServlet(new ServletHolder(hs), PATH_PREFIX + "hello");
        context.addServlet(new ServletHolder(new IndicesServlet()), PATH_PREFIX + "indices");
        context.addServlet(new ServletHolder(new MetricsServletExtended()), PATH_PREFIX + "metrics");
        // Add metrics about CPU, JVM memory etc.
        DefaultExports.initialize();

        server.start();
        //server.join();

        PromExporter.metricslib_up_time.set(System.currentTimeMillis());

        if (MetricsLib.DUMP_TO_FILE_ENABLED && DEFAULT_ES_HOST != null) {
            MetricsLib.fut = new FileUploadThread(new EsClient(DEFAULT_ES_HOST, DEFAULT_ES_PORT));
            MetricsLib.fut.start();
        }

//        EsClient es = new EsClient(DEFAULT_ES_HOST, DEFAULT_ES_PORT);
//        StringBuilder sb = new StringBuilder();
//        sb.append("{\"name\":\"metricslib\",\"version\":\"v").append(METRICSLIB_VERSION).append("\",").append("\"date\":\"").append(new Date().toString()).append("\"}");
//        es.insertDoc("metricslib", sb.toString());

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
