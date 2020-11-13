package si.iskratel.metricslib;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.prometheus.client.hotspot.DefaultExports;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class WhTest {

    public static void main(String[] args) throws Exception {
        startJetty(9070);

        while (true) {

            OkHttpClient httpClient = new OkHttpClient();
            MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

            Request request = new Request.Builder()
                    .url("http://192.168.1.222:9099/alarms")
                    .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
                    .get()
                    .build();

            Response response = httpClient.newCall(request).execute();
            String s = response.body().string();
            System.out.println(response.code() + " - resynchronization: ");
            System.out.println(s + "\n");
            response.close();

            ObjectMapper objectMapper = new ObjectMapper();
            Alarm[] alarms = objectMapper.readValue(s, Alarm[].class);

            System.out.println("received alarms: " + alarms.length);
            for (int i = 0; i < alarms.length; i++) {
                System.out.println(alarms[i].toString());
            }

            Thread.sleep(60 * 1000);

        }
    }

    private static void startJetty(int port) throws Exception {

        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);
        MetricsLib.HelloServlet hs = new MetricsLib.HelloServlet();
//        context.addServlet(new ServletHolder(hs), "/");
        context.addServlet(new ServletHolder(new MetricsLib.MetricsServletExtended()), "/metrics");
        context.addServlet(new ServletHolder(new WebhookServlet()), "/webhook");
        // Add metrics about CPU, JVM memory etc.
        DefaultExports.initialize();

        server.start();
        //server.join();

    }

    static class WebhookServlet extends HttpServlet {

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

            String userAgent = req.getHeader("user-agent");
            String body = getReqBody(req);

            System.out.println("Request received from user-agent=" + userAgent + ", from " + req.getRemoteAddr());
            System.out.println("Body: \n" + body);
            System.out.println("\n");

        }

        private String getReqBody(HttpServletRequest req) throws IOException {

            String body = "";
            String s = req.getReader().readLine();
            while (s != null) {
                body += s;
                s = req.getReader().readLine();
            }

            return body;

        }
    }

}
