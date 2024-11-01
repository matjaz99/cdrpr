package si.matjazcerkvenik.metricslib.servlets;

import si.matjazcerkvenik.metricslib.PMetric;
import si.matjazcerkvenik.metricslib.PMetricRegistry;
import si.matjazcerkvenik.metricslib.PMultiValueMetric;
import si.matjazcerkvenik.metricslib.PromExporter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class RegistryDetailsServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

        PromExporter.metricslib_servlet_requests_total.labels("/registry").inc();

        String registryName = req.getParameter("name");

        resp.getWriter().println("<pre>");
        List<PMetric> metricsList = PMetricRegistry.getRegistry(registryName).getMetricsList();
        if (metricsList != null) {
            for (PMetric m : metricsList) {
                resp.getWriter().println(m.toStringDetail());
            }
        }

        List<PMultiValueMetric> mvMetricsList = PMetricRegistry.getRegistry(registryName).getMultiValueMetricsList();
        if (mvMetricsList != null) {
            for (PMultiValueMetric m : mvMetricsList) {
                resp.getWriter().println(m.toStringDetail());
            }
        }
        resp.getWriter().println("</pre>");

    }

}