package si.iskratel.metricslib;

import io.prometheus.client.Counter;
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

public class MetricsLib {

    private static String METRICSLIB_VERSION = "1.0";
    public static boolean ENABLE_PROMETHEUS_METRICS = true;

    public static final Counter helloRequests = Counter.build()
            .name("metricslib_hello_requests_total")
            .help("Number of hello requests served.")
            .register();

    static class HelloServlet extends HttpServlet {

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException {
            resp.getWriter().println("<h1>Hello MetricsLib v" + METRICSLIB_VERSION + "</h1>");
            // Increment the number of requests.
            helloRequests.inc();
            resp.getWriter().println("<h3>Describe metrics</h3>");
            resp.getWriter().println("<pre>" + PMetricRegistry.describeAllMetrics() + "</pre>");
        }
    }

    static class MetricsServletExtended extends MetricsServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

            for (PMetricRegistry r : PMetricRegistry.getRegistries()) {
                for (PMetric m : r.getMetricsList()) {
                    PromExporter.prom_metricslib_registry_size.labels(r.getName(), m.getName()).set(m.getTimeSeriesSize());
                }
                if (ENABLE_PROMETHEUS_METRICS) {
                    r.convertAllMetricsToPrometheusMetrics();
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

    private static void startJetty(int port) throws Exception {

        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);
        // Expose our example servlet.
        context.addServlet(new ServletHolder(new HelloServlet()), "/hello");
        // Expose Prometheus metrics.
        context.addServlet(new ServletHolder(new MetricsServletExtended()), "/metrics");
        // Add metrics about CPU, JVM memory etc.
        DefaultExports.initialize();

        // Start the webserver.
        server.start();
        //server.join();

    }

}
