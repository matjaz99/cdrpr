package si.iskratel.metricslib.servlets;

import si.iskratel.metricslib.*;
import si.iskratel.metricslib.util.StateLog;
import si.iskratel.metricslib.util.Utils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static si.iskratel.metricslib.MetricsLib.*;

public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        PromExporter.metricslib_servlet_requests_total.labels("/hello").inc();

        resp.getWriter().println("<h1>MetricsLib " + METRICSLIB_API_VERSION + "</h1>");

        resp.getWriter().println("<pre>");
        resp.getWriter().println("Hostname: " + METRICSLIB_HOSTNAME);
        resp.getWriter().println("PID: " + FileClient.readFile(new File(METRICSLIB_PID_FILE)).trim());
        resp.getWriter().println("Start Time: " + Utils.getFormatedTimestamp(METRICSLIB_START_TIME_MILLIS));
        resp.getWriter().println("Up Time: " + Utils.convertToDHMSFormat((int) ((System.currentTimeMillis() - METRICSLIB_START_TIME_MILLIS) / 1000)));
        resp.getWriter().println("</pre>");

        resp.getWriter().println("<pre>");
        resp.getWriter().println("<a href=\"http://" + METRICSLIB_HOSTNAME + ":" + METRICSLIB_PORT + "/metrics\">/metrics</a>");
        resp.getWriter().println("<a href=\"http://" + METRICSLIB_HOSTNAME + ":" + METRICSLIB_PORT + "/indices\">/indices</a>");
        resp.getWriter().println("<a href=\"http://" + METRICSLIB_HOSTNAME + ":" + METRICSLIB_PORT + "/alarms\">/alarms</a>");
        resp.getWriter().println("</pre>");
        resp.getWriter().println("<br/>");

        resp.getWriter().println("<h3>Configuration</h3>");
        resp.getWriter().println("<pre>"
                + "metricslib.port=" + METRICSLIB_PORT + "\n"
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
                + "metricslib.elasticsearch.default.schema=" + ES_DEFAULT_SCHEMA + "\n"
                + "metricslib.elasticsearch.default.host=" + ES_DEFAULT_HOST + "\n"
                + "metricslib.elasticsearch.default.port=" + ES_DEFAULT_PORT + "\n"
                + "metricslib.elasticsearch.createIndexOnStart=" + ES_AUTO_CREATE_INDEX + "\n"
                + "metricslib.elasticsearch.numberOfShards=" + ES_NUMBER_OF_SHARDS + "\n"
                + "metricslib.elasticsearch.numberOfReplicas=" + ES_NUMBER_OF_REPLICAS + "\n"
                + "metricslib.elasticsearch.ilm.policy.name=" + ES_ILM_POLICY_NAME + "\n"
                + "metricslib.elasticsearch.ilm.policy.file=" + ES_ILM_POLICY_FILE + "\n"
                + "metricslib.elasticsearch.cluster.file=" + ES_CLUSTER_FILE + "\n"
                + "metricslib.elasticsearch.healthcheck.interval.seconds=" + ES_HEALTHCHECK_INTERVAL + "\n"
                + "metricslib.alarm.destination=" + ALARM_DESTINATION + "\n"
                + "</pre>");

        resp.getWriter().println("<h3>State log</h3>");
        resp.getWriter().println("<pre>");
        for (String s : StateLog.getStateLog().keySet()) {
            resp.getWriter().println(s + " " + StateLog.getStateLog().get(s));
        }
        resp.getWriter().println("</pre>");

        resp.getWriter().println("<h3>Registries</h3>");
        resp.getWriter().println("<pre>");
        for (PMetricRegistry r : PMetricRegistry.getRegistries()) {
            resp.getWriter().println("<a href=\"http://" + METRICSLIB_HOSTNAME + ":"
                    + METRICSLIB_PORT + "/registry?name=" + r.getName() + "\">"
                    + r.getName() + "</a>");
        }
        resp.getWriter().println("</pre>");

        resp.getWriter().println("<h3>Metrics</h3>");
        resp.getWriter().println("<pre>" + PMetricRegistry.describeMetrics() + "</pre>");

    }

}
