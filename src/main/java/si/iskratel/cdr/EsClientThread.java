package si.iskratel.cdr;

import okhttp3.*;
import si.iskratel.cdr.parser.CdrBean;

public class EsClientThread implements Runnable {

    public int bulkSize = 0;
    public int postCount = 0;
    public StringBuilder sb = new StringBuilder();

    public okhttp3.OkHttpClient httpClient = new OkHttpClient();
    public MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");


    @Override
    public void run() {

        while (Test.running) {

            if (Test.queue.isEmpty()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
                continue;
            }

            CdrBean cdrBean = Test.queue.poll();
            if (cdrBean != null) {
                sb.append("{ \"index\":{} }\n");
                sb.append("{" +
                        "\"callId\":\"" + cdrBean.getCallid() + "\"," +
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
            }
            if (bulkSize % Test.BULK_SIZE == 0 || (Test.queue.isEmpty() && bulkSize > 0)) {
                sendBulkPost();
                sb = new StringBuilder();
                bulkSize = 0;
            }


        }

    }

    public void sendBulkPost() {

        Request request = new Request.Builder()
                .url(Test.ES_URL)
                .addHeader("User-Agent", "OkHttp Bot")
//                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(sb.toString(), MEDIA_TYPE_JSON))
                .build();

        try {

            Response response = httpClient.newCall(request).execute();
            postCount++;
            System.out.println("POST sent: " + postCount + " ThreadId: " + this.hashCode());

            if (!response.isSuccessful()) System.out.println("Unexpected code: " + response);

//        System.out.println(response.body().string());

        } catch (Exception e) {
            e.printStackTrace();
        }





    }
}
