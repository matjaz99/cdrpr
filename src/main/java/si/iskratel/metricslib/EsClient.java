package si.iskratel.metricslib;

import io.prometheus.client.Histogram;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class EsClient {

    private Logger logger = LoggerFactory.getLogger(EsClient.class);

    public static int esClientCount = 0;
    private int clientId;

    private String esHost = "http://elasticvm:9200";

    private Alarm no_connection_to_es = new Alarm(3730080, "Database Connection Fault", 1, "No connection to ElasticSearch",
            "Cannot connect");

//    private OkHttpClient httpClient = new OkHttpClient();
    private MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    // Elastic endpoints
    public static final String ES_API_GET_INDICES_VERBOSE = "/_cat/indices?v";
    public static final String ES_API_BULK_ENDPOINT = "/_bulk";

    /** Set this flag if Metricslib-about document is successfully inserted immediately after start */
    public static boolean ES_IS_READY = false;
    private int retryCount = 0;

    /** This map holds index names for which mapping was already created. This map is used when uploading
     * data from a dumped file, because I have to create index (and template and alias). If data is uploaded from
     * dumped file, there is no PMetric object behind and I cannot tell if index was already created or not. So to avoid
     * (re)creating index on each insert, I will keep track of registered indices in this map.
     * Key is obviously index name, and value has no meaning at all.
     * This implementation should be done differently anyway. This is just a temporary solution.
     */
    private static Map<String, Boolean> mappingCreated = new HashMap<>();

    /**
     * Use this constructor to create ES clients that are bound to this host:port instance, but independent of indices.
     * Index will be used from the name of metrics registry. This client can handle all indices on given host:port.
     * @param host
     * @param port
     */
    public EsClient(String schema, String host, int port) {
        this.clientId = esClientCount++;
        esHost = schema + "://" + host + ":" + port;
    }

    /**
     * Host URL can be: http://hostname:port or https://usr:pass@hostname:port, but nothing more.
     * @param hostUrl
     */
    public EsClient(String hostUrl) {
        this.clientId = esClientCount++;
        esHost = hostUrl;
    }


    /**
     * Create index. Well, first check if template exists and create it if it does not exist. Then check if alias exists
     * and create one if it does not exist yet. The name of alias is the same as the name of registry, while index gets
     * a numeric suffix (*-000000) for the sake of rotating index policy.
     * @param index
     * @return success
     */
    private boolean createIndex(String index) {

        String templateName = index + "_tmpl";

        // 1. check if template exists
        HttpResponse r1 = sendGet("/_template/" + templateName);
        if (r1.responseCode == 0) {
            // exception, exit
            return false;
        } else if (r1.responseCode == 200) {
            logger.info("EsClient[" + clientId + "]: template already exists: " + templateName);
        } else if (r1.responseCode == 404) {
            // 2. create template
            HttpResponse r2 = sendPut("/_template/" + templateName, PMetricFormatter.getTemplateJson(templateName));
            if (!r2.success) return false;
        }

        // 3. check if alias exists
        HttpResponse r3 = sendGet("/_alias/" + index);
        if (r3.responseCode == 0) {
            // exception, exit
            return false;
        } else if (r3.responseCode == 200) {
            logger.info("EsClient[" + clientId + "]: alias already exists: " + index);
            // alias exists, nothing else to do
            return true;
        } else if (r3.responseCode != 404) {
            // something is not right, exit
            return false;
        }

        // ...continue only if 404

        // 4. create index with alias
        String newIndex = index + "-000000";
        logger.info("EsClient[" + clientId + "]: creating index: " + newIndex + " with alias: " + index);
        HttpResponse r4 = sendPut(newIndex, PMetricFormatter.getIndexJson(index));
        if (r4.success) return true;

        logger.warn("EsClient[" + clientId + "]: ...failed to create index.");
        return false;

    }

    /**
     * Send any GET request to elastic.
     * @param uri relative path
     * @return http response
     */
    public HttpResponse sendGet(String uri) {

        HttpResponse response = new HttpResponse();

        if (uri == null) {
            logger.warn("URI is null. Request will be ignored.");
            return response;
        }

        if (!uri.startsWith("/")) uri = "/" + uri;

        Request request = new Request.Builder()
                .url(esHost + uri)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_API_VERSION)
                .get()
                .build();

        response = executeHttpRequest(request);

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
            logger.warn("URI or body is null. Request will be ignored.");
            return response;
        }

        if (!uri.startsWith("/")) uri = "/" + uri;

        Request request = new Request.Builder()
                .url(esHost + uri)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_API_VERSION)
                .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                .build();

        response = executeHttpRequest(request);

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
            logger.warn("URI or body is null. Request will be ignored.");
            return response;
        }

        if (!uri.startsWith("/")) uri = "/" + uri;

        Request request = new Request.Builder()
                .url(esHost + uri)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_API_VERSION)
                .put(RequestBody.create(body, MEDIA_TYPE_JSON))
                .build();

        response = executeHttpRequest(request);

        return response;
    }


    /**
     * Send any custom JSON body to ElasticSearch. The body must have a properly formed NDJSON structure for
     * bulk inserts and the metadata object must include a valid index. The request is sent to _BULK endpoint!
     * @param ndJson custom body in ndjson format
     * @return success
     */
    public boolean sendBulkPost(String ndJson) {

        if (ndJson == null) {
            logger.warn("Body is null. Request will be ignored.");
            return false;
        }

        if (!ES_IS_READY) return false;

        // parse first line to get an index. Example: {"index":{"_index":"pmon_cdr_business_group_idx"}}
        String idx = ndJson.split("\n")[0];
        idx = idx.substring(20, idx.length() - 3);

        if (!mappingCreated.containsKey(idx)) {
            boolean b = createIndex(idx);
            if (b != true) {
                // FIXME where to put setMapping=true on registry?
                return false;
            }
            mappingCreated.put(idx, true);
        }


        Request request = new Request.Builder()
                .url(esHost + ES_API_BULK_ENDPOINT)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_API_VERSION)
                .post(RequestBody.create(ndJson, MEDIA_TYPE_JSON))
                .build();

        return executeHttpRequest(request).success;

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
            logger.warn("EsClient[" + clientId + "]: Metric " + metric.getName() + " contains no time-series points. It will be ignored.");
            return false;
        }

        if (MetricsLib.EXPORT_ENABLED) FileClient.exportToCsv(metric);

        // cannot proceed if ES is not ready; dump to file if enabled
        if (!ES_IS_READY) {
            FileClient.dumpToFile(metric);
            return false;
        }

        // check if index exists in elastic
        if (!PMetricRegistry.getRegistry(metric.getParentRegistry()).isMappingCreated() && MetricsLib.ES_AUTO_CREATE_INDEX) {
            boolean b = createIndex(metric.getParentRegistry());
            if (b == true) {
                PMetricRegistry.getRegistry(metric.getParentRegistry()).setMappingCreated(true);
            } else {
                logger.warn("EsClient[" + clientId + "]: index " + metric.getParentRegistry() + " cannot be created, metrics may not be inserted without index mapping.");
                FileClient.dumpToFile(metric);
                return false;
            }
        }

        // set timestamp if it is not set already
        if (metric.getTimestamp() == 0) metric.setTimestamp(System.currentTimeMillis());

        Request request = new Request.Builder()
                .url(esHost + ES_API_BULK_ENDPOINT)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_API_VERSION)
                .addHeader("metric", metric.getName())
                .post(RequestBody.create(PMetricFormatter.toEsNdJsonString(metric), MEDIA_TYPE_JSON))
                .build();

        try {

            logger.info("EsClient[" + clientId + "]: sending metric: " + metric.getName() + " [size=" + metric.getTimeSeriesSize() + "]");

            while (retryCount <= MetricsLib.RETRIES) {
                success = executeHttpRequest(request).success;
                if (success) break;
                retryCount++;
                Thread.sleep(1500);
                logger.info("EsClient[" + clientId + "]: Retrying [" + retryCount + "] to send " + metric.getName());
            }
            if (!success) {
                logger.info("EsClient[" + clientId + "]: ...retrying [" + retryCount + "] failed for " + metric.getName());
                FileClient.dumpToFile(metric);
            }

        } catch (Exception e) {
            success = false;
            logger.error("Exception: ", e);
        }

        // reset timestamp to 0. If needed set it again with setTimestamp method, or current timestamp will be usd when metric will be sent
        metric.setTimestamp(0);

        return success;

    }

    public boolean sendBulkPost(PMultiValueMetric metric) {

        boolean success = false;
        retryCount = 0;

        // cannot proceed if ES is not ready; dump to file if enabled
        if (!ES_IS_READY) {
            FileClient.dumpToFile(metric);
            return false;
        }

        // check if index exists in elastic
        if (!PMetricRegistry.getRegistry(metric.getParentRegistry()).isMappingCreated() && MetricsLib.ES_AUTO_CREATE_INDEX) {
            boolean b = createIndex(metric.getParentRegistry());
            if (b == true) {
                PMetricRegistry.getRegistry(metric.getParentRegistry()).setMappingCreated(true);
            } else {
                logger.warn("EsClient[" + clientId + "]: index " + metric.getParentRegistry() + " cannot be created, metrics may not be inserted without index mapping.");
                FileClient.dumpToFile(metric);
                return false;
            }
        }

        // set timestamp if it is not set already
        if (metric.getTimestamp() == 0) metric.setTimestamp(System.currentTimeMillis());

        Request request = new Request.Builder()
                .url(esHost + ES_API_BULK_ENDPOINT)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_API_VERSION)
                .addHeader("metric", metric.getName())
                .post(RequestBody.create(PMetricFormatter.toEsNdJsonString(metric), MEDIA_TYPE_JSON))
                .build();

        try {

            logger.info("EsClient[" + clientId + "]: sending metric: " + metric.getName() + " [size=" + "1]");

            while (retryCount <= MetricsLib.RETRIES) {
                success = executeHttpRequest(request).success;
                if (success) break;
                retryCount++;
                Thread.sleep(1500);
                logger.info("EsClient[" + clientId + "]: Retrying [" + retryCount + "] to send " + metric.getName());
            }
            if (!success) {
                logger.info("EsClient[" + clientId + "]: ...retrying [" + retryCount + "] failed for " + metric.getName());
                FileClient.dumpToFile(metric);
            }

        } catch (Exception e) {
            success = false;
            logger.error("Exception: ", e);
        }

        // reset timestamp to 0. If needed set it again with setTimestamp method, or current timestamp will be usd when metric will be sent
        metric.setTimestamp(0);

        return success;
    }

    /**
     * This method actually sends the HTTP request and does all the error handling. Method returns object HttpResponse,
     * which contains a boolean flag if request was successfully executed, a returned http error code and the response
     * itself. Error code 0 means exception, otherwise http error code is returned (200-OK, 404-Not found...).
     * @param request http request
     * @return http response
     */
    private HttpResponse executeHttpRequest(Request request) {

        HttpResponse httpResponse = new HttpResponse();
        long startTime = System.currentTimeMillis();
        long duration = 0;
        String metric = request.headers().get("metric");
        if (metric == null) metric = "null";

        logger.info("EsClient[" + clientId + "]: >>> " + request.method().toUpperCase() + " " + request.url().uri().getPath());

        try {

            Histogram.Timer t = PromExporter.metricslib_http_request_duration_seconds.labels(request.url().toString(), request.method(), metric).startTimer();

            OkHttpClient httpClient = MetricsLib.instantiateHttpClient();
            Response response = httpClient.newCall(request).execute();
            duration = System.currentTimeMillis() - startTime;
            PromExporter.metricslib_http_requests_total.labels(Integer.toString(response.code()), request.method().toUpperCase(), request.url().toString()).inc();
            httpResponse.success = response.isSuccessful();
            httpResponse.responseCode = response.code();
            httpResponse.responseText = response.body().string();
            response.close();

            logger.info("EsClient[" + clientId + "]: <<< " + httpResponse.responseCode + " - [took " + duration + "ms]");
            if (logger.isDebugEnabled()) {
                String resp = httpResponse.responseText;
                if (resp.length() > 300) resp = resp.substring(0, 300) + "...";
                logger.debug("EsClient[" + clientId + "]: <<< " + "response: " + resp);
            }

            double dur = t.observeDuration();
            PromExporter.metricslib_http_request_duration_seconds.labels(request.url().toString(), request.method(), metric).observe(dur);

            // in some cases (eg. disk is full or too many requests) elastic does not insert the data,
            // although errorCode 200 is returned. In such cases, check the response text if contains status:4xx.
            // FIXME improve this check (correctly parse whole json, or regex...)
            if (!metric.equals("null")) {
                // only for inserting metrics
                if (httpResponse.responseText.replace(" ", "").contains("\"status\":4")) {
                    httpResponse.responseCode = 999; // this code indicates that original response code was changed
                    httpResponse.success = false;
                }
            }

            if (httpResponse.responseCode < 200 || httpResponse.responseCode > 399) logger.warn("EsClient[" + clientId + "] response: " + httpResponse.responseText);

            AlarmManager.clearAlarm(no_connection_to_es);

        } catch (UnknownHostException e) {
            logger.error("EsClient[" + clientId + "]: <<< UnknownHostException: " + e.getMessage());
            PromExporter.metricslib_http_requests_total.labels("Unknown Host", request.method().toUpperCase(), request.url().toString()).inc();
            httpResponse.success = false;
            httpResponse.responseCode = 0;
            httpResponse.responseText = "UnknownHostException";
            no_connection_to_es.setAdditionalInfo("Unknown host");
            AlarmManager.raiseAlarm(no_connection_to_es);
        } catch (SocketTimeoutException e) {
            logger.error("EsClient[" + clientId + "]: <<< SocketTimeoutException: " + e.getMessage());
            PromExporter.metricslib_http_requests_total.labels("Timeout", request.method().toUpperCase(), request.url().toString()).inc();
            httpResponse.success = false;
            httpResponse.responseCode = 0;
            httpResponse.responseText = "SocketTimeoutException";
            no_connection_to_es.setAdditionalInfo("Timeout");
            AlarmManager.raiseAlarm(no_connection_to_es);
        } catch (SocketException e) {
            logger.error("EsClient[" + clientId + "]: <<< SocketException: " + e.getMessage());
            PromExporter.metricslib_http_requests_total.labels("Socket Exception", request.method().toUpperCase(), request.url().toString()).inc();
            httpResponse.success = false;
            httpResponse.responseCode = 0;
            httpResponse.responseText = "SocketException";
            no_connection_to_es.setAdditionalInfo("SocketException");
            AlarmManager.raiseAlarm(no_connection_to_es);
        } catch (Exception e) {
            logger.error("EsClient[" + clientId + "]: <<< Exception: ", e);
            e.printStackTrace();
            PromExporter.metricslib_http_requests_total.labels("Exception", request.method().toUpperCase(), request.url().toString()).inc();
            httpResponse.success = false;
            httpResponse.responseCode = 0;
            httpResponse.responseText = "Exception";
            no_connection_to_es.setAdditionalInfo("Unknown error");
            AlarmManager.raiseAlarm(no_connection_to_es);
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
