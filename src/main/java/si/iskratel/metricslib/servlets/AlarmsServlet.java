package si.iskratel.metricslib.servlets;

import io.prometheus.client.exporter.MetricsServlet;
import si.iskratel.metricslib.alarm.AlarmManager;
import si.iskratel.metricslib.PromExporter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AlarmsServlet extends MetricsServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        PromExporter.metricslib_servlet_requests_total.labels("/alarms").inc();

        String json = AlarmManager.toJsonStringAllAlarms();
        resp.getWriter().println(json);

    }

}
