package si.iskratel.metricslib;

import okhttp3.*;
import si.iskratel.simulator.Start;

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
                .addHeader("User-Agent", "OkHttp Bot")
//                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                .build();

        executeHttpRequest(request);

    }

    public void sendBulkPost(PMetric pMetric) {

        Request request = new Request.Builder()
                .url(url)
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
                System.out.println("EsClient[0]: repeat");
                response = httpClient.newCall(request).execute();
                PromExporter.prom_elasticPostsResent.labels(Start.HOSTNAME).inc();
            }
            System.out.println("EsClient[0]: POST sent");
            PromExporter.prom_elasticPostsSent.labels(Start.HOSTNAME).inc();

            if (!response.isSuccessful()) System.out.println("EsClient[" + Start.HOSTNAME + "]: Unexpected code: " + response);

            response.close();

//        System.out.println(response.body().string());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EsClient[0]: Recursive call.");
            PromExporter.prom_elasticPostsResent.labels(Start.HOSTNAME + "").inc();
            executeHttpRequest(request);
        }
    }

}
