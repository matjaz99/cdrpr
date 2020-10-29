package si.iskratel.metricslib;

import io.prometheus.client.Histogram;
import okhttp3.*;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class EsClient {

    public static int esClientCount = 0;
    private int clientId;

    private String host;
    private int port;
    private String esHost = "http://elasticvm:9200";

    private OkHttpClient httpClient = new OkHttpClient();
    private MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    // Elastic endpoints
    public static final String ES_API_GET_INDICES_VERBOSE = "/_cat/indices?v";
    public static final String ES_API_BULK_ENDPOINT = "/_bulk";

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
        esHost = "http://" + host + ":" + port;
        // just initialize metrics with value 0
        PromExporter.metricslib_attempted_requests_total.labels("EsClient[" + clientId + "]", esHost);
        PromExporter.metricslib_failed_requests_total.labels("EsClient[" + clientId + "]", esHost, "null");
    }


    /**
     * Create index.
     * @param metric
     * @return success
     */
    private boolean createIndex(PMetric metric) {

        String index = metric.getParentRegistry();
        String templateName = index + "_tmpl";

        // 1. check if template exists
        HttpResponse r1 = sendGet("/_template/" + templateName);
        if (r1.responseCode == 0) {
            // exception, exit
            return false;
        } else if (r1.responseCode == 200) {
            System.out.println("INFO:  EsClient[" + clientId + "]: template already exists: " + templateName);
        } else if (r1.responseCode == 404) {
            // 2. create template
            HttpResponse r2 = sendPut("/_template/" + templateName, PMetricFormatter.toTemplateJson(templateName));
        }

        // 3. check if alias exists
        HttpResponse r3 = sendGet("/_alias/" + index);
        if (r3.responseCode == 0) {
            // exception, exit
            return false;
        } else if (r3.responseCode == 200) {
            System.out.println("INFO:  EsClient[" + clientId + "]: alias already exists: " + index);
            // alias exists, nothing else to do
            return true;
        } else if (r3.responseCode == 404) {
            // 2. create alias
            HttpResponse r4 = sendPut("/_alias/" + index, PMetricFormatter.toTemplateJson(templateName));
        }

        // 4. create index with alias
        String newIndex = index + "-000000";

        System.out.println("INFO:  EsClient[" + clientId + "]: creating index: " + newIndex + " with alias: " + index);

        Request request = new Request.Builder()
                .url("http://" + host + ":" + port + "/" + newIndex)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
                .put(RequestBody.create(PMetricFormatter.toIndexJson(index), MEDIA_TYPE_JSON))
                .build();

        if (executeHttpRequest(request, "createIndex").success) return true;
        System.out.println("WARN:  EsClient[" + clientId + "]: ...failed to create index.");
        return false;

    }

//    /**
//     * Check if index exists. If yes, then 200 is returned. If no, then 404 is returned. If 0 is returned,
//     * it means exception occurred when sending request and no http error code was retrieved.
//     * @param index
//     * @return http error code
//     */
//    private boolean checkIndex(String index) {
//
//        Request request = new Request.Builder()
//                .url("http://" + host + ":" + port + "/" + index)
//                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
//                .get()
//                .build();
//
//        HttpResponse resp = executeHttpRequest(request, "checkIndex");
//
//        if (resp.responseCode == 200) {
//            System.out.println("INFO:  EsClient[" + clientId + "]: index already exists: " + index);
//            return true;
//        }
//        if (resp.responseCode == 0) {
//            // exception
//            return false;
//        }
//        return false;
//    }

//    /**
//     * Return true only if template already exists.
//     * @param templateName
//     * @return true if template exists
//     */
//    private boolean checkTemplate(String templateName) {
//
//        Request request = new Request.Builder()
//                .url(esHost + "/_template/" + templateName)
//                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
//                .get()
//                .build();
//
//        HttpResponse resp = executeHttpRequest(request, "checkTemplate");
//
//        if (resp.responseCode == 200) {
//            System.out.println("INFO:  EsClient[" + clientId + "]: template already exists: " + templateName);
//            return true;
//        }
//        if (resp.responseCode == 0) {
//            // exception
//            return false;
//        }
//
//        return false;
//    }

//    private boolean checkAlias(String alias) {
//
//        Request request = new Request.Builder()
//                .url(esHost + "/_alias/" + alias)
//                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
//                .get()
//                .build();
//
//        HttpResponse resp = executeHttpRequest(request, "checkAlias");
//
//        if (resp.responseCode == 200) {
//            System.out.println("INFO:  EsClient[" + clientId + "]: alias already exists: " + alias);
//            return true;
//        }
//        if (resp.responseCode == 0) {
//            // exception
//            return false;
//        }
//
//        return false;
//    }

//    private boolean createTemplate(String templateName) {
//
//        System.out.println("INFO:  EsClient[" + clientId + "]: creating template: " + templateName);
//
//        Request request = new Request.Builder()
//                .url(esHost + "/_template/" + templateName)
//                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
//                .put(RequestBody.create(PMetricFormatter.toTemplateJson(templateName), MEDIA_TYPE_JSON))
//                .build();
//
//        if (executeHttpRequest(request, "createTemplate").success) return true;
//        System.out.println("WARN:  EsClient[" + clientId + "]: ...failed to create template.");
//        return false;
//
//    }

    /**
     * Send any GET request to elastic.
     * @param uri relative path
     * @return http response
     */
    public HttpResponse sendGet(String uri) {

        HttpResponse response = new HttpResponse();

        if (uri == null) {
            System.out.println("WARN:  uri is null. Request will be ignored.");
            return response;
        }

        if (!uri.startsWith("/")) uri = "/" + uri;

        Request request = new Request.Builder()
                .url(esHost + uri)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
                .get()
                .build();
        System.out.println("INFO:  Executing request on url: " + esHost + uri);

        response = executeHttpRequest(request, "sendGet");

        return response;
    }


    /**
     * Send any POST request to elastic. Body of the message must be properly formatted according to Elastic requirements.
     * @param uri relative path
     * @param body json body
     * @return http response
     */
    public HttpResponse sendPost(String uri, String body) {

        HttpResponse response = new HttpResponse();

        if (uri == null || body == null) {
            System.out.println("WARN:  uri or body is null. Request will be ignored.");
            return response;
        }

        if (!uri.startsWith("/")) uri = "/" + uri;

        Request request = new Request.Builder()
                .url(esHost + uri)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
                .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                .build();
        System.out.println("INFO:  Executing request on url: " + esHost + uri);

        response = executeHttpRequest(request, "sendPost");

        return response;
    }

    /**
     * Send any PUT request to elastic. Body of the message must be properly formatted according to Elastic requirements.
     * @param uri relative path
     * @param body json body
     * @return http response
     */
    public HttpResponse sendPut(String uri, String body) {

        HttpResponse response = new HttpResponse();

        if (uri == null || body == null) {
            System.out.println("WARN:  uri or body is null. Request will be ignored.");
            return response;
        }

        if (!uri.startsWith("/")) uri = "/" + uri;

        Request request = new Request.Builder()
                .url(esHost + uri)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
                .put(RequestBody.create(body, MEDIA_TYPE_JSON))
                .build();
        System.out.println("INFO:  Executing request on url: " + esHost + uri);

        response = executeHttpRequest(request, "sendPut");

        return response;
    }


    /**
     * Send any custom JSON body to ElasticSearch. The body must have a properly formed NDJSON structure for
     * bulk inserts and the metadata object must include a valid index.
     * @param body custom body in ndjson format
     * @return success
     */
    public boolean sendBulkPost(String body) {

        if (body == null) {
            System.out.println("WARN:  Body is null. Request will be ignored.");
            return false;
        }

        Request request = new Request.Builder()
                .url(esHost + ES_API_BULK_ENDPOINT)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
                .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                .build();
        System.out.println("INFO:  Executing request on url: " + esHost + ES_API_BULK_ENDPOINT);

        return executeHttpRequest(request, "bulk").success;

    }

    /**
     * Send all metrics in given registry to ElasticSearch.
     * @param registry registry name
     */
    public void sendBulkPost(PMetricRegistry registry) {
        for (PMetric m : registry.getMetricsList()) {
            sendBulkPost(m);
        }
    }

    /**
     * Send given metric to ElasticSearch. Method will retry to send the metric until max retries (configurable) is reached.
     * Then it will dump the metrics to file in 'dump' directory (if enabled).
     * @param metric object
     * @return true if successful, false if it fails
     */
    public boolean sendBulkPost(PMetric metric) {

        boolean success = false;
        retryCount = 0;

        if (metric.getTimeSeriesSize() == 0) {
            System.out.println("WARN:  EsClient[" + clientId + "]: Metric " + metric.getName() + " contains no time-series points. It will be ignored.");
            return false;
        }

        // check if index exists in elastic
        if (!PMetricRegistry.getRegistry(metric.getParentRegistry()).isMappingCreated() && MetricsLib.ES_AUTO_CREATE_INDEX) {
            boolean b = createIndex(metric);
            if (b == true) {
                PMetricRegistry.getRegistry(metric.getParentRegistry()).setMappingCreated(true);
            } else {
                System.out.println("ERROR: EsClient[" + clientId + "]: Index cannot be created, metrics may not be inserted without index mapping. Now what? I will drop this metric!!!!!");
                return false;
            }
        }

        // set timestamp if it is not set already
        if (metric.getTimestamp() == 0) metric.setTimestamp(System.currentTimeMillis());

        Histogram.Timer t = PromExporter.metricslib_http_request_time.labels("EsClient[" + clientId + "]", esHost + ES_API_BULK_ENDPOINT, "POST", metric.getName()).startTimer();

        Request request = new Request.Builder()
                .url(esHost + ES_API_BULK_ENDPOINT)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
                .post(RequestBody.create(PMetricFormatter.toEsNdJsonString(metric), MEDIA_TYPE_JSON))
                .build();

        try {

            while (retryCount <= MetricsLib.RETRIES) {
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
                    PromExporter.metricslib_dropped_metrics_total.labels("EsClient[" + clientId + "]").inc(metric.getTimeSeriesSize());
                }
            }

        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }

        double dur = t.observeDuration();
        PromExporter.metricslib_http_request_time.labels("EsClient[" + clientId + "]", esHost + ES_API_BULK_ENDPOINT, "POST", metric.getName()).observe(dur);

        // reset timestamp to 0. If needed set it again with setTimestamp method, or current timestamp will be usd when metric will be sent
        metric.setTimestamp(0);

        return success;

    }

    /**
     * This method actually sends the HTTP request and does all the error handling. Method returns object HttpResponse,
     * which contains a boolean flag if request was successfully executed, a returned http error code and the response
     * itself. Error code 0 means exception, otherwise http error code is returned (200-OK, 404-Not found...).
     * @param request
     * @return http response
     */
    private HttpResponse executeHttpRequest(Request request, String reqId) {

        HttpResponse httpResponse = new HttpResponse();
        long startTime = System.currentTimeMillis();
        long duration = 0;

        System.out.println("INFO:  EsClient[" + clientId + "]: >>> sending: " + reqId);

        try {

            PromExporter.metricslib_attempted_requests_total.labels("EsClient[" + clientId + "]", request.url().toString()).inc();
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                PromExporter.metricslib_failed_requests_total.labels("EsClient[" + clientId + "]", request.url().toString(), "" + response.code()).inc();
            }
            duration = System.currentTimeMillis() - startTime;
            System.out.println("INFO:  EsClient[" + clientId + "]: <<< " + request.method().toUpperCase() + " " + request.url().toString() + " - " + response.code() + " - [took " + duration + "ms]");
            httpResponse.success = response.isSuccessful();
            httpResponse.responseCode = response.code();
            httpResponse.responseText = response.body().string();
            response.close();

        } catch (UnknownHostException e) {
            System.err.println("ERROR: EsClient[" + clientId + "]: <<< UnknownHostException: " + e.getMessage());
            PromExporter.metricslib_failed_requests_total.labels("EsClient[" + clientId + "]", request.url().toString(), "UnknownHostException").inc();
            httpResponse.success = false;
            httpResponse.responseCode = 0;
            httpResponse.responseText = "UnknownHostException";
        } catch (SocketTimeoutException e) {
            System.err.println("ERROR: EsClient[" + clientId + "]: <<< SocketTimeoutException: " + e.getMessage());
            PromExporter.metricslib_failed_requests_total.labels("EsClient[" + clientId + "]", request.url().toString(), "SocketTimeoutException").inc();
            httpResponse.success = false;
            httpResponse.responseCode = 0;
            httpResponse.responseText = "SocketTimeoutException";
        } catch (SocketException e) {
            System.err.println("ERROR: EsClient[" + clientId + "]: <<< SocketException: " + e.getMessage());
            PromExporter.metricslib_failed_requests_total.labels("EsClient[" + clientId + "]", request.url().toString(), "SocketException").inc();
            httpResponse.success = false;
            httpResponse.responseCode = 0;
            httpResponse.responseText = "SocketException";
        } catch (Exception e) {
            System.err.println("ERROR: EsClient[" + clientId + "]: <<< Exception: " + e.getMessage());
            e.printStackTrace();
            PromExporter.metricslib_failed_requests_total.labels("EsClient[" + clientId + "]", request.url().toString(), "Exception").inc();
            httpResponse.success = false;
            httpResponse.responseCode = 0;
            httpResponse.responseText = "Exception";
        }

        return httpResponse;

    }

    /**
     * This class is for internal use only. Http response contains http error code, response body and success flag,
     * which indicates if something went wrong, like an exception.
     */
    public class HttpResponse {

        public boolean success = false;
        public int responseCode = 0;
        public String responseText;

        @Override
        public String toString() {
            return "HttpResponse{" +
                    "success=" + success +
                    ", responseCode=" + responseCode +
                    ", responseText='" + responseText + '\'' +
                    '}';
        }
    }

}
