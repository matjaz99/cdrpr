package si.iskratel.cdr;

import okhttp3.*;
import si.iskratel.cdr.parser.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class CdrSimulatorThread extends Thread {

    private boolean running = true;
    private int threadId = 0;
    private int bulkSize = 0;
    private int postCount = 0;
    private int resendCount = 0;
    private long totalCdrCount = 0;
    private long startTime = 0;
    private long endTime = 0;
    private int timeBeforeRinging = 2500;

    private StringBuilder sb = new StringBuilder();

    private OkHttpClient httpClient = new OkHttpClient();
    private MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    public CdrSimulatorThread(int id) {
        threadId = id;
    }


    public void run() {

        startTime = System.currentTimeMillis();

        while (running) {

            try {
                Thread.sleep(getRandomGaussian(Test.SIMULATOR_DELAY, Test.SIMULATOR_DELAY / 2));
            } catch (InterruptedException e) {
            }

            simulate();

            if (totalCdrCount % 20000 == 0) {
                endTime = System.currentTimeMillis();
                long processingTime = endTime - startTime;
                System.out.println("----- Results -----");
                System.out.println("\tThread ID: " + threadId);
                System.out.println("\tRecords count: " + totalCdrCount);
                System.out.println("\tProcessing time: " + processingTime);
                System.out.println("\tRate: " + (totalCdrCount * 1.0 / processingTime / 1.0 * 1000));
                System.out.println("\tPost requests count: " + postCount);
                System.out.println("\tResend count: " + resendCount);
            }
        }

        // send what is left to be sent
        bulkSize = Test.BULK_SIZE;
        sendCdrBulkPost();

    }

    public void simulate() {

        CdrBean cdrBean = new CdrBean();
        cdrBean.setId((int) totalCdrCount);
        cdrBean.setCallid(totalCdrCount);
        cdrBean.setSequence(2);
        cdrBean.setCallType(0);

        String a = getANumber();
        cdrBean.setCallingNumber(a);
        String b = "" + getRandomInRange(Test.SIMULATOR_BNUM_START, Test.SIMULATOR_BNUM_START + Test.SIMULATOR_BNUM_RANGE);
        cdrBean.setCalledNumber(b);

        timeBeforeRinging = getRandomGaussian(timeBeforeRinging, 100);
        if (timeBeforeRinging < 300) timeBeforeRinging = 2500;
        if (timeBeforeRinging > 5000) timeBeforeRinging = 2500;
        cdrBean.setCdrTimeBeforeRinging(timeBeforeRinging);
        cdrBean.setCdrRingingTimeBeforeAnsw(0);

        cdrBean.setCause(Test.SIMULATOR_CALL_REASON);
        if (Test.SIMULATOR_CALL_REASON == 0) {
            if (totalCdrCount % 5 == 0) {
                cdrBean.setCause(16);
            } else if (totalCdrCount % 7 == 0) {
                cdrBean.setCause(18);
            } else if (totalCdrCount % 9 == 0) {
                cdrBean.setCause(21);
            } else if (totalCdrCount % 11 == 0) {
                cdrBean.setCause(34);
            } else if (totalCdrCount % 13 == 0) {
                cdrBean.setCause(65);
            } else if (totalCdrCount % 17 == 0) {
                cdrBean.setCause(111);
            } else {
                cdrBean.setCause(getRandomInRange(1, 127));
            }
        }

        int duration = 0;
        if (cdrBean.getCause() == 16) {
//            if (totalCdrCount % 2 == 0) {
//                duration = getRandomInRange(100, 900);
//            } else if (totalCdrCount % 3 == 0) {
//                duration = getRandomInRange(300, 1200);
//            } else if (totalCdrCount % 5 == 0) {
//                duration = getRandomInRange(900, 1800);
//            } else if (totalCdrCount % 7 == 0) {
//                duration = getRandomInRange(1200, 2800);
//            } else if (totalCdrCount % 9 == 0) {
//                duration = getRandomInRange(1800, 3800);
//            } else if (totalCdrCount % 11 == 0) {
//                duration = getRandomInRange(3600, 4800);
//            } else {
//                duration = getRandomInRange(200, 4800);
//            }
            if (totalCdrCount % 2 == 0) {
                duration = getRandomGaussian(500, 100);
            } else if (totalCdrCount % 3 == 0) {
                duration = getRandomGaussian(900, 300);
            } else if (totalCdrCount % 5 == 0) {
                duration = getRandomGaussian(1200, 500);
            } else if (totalCdrCount % 7 == 0) {
                duration = getRandomGaussian(1800, 700);
            } else if (totalCdrCount % 9 == 0) {
                duration = getRandomGaussian(2200, 1000);
            } else if (totalCdrCount % 11 == 0) {
                duration = getRandomGaussian(2600, 2000);
            } else {
                duration = getRandomInRange(200, 4800);
            }
            cdrBean.setCdrRingingTimeBeforeAnsw(getRandomGaussian(15000, 10000));
        }
        duration = duration * 1000; // to millis
        cdrBean.setDuration(duration);

        Date d = new Date();
        cdrBean.setStartTime(d);
        long st = d.getTime();
        long et = st + duration;
        Date d2 = new Date(et);
        cdrBean.setEndTime(d2);

        cdrBean.setInTrunkId(getRandomGaussian(4, 8));
        cdrBean.setInTrunkGroupId(9970 + getRandomGaussian(5, 5));
        cdrBean.setOutTrunkId(getRandomGaussian(2, 3));
        cdrBean.setOutTrunkGroupId(8830 + getRandomGaussian(6, 5));

        if (duration > 0) {
            StorageThread.addCall(a, et);
        }
        // size je v bistvu število aktivnih sessionov/klicev

        totalCdrCount++;
        putToStringBuilder(cdrBean);
        sendCdrBulkPost();

    }

    private String getANumber() {
        long now = System.currentTimeMillis();
        int a = 0;
        while (true) {
            if (a == 0) {
                a = getRandomInRange(Test.SIMULATOR_ANUM_START, Test.SIMULATOR_ANUM_START + Test.SIMULATOR_ANUM_RANGE);
            } else {
                a++;
            }
            if (!StorageThread.contains(a + "")) {
                break;
            }
        }
        return a + "";
    }

    private int getRandomInRange(int min, int max) {
        // min and max are inclusive
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    private int getRandomGaussian(int mean, int dev) {
        // min and max are inclusive
        Random r = new Random();
        double gauss = r.nextGaussian();
        return Math.abs((int) (mean + gauss * dev));
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
                System.out.println("CdrSimulatorThread[" + threadId + "]: Retrying to send [" + postCount + "].");
            }
            postCount++;
            sb = new StringBuilder();
            bulkSize = 0;
            System.out.println("CdrSimulatorThread[" + threadId + "]: POST sent count: " + postCount);

            if (!response.isSuccessful()) System.out.println("CdrSimulatorThread[" + threadId + "]: Unexpected code: " + response);

            response.close();

//        System.out.println(response.body().string());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("CdrSimulatorThread[" + threadId + "]: Recursive call.");
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
        sb.append("\"causeString\":\"").append(Test.releaseCausesProps.getOrDefault(cdrBean.getCause() + "", "unknown")).append("\",");
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
        sb.append("\"nodeId\":\"").append(Test.SIMULATOR_NODEID).append("\",");
        sb.append("\"timestamp\":").append(cdrBean.getStartTime().getTime()).append("}\n");
        bulkSize++;
    }

    private String toDateString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return dateFormat.format(date);
    }

    public boolean isRunning() { return running; }

    public void setRunning(boolean running) {
        this.running = running;
    }

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

}
