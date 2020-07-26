package si.iskratel.metricslib;

import io.prometheus.client.Histogram;
import okhttp3.*;

public class EsClient {

    private String url = "http://mcrk-docker-1:9200/cdraggs/_bulk?pretty";

    private static OkHttpClient httpClient = new OkHttpClient();
    private static MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    public EsClient(String url) {
        this.url = url;
    }

    public void sendBulkPost(String body) {

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
                .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                .build();

        executeHttpRequest(request);

    }

    public void sendBulkPost(PMetric pMetric) {

        Histogram.Timer t = PromExporter.prom_bulkSendHistogram.labels("EsClient", url, "executeHttpRequest").startTimer();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
                .post(RequestBody.create(pMetric.toEsNdJsonBulkString(), MEDIA_TYPE_JSON))
                .build();

        executeHttpRequest(request);

        t.observeDuration();
        PromExporter.prom_bulkSendHistogram.labels("EsClient", url, "executeHttpRequest").observe(pMetric.getTimeSeriesSize());

    }

    private void executeHttpRequest(Request request) {

        try {

            PromExporter.prom_metricslib_attempted_requests_total.labels("EsClient", url).inc();
            Response response = httpClient.newCall(request).execute();
            while (!response.isSuccessful()) {
                System.out.println("EsClient[0]: repeat, unexpected code: " + response);
                PromExporter.prom_metricslib_failed_requests_total.labels("EsClient", url, "" + response.code()).inc();
                PromExporter.prom_metricslib_attempted_requests_total.labels("EsClient", url).inc();
                Thread.sleep(1500);
                response = httpClient.newCall(request).execute();
            }
            System.out.println("EsClient[0]: POST sent");

            response.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EsClient[0]: Recursive call.");
            PromExporter.prom_metricslib_failed_requests_total.labels("EsClient", url, "Exception").inc();
            executeHttpRequest(request);
        }

    }

}
