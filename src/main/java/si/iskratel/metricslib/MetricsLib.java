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
import java.util.Properties;

public class MetricsLib {

    public static String METRICSLIB_VERSION = "1.0";
    public static boolean EXPORT_PROMETHEUS_METRICS = true;
    public static int RETRIES = 3;
    public static String DUMP_DIRECTORY = "dump/";
    public static boolean DUMP_TO_FILE_ENABLED = true;

    static class HelloServlet extends HttpServlet {

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException {
            PromExporter.helloRequests.inc();

            resp.getWriter().println("<h1>MetricsLib v" + METRICSLIB_VERSION + "</h1>");

            resp.getWriter().println("<h3>Configuration</h3>");
            resp.getWriter().println("<pre>metricslib.client.prometheus.export=" + EXPORT_PROMETHEUS_METRICS + "\n"
                    + "metricslib.client.retry=" + RETRIES + "\n"
                    + "metricslib.client.dump.directory=" + DUMP_DIRECTORY + "</pre>");

            resp.getWriter().println("<h3>Registries</h3>");
            resp.getWriter().println("<pre>");
            for (PMetricRegistry r : PMetricRegistry.getRegistries()) {
                resp.getWriter().println(r.getName());
            }
            resp.getWriter().println("</pre>");

            resp.getWriter().println("<h3>Metrics</h3>");
            resp.getWriter().println("<pre>" + PMetricRegistry.describeMetrics() + "</pre>");
            resp.getWriter().println("<a href=\"http://localhost:9099/metrics\">/metrics</a>");
        }
    }

    static class MetricsServletExtended extends MetricsServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

            for (PMetricRegistry reg : PMetricRegistry.getRegistries()) {
                for (PMetric m : reg.getMetricsList()) {
                    PromExporter.prom_metricslib_metrics_total.labels(reg.getName(), m.getName()).set(m.getTimeSeriesSize());
                }
                if (EXPORT_PROMETHEUS_METRICS) {
                    reg.collectPrometheusMetrics(reg.getName());
                }
            }
            super.doGet(req, resp);
        }
    }

    public static void init() throws Exception {
        init(9099);
    }

    public static void init(int port) throws Exception {
        startJetty(port);
    }

    public static void init(Properties props) throws Exception {
        int port = Integer.parseInt((String) props.getOrDefault("metricslib.jetty.port", "9099"));
        RETRIES = Integer.parseInt((String) props.getOrDefault("metricslib.client.resend", "3"));
        String dd = (String) props.getOrDefault("metricslib.client.dump.directory", "");
        if (dd.length() > 0 && !dd.endsWith("/")) dd += "/";
        DUMP_DIRECTORY = dd;
        DUMP_TO_FILE_ENABLED = Boolean.parseBoolean((String) props.getOrDefault("metricslib.client.dump.enabled", "true"));
        startJetty(port);
    }

    private static void startJetty(int port) throws Exception {

        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);
        HelloServlet hs = new HelloServlet();
        context.addServlet(new ServletHolder(hs), "/");
        context.addServlet(new ServletHolder(hs), "/hello");
        context.addServlet(new ServletHolder(new MetricsServletExtended()), "/metrics");
        // Add metrics about CPU, JVM memory etc.
        DefaultExports.initialize();

        server.start();
        //server.join();

    }

}
