package si.iskratel.simulator;

import okhttp3.*;
import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.metricslib.PromExporter;

public class AllCallData implements Runnable {

    private boolean running = true;
    private int threadId = 0;
    private int sendInterval = Start.SEND_INTERVAL_SEC * 1000;
    private int bulkCount = 0;

    private StringBuilder sb = new StringBuilder();

    private OkHttpClient httpClient = new OkHttpClient();
    private MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    private int dynamicBulkSize = Start.BULK_SIZE;

    public AllCallData(int id) {
        this.threadId = id;
    }


    @Override
    public void run() {

        while (running) {

            try {
                Thread.sleep(sendInterval);
            } catch (InterruptedException e) {
            }

            while (Start.getQueueSize() > 0 && bulkCount < dynamicBulkSize) {

                CdrBean c = Start.pollCdr();
                if (c != null) {
                    putToStringBuilder(c);
                    bulkCount++;
                } else {
                    break;
                }

            }

            if (Start.getQueueSize() > 3 * Start.BULK_SIZE) {
                sendInterval = sendInterval - 10;
                if (sendInterval < 1) sendInterval = 1;
            }
            if (Start.getQueueSize() > 5 * Start.BULK_SIZE) {
                dynamicBulkSize = dynamicBulkSize + 100;
                if (dynamicBulkSize > 100000) dynamicBulkSize = 100000;
            }
            if (Start.getQueueSize() < Start.BULK_SIZE) {
                sendInterval = Start.SEND_INTERVAL_SEC * 1000;
                dynamicBulkSize = Start.BULK_SIZE;
            }

            PrometheusMetrics.bulkCount.set(bulkCount);
            PrometheusMetrics.bulkSize.set(dynamicBulkSize);
            PrometheusMetrics.sendInterval.set(sendInterval);

            sendBulkPost();
            bulkCount = 0;

//            if (totalCount % 20000 == 0) {
//                endTime = System.currentTimeMillis();
//                long processingTime = endTime - startTime;
//                System.out.println("----- Results -----");
////                System.out.println("\tThread ID: " + threadId);
//                System.out.println("\tRecords count: " + totalCount);
//                System.out.println("\tProcessing time: " + processingTime);
//                System.out.println("\tRate: " + (totalCount * 1.0 / processingTime / 1.0 * 1000));
//                System.out.println("\tPost requests count: " + postCount);
//                System.out.println("\tResend count: " + resendCount);
//            }
        }

    }

    public void sendBulkPost() {

        Request request = new Request.Builder()
                .url(Start.ES_URL)
                .addHeader("User-Agent", "OkHttp Bot")
//                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(sb.toString(), MEDIA_TYPE_JSON))
                .build();

        executeHttpRequest(request);

    }

    private void executeHttpRequest(Request request) {
        try {

            Response response = httpClient.newCall(request).execute();
            while (!response.isSuccessful()) {
                Thread.sleep(1500);
                response = httpClient.newCall(request).execute();
                PromExporter.prom_elasticPostsResent.labels(threadId + "").inc();
            }
            PromExporter.prom_elasticPostsSent.labels(threadId + "").inc();
            sb = new StringBuilder();

            if (!response.isSuccessful()) System.out.println("ElasticPersistenceClient[" + threadId + "]: Unexpected code: " + response);

            response.close();

//        System.out.println(response.body().string());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ElasticPersistenceClient[" + threadId + "]: Recursive call.");
            PromExporter.prom_elasticPostsResent.labels(threadId + "").inc();
            executeHttpRequest(request);
        }
    }

    private void putToStringBuilderMinimalistic(CdrBean cdrBean) {
        sb.append("{ \"index\":{} }\n").append("{");
        sb.append("\"callingNumber\":\"").append(cdrBean.getCallingNumber()).append("\",");
        sb.append("\"calledNumber\":\"").append(cdrBean.getCalledNumber()).append("\",");
        sb.append("\"duration\":").append(cdrBean.getDuration()).append(",");
        sb.append("\"cause\":").append(cdrBean.getCause()).append(",");
        sb.append("\"nodeId\":\"").append(cdrBean.getNodeId()).append("\",");
        sb.append("\"timestamp\":").append(cdrBean.getStartTime().getTime()).append("}\n");
    }

    private void putToStringBuilder(CdrBean cdrBean) {

        if (Start.SIMULATOR_MINIMUM_DATA) {
            putToStringBuilderMinimalistic(cdrBean);
            return;
        }

        sb.append("{ \"index\":{} }\n").append("{");
        sb.append("\"id\":\"").append(cdrBean.getId()).append("\",");
        sb.append("\"callId\":\"").append(cdrBean.getCallid()).append("\",");
        sb.append("\"sequence\":\"").append(cdrBean.getSequence()).append("\",");
        sb.append("\"callType\":\"").append(cdrBean.getCallType()).append("\",");
        sb.append("\"ownerNumber\":\"").append(cdrBean.getOwnerNumber()).append("\",");
        sb.append("\"callingNumber\":\"").append(cdrBean.getCallingNumber()).append("\",");
        sb.append("\"calledNumber\":\"").append(cdrBean.getCalledNumber()).append("\",");
        sb.append("\"cdrTimeBeforeRinging\":").append(cdrBean.getCdrTimeBeforeRinging()).append(",");
        sb.append("\"cdrRingingTimeBeforeAnsw\":").append(cdrBean.getCdrRingingTimeBeforeAnsw()).append(",");
        sb.append("\"duration\":").append(cdrBean.getDuration()).append(",");
        sb.append("\"cause\":").append(cdrBean.getCause()).append(",");
        sb.append("\"causeString\":\"").append(Start.releaseCausesProps.getOrDefault(cdrBean.getCause() + "", "unknown")).append("\",");
        sb.append("\"callReleasingSide\":\"").append(cdrBean.getCallReleasingSide()).append("\",");
        sb.append("\"startTime\":\"").append(Utils.toDateString(cdrBean.getStartTime())).append("\",");
        sb.append("\"endTime\":\"").append(Utils.toDateString(cdrBean.getEndTime())).append("\",");
        sb.append("\"cacType\":\"").append(cdrBean.getCacType()).append("\",");
        sb.append("\"cacPrefix\":\"").append(cdrBean.getCacPrefix()).append("\",");
        sb.append("\"cacNumber\":\"").append(cdrBean.getCacNumber()).append("\",");
        sb.append("\"inTrunkId\":\"").append(cdrBean.getInTrunkId()).append("\",");
        sb.append("\"inTrunkGroupId\":\"").append(cdrBean.getInTrunkGroupId()).append("\",");
        sb.append("\"outTrunkId\":\"").append(cdrBean.getOutTrunkId()).append("\",");
        sb.append("\"outTrunkGroupId\":\"").append(cdrBean.getOutTrunkGroupId()).append("\",");
        sb.append("\"inTrunkGroupName\":\"").append(cdrBean.getInTrunkGroupNameIE144()).append("\",");
        sb.append("\"outTrunkGroupName\":\"").append(cdrBean.getOutTrunkGroupNameIE145()).append("\",");
        sb.append("\"icId\":\"").append(cdrBean.getIcid()).append("\",");
        sb.append("\"chgUnits\":\"").append(cdrBean.getChgUnits()).append("\",");
        sb.append("\"price\":\"").append(cdrBean.getPrice()).append("\",");
        sb.append("\"servId\":\"").append(cdrBean.getServId()).append("\",");
        sb.append("\"servIdOrig\":\"").append(cdrBean.getServIdOrig()).append("\",");
        sb.append("\"servIdTerm\":\"").append(cdrBean.getServIdTerm()).append("\",");
        sb.append("\"ctxCall\":\"").append(cdrBean.getCtxCall()).append("\",");
        sb.append("\"ctxCallingNumber\":\"").append(cdrBean.getCtxCallingNumber()).append("\",");
        sb.append("\"ctxCalledNumber\":\"").append(cdrBean.getCtxCalledNumber()).append("\",");
        sb.append("\"bgidOrig\":\"").append(cdrBean.getBgidOrig()).append("\",");
        sb.append("\"bgidTerm\":\"").append(cdrBean.getBgidTerm()).append("\",");
        sb.append("\"nodeId\":\"").append(cdrBean.getNodeId()).append("\",");
        sb.append("\"timestamp\":").append(cdrBean.getStartTime().getTime()).append("}\n");
    }

}
