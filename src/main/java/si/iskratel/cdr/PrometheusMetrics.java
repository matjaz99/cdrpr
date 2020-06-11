package si.iskratel.cdr;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
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

public class PrometheusMetrics {

	public static final Counter requests = Counter.build().name("hello_world_requests_total")
			.help("Number of hello world requests served.").register();
	public static final Gauge bulkSize = Gauge.build().name("cdrpr_bulk_size")
			.labelNames("threadId").help("Bulk size.").register();
	public static final Counter totalCdrGenerated = Counter.build().name("cdrpr_cdrs_generated_total")
			.labelNames("threadId").help("Number of generated cdrs.").register();
	public static final Counter elasticPostsSent = Counter.build().name("cdrpr_elastic_post_requests_total")
			.labelNames("threadId").help("Number of POST requests.").register();
	public static final Counter elasticPostsResent = Counter.build().name("cdrpr_elastic_post_requests_resend_total")
			.labelNames("threadId").help("Number of resent POST requests.").register();
	public static final Gauge queueSize = Gauge.build().name("cdrpr_queue_size")
			.help("Number of cdrs in queue.").register();
	public static final Gauge callsInProgress = Gauge.build().name("cdrpr_calls_in_progress")
			.help("Number of calls in progress.").register();

	static class HelloServlet extends HttpServlet {

		@Override
		protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
				throws ServletException, IOException {
			resp.getWriter().println("Hello World!");
			// Increment the number of requests.
			requests.inc();
		}
	}

	public static void startJetty() throws Exception {

		Server server = new Server(9099);
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		server.setHandler(context);
		// Expose our example servlet.
		context.addServlet(new ServletHolder(new HelloServlet()), "/");
		// Expose Prometheus metrics.
		context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
		// Add metrics about CPU, JVM memory etc.
		DefaultExports.initialize();

		// Start the webserver.
		server.start();
		//server.join();

	}

}