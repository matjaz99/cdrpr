package si.iskratel.simulator.generator;

import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.simulator.Props;
import si.iskratel.simulator.SimulatorMetrics;
import si.iskratel.simulator.Start;

import java.util.Date;
import java.util.Random;

/**
 * This thread generates CdrBeans with random data.
 */
public class CdrNodeGeneratorThread extends Thread {

    private boolean running = true;
    private int threadId = 0;
    private long totalCount = 0;
    private int randomFactor = 1;
    private static String nodeId;

    public CdrNodeGeneratorThread(int id, String nodeId) {
        threadId = id;
        this.nodeId = nodeId;
        randomFactor = getRandomInRange(2, 12);
    }


    public void run() {

//        int delay = getRandomGaussian(Start.SIMULATOR_CALL_DELAY, Start.SIMULATOR_CALL_DELAY / 4);
        long delay = Props.SIMULATOR_CALL_DELAY;

        while (running) {

            try {
//                double cosFact = Math.abs(getCosFactor(randomFactor * 3600));
//                System.out.println("cosFact[" + threadId + "]: " + cosFact);
                delay = (long) (delay * Math.abs(getCosFactor(randomFactor * 3600))) + getRandomInRange(10, 400);
//                System.out.println("delay[" + threadId + "]: " + delay);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }

            CdrBean cdrBean = simulateCall();
            StorageThread.addCdr(cdrBean);
            totalCount++;
            SimulatorMetrics.totalCdrGenerated.labels(threadId + "").inc();

        }

    }

    private CdrBean simulateCall() {

        CdrBean cdrBean = new CdrBean();
        cdrBean.setId((int) totalCount);
        cdrBean.setCallid(totalCount);
        cdrBean.setSequence(2);
        cdrBean.setCallType(0);
        cdrBean.setNodeId(Start.getRandomNodeId());

        String aNumber = getAvailableANumber();
        cdrBean.setCallingNumber(aNumber);
        String bNumber = "" + getRandomInRange(Props.SIMULATOR_BNUM_START, Props.SIMULATOR_BNUM_START + Props.SIMULATOR_BNUM_RANGE);
        cdrBean.setCalledNumber(bNumber);

        cdrBean.setCdrTimeBeforeRinging((int) (getRandomGaussian(2500, 100)));
        cdrBean.setCdrRingingTimeBeforeAnsw((int) (getRandomGaussian(25000, 1000)));

        cdrBean.setCause(Props.SIMULATOR_CALL_REASON);
        int rnd = getRandomInRange(1, 100);
        if (Props.SIMULATOR_CALL_REASON == 0) {
            if (rnd % 2 == 0) {
                cdrBean.setCause(16);
            } else if (rnd % 3 == 0 && rnd % 2 == 0) {
                cdrBean.setCause(17);
            } else if (rnd % 5 == 0 && rnd % 2 == 0) {
                cdrBean.setCause(19);
            } else if (rnd % 7 == 0 && rnd % 2 == 0) {
                cdrBean.setCause(21);
            } else if (rnd % 9 == 0 && rnd % 2 == 0) {
                cdrBean.setCause(38);
            } else if (rnd % 11 == 0) {
                cdrBean.setCause(3);
            } else if (rnd % 13 == 0) {
                cdrBean.setCause(6);
            } else {
                cdrBean.setCause(getRandomInRange(1, 127));
            }
        }

        int duration = 0;
        if (cdrBean.getCause() == 16) {
            if (totalCount % 2 == 0) {
                duration = getRandomGaussian(500, 200);
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
            cdrBean.setCdrRingingTimeBeforeAnsw(getRandomGaussian(15000, 5000));
        }
        duration = (int) (duration * Math.abs(getCosFactor(randomFactor))) + 30;
        duration = duration * 1000; // to millis
        cdrBean.setDuration(duration);

        Date d = new Date();
        cdrBean.setStartTime(d);
        long st = d.getTime();
        long et = st + duration;
        Date d2 = new Date(et);
        cdrBean.setEndTime(d2);

        cdrBean.setInTrunkId(220 + getRandomGaussian(10, 6));
        cdrBean.setInTrunkGroupId(9940 + getRandomGaussian(10, 11));
        cdrBean.setOutTrunkId(130 + getRandomGaussian(5, 5));
        cdrBean.setOutTrunkGroupId(8440 + getRandomGaussian(10, 13));

        String[] ttArray = {"INC", "OUT", "LOCAL", "TRANSIT"};
        cdrBean.setTrafficType(ttArray[getRandomInRange(0, ttArray.length - 1)]);

        cdrBean.setBgidOrig(getRandomInRange(1, 4));
        cdrBean.setBgidTerm(getRandomInRange(10, 15));
        cdrBean.setCgidOrig(getRandomInRange(50, 54));
        cdrBean.setCgidTerm(getRandomInRange(60, 64));
        cdrBean.setCtxCall(getRandomGaussian(100, 2));
        cdrBean.setCentrexCallType(getRandomInRange(1,2));
        cdrBean.setServId((short) getRandomInRange(1, 9));
        cdrBean.setServIdOrig((short) getRandomInRange(1, 3));
        cdrBean.setServIdTerm((short) getRandomInRange(1, 3));
        cdrBean.setCallingSubscriberGroup(getRandomInRange(100, 105));
        cdrBean.setCalledSubscriberGroup(getRandomInRange(200, 225));
        cdrBean.setVoipRxCodecType(getRandomInRange(1, 17));
        cdrBean.setVoipTxCodecType(getRandomInRange(11, 27));

        if (duration > 0) {
            StorageThread.addCall(aNumber, et);
        }

        return cdrBean;

    }

    private String getAvailableANumber() {
        int a = getRandomInRange(Props.SIMULATOR_ANUM_START, Props.SIMULATOR_ANUM_START + Props.SIMULATOR_ANUM_RANGE);
        while (StorageThread.contains(a + "")) {
            a++;
        }
        return a + "";
    }

    private static int getRandomInRange(int min, int max) {
        // min and max are inclusive
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    private static int getRandomGaussian(int mean, int dev) {
        Random r = new Random();
        double gauss = r.nextGaussian();
        return Math.abs((int) (mean + gauss * dev));
    }

    public static double getCosFactor(int periodSeconds) {
        double t = System.currentTimeMillis() / 1000;
        return Math.cos(t * 2 * 3.14 / periodSeconds);
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
