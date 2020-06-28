package si.iskratel.cdr;

import okhttp3.*;

import java.net.InetAddress;

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

    private static void executeHttpRequest(Request request) {
        try {

            Response response = httpClient.newCall(request).execute();
            while (!response.isSuccessful()) {
                Thread.sleep(1500);
                System.out.println("EsStoreAggregatedCalls[0]: repeat");
                response = httpClient.newCall(request).execute();
                PrometheusMetrics.elasticPostsResent.labels(Start.HOSTNAME).inc();
            }
            System.out.println("EsStoreAggregatedCalls[0]: POST sent");
            PrometheusMetrics.elasticPostsSent.labels(Start.HOSTNAME).inc();

            if (!response.isSuccessful()) System.out.println("EsStoreAggregatedCalls[" + Start.HOSTNAME + "]: Unexpected code: " + response);

            response.close();

//        System.out.println(response.body().string());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EsStoreAggregatedCalls[0]: Recursive call.");
            PrometheusMetrics.elasticPostsResent.labels(Start.HOSTNAME + "").inc();
            executeHttpRequest(request);
        }
    }

}
