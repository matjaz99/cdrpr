package si.iskratel.metricslib;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import okhttp3.*;
import org.codehaus.commons.nullanalysis.NotNull;
import org.codehaus.commons.nullanalysis.Nullable;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.net.ssl.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Stream;

public class MetricsLib {

    /** Just a version */
    public static String METRICSLIB_API_VERSION = "v1";
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
    public static int UPLOAD_INTERVAL_SECONDS = 16;
    public static String ES_DEFAULT_SCHEMA = "http";
    public static String ES_BASIC_USER = "admin";
    public static String ES_BASIC_PASS = "admin";
    public static String ES_DEFAULT_HOST = "localhost";
    public static int ES_DEFAULT_PORT = 9200;
    public static int ES_HEALTHCHECK_INTERVAL = 3000;
    /** Choose whether or not you want index to be automatically created and how */
    public static boolean ES_AUTO_CREATE_INDEX = true;
    public static int ES_NUMBER_OF_SHARDS = 1;
    public static int ES_NUMBER_OF_REPLICAS = 0;
    public static String ES_ILM_POLICY_NAME = "pmon_ilm_policy";
    public static String ALARM_DESTINATION = "http://localhost:9097/webhook";
    public static boolean EXPORT_ENABLED = false;
    public static String EXPORT_DIRECTORY = "export/";

    /** A separate thread for uploading files */
    public static FileClient fut;

    private static Server server;

    private MetricsLib() { }

    static class HelloServlet extends HttpServlet {

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
            PromExporter.metricslib_servlet_requests_total.labels("/hello").inc();

            resp.getWriter().println("<h1>MetricsLib " + METRICSLIB_API_VERSION + "</h1>");

            resp.getWriter().println("<a href=\"http://" + METRICSLIB_HOSTNAME + ":" + METRICSLIB_PORT + "/metrics\">/metrics</a>");
            resp.getWriter().println("<a href=\"http://" + METRICSLIB_HOSTNAME + ":" + METRICSLIB_PORT + "/indices\">/indices</a>");
            resp.getWriter().println("<a href=\"http://" + METRICSLIB_HOSTNAME + ":" + METRICSLIB_PORT + "/alarms\">/alarms</a>");

            resp.getWriter().println("<h3>Configuration</h3>");
            resp.getWriter().println("<pre>"
                    + "metricslib.hostname=" + METRICSLIB_HOSTNAME + "\n"
                    + "metricslib.port=" + METRICSLIB_PORT + "\n"
                    + "metricslib.isContainerized=" + METRICSLIB_IS_CONTAINERIZED + "\n"
                    + "metricslib.pathPrefix=" + PATH_PREFIX + "\n"
                    + "metricslib.prometheus.enable=" + PROM_METRICS_EXPORT_ENABLE + "\n"
                    + "metricslib.prometheus.include.registry=" + Arrays.toString(PROM_INCLUDE_REGISTRY) + "\n"
                    + "metricslib.prometheus.exclude.registry=" + Arrays.toString(PROM_EXCLUDE_REGISTRY) + "\n"
                    + "metricslib.client.retry=" + RETRIES + "\n"
                    + "metricslib.client.retry.interval.millis=" + RETRY_INTERVAL_MILLISECONDS + "\n"
                    + "metricslib.client.bulk.size=" + BULK_SIZE + "\n"
                    + "metricslib.client.dump.enabled=" + DUMP_TO_FILE_ENABLED + "\n"
                    + "metricslib.client.dump.directory=" + DUMP_DIRECTORY + "\n"
                    + "metricslib.upload.interval.seconds=" + UPLOAD_INTERVAL_SECONDS + "\n"
                    + "metricslib.elasticsearch.default.host=" + ES_DEFAULT_HOST + "\n"
                    + "metricslib.elasticsearch.default.port=" + ES_DEFAULT_PORT + "\n"
                    + "metricslib.elasticsearch.createIndexOnStart=" + ES_AUTO_CREATE_INDEX + "\n"
                    + "metricslib.elasticsearch.numberOfShards=" + ES_NUMBER_OF_SHARDS + "\n"
                    + "metricslib.elasticsearch.numberOfReplicas=" + ES_NUMBER_OF_REPLICAS + "\n"
                    + "metricslib.elasticsearch.ilm.policy.name=" + ES_ILM_POLICY_NAME + "\n"
                    + "metricslib.elasticsearch.healthcheck.interval.seconds=" + ES_HEALTHCHECK_INTERVAL + "\n"
                    + "metricslib.alarm.destination=" + ALARM_DESTINATION + "\n"
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

            EsClient e = new EsClient(ES_DEFAULT_SCHEMA, ES_DEFAULT_HOST, ES_DEFAULT_PORT);
            String s = e.sendGet(EsClient.ES_API_GET_INDICES_VERBOSE).responseText;

            resp.getWriter().println("<h1>Elasticsearch indices</h1>");
            resp.getWriter().println("<h3>" + ES_DEFAULT_HOST + ":" + ES_DEFAULT_PORT + "</h3>");
            resp.getWriter().println("<pre>" + s + "</pre>");

            long docCount = 0;
            double docSize = 0.0;
            String[] lines = s.split("\n");
            for (int i = 1; i < lines.length; i++) {
                String[] cols = lines[i].split("\\s+");
                docCount += Long.parseLong(cols[6].trim());
                if (cols[8].trim().endsWith("mb")) {
                    docSize += Double.parseDouble(cols[8].trim().replace("mb", "")) / 1024;
                }
                if (cols[8].trim().endsWith("gb")) {
                    docSize += Double.parseDouble(cols[8].trim().replace("gb", ""));
                }
            }

            resp.getWriter().println("<pre>-----------------------------------------------------------------------------------------------------------------------------------------------------------</pre>");
            resp.getWriter().println("<pre>Total documents: " + docCount + "</pre>");
            resp.getWriter().println("<pre>Total size: " + docSize + " GB</pre>");

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

    static class AlarmsServlet extends MetricsServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

            PromExporter.metricslib_servlet_requests_total.labels("/alarms").inc();

            String json = AlarmManager.toJsonStringAllAlarms();
            resp.getWriter().println(json);

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
     * Just give me the properties.
     * @param props
     * @throws Exception if properties contain invalid values
     */
    public static void init(Properties props) throws Exception {
        METRICSLIB_PORT = Integer.parseInt((String) props.getOrDefault("metricslib.jetty.port", "9099"));
        PATH_PREFIX = (String) props.getOrDefault("metricslib.jetty.pathPrefix", "/");
        if (PATH_PREFIX.length() > 0 && !PATH_PREFIX.endsWith("/")) PATH_PREFIX += "/";

        RETRIES = Integer.parseInt((String) props.getOrDefault("metricslib.client.retry.count", "3"));
        RETRY_INTERVAL_MILLISECONDS = Integer.parseInt((String) props.getOrDefault("metricslib.client.retry.interval.millis", "1500"));
        BULK_SIZE = Integer.parseInt((String) props.getOrDefault("metricslib.client.bulk.size", "50000"));
        String dd = (String) props.getOrDefault("metricslib.dump.directory", "");
        if (dd.length() > 0 && !dd.endsWith("/")) dd += "/";
        DUMP_DIRECTORY = dd;
        DUMP_TO_FILE_ENABLED = Boolean.parseBoolean((String) props.getOrDefault("metricslib.dump.enabled", "true"));
        UPLOAD_INTERVAL_SECONDS = Integer.parseInt((String) props.getOrDefault("metricslib.upload.interval.seconds", "45"));

        PROM_METRICS_EXPORT_ENABLE = Boolean.parseBoolean((String) props.getOrDefault("metricslib.prometheus.enable", "true"));
        String include = (String) props.getOrDefault("metricslib.prometheus.include.registry", "_all");
        if (include.length() == 0) include = "_all";
        PROM_INCLUDE_REGISTRY = include.split(",");
        String exclude = (String) props.getOrDefault("metricslib.prometheus.exclude.registry", "");
        PROM_EXCLUDE_REGISTRY = exclude.split(",");

        ES_DEFAULT_SCHEMA = (String) props.getOrDefault("metricslib.elasticsearch.default.schema", "http");
        ES_DEFAULT_HOST = (String) props.getOrDefault("metricslib.elasticsearch.default.host", null);
        ES_DEFAULT_PORT = Integer.parseInt((String) props.getOrDefault("metricslib.elasticsearch.default.port", "0"));
        ES_HEALTHCHECK_INTERVAL = Integer.parseInt((String) props.getOrDefault("metricslib.elasticsearch.healthcheck.interval.seconds", "3000"));
        ES_AUTO_CREATE_INDEX = Boolean.parseBoolean((String) props.getOrDefault("metricslib.elasticsearch.createIndexOnStart", "true"));
        ES_NUMBER_OF_SHARDS = Integer.parseInt((String) props.getOrDefault("metricslib.elasticsearch.numberOfShards", "1"));
        ES_NUMBER_OF_REPLICAS = Integer.parseInt((String) props.getOrDefault("metricslib.elasticsearch.numberOfReplicas", "0"));
        ES_ILM_POLICY_NAME = (String) props.getOrDefault("metricslib.elasticsearch.ilm.policy.name", "pmon_ilm_policy");

        ALARM_DESTINATION = (String) props.getOrDefault("metricslib.alarm.destination", null);

        startJetty(METRICSLIB_PORT);
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

        if (METRICSLIB_PORT > 0 && server == null) {
            server = new Server(port);
            ServletContextHandler context = new ServletContextHandler();
            context.setContextPath("/");
            server.setHandler(context);
            HelloServlet hs = new HelloServlet();
//        context.addServlet(new ServletHolder(hs), "/");
            context.addServlet(new ServletHolder(hs), PATH_PREFIX + "hello");
            context.addServlet(new ServletHolder(new IndicesServlet()), PATH_PREFIX + "indices");
            context.addServlet(new ServletHolder(new MetricsServletExtended()), PATH_PREFIX + "metrics");
            context.addServlet(new ServletHolder(new AlarmsServlet()), PATH_PREFIX + "alarms");
            // Add metrics about CPU, JVM memory etc.
            DefaultExports.initialize();

            server.start();
            //server.join();
        }

        PromExporter.metricslib_up_time.set(System.currentTimeMillis());

        if (MetricsLib.DUMP_TO_FILE_ENABLED && ES_DEFAULT_HOST != null) {
            MetricsLib.fut = new FileClient(new EsClient(ES_DEFAULT_SCHEMA, ES_DEFAULT_HOST, ES_DEFAULT_PORT));
            MetricsLib.fut.start();
        }


        Thread eht = new Thread(new EsHealthcheckThread());
        eht.setName("EsHealthcheckThread");
        eht.start();

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

    public static OkHttpClient instantiateHttpClient() {

        if (ES_DEFAULT_SCHEMA.equalsIgnoreCase("http")) {
            return new OkHttpClient();
        }

        // continue if https

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        try {

            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            if (ES_BASIC_USER != null && ES_BASIC_PASS != null) {
                builder.authenticator(new Authenticator() {
                    @Nullable
                    @Override
                    public Request authenticate(@Nullable Route route, @NotNull Response response) throws IOException {
                        if (response.request().header("Authorization") != null)
                            return null;  //if you've tried to authorize and failed, give up

                        String credential = Credentials.basic(ES_BASIC_USER, ES_BASIC_PASS);
                        return response.request().newBuilder().header("Authorization", credential).build();
                    }
                });
            }

            return builder.build();

        } catch (Exception e) {
            return null;
        }

    }

}
