package si.iskratel.monitoring;

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

    public static boolean ENABLE_PROMETHEUS_METRICS = true;

    public static final Counter requests = Counter.build()
            .name("hello_world_requests_total")
            .help("Number of hello world requests served.")
            .register();

    static class HelloServlet extends HttpServlet {

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException {
            resp.getWriter().println("Hello World!");
            // Increment the number of requests.
            requests.inc();
        }
    }

    static class MetricsServletExtended extends MetricsServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

            for (PMetricRegistry r : PMetricRegistry.getRegistries()) {
                for (PMetric m : r.getMetricsList()) {
                    ApplicationMetrics.metricslib_registry_size.labels(r.getName(), m.getName()).set(m.getTimeSeriesSize());
                }
            }
            if (ENABLE_PROMETHEUS_METRICS) {
                PMetricRegistry.exportToPrometheusMetrics();
            }
            super.doGet(req, resp);
        }
    }

    public static void startJetty() throws Exception {

        Server server = new Server(9099);
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
