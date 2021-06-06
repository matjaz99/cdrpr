package si.iskratel.metricslib.servlets;

import si.iskratel.metricslib.EsClient;
import si.iskratel.metricslib.PromExporter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static si.iskratel.metricslib.MetricsLib.*;

public class IndicesServlet extends HttpServlet {

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
