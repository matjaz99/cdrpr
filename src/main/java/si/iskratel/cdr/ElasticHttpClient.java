package si.iskratel.cdr;

import okhttp3.*;
import si.iskratel.cdr.parser.CdrBean;

public class ElasticHttpClient {


    public static String url = "http://pgcentos:9200/cdrs/_doc?pretty";
    public static okhttp3.OkHttpClient httpClient = new OkHttpClient();
    public static MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    public static int postCount = 0;

    public static void sendOkhttpPost(CdrBean cdrBean) throws Exception {

        if (httpClient == null) httpClient = new OkHttpClient();


        // form parameters
        String json = "{" +
                "\"callid\":\"" + cdrBean.getCallid() + "\"," +
                "\"ownerNumber\":\"" + cdrBean.getOwnerNumber() + "\"," +
                "\"callingNumber\":\"" + cdrBean.getCallingNumber() + "\"," +
                "\"calledNumber\":\"" + cdrBean.getCalledNumber() + "\"," +
                "\"cdrTimeBeforeRinging\":" + cdrBean.getCdrTimeBeforeRinging() + "," +
                "\"cdrRingingTimeBeforeAnsw\":" + cdrBean.getCdrRingingTimeBeforeAnsw() + "," +
                "\"duration\":" + cdrBean.getDuration() + "," +
                "\"cause\":" + cdrBean.getCause() + "," +
                "\"cacType\":\"" + cdrBean.getCacType() + "\"," +
                "\"cacPrefix\":\"" + cdrBean.getCacPrefix() + "\"," +
                "\"cacNumber\":\"" + cdrBean.getCacNumber() + "\"," +
                "\"inTrunkId\":\"" + cdrBean.getInTrunkId() + "\"," +
                "\"inTrunkGroupId\":\"" + cdrBean.getInTrunkGroupId() + "\"," +
                "\"outTrunkId\":\"" + cdrBean.getOutTrunkId() + "\"," +
                "\"outTrunkGroupId\":\"" + cdrBean.getOutTrunkGroupId() + "\"," +
                "\"timestamp\":" + System.currentTimeMillis() + "" +
                "}";

        System.out.println(json);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "OkHttp Bot")
//                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(json, MEDIA_TYPE_JSON))
                .build();

        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) System.out.println("Unexpected code: " + response);

        // Get response body
        System.out.println(response.body().string());

    }

    public static int bulkSize = 0;
    public static StringBuilder sb = new StringBuilder();

    public static void sendBulkPost(CdrBean cdrBean) throws Exception {

        sb.append("{ \"index\":{} }\n");
        sb.append("{" +
                "\"callid\":\"" + cdrBean.getCallid() + "\"," +
                "\"ownerNumber\":\"" + cdrBean.getOwnerNumber() + "\"," +
                "\"callingNumber\":\"" + cdrBean.getCallingNumber() + "\"," +
                "\"calledNumber\":\"" + cdrBean.getCalledNumber() + "\"," +
                "\"cdrTimeBeforeRinging\":" + cdrBean.getCdrTimeBeforeRinging() + "," +
                "\"cdrRingingTimeBeforeAnsw\":" + cdrBean.getCdrRingingTimeBeforeAnsw() + "," +
                "\"duration\":" + cdrBean.getDuration() + "," +
                "\"cause\":" + cdrBean.getCause() + "," +
                "\"cacType\":\"" + cdrBean.getCacType() + "\"," +
                "\"cacPrefix\":\"" + cdrBean.getCacPrefix() + "\"," +
                "\"cacNumber\":\"" + cdrBean.getCacNumber() + "\"," +
                "\"inTrunkId\":\"" + cdrBean.getInTrunkId() + "\"," +
                "\"inTrunkGroupId\":\"" + cdrBean.getInTrunkGroupId() + "\"," +
                "\"outTrunkId\":\"" + cdrBean.getOutTrunkId() + "\"," +
                "\"outTrunkGroupId\":\"" + cdrBean.getOutTrunkGroupId() + "\"," +
                "\"timestamp\":" + System.currentTimeMillis() + "" +
                "}\n");
        bulkSize++;

        if (bulkSize % Test.BULK_SIZE != 0) return;

        Request request = new Request.Builder()
                .url(Test.ES_URL)
                .addHeader("User-Agent", "OkHttp Bot")
//                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(sb.toString(), MEDIA_TYPE_JSON))
                .build();

        Response response = httpClient.newCall(request).execute();
        postCount++;
        System.out.println("POST sent: " + postCount);

        if (!response.isSuccessful()) System.out.println("Unexpected code: " + response);

        // Get response body
//        System.out.println(response.body().string());

        sb = new StringBuilder();
        bulkSize = 0;

    }

}
