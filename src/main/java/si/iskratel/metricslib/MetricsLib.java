package si.iskratel.metricslib;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import okhttp3.*;
import org.codehaus.commons.nullanalysis.NotNull;
import org.codehaus.commons.nullanalysis.Nullable;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import si.iskratel.metricslib.servlets.*;

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
import java.util.Properties;
import java.util.stream.Stream;

public class MetricsLib {

    /** MetricsLib API version */
    public static String METRICSLIB_API_VERSION = "v1";
    /** Hostname where MetricsLib is running */
    public static String METRICSLIB_HOSTNAME;
    /** Port for Jetty http server */
    public static int METRICSLIB_PORT = 9099;
    /** Timestamp when MetricsLib was initialized */
    public static long METRICSLIB_START_TIME_MILLIS = System.currentTimeMillis();
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
    /** Number of retries to send data to storage before dumping to file */
    public static int RETRIES = 3;
    /** Delay between consecutive retries */
    public static int RETRY_INTERVAL_MILLISECONDS = 1500;
    public static int BULK_SIZE = 50000;
    /** If still failing, then dump metrics to this directory */
    public static String DUMP_DIRECTORY = "dump/";
    /** Dump only if dumping is enabled */
    public static boolean DUMP_TO_FILE_ENABLED = false;
    /** Interval for uploading dumped files */
    public static int UPLOAD_INTERVAL_SECONDS = 25;
    /* ElasticSearch configuration */
    /** Schema: http or https */
    public static String ES_DEFAULT_SCHEMA = "http";
    public static String ES_BASIC_USER = "admin";
    public static String ES_BASIC_PASS = "admin";
    public static String ES_DEFAULT_HOST = "localhost";
    public static int ES_DEFAULT_PORT = 9200;
    public static int ES_HEALTHCHECK_INTERVAL = 3000;
    /** Choose whether or not you want index to be automatically created and how */
    public static boolean ES_AUTO_CREATE_INDEX = true;
    /** Number of shards. Used when creating index template. */
    public static int ES_NUMBER_OF_SHARDS = 1;
    /** Number of replicas. Used when creating index template. */
    public static int ES_NUMBER_OF_REPLICAS = 0;
    /** The name of ILM policy. Used when creating index template. */
    public static String ES_ILM_POLICY_NAME = "pmon_ilm_policy";
    public static String ES_ILM_POLICY_FILE = "pmon_ilm_policy.json";
    public static String ES_ILM_CLUSTER_FILE = "cluster.json";
    /** Alarm endpoint where alarms are pushed */
    public static String ALARM_DESTINATION = "http://localhost:9097/webhook";
    public static boolean EXPORT_ENABLED = false;
    public static String EXPORT_DIRECTORY = "export/";

    /** A separate thread for uploading files */
    public static FileClient fut;

    private static Server server;

    private MetricsLib() { }


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
        ES_ILM_POLICY_NAME = (String) props.getOrDefault("metricslib.elasticsearch.ilm.policy.name", "ilm_policy");
        ES_ILM_POLICY_FILE = (String) props.getOrDefault("metricslib.elasticsearch.ilm.policy.file", "ilm_policy.json");
        ES_ILM_CLUSTER_FILE = (String) props.getOrDefault("metricslib.elasticsearch.ilm.cluster", "cluster.json");

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
            context.addServlet(new ServletHolder(new RegistryDetailsServlet()), PATH_PREFIX + "registry");
            // Add metrics about CPU, JVM memory etc.
            DefaultExports.initialize();

            server.start();
            //server.join();
        }

        PromExporter.metricslib_up_time.set(METRICSLIB_START_TIME_MILLIS);

        if (MetricsLib.DUMP_TO_FILE_ENABLED && ES_DEFAULT_HOST != null) {
            MetricsLib.fut = new FileClient(new EsClient(ES_DEFAULT_SCHEMA, ES_DEFAULT_HOST, ES_DEFAULT_PORT));
            MetricsLib.fut.setName("FileUploadThread");
            MetricsLib.fut.start();
        }


        Thread eht = new Thread(new EsHealthcheckThread());
        eht.setName("EsHealthCheckThread");
        eht.start();

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
