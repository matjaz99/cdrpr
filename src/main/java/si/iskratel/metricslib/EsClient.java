package si.iskratel.metricslib;

import io.prometheus.client.Histogram;
import okhttp3.*;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class EsClient {

    private String url = "http://mcrk-docker-1:9200/cdraggs/_bulk";
    private String host;
    private int port;
//    private String index;

    private OkHttpClient httpClient = new OkHttpClient();
    private MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    private int retryCount = 0;

    /**
     * Use this constructor to create ES clients that are bound to this host:port instance, but independent of indices.
     * Index will be used from the name of metrics registry. This client can handle all indices on given host:port.
     * @param host
     * @param port
     * @param index
     */
    public EsClient(String host, int port, String index) {
        this.host = host;
        this.port = port;
//        this.index = index;
//        this.url = "http://" + host + ":" + port + "/" + index + "/_bulk";
        this.url = "http://" + host + ":" + port + "/_bulk";
    }

    /**
     * If you use this constructor, then you are bound to this host:port/index instance. You cannot control the
     * indices, unless you create new EsClient.
     * @param url
     */
    @Deprecated
    public EsClient(String url) {
        this.url = url;
    }

    public void setMapping() {

        String s = "{\n" +
                "  \"mappings\": {\n" +
                "    \"properties\": {\n" +
                "      \"value\": {\"type\": \"double\"},\n" +
                "    \t\"timestamp\": {\"type\": \"date\", \"format\": \"epoch_millis\"}\n" +
                "    }\n" +
                "  }\n" +
                "}";
        // TODO send PUT request
    }


    /**
     * Send any custom JSON body to ElasticSearch.
     * @param body
     * @return success
     */
    public boolean sendBulkPost(String body) {

        if (body == null) {
            System.out.println("WARN: Body is null. Request will be ignored.");
            return false;
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
                .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                .build();
        System.out.println("Executing request on url: " + url);

        return executeHttpRequest(request);

    }

    /**
     * Send all metrics in given registry to ElasticSearch.
     * @param registry
     */
    public void sendBulkPost(PMetricRegistry registry) {
        for (PMetric m : registry.getMetricsList()) {
            sendBulkPost(m);
        }
    }

    /**
     * Send given metric to ElasticSearch. Method will retry to send the metric until max retries (configurable) is reached.
     * Then it will dump the metrics to file in 'dump' directory.
     * @param pMetric
     * @return success
     */
    public boolean sendBulkPost(PMetric pMetric) {

        boolean success = false;
        retryCount = 0;

        if (pMetric.getTimeSeriesSize() == 0) {
            System.out.println("WARN: Metric " + pMetric.getName() + " contains no time-series points. It will be ignored.");
            return success;
        }

        System.out.println("sending metrics: " + pMetric.getName() + ", size: " + pMetric.getTimeSeriesSize());

        Histogram.Timer t = PromExporter.metricslib_bulk_request_time.labels("EsClient", url, "executeHttpRequest").startTimer();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
                .post(RequestBody.create(PMetricFormatter.toEsNdJsonString(pMetric, pMetric.getParentRegistry()), MEDIA_TYPE_JSON))
                .build();

        try {

            while (retryCount < MetricsLib.RETRIES) {
                success = executeHttpRequest(request);
                if (success) break;
                retryCount++;
                Thread.sleep(1500);
                System.out.println("EsClient[0]: Retrying to send...");
            }
            if (!success) {
                System.out.println("WARN: EsClient[0]: ...retrying failed!");
                if (MetricsLib.DUMP_TO_FILE_ENABLED) {
                    System.out.println("EsClient[0]: Dumping to file");
                    FileClient.dumpToFile(this, pMetric);
                    PromExporter.metricslib_dump_to_file_total.labels("EsClient").inc();
                }
            }

        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }


        t.observeDuration();
        PromExporter.metricslib_bulk_request_time.labels("EsClient", url, "executeHttpRequest").observe(pMetric.getTimeSeriesSize());

        return success;

    }

    /**
     * This method actually sends the HTTP request and does all the error handling.
     * @param request
     * @return success
     */
    private boolean executeHttpRequest(Request request) {

        try {

            PromExporter.metricslib_attempted_requests_total.labels("EsClient", url).inc();
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                System.out.println("EsClient[0]: unexpected code: " + response);
                PromExporter.metricslib_failed_requests_total.labels("EsClient", url, "" + response.code()).inc();
                response.close();
                return false;
            }
            System.out.println("--> EsClient[0]: POST successfully sent");
            response.close();
            return true;

        } catch (SocketTimeoutException e) {
            System.err.println("SocketTimeoutException: " + e.getMessage());
            PromExporter.metricslib_failed_requests_total.labels("EsClient", url, "SocketTimeoutException").inc();
        } catch (SocketException e) {
            System.err.println("SocketException: " + e.getMessage());
            PromExporter.metricslib_failed_requests_total.labels("EsClient", url, "SocketException").inc();
        } catch (Exception e) {
            e.printStackTrace();
            PromExporter.metricslib_failed_requests_total.labels("EsClient", url, "Exception").inc();
        }

        return false;

    }

//    public String getIndex() {
//        return index;
//    }

    public String sendGetIndices() {
        Request request = new Request.Builder()
                .url("http://" + host + ":" + port + "/_cat/indices?v")
                .build();
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
