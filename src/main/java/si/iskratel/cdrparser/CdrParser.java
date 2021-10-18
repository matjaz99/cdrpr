package si.iskratel.cdrparser;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.iskratel.cdr.manager.BadCdrRecordException;
import si.iskratel.cdr.parser.*;
import si.iskratel.metricslib.*;
import si.iskratel.simulator.Start;
import si.iskratel.simulator.Utils;

import java.io.*;
import java.util.*;

public class CdrParser {

    private static Logger logger = LoggerFactory.getLogger(CdrParser.class);

    public static Properties releaseCausesProps;

    public static PMetric cdr_records_total = PMetric.build()
            .setName("cdrparser_records_total")
            .setHelp("Number of extracted records")
            .setLabelNames("fileType")
            .register();

    static {
        releaseCausesProps = new Properties();
        try {
            releaseCausesProps.load(new FileInputStream("config/call_release_causes.properties"));
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
        }
    }


    public static CdrData parse(File f) throws Exception {

        CdrData cdrData = new CdrData();

        FileInputStream is = new FileInputStream(f);
//        ByteArrayInputStream bais = new ByteArrayInputStream(is.readAllBytes()); // requires Java 9!!!
        byte[] bytes = IOUtils.toByteArray(is);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        List<DataRecord> list = CDRReader.readDataRecords(bais);
        logger.info("records in file: " + list.size());

        for (DataRecord dr : list) {
//            logger.debug(dr.toString());
            CdrBeanCreator cbc = new CdrBeanCreator() {
                @Override
                public void setSpecificBeanValues(CdrObject cdrObj, CdrBean cdrBean) {

                }
            };
            try {
                CdrBean cdrBean = cbc.parseBinaryCdr(dr.getDataRecordBytes(), null);
                cdrData.cdrList.add(cdrBean);
                cdr_records_total.setLabelValues("CDR").inc();
//                logger.debug(cdrBean.toString());
            } catch (BadCdrRecordException e) {
                PpdrBean ppdrBean = cbc.parseBinaryPpdr(dr);
                cdrData.ppdrList.add(ppdrBean);
                cdr_records_total.setLabelValues("PPDR").inc();
            } catch (Exception e) {
                logger.error("Exception: ", e);
                cdr_records_total.setLabelValues("Unknown").inc();
            }
        }

        // PpdrBean [recordLength=69, recordIndex=207961128, recordTime=Wed Sep 01 14:01:22 CEST 2021, trunkGroupId=2, trunkGroupName=IX_Dialogic_LJ_1, numberOfAllTrunks=1500, numberOfOutOfServiceTrunks=0, trunkGroupOperatingMode=0]

        Map<Integer, String> tg_id_name = new HashMap<>();
        Map<String, Integer> tg_name_id = new HashMap<>();
        for (int i = 0; i < cdrData.ppdrList.size(); i++) {
            tg_id_name.put(cdrData.ppdrList.get(i).getTrunkGroupId(), cdrData.ppdrList.get(i).getTrunkGroupName());
            tg_name_id.put(cdrData.ppdrList.get(i).getTrunkGroupName(), cdrData.ppdrList.get(i).getTrunkGroupId());
        }

        for (int i = 0; i < cdrData.cdrList.size(); i++) {
            if (cdrData.cdrList.get(i).getInTrunkGroupId() == null) {
                cdrData.cdrList.get(i).setInTrunkGroupId(
                        tg_name_id.getOrDefault(cdrData.cdrList.get(i).getInTrunkGroupNameIE144(), null));
            }
            if (cdrData.cdrList.get(i).getInTrunkGroupNameIE144() == null) {
                cdrData.cdrList.get(i).setInTrunkGroupNameIE144(
                        tg_id_name.getOrDefault(cdrData.cdrList.get(i).getInTrunkGroupId(), null));

            }
            if (cdrData.cdrList.get(i).getOutTrunkGroupId() == null) {
                cdrData.cdrList.get(i).setOutTrunkGroupId(
                        tg_name_id.getOrDefault(cdrData.cdrList.get(i).getOutTrunkGroupNameIE145(), null));
            }
            if (cdrData.cdrList.get(i).getOutTrunkGroupNameIE145() == null) {
                cdrData.cdrList.get(i).setOutTrunkGroupNameIE145(
                        tg_id_name.getOrDefault(cdrData.cdrList.get(i).getOutTrunkGroupId(), null));
            }
        }

        return cdrData;

    }


    public static String toCsv(CdrBean cdrBean) {

        StringBuilder sb = new StringBuilder();

        sb.append(cdrBean.getId()).append(",");
        sb.append(cdrBean.getSequence()).append(",");
        sb.append(cdrBean.getCallType()).append(",");
        sb.append(cdrBean.getOwnerNumber()).append(",");
        sb.append(cdrBean.getCallingNumber()).append(",");
        sb.append(cdrBean.getCalledNumber()).append(",");
        sb.append(cdrBean.getCdrTimeBeforeRinging()).append(",");
        sb.append(cdrBean.getCdrRingingTimeBeforeAnsw()).append(",");
        sb.append(cdrBean.getDuration()).append(",");
        sb.append(cdrBean.getCause()).append(",");
        sb.append(releaseCausesProps.getOrDefault(cdrBean.getCause() + "", "unknown")).append(",");
        sb.append(cdrBean.getCallReleasingSide()).append(",");
        sb.append(Utils.toDateString(cdrBean.getStartTime())).append(",");
        sb.append(Utils.toDateString(cdrBean.getEndTime())).append(",");
//        sb.append("\"cacType\":\"").append(cdrBean.getCacType()).append("\",");
//        sb.append("\"cacPrefix\":\"").append(cdrBean.getCacPrefix()).append("\",");
//        sb.append("\"cacNumber\":\"").append(cdrBean.getCacNumber()).append("\",");
        sb.append(cdrBean.getInTrunkId()).append(",");
        sb.append(cdrBean.getInTrunkGroupId()).append(",");
        sb.append(cdrBean.getOutTrunkId()).append(",");
        sb.append(cdrBean.getOutTrunkGroupId()).append(",");
        sb.append(cdrBean.getInTrunkGroupNameIE144()).append(",");
        sb.append(cdrBean.getOutTrunkGroupNameIE145()).append(",");
//        sb.append("\"servId\":\"").append(cdrBean.getServId()).append("\",");
//        sb.append("\"servIdOrig\":\"").append(cdrBean.getServIdOrig()).append("\",");
//        sb.append("\"servIdTerm\":\"").append(cdrBean.getServIdTerm()).append("\",");
//        sb.append("\"ctxCall\":\"").append(cdrBean.getCtxCall()).append("\",");
//        sb.append("\"ctxCallingNumber\":\"").append(cdrBean.getCtxCallingNumber()).append("\",");
//        sb.append("\"ctxCalledNumber\":\"").append(cdrBean.getCtxCalledNumber()).append("\",");
//        sb.append("\"bgidOrig\":\"").append(cdrBean.getBgidOrig()).append("\",");
//        sb.append("\"bgidTerm\":\"").append(cdrBean.getBgidTerm()).append("\",");
        sb.append(cdrBean.getNodeId()).append(",");
        sb.append(cdrBean.getStartTime().getTime());
        sb.append("\n");

        return sb.toString();
    }


    public static String toEsNdjson(String index, CdrBean cdrBean) {

        StringBuilder sb = new StringBuilder();

        sb.append("{\"index\":{\"_index\":\"").append(index).append("\"}}\n");
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
        sb.append("\"causeString\":\"").append(releaseCausesProps.getOrDefault(cdrBean.getCause() + "", "unknown")).append("\",");
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
        sb.append("\"servId\":\"").append(cdrBean.getServId()).append("\",");
        sb.append("\"servIdOrig\":\"").append(cdrBean.getServIdOrig()).append("\",");
        sb.append("\"servIdTerm\":\"").append(cdrBean.getServIdTerm()).append("\",");
        sb.append("\"ctxCall\":\"").append(cdrBean.getCtxCall()).append("\",");
        sb.append("\"ctxCallingNumber\":\"").append(cdrBean.getCtxCallingNumber()).append("\",");
        sb.append("\"ctxCalledNumber\":\"").append(cdrBean.getCtxCalledNumber()).append("\",");
        sb.append("\"bgidOrig\":\"").append(cdrBean.getBgidOrig()).append("\",");
        sb.append("\"bgidTerm\":\"").append(cdrBean.getBgidTerm()).append("\",");
        sb.append("\"nodeId\":\"").append(cdrBean.getNodeId()).append("\",");
        sb.append("\"@timestamp\":").append(cdrBean.getStartTime().getTime()).append("}\n");

        return sb.toString();
    }

    public static String toEsNdjsonShort(String index, CdrBean cdrBean) {

        StringBuilder sb = new StringBuilder();

        sb.append("{\"index\":{\"_index\":\"").append(index).append("\"}}\n");
        sb.append("{");
        sb.append("\"id\":\"").append(cdrBean.getId()).append("\",");
        sb.append("\"sequence\":\"").append(cdrBean.getSequence()).append("\",");
        sb.append("\"ownerNumber\":\"").append(cdrBean.getOwnerNumber()).append("\",");
        sb.append("\"callingNumber\":\"").append(cdrBean.getCallingNumber()).append("\",");
        sb.append("\"calledNumber\":\"").append(cdrBean.getCalledNumber()).append("\",");
        sb.append("\"cdrTimeBeforeRinging\":").append(cdrBean.getCdrTimeBeforeRinging()).append(",");
        sb.append("\"cdrRingingTimeBeforeAnsw\":").append(cdrBean.getCdrRingingTimeBeforeAnsw()).append(",");
        sb.append("\"duration\":").append(cdrBean.getDuration()).append(",");
        sb.append("\"cause\":").append(cdrBean.getCause()).append(",");
        sb.append("\"causeString\":\"").append(releaseCausesProps.getOrDefault(cdrBean.getCause() + "", "unknown")).append("\",");
        sb.append("\"startTime\":\"").append(Utils.toDateString(cdrBean.getStartTime())).append("\",");
        sb.append("\"endTime\":\"").append(Utils.toDateString(cdrBean.getEndTime())).append("\",");
        sb.append("\"inTrunkGroupId\":\"").append(cdrBean.getInTrunkGroupId()).append("\",");
        sb.append("\"outTrunkGroupId\":\"").append(cdrBean.getOutTrunkGroupId()).append("\",");
        sb.append("\"inTrunkGroupName\":\"").append(cdrBean.getInTrunkGroupNameIE144()).append("\",");
        sb.append("\"outTrunkGroupName\":\"").append(cdrBean.getOutTrunkGroupNameIE145()).append("\",");
        sb.append("\"nodeId\":\"").append(cdrBean.getNodeId()).append("\",");
        sb.append("\"@timestamp\":").append(cdrBean.getStartTime().getTime()).append("}\n");

        return sb.toString();
    }

    public static PMultiValueMetric toMultivalueMetric(CdrBean cdrBean) {

        PMultiValueMetric mv = PMultiValueMetric.build()
                .setName("cdr_multivalue_metric")
                .setHelp("cdr data")
                .register("cdr_index");

        PMultivalueTimeSeries mvts = new PMultivalueTimeSeries();
        mvts.addValue("id", cdrBean.getId());
        mvts.addValue("callId", cdrBean.getCallid());
        mvts.addValue("sequence", cdrBean.getSequence());
        mvts.addValue("callType", cdrBean.getCallType());
        mvts.addLabel("ownerNumber", cdrBean.getOwnerNumber());
        mvts.addLabel("callingNumber", cdrBean.getCallingNumber());
        mvts.addLabel("calledNumber", cdrBean.getCalledNumber());
        mvts.addValue("cdrTimeBeforeRinging", cdrBean.getCdrTimeBeforeRinging());
        mvts.addValue("cdrRingingTimeBeforeAnsw", cdrBean.getCdrRingingTimeBeforeAnsw());
        mvts.addValue("duration", cdrBean.getDuration());
        mvts.addValue("cause", cdrBean.getCause());
        mvts.addLabel("causeString", (String) releaseCausesProps.getOrDefault(cdrBean.getCause() + "", "unknown"));
        mvts.addValue("callReleasingSide", cdrBean.getCallReleasingSide());
        mvts.addLabel("startTime", Utils.toDateString(cdrBean.getStartTime()));
        mvts.addLabel("endTime", Utils.toDateString(cdrBean.getEndTime()));
        mvts.addValue("cacType", cdrBean.getCacType());
        mvts.addValue("cacPrefix", cdrBean.getCacPrefix());
        mvts.addValue("cacNumber", cdrBean.getCacNumber());
        mvts.addValue("inTrunkId", cdrBean.getInTrunkId());
        mvts.addValue("inTrunkGroupId", cdrBean.getInTrunkGroupId());
        mvts.addValue("outTrunkId", cdrBean.getOutTrunkId());
        mvts.addValue("outTrunkGroupId", cdrBean.getOutTrunkGroupId());
        mvts.addLabel("inTrunkGroupName", cdrBean.getInTrunkGroupNameIE144());
        mvts.addLabel("outTrunkGroupName", cdrBean.getOutTrunkGroupNameIE145());
        mvts.addValue("servId", cdrBean.getServId());
        mvts.addValue("servIdOrig", cdrBean.getServIdOrig());
        mvts.addValue("servIdTerm", cdrBean.getServIdTerm());
        mvts.addValue("ctxCall", cdrBean.getCtxCall());
        mvts.addLabel("ctxCallingNumber", cdrBean.getCtxCallingNumber());
        mvts.addLabel("ctxCalledNumber", cdrBean.getCtxCalledNumber());
        mvts.addValue("bgidOrig", cdrBean.getBgidOrig());
        mvts.addValue("bgidTerm", cdrBean.getBgidTerm());
        mvts.addLabel("nodeId", cdrBean.getNodeId());

        mv.setTimestamp(cdrBean.getStartTime().getTime());

        return mv;
    }

}
