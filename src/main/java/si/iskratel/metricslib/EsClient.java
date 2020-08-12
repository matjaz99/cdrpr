package si.iskratel.metricslib;

import io.prometheus.client.Histogram;
import okhttp3.*;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class EsClient {

    private String url = "http://mcrk-docker-1:9200/cdraggs/_bulk";

    private OkHttpClient httpClient = new OkHttpClient();
    private MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    private int retryCount = 0;
    private FileUploadThread fut;

    public EsClient(String url) {
        this.url = url;
        if (MetricsLib.DUMP_TO_FILE_ENABLED) {
            fut = new FileUploadThread(this);
            fut.start();
        }

    }

    public void sendBulkPost(PMetricRegistry registry) {
        for (PMetric m : registry.getMetricsList()) {
            sendBulkPost(m);
        }
    }

    public boolean sendBulkPost(String body) {

        if (body == null) {
            System.out.println("WARN: Body is null. It will be ignored.");
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

    public boolean sendBulkPost(PMetric pMetric) {

        boolean success = false;
        retryCount = 0;

        if (pMetric.getTimeSeriesSize() == 0) {
            System.out.println("WARN: Metric " + pMetric.getName() + " contains no time-series points. It will be ignored.");
            return success;
        }

        System.out.println("-> sending metrics: " + pMetric.getName() + ", size: " + pMetric.getTimeSeriesSize());

        Histogram.Timer t = PromExporter.prom_bulkSendHistogram.labels("EsClient", url, "executeHttpRequest").startTimer();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "MetricsLib/" + MetricsLib.METRICSLIB_VERSION)
                .post(RequestBody.create(pMetric.toEsNdJsonBulkString(), MEDIA_TYPE_JSON))
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
                }
            }

        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }


        t.observeDuration();
        PromExporter.prom_bulkSendHistogram.labels("EsClient", url, "executeHttpRequest").observe(pMetric.getTimeSeriesSize());

        return success;

    }

    private boolean executeHttpRequest(Request request) {

        try {

            PromExporter.prom_metricslib_attempted_requests_total.labels("EsClient", url).inc();
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                System.out.println("EsClient[0]: unexpected code: " + response);
                PromExporter.prom_metricslib_failed_requests_total.labels("EsClient", url, "" + response.code()).inc();
                response.close();
                return false;
            }
            System.out.println("EsClient[0]: POST successfully sent");
            response.close();
            return true;

        } catch (SocketTimeoutException e) {
            System.err.println("SocketTimeoutException: " + e.getMessage());
            PromExporter.prom_metricslib_failed_requests_total.labels("EsClient", url, "SocketTimeoutException").inc();
        } catch (SocketException e) {
            System.err.println("SocketException: " + e.getMessage());
            PromExporter.prom_metricslib_failed_requests_total.labels("EsClient", url, "SocketException").inc();
        } catch (Exception e) {
            e.printStackTrace();
            PromExporter.prom_metricslib_failed_requests_total.labels("EsClient", url, "Exception").inc();
        }

        return false;

    }

}
