package si.iskratel.monitoring;

import okhttp3.*;
import si.iskratel.simulator.PrometheusMetrics;
import si.iskratel.simulator.Start;

public class EsClient {

    private static OkHttpClient httpClient = new OkHttpClient();
    private static MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    public static void sendBulkPost(String body) {

        Request request = new Request.Builder()
                .url(Start.ES_URL)
                .addHeader("User-Agent", "OkHttp Bot")
//                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                .build();

        executeHttpRequest(request);

    }

    public static void sendBulkPost(PMetric pMetric) {

        Request request = new Request.Builder()
                .url(Start.ES_URL)
                .addHeader("User-Agent", "OkHttp Bot")
//                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(pMetric.toEsBulkJsonString(), MEDIA_TYPE_JSON))
                .build();

        executeHttpRequest(request);

    }

    private static void executeHttpRequest(Request request) {
        try {

            Response response = httpClient.newCall(request).execute();
            while (!response.isSuccessful()) {
                Thread.sleep(1500);
                System.out.println("EsStoreAggregatedCalls[0]: repeat");
                response = httpClient.newCall(request).execute();
                ApplicationMetrics.elasticPostsResent.labels(Start.HOSTNAME).inc();
            }
            System.out.println("EsStoreAggregatedCalls[0]: POST sent");
            ApplicationMetrics.elasticPostsSent.labels(Start.HOSTNAME).inc();

            if (!response.isSuccessful()) System.out.println("EsStoreAggregatedCalls[" + Start.HOSTNAME + "]: Unexpected code: " + response);

            response.close();

//        System.out.println(response.body().string());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EsStoreAggregatedCalls[0]: Recursive call.");
            ApplicationMetrics.elasticPostsResent.labels(Start.HOSTNAME + "").inc();
            executeHttpRequest(request);
        }
    }

}
