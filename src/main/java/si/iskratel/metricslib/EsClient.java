package si.iskratel.metricslib;

import io.prometheus.client.Histogram;
import okhttp3.*;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class EsClient {

    public static int esClientCount = 0;
    private int clientId;

    private String url = "http://mcrk-docker-1:9200/cdraggs/_bulk";
    private String host;
    private int port;

    private OkHttpClient httpClient = new OkHttpClient();
    private MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    private int retryCount = 0;

    /**
     * Use this constructor to create ES clients that are bound to this host:port instance, but independent of indices.
     * Index will be used from the name of metrics registry. This client can handle all indices on given host:port.
     * @param host
     * @param port
     */
    public EsClient(String host, int port) {
        this.clientId = esClientCount++;
        this.host = host;
        this.port = port;
//        this.url = "http://" + host + ":" + port + "/" + index + "/_bulk";
        this.url = "http://" + host + ":" + port + "/_bulk";
    }

    /**
     * If you use this constructor, then you are bound to this host:port/index instance. You cannot control the
     * indices, unless you create new EsClient.
     * @param url
     */
    public EsClient(String url) {
        this.clientId = esClientCount++;
        this.url = url;
    }

    private boolean createIndex(String index) {

        String s = "{\n" +
                "  \"aliases\": {\n" +
                "    \"${ALIAS_NAME}\": {}\n" +
                "  }," +
                "  \"settings\": {\n" +
                "    \"number_of_shards\": 1\n" +
                "  }," +
                "  \"mappings\": {\n" +
                "    \"properties\": {\n" +
                "      \"m_name\": {\"type\": \"keyword\"},\n" +
                "      \"value\": {\"type\": \"double\"},\n" +
                "      \"timestamp\": {\"type\": \"date\", \"format\": \"epoch_millis\"}\n" +
                "    }\n" +
                "  }\n" +
                "}";
        s = s.replace("${ALIAS_NAME}", index + "_alias");

        int i = checkIndex(index);

        if (i == 200) {
            System.out.println("INFO:  EsClient[" + clientId + "]: index already exists: " + index);
            return true;
        }
        if (i == 0) {
            // exception
            return false;
        }

        System.out.println("INFO:  EsClient[" + clientId + "]: creating index: " + index + " with alias: " + index + "_alias");

        Request request = new Request.Builder()
                .url("http://" + host + ":" + port + "/" + index)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
                .put(RequestBody.create(s, MEDIA_TYPE_JSON))
                .build();

        if (executeHttpRequest(request, "createIndex").success) return true;
        System.out.println("WARN:  EsClient[" + clientId + "]: ...failed to create index.");
        return false;

    }

    private int checkIndex(String index) {

        Request request = new Request.Builder()
                .url("http://" + host + ":" + port + "/" + index)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
                .get()
                .build();

        return executeHttpRequest(request, "checkIndex").responseCode;
    }


    /**
     * Send any custom JSON body to ElasticSearch.
     * @param body
     * @return success
     */
    public boolean sendBulkPost(String body) {

        if (body == null) {
            System.out.println("WARN:  Body is null. Request will be ignored.");
            return false;
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
                .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                .build();
        System.out.println("INFO:  Executing request on url: " + url);

        return executeHttpRequest(request, "-").success;

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
     * @param metric
     * @return true if successful, false if it fails
     */
    public boolean sendBulkPost(PMetric metric) {

        boolean success = false;
        retryCount = 0;

        if (metric.getTimeSeriesSize() == 0) {
            System.out.println("WARN:  EsClient[" + clientId + "]: Metric " + metric.getName() + " contains no time-series points. It will be ignored.");
            return false;
        }

        // check if index exists in elastic, only on first run
        if (!PMetricRegistry.getRegistry(metric.getParentRegistry()).isMappingCreated()) {
            boolean b = createIndex(metric.getParentRegistry());
            if (b == true) {
                PMetricRegistry.getRegistry(metric.getParentRegistry()).setMappingCreated(true);
            } else {
                System.out.println("ERROR: Index cannot be created, metrics may not be inserted without index mapping. Now what? I will drop this metric!!!!!");
                return false;
            }
        }

        if (metric.getTimestamp() == 0) metric.setTimestamp(System.currentTimeMillis());

        Histogram.Timer t = PromExporter.metricslib_bulk_request_time.labels("EsClient[" + clientId + "]", url, "POST").startTimer();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
                .post(RequestBody.create(PMetricFormatter.toEsNdJsonString(metric, metric.getParentRegistry() + "_alias"), MEDIA_TYPE_JSON))
                .build();

        try {

            while (retryCount < MetricsLib.RETRIES) {
                success = executeHttpRequest(request, "metric " + metric.getName() + " [size=" + metric.getTimeSeriesSize() + "]").success;
                if (success) break;
                retryCount++;
                Thread.sleep(1500);
                System.out.println("INFO:  EsClient[" + clientId + "]: Retrying to send " + metric.getName());
            }
            if (!success) {
                System.out.println("WARN:  EsClient[" + clientId + "]: ...retrying failed for " + metric.getName());
                if (MetricsLib.DUMP_TO_FILE_ENABLED) {
                    System.out.println("INFO:  EsClient[" + clientId + "]: Dumping to file: " + metric.getName());
                    FileClient.dumpToFile(this, metric);
                    PromExporter.metricslib_dump_to_file_total.labels("EsClient[" + clientId + "]").inc();
                } else {
                    System.out.println("ERROR: EsClient[" + clientId + "]: Dumping is disabled. Metric will be dropped!!!");
                    PromExporter.metricslib_dropped_metrics_total.labels("EsClient[" + clientId + "]").inc();
                }
            }

        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }

        t.observeDuration();
        PromExporter.metricslib_bulk_request_time.labels("EsClient[" + clientId + "]", url, "executeHttpRequest").observe(metric.getTimeSeriesSize());

        metric.setTimestamp(0);

        return success;

    }

    /**
     * This method actually sends the HTTP request and does all the error handling.
     * @param request
     * @return success
     */
    private HttpResponse executeHttpRequest(Request request, String reqId) {

        HttpResponse httpResponse = new HttpResponse();

        System.out.println("INFO:  EsClient[" + clientId + "]: >>> sending: " + reqId);

        try {

            PromExporter.metricslib_attempted_requests_total.labels("EsClient[" + clientId + "]", url).inc();
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                PromExporter.metricslib_failed_requests_total.labels("EsClient[" + clientId + "]", url, "" + response.code()).inc();
            }
            System.out.println("INFO:  EsClient[" + clientId + "]: <<< " + request.method().toUpperCase() + " " + request.url().toString() + " - " + response.code());
            httpResponse.success = response.isSuccessful();
            httpResponse.responseCode = response.code();
            httpResponse.responseText = response.body().string();
            response.close();

        } catch (SocketTimeoutException e) {
            System.err.println("ERROR: EsClient[" + clientId + "]: <<< SocketTimeoutException: " + e.getMessage());
            PromExporter.metricslib_failed_requests_total.labels("EsClient[" + clientId + "]", url, "SocketTimeoutException").inc();
            httpResponse.success = false;
            httpResponse.responseCode = 0;
            httpResponse.responseText = "SocketTimeoutException";
        } catch (SocketException e) {
            System.err.println("ERROR: EsClient[" + clientId + "]: <<< SocketException: " + e.getMessage());
            PromExporter.metricslib_failed_requests_total.labels("EsClient[" + clientId + "]", url, "SocketException").inc();
            httpResponse.success = false;
            httpResponse.responseCode = 0;
            httpResponse.responseText = "SocketException";
        } catch (Exception e) {
            System.err.println("ERROR: EsClient[" + clientId + "]: <<< Exception: " + e.getMessage());
            e.printStackTrace();
            PromExporter.metricslib_failed_requests_total.labels("EsClient[" + clientId + "]", url, "Exception").inc();
            httpResponse.success = false;
            httpResponse.responseCode = 0;
            httpResponse.responseText = "Exception";
        }

        return httpResponse;

    }

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

    /**
     * This class is for internal use only. Http response contains http error code, response body and success flag,
     * which indicates if something went wrong, like an exception.
     */
    private class HttpResponse {

        public boolean success = false;
        public int responseCode = 0;
        public String responseText;

    }

}
