package si.iskratel.cdr;

import okhttp3.*;
import org.apache.commons.io.IOUtils;
import si.iskratel.cdr.manager.BadCdrRecordException;
import si.iskratel.cdr.parser.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class EsClientThread2 extends Thread {

    private LinkedBlockingQueue<File> filesQueue = new LinkedBlockingQueue();

    private boolean running = true;
    private String nodeId;
    private int threadId = 0;
    private int filesCount = 0;
    private int bulkSize = 0;
    private int postCount = 0;
    private int resendCount = 0;
    private long totalCdrCount = 0;
    private long badCdrRecordExceptionCount = 0;
    private long startTime = 0;
    private long endTime = 0;

    private StringBuilder sb = new StringBuilder();

    private OkHttpClient httpClient = new OkHttpClient();
    private MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    public EsClientThread2(int id, String nodeId) {
        this.nodeId = nodeId;
        threadId = id;
    }

    public void addFile(File f) {
        try {
            filesQueue.put(f);
            filesCount++;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void run() {

        startTime = System.currentTimeMillis();

        while (!filesQueue.isEmpty()) {
            parse(filesQueue.poll());
        }

        // send what is left to be sent
        bulkSize = Test.BULK_SIZE;
        sendCdrBulkPost();

        endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;

        System.out.println("----- Results -----");
        System.out.println("\tThread ID: " + threadId);
        System.out.println("\tProcessed files: " + filesCount);
        System.out.println("\tRecords count: " + totalCdrCount);
        System.out.println("\tBad records count: " + badCdrRecordExceptionCount);
        System.out.println("\tProcessing time: " + processingTime);
        System.out.println("\tRate: " + (totalCdrCount * 1.0 / processingTime / 1.0 * 1000));
        System.out.println("\tPost requests count: " + postCount);
        System.out.println("\tResend count: " + resendCount);

        running = false;

    }

    public void parse(File f) {

        try {

            FileInputStream is = new FileInputStream(f);
//        ByteArrayInputStream bais = new ByteArrayInputStream(is.readAllBytes()); // requires Java 9!!!
            byte[] bytes = IOUtils.toByteArray(is);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            List<DataRecord> list = CDRReader.readDataRecords(bais);
            Test.debug("records in file: " + list.size());

            for (DataRecord dr : list) {
                //Test.debug(dr.toString());
                CdrBeanCreator cbc = new CdrBeanCreator() {
                    @Override
                    public void setSpecificBeanValues(CdrObject cdrObj, CdrBean cdrBean) {

                    }
                };
                try {
                    CdrBean cdrBean = cbc.parseBinaryCdr(dr.getDataRecordBytes(), null);
                    totalCdrCount++;
                    putToStringBuilder(cdrBean);
                    sendCdrBulkPost();
                    //Test.debug(cdrBean.toString());
                } catch (BadCdrRecordException e) {
                    badCdrRecordExceptionCount++;
                    PpdrBean ppdrBean = cbc.parseBinaryPpdr(dr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendCdrBulkPost() {

        if (bulkSize % Test.BULK_SIZE != 0) return;

        Request request = new Request.Builder()
                .url(Test.ES_URL)
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
                sleep(1500);
                response = httpClient.newCall(request).execute();
                resendCount++;
                System.out.println("Retrying to send [" + postCount + "]. ThreadId: " + threadId);
            }
            postCount++;
            sb = new StringBuilder();
            bulkSize = 0;
            System.out.println("POST sent: " + postCount + " ThreadId: " + threadId);

            if (!response.isSuccessful()) System.out.println("Unexpected code: " + response);

            response.close();

//        System.out.println(response.body().string());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Recursive call. ThreadId: " + threadId);
            resendCount++;
            executeHttpRequest(request);
        }
    }

    private void putToStringBuilder(CdrBean cdrBean) {
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
        sb.append("\"callReleasingSide\":\"").append(cdrBean.getCallReleasingSide()).append("\",");
        sb.append("\"startTime\":\"").append(toDateString(cdrBean.getStartTime())).append("\",");
        sb.append("\"endTime\":\"").append(toDateString(cdrBean.getEndTime())).append("\",");
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
        sb.append("\"nodeId\":\"").append(nodeId).append("\",");
        sb.append("\"timestamp\":").append(cdrBean.getStartTime().getTime()).append("}\n");
        bulkSize++;
    }

    private String toDateString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return dateFormat.format(date);
    }

    public boolean isRunning() { return running; }

    public int getThreadId() {
        return threadId;
    }

    public int getPostCount() {
        return postCount;
    }

    public int getResendCount() {
        return resendCount;
    }

    public long getTotalCdrCount() {
        return totalCdrCount;
    }

    public long getBadCdrRecordExceptionCount() {
        return badCdrRecordExceptionCount;
    }
}
