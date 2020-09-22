package si.iskratel.simulator;

import si.iskratel.metricslib.EsClient;
import si.iskratel.metricslib.PMetric;
import si.iskratel.metricslib.PMetricRegistry;

import java.util.Random;

public class XmlSimulatorThread extends Thread {

    private String[] measurements = {"SC.AttSessions.orig", "IT.SUBS.OrigIncmpl", "SC.FailSession.404", "IT.SUBS.OrgUnsuccOther", "IT.SUBS.OrgOut", "SC.FailSession.486.orig", "SC.RelAfterRing.orig", "SC.AnsSession.orig", "IT.SUBS.OrgNoTrunk", "IT.SUBS.OrgTraffic", "SC.AttSessions.term", "IT.SUBSTermIncom", "SC.FailSession.486.term", "SC.RelAfterRing.term", "SC.AnsSession.term", "IT.SUBS.TermUnsuccOther", "IT.SUBS.TermTraffic", "IT.SUBS.OrgOutIncmpl", "IT.SUBS.OrgOutWrongDN", "IT.SUBS.OrgOutUnsuccOther", "IT.SUBS.OrgOutTermBussy", "IT.SUBS.OrgOutTermNoAnswer", "IT.SUBS.OrgOutTermAnswer", "IT.SUBS.TermIncTermBusy", "IT.SUBS.TermIncNoAnswer", "IT.SUBS.TermIncAnswer", "IT.SUBS.TermIncUnsuccOther", "IT.TG.IncCallAtt", "IT.TG.IncIncmpl", "IT.TG.IncWrongDN", "IT.TG.IncUnsuccOther", "IT.TG.IncOut", "IT.TG.IncTermBusy", "IT.TG.IncTermNoAnswer", "IT.TG.IncAnswer", "IT.TG.IncNoTrunk", "IT.TG.IncTraffic", "IT.TG.OutCallAtt", "IT.TG.OutInc", "IT.TG.OutTermBusy", "IT.TG.OutTermNoAnswer", "IT.TG.OutAnswer", "IT.TG.OutUnsuccOther", "IT.TG.OutNoTrunk", "IT.TG.OutTraffic", "IT.TG.IncOutIncmpl", "IT.TG.IncOutWrongDN", "IT.TG.IncOutUnsuccOther", "IT.TG.IncOut.TermBusy", "IT.TG.IncOut.TermNoAnswer", "IT.TG.IncOut.Answer", "IT.TG.OutIncTermBusy", "IT.TG.OutIncTermNoAnswer", "IT.TG.OutIncAnswer", "IT.TG.OutIncUsuccOther", "IT.TG.OutIncNoTrunk", "IT.T.IncCallAtt", "IT.T.IncIncmpl", "IT.T.IncWrongDN", "IT.T.IncUnsuccOther", "IT.T.IncOut", "IT.T.IncTermBusy", "IT.T.IncTermNoAnswer", "IT.T.IncAnswer", "IT.T.IncNoTrunk", "IT.T.IncTraffic", "IT.T.OutCallAtt", "IT.T.OutInc", "IT.T.OutTermBusy", "IT.T.OutTermNoAnswer", "IT.T.OutAnswer", "IT.T.OutUnsuccOther", "IT.T.OutNoTrunk", "IT.T.OutTraffic", "IT.T.IncOutIncmpl", "IT.T.IncOutWrongDN", "IT.T.IncOutUnsuccOther", "IT.T.IncOutTermBusy", "IT.T.IncOutTermNoAnswer", "IT.T.IncOutTermAnswer", "IT.T.OutIncTermBusy", "IT.T.OutIncTermNoAnswer", "IT.T.OutIncAnswer", "IT.T.OutIncUsucc", "IT.T.OutIncNoTrunk", "IT.RouteCallAtt", "IT.RouteIncom", "IT.RouteUnsuccOther", "IT.RouteIncUnsuccOther", "IT.RouteTermBusy", "IT.RouteIncTermBusy", "IT.RouteTermNoAnswer", "IT.RouteIncTermNoAnswer", "IT.RouteAnswer", "IT.RouteIncAnswer", "IT.RouteNoTrunk", "IT.RouteIncNoTrunk", "IT.DTMF.SuRecReq", "IT.DTMF.SuRecReqSucc", "IT.DTMF.SuRecTraffic", "IT.DTMF.SuGenReq", "IT.DTMF.SuGenReqSucc", "IT.DTMF.SuGenTraffic", "IT.R2.SuRecReq", "IT.R2.SuRecReqSucc", "IT.R2.SuRecTraffic", "IT.R2.SuGenReq", "IT.R2.SuGenReqSucc", "IT.R2.SuGenTraffic", "IT.PT.SuRecReq", "IT.PT.SuRecReqSucc", "IT.PT.SuRecTraffic", "IT.PT.SuGenReq", "IT.PT.SuGenReqSucc", "IT.PT.SuGenTraffic", "IT.SND.SuRecReq", "IT.SND.SuRecReqSucc", "IT.SND.SuRecTraffic", "IT.SND.SuGenReq", "IT.SND.SuGenReqSucc", "IT.SND.SuGenTraffic", "IT.AON.SuRecReq", "IT.AON.SuRecReqSucc", "IT.AON.SuRecTraffic", "IT.AON.SuGenReq", "IT.AON.SuGenReqSucc", "IT.AON.SuGenTraffic", "IT.R1.SuRecReq", "IT.R1.SuRecReqSucc", "IT.R1.SuRecTraffic", "IT.R1.SuGenReq", "IT.R1.SuGenReqSucc", "IT.R1.SuGenTraffic", "IT.CTXOrgCallAtt", "IT.CTXOrgExt", "IT.CTXOrgRemIntCall", "IT.CTXOrgIncmpl", "IT .CTXOrgExtIncmpl", "IT.CTXOrgWrongDN", "IT.CTXOrgExtWrongDN", "IT. CTXOrgRstrUnsucc", "IT.CTXOrgUnsuccOther", "IT.CTXOrgExtUnsucc Other", "IT.CTXOrgTermBusy", "IT.CTXOrgExtTermBusy", "IT.CTXOrgTermNoAnswer", "IT.CTXOrgExtTermNoAnswer", "IT.CTXOrgAnswer", "IT. CTXOrgExtAnswer", "IT.CTXTermCallAtt", "IT.CTXTermExt ", "IT.CTXTermRemIntCall", "IT.CTXTermRstrUnsucc", "IT.CTXTermUnsuccOther", "IT.CTXTermExtUnsuccOther", "IT.CTXTermTermBusy", "IT.CTXTermExtTermBusy", "IT.CTXTermTermNoAnswer", "IT.CTXTermExtTermNoAnswer", "IT.CTXTermAnswer", "IT.CTXTermExtAnswer ", "IT.PfxCallAtt", "IT.PfxIncCall", "IT.PfxTermBusy", "IT.PfxIncTermBusy", "IT.PfxTermNoAnswer", "IT.PfxIncTermNoanswer", "IT.PfxUnsuccOther", "IT.PfxIncUsuccOther", "IT.PfxAnswer", "IT.PfxIncAnswer", "IT.PfxIncmpl", "IT.PfxIncIncmpl", "IT.PfxWrongDN", "IT.PfxIncWrongDN", "IT.PfxNoTrunk ", "IT.PfxIncNoTrunk", "IT.CACCallAtt", "IT.CACIncCall", "IT.CACTermBusy", "IT.CACIncTermBusy", "IT.CACTermNoAnswer", "IT.CACIncTermNoAnswer", "IT.CACUnsuccOther", "IT.CACIncUsucc", "IT.CACAnswer", "IT.CACIncAnswer", "IT.CACIncmpl", "IT.CACIncIncmpl", "IT.CACWrongDN", "IT.CACIncWrongDN", "IT.CACNoTrunk ", "IT.CACIncNiTrunk", "IT.CACNotUsed", "IT.SS7_SL_InService", "IT.SS7_SL_Failure", "IT.SS7_SL_SUINError", "IT.SS7_SL_Unavailable ", "IT.SS7_SL_SIFSIOTrans", "IT.SS7_SL_SIFSIORec", "IT.SS7_SL_MSUDiscard", "IT.SS7_SP_Inaccessible", "IT.SS7_SP_InaccDurat", "IT.SS7_SP_MSUDiscard", "IT.SS7_ISUP_MsgSent", "IT.SS7_ISUP_MsgSent", "IT.SS7_SCCP_NoNtrTransl", "IT.SS7_SCCP_NoAdrTransl", "IT.SS7_SCCP_NWFail", "IT.SS7_SCCP_NWCongest", "IT.SS7_SCCP_SubSistFail", "IT.SS7_SCCP_Unequipped", "IT.SS7_SCCP_SyntaxError", "IT.SS7_SCCP_Unknown", "IT.SS7_SCCP_MsgOrigin", "IT.SS7_SCCP_MsgReceived ", "IT.SS7_SCCP_MsgBkpSS", "IT.SS7_TCAP_MsgSent", "IT.SS7_TCAP_MsgReceived", "IT.SS7_TCAP_UnrecogMsg", "IT.SS7_TCAP_IncorrectTP", "IT.SS7_TCAP_BadlyFormat", "IT.SS7_TCAP_UnrecogTID", "IT.SS7_TCAP_UnrecogComp", "IT.SS7_TCAP_MistypeComp", "IT.SS7_TCAP_BadlyComp", "IT.AudioTraffic", "IT.FaxTraffic", "IT.DataTraffic", "IT.CodTypeG711Traffic", "IT.CodTypeG723Traffic ", "IT.CodTypeG729Traffic", "IT.CodTypeFaxT38Traffic", "IT.VoIPCalls", "IT.IPFaxCalls ", "IT.IPModemCalls ", "IT.CodTypeG711Calls", "IT.CodTypeG723Calls", "IT.CodTypeG729Calls ", "IT.CodTypeT38Calls", "IT.CallsUnderDur1", "IT.CallsDur1toDur2 ", "IT.CallsDur2toDur3", "IT.CallsOverDur3", "IT.RxPacketCount", "IT.TxPacketCount", "IT.RxOctets", "IT.TxOctets", "IT.SilencePacketCount", "IT.IdlePacketCount", "IT.DTMFRxPackets", "IT.DTMFTxPackets", "IT.LostPacketsCount", "IT.PktLostByNetwork", "IT.DroppedPacketCount", "IT.ReplayPacketCount", "IT.AvgPlayoutDelay", "IT.AvgFrameJitter", "IT.NoOfMeasuredCalls", "IT.M3UAGen_RoutFailures ", "IT.M3UAGen_AppUnreachable", "IT.M3UAAssoc_Aspup", "IT.M3UAAssoc_Aspac", "IT.M3UAAssoc_Aspdn", "IT.M3UAAssoc_Aspia", "IT.M3UAAssoc_AspupAck", "IT.M3UAAssoc_AspacAck", "IT.M3UAAssoc_AspdnAck", "IT.M3UAAssoc_AspiaAck", "IT.M3UAAssoc_Notify ", "IT.M3UAAssoc_Daud", "IT.M3UAAssoc_Duna", "IT.M3UAAssoc_Dava", "IT.M3UAAssoc_Dupu", "IT.M3UAAssoc_DataOut ", "IT.M3UAAssoc_DataIn", "IT.M3UAAssoc_ErrorOut", "IT.M3UAAssoc_ErrorIn", "IT.M3UAAssoc_SconOut ", "IT.M3UAAssoc_SconIn", "IT.M2UAGen_RoutFailures", "IT.M2UAGen_AppUnreachable", "IT.M2UAAssoc_Aspup", "IT.M2UAAssoc_Aspac ", "IT.M2UAAssoc_Aspdn", "IT.M2UAAssoc_Aspia", "IT.M2UAAssoc_AspupAck", "IT.M2UAAssoc_AspacAck", "IT.M2UAAssoc_AspdnAck", "IT.M2UAAssoc_AspiaAck", "IT.M2UAAssoc_Notify", "IT.M2UAAssoc_DataIn", "IT.M2UAAssoc_DataOut", "IT.M2UAAssoc_ErrorOut", "IT.M2UAAssoc_ErrorIn", "IT.SCTPGen_CurrEstab", "IT.SCTPGen_ActiveEstabs", "IT.SCTPGen_PasiveEstabs", "IT.SCTPGen_Aborteds", "IT.SCTPGen_ShutDowns", "IT.SCTPGen_OutOfBlue", "IT.SCTPAssoc_ChecksumErr", "IT.SCTPAssoc_CtrlChunksOUT", "IT.SCTPAssoc_OrderChunksOUT", "IT.SCTPAssoc_UnorderChunksOUT", "IT.SCTPAssoc_CtrlChunksIN", "IT.SCTPAssoc_OrderChunksIN", "IT.SCTPAssoc_UnorderChunksIN", "IT.SCTPAssoc_FragUsrMsgs", "IT.SCTPAssoc_ReasmUsrMsgs", "IT.SCTPAssoc_SCTPPacketsOUT ", "IT.SCTPAssoc_SCTPPacketsIN", "IT.ZSLUnsuccNoSU", "IT.ZSLUnsuccAON", "IT.ZSLSuccFirstReq", "IT.ZSLSuccSecondReq ", "IT.ZSLSuccThirdReq", "IT ZSLInterrupted ", "IT.SLUnsuccNoSU", "IT.SLUnsuccAON", "IT.SLSuccFirstReq", "IT.SLSuccSecondReq", "IT.SLSuccThirdReq", "IT.SLInterrupted", "IT.IUAGen_IntfFailure", "IT.IUAGen_IntfADMUnoperational", "IT.IUAGen_StreamUnavailable", "IT.IUAAssoc_Aspup", "IT.IUAAssoc_Aspac", "IT.IUAAssoc_Aspdn", "IT.IUAAssoc_Aspia ", "IT.IUAAssoc_AspupAck", "IT.IUAAssoc_AspacAck", "IT.IUAAssoc_AspdnAck", "IT.IUAAssoc_AspiaAck", "IT.IUAAssoc_Notify", "IT.IUAAssoc_DataOut", "IT.IUAAssoc_DataIn", "IT.IUAAssoc_ErrorOut ", "IT.IUAAssoc_ErrorIn", "IT.V5UAGen_IntfFailure", "IT.V5UAGen_IntfADMUnoperational", "IT.V5UAAssoc_Aspup", "IT.V5UAAssoc_Aspac", "IT.V5UAAssoc_Aspdn ", "IT.V5UAAssoc_Aspia", "IT.V5UAAssoc_AspupAck", "IT.V5UAAssoc_AspacAck", "IT.V5UAAssoc_AspdnAck", "IT.V5UAAssoc_AspiaAck", "IT.V5UAAssoc_Notify", "IT.V5UAAssoc_DataOut", "IT.V5UAAssoc_DataIn", "IT.V5UAAssoc_ErrorOut ", "IT.V5UAAssoc_ErrorIn", "IT.V5UAAssoc_LinkStatus", "IT.V5UAAssoc_SaBitStatus", "IT.V5UAAssoc_ErrorInd", "IT.DIAMETERGeneral_UnSentMsg", "IT.DIAMETERGeneral_ConnNotFound", "IT.DIAM_CER", "IT.DIAM_CEA", "IT.DIAM_ASR", "IT.DIAM_ASA", "IT.DIAM_DPR", "IT.DIAM_DPA", "IT.DIAM_SAR", "IT.DIAM_SAA", "IT.DIAM_MAR", "IT.DIAM_MAA", "IT.DIAM_LIR", "IT.DIAM_LIA", "IT.DIAM_UAR", "IT.DIAM_UAA", "IT.DIAM_PPR", "IT.DIAM_PPA", "IT.DIAM_RTR", "IT.DIAM_RTA", "IT.DIAM_UDR ", "IT.DIAM_UDA ", "IT.DIAM_PNR", "IT.DIAM_PNA", "IT.DIAM_PUR", "IT.DIAM_PUA", "IT.DIAM_UnsuccesSent", "IT.DIAM_UnknwMsgSend", "IT.DIAM_UnknwMsgRecv", "IT.DIAM_CCR", "IT.DIAM_CCA", "IT.DIAM_ACR", "IT.DIAM_ACA", "IT.SS.3PTYInvSucc", "IT.SS.ABDSAct", "IT.SS.ABDSActSucc", "IT.SS.ABDSDeact", "IT.SS.ABDSDeactSucc", "IT.SS.ABDSInt", "IT.SS.ABDSIntSecc", "CONF.AttCreation", "IT.SS.ACSAct", "IT.SS.ACSActSucc", "IT.SS.ACSDeact", "IT.SS.ACSDeactSucc", "IT.SS.ACSInt", "IT.SS.ACSIntSucc", "IT.SS.ACSInvSucc", "IT.SS.MCEAct", "IT.SS.MCEActSucc", "SC.CDUsed", "IT.SS.CFNRAct", "IT.SS.CFNRActSucc", "IT.SS.CFNRDeact", "IT.SS.CFNRDeactSucc", "IT.SS.CFNRInt", "IT.SS.CFNRIntSucc", "SC.CFNRUsed", "IT.SS.CFBAct", "IT.SS.CFBActSucc", "IT.SS.CFBDeact", "IT.SS.CFBDeactSucc", "IT.SS.CFBInt", "IT.SS.CFBIntSucc", "SC.CFBUsed", "IT.SS.CFUAct", "IT.SS.CFUActSucc", "IT.SS.CFUDeact", "IT.SS.CFUDeactSucc", "IT.SS.CFUInt", "IT.SS.CFUIntSucc", "SC.CFUUsed", "IT.SS.CFUTAct", "IT.SS.CFUTActSucc", "IT.SS.CFUTDeact", "IT.SS.CFUTDeactSucc", "IT.SS.CFUTInt", "IT.SS.CFUTIntSucc", "SC.CFUUsed", "IT.SS.CINTInvSucc", "IT.SS.CPUInvSucc", "IT.SS.CPUPAct", "IT.SS.CPUPActSucc", "IT.SS.CPUPDeact", "IT.SS.CPUPDeactSucc", "IT.SS.CPUPInt", "IT.SS.CPUPIntSucc", "SC.ECTBlindUsed", "IT.SS.CWAct", "IT.SS.CWActSucc", "IT.SS.CWDeact", "IT.SS.CWDeactSucc", "IT.SS.CWInt", "IT.SS.CWIntSucc", "IT.SS.CAMPInvSucc", "IT.SS.CAMPPAct", "IT.SS.CAMPPActSucc", "IT.SS.CAMPPDeact", "IT.SS.CAMPPDeactSucc", "IT.SS.CAMPPInt", "IT.SS.CAMPPIntSucc", "IT.SS.CCBSAct", "IT.SS.CCBSActSucc", "IT.SS.CCBSDeact", "IT.SS.CCBSDeactSucc", "IT.SS.CCBSInt", "IT.SS.CCBSIntSucc", "SC.CCBSUsed", "IT.SS.DNDAct", "IT.SS.DNDActSucc", "IT.SS.DNDDeact", "IT.SS.DNDDeactSucc", "IT.SS.DNDInt", "IT.SS.DNDIntSucc", "IT.SS.DNDOInvSucc", "IT.SS.HOTDAct", "IT.SS.HOTDActSucc", "IT.SS.HOTDDeact", "IT.SS.HOTDDeactSucc", "IT.SS.HOTDInt", "IT.SS.HOTDIntSucc", "IT.SS.KEYAct", "IT.SS.KEYActSucc", "IT.SS.MCIDInvSucc", "IT.SS.CBSCAct", "IT.SS.CBSCActSucc", "IT.SS.CBSCDeact", "IT.SS.CBSCDeactSucc", "IT.SS.CBSCInt", "IT.SS.CBSCIntSucc", "IT.SS.CFXDAct", "IT.SS.CFXDActSucc", "IT.SS.CFXDDeact", "IT.SS.CFXDDeactSucc", "IT.SS.CFXDInt", "IT.SS.CFXDIntSucc", "IT.SS.NCAct", "IT.SS.NCActSucc", "IT.SS.NCDeact", "IT.SS.NCDeactSucc", "IT.SS.NCInt", "IT.SS.NCIntSucc", "IT.SS.NCInvSucc", "IT.SS.CFNRcAct", "IT.SS.CFNRcActSucc", "IT.SS.CFNRcDeact", "IT.SS.CFNRcDeactSucc", "IT.SS.CFNRcInt", "IT.SS.CFNRcIntSucc", "SC.CFNRcUsed", "UR.AttInitReg", "UR.SuccInitReg", "UR.AttReReg", "UR.SuccReReg", "UR.AttDeRegUe", "UR.SuccDeRegUe", "UR.AttDeRegHss", "UR.SuccDeRegHss", "UR.Att3rdPartyReg", "UR.Succ3rdPartyReg", "UR.AttUAR", "UR.SuccUAA.2001", "UR.SuccUAA.2002", "UR.AttSAR", "UR.SuccSAA", "IT.AGCFOrgCallAtt", "IT.AGCFOrgEstablCall", "IT.AGCFOrgAnswer", "IT.AGCFTermCallAtt", "IT.AGCFTermEstablCall", "IT.AGCFTermAnswer", "IT.AGCFCummDurOrgCalls ", "IT.AGCFCummDurTermCalls "};
    private String[] elementTypes = {"S-CSCF", "P-CSCF", "TAS", "HSS", "MG", "MS", "AGCF", "MGCF", "BGCF"};

    @Override
    public void run() {

        EsClient esClient = new EsClient(Start.ES_HOST, Start.ES_PORT);

        PMetric xml_metric = PMetric.build()
                .setName("pmon_xml_metric")
                .setHelp("Metric from xml")
                .setLabelNames("node", "measurement", "elementType", "measInfoId", "jobId")
                .register("pmon_xml_measurements_idx");

        PMetric inventory_metric = PMetric.build()
                .setName("pmon_cdr_inventory")
                .setHelp("Inventory of PAM-CDR module")
                .setLabelNames("nodeId", "nodeName", "productCategory", "subType", "status")
                .register("pmon_inventory_idx");

        while (true) {

            try {
                Thread.sleep(5 * 60 * 1000);
            } catch (InterruptedException e) {
            }

            PMetricRegistry.getRegistry("pmon_xml_measurements_idx").resetMetrics();

            for (int i = 0; i < measurements.length; i++) {
                xml_metric.setLabelValues(
                        Start.getRandomNodeId(),
                        measurements[i],
                        elementTypes[getRandomInRange(0, elementTypes.length - 1)],
                        "" + getRandomInRange(0, 5),
                        "" + getRandomInRange(1, 3)
                ).set(getRandomInRange(0, 5000));
            }
            esClient.sendBulkPost(xml_metric);

            String[] nodes = Start.SIMULATOR_NODEID.split(",");
            for (int i = 0; i < nodes.length; i++) {
                inventory_metric.setLabelValues("10480" + i, nodes[i], "elementType", "subType", "No CDR files found").set(1);
            }
            esClient.sendBulkPost(inventory_metric);

        }

    }

    /**
     * Return random number in range (inclusive)
     * @param min
     * @param max
     * @return
     */
    private int getRandomInRange(int min, int max) {
        // min and max are inclusive
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}
