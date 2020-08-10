package si.iskratel.simulator;

import si.iskratel.cdr.parser.*;

import java.util.*;

public class CdrSimulatorThread extends Thread {

    private boolean running = true;
    private int threadId = 0;
    private long totalCount = 0;

    private int timeBeforeRinging = 2500;

    public CdrSimulatorThread(int id) {
        threadId = id;
    }


    public void run() {

        int delay = getRandomGaussian(Start.SIMULATOR_CALL_DELAY, Start.SIMULATOR_CALL_DELAY / 2);

        while (running) {

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }

            simulateCall();

        }

    }

    private void simulateCall() {

        CdrBean cdrBean = new CdrBean();
        cdrBean.setId((int) totalCount);
        cdrBean.setCallid(totalCount);
        cdrBean.setSequence(2);
        cdrBean.setCallType(0);
        cdrBean.setNodeId(Start.getRandomNodeId());

        String a = getANumber();
        cdrBean.setCallingNumber(a);
        String b = "" + getRandomInRange(Start.SIMULATOR_BNUM_START, Start.SIMULATOR_BNUM_START + Start.SIMULATOR_BNUM_RANGE);
        cdrBean.setCalledNumber(b);

        timeBeforeRinging = getRandomGaussian(timeBeforeRinging, 100);
        if (timeBeforeRinging < 300) timeBeforeRinging = 2500;
        if (timeBeforeRinging > 5000) timeBeforeRinging = 2500;
        cdrBean.setCdrTimeBeforeRinging(timeBeforeRinging);
        cdrBean.setCdrRingingTimeBeforeAnsw(0);

        cdrBean.setCause(Start.SIMULATOR_CALL_REASON);
        if (Start.SIMULATOR_CALL_REASON == 0) {
            if (totalCount % 2 == 0) {
                cdrBean.setCause(16);
            } else if (totalCount % 3 == 0) {
                cdrBean.setCause(17);
            } else if (totalCount % 5 == 0) {
                cdrBean.setCause(19);
            } else if (totalCount % 7 == 0) {
                cdrBean.setCause(21);
            } else if (totalCount % 9 == 0) {
                cdrBean.setCause(38);
            } else if (totalCount % 11 == 0) {
                cdrBean.setCause(3);
            } else if (totalCount % 13 == 0) {
                cdrBean.setCause(6);
            } else {
                cdrBean.setCause(getRandomInRange(1, 127));
            }
        }

        int duration = 0;
        if (cdrBean.getCause() == 16) {
            if (totalCount % 2 == 0) {
                duration = getRandomGaussian(500, 100);
            } else if (totalCount % 3 == 0) {
                duration = getRandomGaussian(900, 300);
            } else if (totalCount % 5 == 0) {
                duration = getRandomGaussian(1200, 500);
            } else if (totalCount % 7 == 0) {
                duration = getRandomGaussian(1800, 700);
            } else if (totalCount % 9 == 0) {
                duration = getRandomGaussian(2200, 1000);
            } else if (totalCount % 11 == 0) {
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

        cdrBean.setInTrunkId(220 + getRandomGaussian(10, 4));
        cdrBean.setInTrunkGroupId(9940 + getRandomGaussian(10, 5));
        cdrBean.setOutTrunkId(130 + getRandomGaussian(5, 3));
        cdrBean.setOutTrunkGroupId(8440 + getRandomGaussian(10, 10));

        cdrBean.setBgidOrig(getRandomInRange(1, 9));
        cdrBean.setBgidTerm(getRandomInRange(10, 19));
        cdrBean.setCgidOrig(getRandomInRange(50, 59));
        cdrBean.setCgidTerm(getRandomInRange(60, 69));
        cdrBean.setCtxCall(getRandomGaussian(100, 10));
        cdrBean.setCentrexCallType(getRandomInRange(1,4));

        if (duration > 0) {
            StorageThread.addCall(a, et);
        }

        Start.addCdr(cdrBean);
        totalCount++;
        PrometheusMetrics.totalCdrGenerated.labels(threadId + "").inc();

    }

    private String getANumber() {
        int a = 0;
        while (true) {
            if (a == 0) {
                a = getRandomInRange(Start.SIMULATOR_ANUM_START, Start.SIMULATOR_ANUM_START + Start.SIMULATOR_ANUM_RANGE);
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
        Random r = new Random();
        double gauss = r.nextGaussian();
        return Math.abs((int) (mean + gauss * dev));
    }





    public boolean isRunning() { return running; }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getThreadId() {
        return threadId;
    }

    public long getTotalCount() {
        return totalCount;
    }

}
