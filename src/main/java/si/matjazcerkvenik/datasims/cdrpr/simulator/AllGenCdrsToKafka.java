package si.matjazcerkvenik.datasims.cdrpr.simulator;

import si.matjazcerkvenik.datasims.cdrpr.cdr.parser.CdrBean;
import si.matjazcerkvenik.datasims.Start;
import si.matjazcerkvenik.metricslib.KafkaClient;
import si.matjazcerkvenik.datasims.cdrpr.simulator.generator.StorageThread;

public class AllGenCdrsToKafka implements Runnable {

    private boolean running = true;
    private int threadId = 0;
    private KafkaClient kafkaClient;
    private String TOPIC_NAME = "pmon_all_calls_topic";


    public AllGenCdrsToKafka(int id) {
        this.threadId = id;
        kafkaClient = new KafkaClient();
    }


    @Override
    public void run() {

        System.out.println("Starting Kafka producer");

        while (running) {

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }

            while (StorageThread.getQueueSize() > 0) {

                CdrBean c = StorageThread.pollCdr();
                if (c != null) {
                    String call = toJson(c);
                    kafkaClient.sendMsg(TOPIC_NAME, call);
                } else {
                    break;
                }

            }
        }

    }



    private String toJsonMinimalistic(CdrBean cdrBean) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"index\":{} }\n").append("{");
        sb.append("\"callingNumber\":\"").append(cdrBean.getCallingNumber()).append("\",");
        sb.append("\"calledNumber\":\"").append(cdrBean.getCalledNumber()).append("\",");
        sb.append("\"duration\":").append(cdrBean.getDuration()).append(",");
        sb.append("\"cause\":").append(cdrBean.getCause()).append(",");
        sb.append("\"nodeId\":\"").append(cdrBean.getNodeId()).append("\",");
        sb.append("\"timestamp\":").append(cdrBean.getStartTime().getTime()).append("}\n");
        return sb.toString();
    }

    private String toJson(CdrBean cdrBean) {

        if (Props.SIMULATOR_MINIMUM_DATA) {
            return toJsonMinimalistic(cdrBean);
        }

        StringBuilder sb = new StringBuilder();

        sb.append("{");
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
        sb.append("\"causeString\":\"").append(Props.releaseCausesProps.getOrDefault(cdrBean.getCause() + "", "unknown")).append("\",");
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
        sb.append("\"timestamp\":").append(cdrBean.getStartTime().getTime());
        sb.append("}");

        return sb.toString();
    }

}
