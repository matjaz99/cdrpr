package si.iskratel.cdr.parser;

import java.util.*;

public class CdrBean {

  protected String nodeId;
  protected String causeString;

  protected int id;
  protected long callid = 0;
  protected String icid = null;
  protected int cdrType = 200; // CALL, TIME CHANGED ON CS, CS RESTART
  protected short recordSubType = 0; // CDR of type CALL has following subtype: CALL, FAU, FAIS
  protected int sequence = 0;
  protected String ownerNumber = "";
  protected String callingNumber = "";
  protected String calledNumber = "";
  protected String redirectingNumber = null;
  protected String connectedNumber = null;
  protected int callingNumberCLIR = 0;
  protected Date startTime;
  protected Date endTime;
  protected long chgUnits = 0;
  protected Integer cdrTimeBeforeRinging = null;
  protected Integer cdrRingingTimeBeforeAnsw = null;
  protected long duration = 0;
  protected int cause = 0;
  protected double price = 0;
  protected short servIdOrig = 0;
  protected short servIdTerm = 0;
  protected short servId = 0;
  protected boolean servSucc = false;
  protected Integer supplementaryInputType = null;
  protected int bgidOrig = 0;
  protected int bgidTerm = 0;
  protected int cgidOrig = 0;
  protected int cgidTerm = 0;
  protected int centrexCallType = 0;
  protected int ctxCall = 0;
  protected byte[] binaryRecord;
  protected String ctxCallingNumber = null;
  protected String ctxCalledNumber = null;
  protected String ctxRedirectingNumber = null;
  protected Integer inTrunkGroupId = null;
  protected Integer inTrunkId = null;
  protected Integer inTrunkIdIE144 = null;
  protected String inTrunkGroupNameIE144 = null;
  protected Integer outTrunkGroupId = null;
  protected Integer outTrunkId = null;
  protected Integer outTrunkIdIE145 = null;
  protected String outTrunkGroupNameIE145 = null;
  protected Integer callType = null;
  protected Integer callingSubscriberGroup = null;
  protected Integer calledSubscriberGroup = null;
  protected Integer origSideSndLineType = null;
  protected Integer termSideSndLineType = null;
  protected Integer callReleasingSide = null;
  protected Integer cacNumber = null;
  protected Integer cacPrefix = null;
  protected Integer cacType = null;
  protected Integer voipRxCodecType = null;
  protected Integer voipTxCodecType = null;
  protected Integer voipSide = null;
  protected Integer voipCallType = null;
  protected Long voipRxPackets = null;
  protected Long voipTxPackets = null;
  protected Long voipRxOctets = null;
  protected Long voipTxOctets = null;
  protected Long voipPacketsLost = null;
  protected Integer voipAverageJitter = null;
  protected Integer voipAverageLatency = null;
  protected Integer voipEchoReturnLoss = null;
  protected Long voipPacketsSentAndLost = null;
  protected Integer voipMaxPacketsLostInBurst = null;
  protected Integer voipMaxJitter = null;
  protected Integer voipMinJitter = null;
  protected Integer voipRxMos = null;
  protected Integer voipTxMos = null;
  protected Integer voipFaxModulationType = null;
  protected Integer voipFaxTransferRate = null;
  protected Integer voipFaxModemRetrains = null;
  protected Integer voipFaxPagesTransferred = null;
  protected Integer voipFaxPagesRepeated = null;

  public CdrBean() {
  }

  public CdrBean(String nodeId, String cause, int duration) {
    this.nodeId = nodeId;
    this.causeString = cause;
    this.duration = duration;
  }

  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  public String getCauseString() {
    return causeString;
  }

  public void setCauseString(String causeString) {
    this.causeString = causeString;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public long getCallid() {
    return callid;
  }

  public void setCallid(long callid) {
    this.callid = callid;
  }

  public Integer getCallType() {
    return callType;
  }

  public void setCallType(Integer callType) {
    this.callType = callType;
  }

  public String getIcid() {
    return icid;
  }

  public void setIcid(String icid) {
    this.icid = icid;
  }

  public short getRecordSubType() {
    return recordSubType;
  }

  public void setRecordSubType(short serviceType) {
    this.recordSubType = serviceType;
  }

  public int getSequence() {
    return sequence;
  }

  public void setSequence(int sequence) {
    this.sequence = sequence;
  }

  public String getOwnerNumber() {
    return ownerNumber;
  }

  public void setOwnerNumber(String ownerNumber) {
    this.ownerNumber = ownerNumber;
  }

  public String getCallingNumber() {
    return callingNumber;
  }

  public void setCallingNumber(String callingNumber) {
    this.callingNumber = callingNumber;
  }

  public String getCalledNumber() {
    return calledNumber;
  }

  public void setCalledNumber(String calledNumber) {
    this.calledNumber = calledNumber;
  }

  public String getRedirectingNumber() {
    return redirectingNumber;
  }

  public void setRedirectingNumber(String redirectingNumber) {
    this.redirectingNumber = redirectingNumber;
  }

  public String getConnectedNumber() {
    return connectedNumber;
  }

  public void setConnectedNumber(String connectedNumber) {
    this.connectedNumber = connectedNumber;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startdatetime) {
    this.startTime = startdatetime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date enddatetime) {
    this.endTime = enddatetime;
  }

  public long getChgUnits() {
    return chgUnits;

  }

  public void setChgUnits(long chargingUnits) {
    this.chgUnits = chargingUnits;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public int getCause() {
    return cause;
  }

  public void setCause(int releaseCause) {
    this.cause = releaseCause;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public short getServIdOrig() {
    return servIdOrig;
  }

  public void setServIdOrig(short serviceIdOrig) {
    this.servIdOrig = serviceIdOrig;
  }

  public short getServIdTerm() {
    return servIdTerm;
  }

  public void setServIdTerm(short serviceIdTerm) {
    this.servIdTerm = serviceIdTerm;
  }

  public short getServId() {
    return servId;
  }

  public void setServId(short servId) {
    this.servId = servId;
  }

  public boolean isServSucc() {
    return servSucc;
  }

  public void setServSucc(boolean servSucc) {
    this.servSucc = servSucc;
  }

  public Integer getSupplementaryInputType() {
    return supplementaryInputType;
  }

  public void setSupplementaryInputType(Integer suppInputType) {
    this.supplementaryInputType = suppInputType;
  }

  public int getBgidOrig() {
    return bgidOrig;
  }

  public void setBgidOrig(int bgidOrig) {
    this.bgidOrig = bgidOrig;
  }

  public int getBgidTerm() {
    return bgidTerm;
  }

  public void setBgidTerm(int bgidTerm) {
    this.bgidTerm = bgidTerm;
  }
  
  public int getCgidOrig() {
    return cgidOrig;
  }

  public void setCgidOrig(int cgidOrig) {
    this.cgidOrig = cgidOrig;
  }

  public int getCgidTerm() {
    return cgidTerm;
  }

  public void setCgidTerm(int cgidTerm) {
    this.cgidTerm = cgidTerm;
  }

  public int getCentrexCallType() {
    return centrexCallType;
  }

  public void setCentrexCallType(int centrexCallType) {
    this.centrexCallType = centrexCallType;
  }

  public int getCtxCall() {
    return ctxCall;
  }

  public void setCtxCall(int centrexCall) {
    this.ctxCall = centrexCall;
  }

  public byte[] getBinaryRecord() {
    return binaryRecord;
  }

  public void setBinaryRecord(byte[] binaryRecord) {
    this.binaryRecord = binaryRecord;
  }

  public int getCdrType() {
    return cdrType;
  }

  public void setCdrType(int cdrType) {
    this.cdrType = cdrType;
  }

  public String getCtxCallingNumber() {
    return ctxCallingNumber;
  }

  public void setCtxCallingNumber(String ctxCallingNumber) {
    this.ctxCallingNumber = ctxCallingNumber;
  }

  public String getCtxCalledNumber() {
    return ctxCalledNumber;
  }

  public void setCtxCalledNumber(String ctxCalledNumber) {
    this.ctxCalledNumber = ctxCalledNumber;
  }

  public String getCtxRedirectingNumber() {
    return ctxRedirectingNumber;
  }

  public void setCtxRedirectingNumber(String ctxRedirectingNumber) {
    this.ctxRedirectingNumber = ctxRedirectingNumber;
  }

  public Integer getInTrunkGroupId() {
    return inTrunkGroupId;
  }

  public void setInTrunkGroupId(Integer trunkInId) {
    this.inTrunkGroupId = trunkInId;
  }

  public Integer getInTrunkId() {
    return inTrunkId;
  }

  public void setInTrunkId(Integer inTrunkId) {
    this.inTrunkId = inTrunkId;
  }

  public Integer getInTrunkIdIE144() {
    return inTrunkIdIE144;
  }

  public void setInTrunkIdIE144(Integer inTrunkIdIE144) {
    this.inTrunkIdIE144 = inTrunkIdIE144;
  }

  public String getInTrunkGroupNameIE144() {
    return inTrunkGroupNameIE144;
  }

  public void setInTrunkGroupNameIE144(String inTrunkName) {
    this.inTrunkGroupNameIE144 = inTrunkName;
  }

  public Integer getOutTrunkGroupId() {
    return outTrunkGroupId;
  }

  public void setOutTrunkGroupId(Integer outTrunkGroupId) {
    this.outTrunkGroupId = outTrunkGroupId;
  }

  public Integer getOutTrunkId() {
    return outTrunkId;
  }

  public void setOutTrunkId(Integer outTrunkId) {
    this.outTrunkId = outTrunkId;
  }

  public Integer getOutTrunkIdIE145() {
    return outTrunkIdIE145;
  }

  public void setOutTrunkIdIE145(Integer outTrunkIdIE144) {
    this.outTrunkIdIE145 = outTrunkIdIE144;
  }

  public String getOutTrunkGroupNameIE145() {
    return outTrunkGroupNameIE145;
  }

  public void setOutTrunkGroupNameIE145(String outTrunkName) {
    this.outTrunkGroupNameIE145 = outTrunkName;
  }

  public int getCallingNumberCLIR() {
    return callingNumberCLIR;
  }

  public void setCallingNumberCLIR(int callingNumberCLIR) {
    this.callingNumberCLIR = callingNumberCLIR;
  }

  public Integer getCallingSubscriberGroup() {
    return callingSubscriberGroup;
  }

  public void setCallingSubscriberGroup(Integer callingSubscriberGroup) {
    this.callingSubscriberGroup = callingSubscriberGroup;
  }

  public Integer getCalledSubscriberGroup() {
    return calledSubscriberGroup;
  }

  public void setCalledSubscriberGroup(Integer calledSubscriberGroup) {
    this.calledSubscriberGroup = calledSubscriberGroup;
  }

  public Integer getOrigSideSndLineType() {
    return origSideSndLineType;
  }

  public void setOrigSideSndLineType(Integer origSideSndLineType) {
    this.origSideSndLineType = origSideSndLineType;
  }

  public Integer getTermSideSndLineType() {
    return termSideSndLineType;
  }

  public void setTermSideSndLineType(Integer termSideSndLineType) {
    this.termSideSndLineType = termSideSndLineType;
  }

  public Integer getCallReleasingSide() {
    return callReleasingSide;
  }

  public void setCallReleasingSide(Integer callReleasingSide) {
    this.callReleasingSide = callReleasingSide;
  }
  
  public Integer getCdrRingingTimeBeforeAnsw() {
    return cdrRingingTimeBeforeAnsw;
  }

  public void setCdrRingingTimeBeforeAnsw(Integer cdrRingingTimeBeforeAnsw) {
    this.cdrRingingTimeBeforeAnsw = cdrRingingTimeBeforeAnsw;
  }

  public Integer getCdrTimeBeforeRinging() {
    return cdrTimeBeforeRinging;
  }

  public void setCdrTimeBeforeRinging(Integer cdrTimeBeforeRinging) {
    this.cdrTimeBeforeRinging = cdrTimeBeforeRinging;
  }
  
  public Integer getCdrCacNumber() {
    return this.cacNumber;
  }
  
  public void setCdrCacNumber(Integer cdrCacNumber) {
    this.cacNumber = cdrCacNumber;
  }

  public Integer getCacPrefix() {
    return cacPrefix;
  }

  public void setCacPrefix(Integer cacPrefix) {
    this.cacPrefix = cacPrefix;
  }

  public Integer getCacType() {
    return cacType;
  }

  public void setCacType(Integer cacType) {
    this.cacType = cacType;
  }

  public Integer getCacNumber() {
    return cacNumber;
  }

  public void setCacNumber(Integer cacNumber) {
    this.cacNumber = cacNumber;
  }

  public Integer getVoipRxCodecType() {
    return voipRxCodecType;
  }

  public void setVoipRxCodecType(Integer voipRxCodecType) {
    this.voipRxCodecType = voipRxCodecType;
  }

  public Integer getVoipTxCodecType() {
    return voipTxCodecType;
  }

  public void setVoipTxCodecType(Integer voipTxCodecType) {
    this.voipTxCodecType = voipTxCodecType;
  }

  public Integer getVoipSide() {
    return voipSide;
  }

  public void setVoipSide(Integer voipSide) {
    this.voipSide = voipSide;
  }

  public Integer getVoipCallType() {
    return voipCallType;
  }

  public void setVoipCallType(Integer voipCallType) {
    this.voipCallType = voipCallType;
  }

  public Long getVoipRxPackets() {
    return voipRxPackets;
  }

  public void setVoipRxPackets(Long voipRxPackets) {
    this.voipRxPackets = voipRxPackets;
  }

  public Long getVoipTxPackets() {
    return voipTxPackets;
  }

  public void setVoipTxPackets(Long voipTxPackets) {
    this.voipTxPackets = voipTxPackets;
  }

  public Long getVoipRxOctets() {
    return voipRxOctets;
  }

  public void setVoipRxOctets(Long voipRxOctets) {
    this.voipRxOctets = voipRxOctets;
  }

  public Long getVoipTxOctets() {
    return voipTxOctets;
  }

  public void setVoipTxOctets(Long voipTxOctets) {
    this.voipTxOctets = voipTxOctets;
  }

  public Long getVoipPacketsLost() {
    return voipPacketsLost;
  }

  public void setVoipPacketsLost(Long voipPacketsLost) {
    this.voipPacketsLost = voipPacketsLost;
  }

  public Integer getVoipAverageJitter() {
    return voipAverageJitter;
  }

  public void setVoipAverageJitter(Integer voipAverageJitter) {
    this.voipAverageJitter = voipAverageJitter;
  }

  public Integer getVoipAverageLatency() {
    return voipAverageLatency;
  }

  public void setVoipAverageLatency(Integer voipAverageLatency) {
    this.voipAverageLatency = voipAverageLatency;
  }

  public Integer getVoipEchoReturnLoss() {
    return voipEchoReturnLoss;
  }

  public void setVoipEchoReturnLoss(Integer voipEchoReturnLoss) {
    this.voipEchoReturnLoss = voipEchoReturnLoss;
  }

  public Long getVoipPacketsSentAndLost() {
    return voipPacketsSentAndLost;
  }

  public void setVoipPacketsSentAndLost(Long voipPacketsSentAndLost) {
    this.voipPacketsSentAndLost = voipPacketsSentAndLost;
  }

  public Integer getVoipMaxPacketsLostInBurst() {
    return voipMaxPacketsLostInBurst;
  }

  public void setVoipMaxPacketsLostInBurst(Integer voipMaxPacketsLostInBurst) {
    this.voipMaxPacketsLostInBurst = voipMaxPacketsLostInBurst;
  }

  public Integer getVoipMaxJitter() {
    return voipMaxJitter;
  }

  public void setVoipMaxJitter(Integer voipMaxJitter) {
    this.voipMaxJitter = voipMaxJitter;
  }

  public Integer getVoipMinJitter() {
    return voipMinJitter;
  }

  public void setVoipMinJitter(Integer voipMinJitter) {
    this.voipMinJitter = voipMinJitter;
  }

  public Integer getVoipRxMos() {
    return voipRxMos;
  }

  public void setVoipRxMos(Integer voipRxMos) {
    this.voipRxMos = voipRxMos;
  }

  public Integer getVoipTxMos() {
    return voipTxMos;
  }

  public void setVoipTxMos(Integer voipTxMos) {
    this.voipTxMos = voipTxMos;
  }

  public Integer getVoipFaxModulationType() {
    return voipFaxModulationType;
  }

  public void setVoipFaxModulationType(Integer voipFaxModulationType) {
    this.voipFaxModulationType = voipFaxModulationType;
  }

  public Integer getVoipFaxTransferRate() {
    return voipFaxTransferRate;
  }

  public void setVoipFaxTransferRate(Integer voipFaxTransferRate) {
    this.voipFaxTransferRate = voipFaxTransferRate;
  }

  public Integer getVoipFaxModemRetrains() {
    return voipFaxModemRetrains;
  }

  public void setVoipFaxModemRetrains(Integer voipFaxModemRetrains) {
    this.voipFaxModemRetrains = voipFaxModemRetrains;
  }

  public Integer getVoipFaxPagesTransferred() {
    return voipFaxPagesTransferred;
  }

  public void setVoipFaxPagesTransferred(Integer voipFaxPagesTransferred) {
    this.voipFaxPagesTransferred = voipFaxPagesTransferred;
  }

  public Integer getVoipFaxPagesRepeated() {
    return voipFaxPagesRepeated;
  }

  public void setVoipFaxPagesRepeated(Integer voipFaxPagesRepeated) {
    this.voipFaxPagesRepeated = voipFaxPagesRepeated;
  }

  // TODO print binary record
  @Override
  public String toString() {
    return "CdrBean [id=" + id + ", callid=" + callid + ", icid=" + icid + ", cdrType=" + cdrType + ", recordSubType=" + recordSubType
        + ", sequence=" + sequence + ", ownerNumber=" + ownerNumber + ", callingNumber=" + callingNumber + ", calledNumber=" + calledNumber
        + ", redirectingNumber=" + redirectingNumber + ", connectedNumber=" + connectedNumber + ", callingNumberCLIR=" + callingNumberCLIR
        + ", startTime=" + startTime + ", endTime=" + endTime + ", chgUnits=" + chgUnits + ", cdrTimeBeforeRinging=" + cdrTimeBeforeRinging
        + ", cdrRingingTimeBeforeAnsw=" + cdrRingingTimeBeforeAnsw + ", duration=" + duration + ", cause=" + cause + ", price=" + price
        + ", servIdOrig=" + servIdOrig + ", servIdTerm=" + servIdTerm + ", servId=" + servId + ", servSucc=" + servSucc
        + ", supplementaryInputType=" + supplementaryInputType + ", bgidOrig=" + bgidOrig + ", bgidTerm=" + bgidTerm + ", cgidOrig="
        + cgidOrig + ", cgidTerm=" + cgidTerm + ", centrexCallType=" + centrexCallType + ", ctxCall=" + ctxCall + ", ctxCallingNumber="
        + ctxCallingNumber + ", ctxCalledNumber=" + ctxCalledNumber + ", ctxRedirectingNumber=" + ctxRedirectingNumber + ", inTrunkGroupId="
        + inTrunkGroupId + ", inTrunkId=" + inTrunkId + ", inTrunkIdIE144=" + inTrunkIdIE144 + ", inTrunkGroupNameIE144="
        + inTrunkGroupNameIE144 + ", outTrunkGroupId=" + outTrunkGroupId + ", outTrunkId=" + outTrunkId + ", outTrunkIdIE145="
        + outTrunkIdIE145 + ", outTrunkGroupNameIE145=" + outTrunkGroupNameIE145 + ", callType=" + callType + ", callingSubscriberGroup="
        + callingSubscriberGroup + ", calledSubscriberGroup=" + calledSubscriberGroup + ", origSideSndLineType=" + origSideSndLineType
        + ", termSideSndLineType=" + termSideSndLineType + ", callReleasingSide=" + callReleasingSide + ", cacNumber=" + cacNumber
        + ", cacPrefix=" + cacPrefix + ", cacType=" + cacType + ", voipRxCodecType=" + voipRxCodecType + ", voipTxCodecType="
        + voipTxCodecType + ", voipSide=" + voipSide + ", voipCallType=" + voipCallType + ", voipRxPackets=" + voipRxPackets
        + ", voipTxPackets=" + voipTxPackets + ", voipRxOctets=" + voipRxOctets + ", voipTxOctets=" + voipTxOctets + ", voipPacketsLost="
        + voipPacketsLost + ", voipAverageJitter=" + voipAverageJitter + ", voipAverageLatency=" + voipAverageLatency
        + ", voipEchoReturnLoss=" + voipEchoReturnLoss + ", voipPacketsSentAndLost=" + voipPacketsSentAndLost
        + ", voipMaxPacketsLostInBurst=" + voipMaxPacketsLostInBurst + ", voipMaxJitter=" + voipMaxJitter + ", voipMinJitter="
        + voipMinJitter + ", voipRxMos=" + voipRxMos + ", voipTxMos=" + voipTxMos + ", voipFaxTransferRate=" + voipFaxTransferRate
        + ", voipFaxModemRetrains=" + voipFaxModemRetrains + ", voipFaxPagesTransferred=" + voipFaxPagesTransferred
        + ", voipFaxPagesRepeated=" + voipFaxPagesRepeated + "]";
  }

}