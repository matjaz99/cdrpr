
package si.iskratel.cdr.parser;

import java.util.Date;
import java.util.Hashtable;
import si.iskratel.cdr.parser.SupplementaryService;

public class CdrObject {

  private Hashtable<String, Object> cdrProperties = new Hashtable<String, Object>();

  public CdrObject() {
    setCdrIndex(new Long(0));
    setCdrRecordType(FixedPartCdr.CDR_TYPE_CALL);
    setCdrRecordSequence(1);
    setCdrChargeUnits(1L);
    setCdrFlagF1(true); // call
    setCdrFlagF2(false); // fau
    setCdrFlagF3(false); // fais
    setCdrFlagF17(false); // ctx
  }

  public Hashtable<String, Object> getCdrProperties() {
    return cdrProperties;
  }

  public void setCdrProperties(Hashtable<String, Object> cdrProperties) {
    this.cdrProperties = cdrProperties;
  }

  public Object getProperty(String aPropertyName) {
    return cdrProperties.get(aPropertyName);
  }

  public void setProperty(String aPropertyName, Object aProperty) {
    cdrProperties.put(aPropertyName, aProperty);
  }

  public void clearAllProperties() {
    cdrProperties.clear();
  }

  public void replaceProperty(String aPropertyName, Object aProperty) {
    cdrProperties.remove(aPropertyName);
    cdrProperties.put(aPropertyName, aProperty);
  }

  public boolean containKey(String key) {
    return (cdrProperties.containsKey(key));
  }

  public boolean isEmpty() {
    return this.cdrProperties.isEmpty();
  }

  // -------------------------------------------------------------------------------------------------
  public void setCdrBytes(byte[] cdrBytes) {
    setProperty(CdrProperty.CDR_BYTES, cdrBytes);
  }

  public byte[] getCdrBytes() {
    return (byte[]) getProperty(CdrProperty.CDR_BYTES);
  }

  public void setCdrIndex(Long cdrIndex) {
    setProperty(CdrProperty.CDR_INDEX, cdrIndex);
  }

  public Long getCdrIndex() {
    return (Long) getProperty(CdrProperty.CDR_INDEX);
  }

  public void setCdrCallId(Long cdrCallId) {
    setProperty(CdrProperty.CALL_ID, cdrCallId);
  }

  public Long getCdrCallId() {
    return (Long) getProperty(CdrProperty.CALL_ID);
  }

  public void setCdrOrigCallId(Long cdrOrigCallId) {
    setProperty(CdrProperty.ORIG_CALL_ID, cdrOrigCallId);
  }

  public Long getCdrOrigCallId() {
    return (Long) getProperty(CdrProperty.ORIG_CALL_ID);
  }

  public void setCdrIcid(String cdrIcid) {
    setProperty(CdrProperty.ICID, cdrIcid);
  }

  public String getCdrIcid() {
    return (String) getProperty(CdrProperty.ICID);
  }

  public void setCdrIcidHostname(String cdrIcidHostname) {
    setProperty(CdrProperty.ICID_HOSTNAME, cdrIcidHostname);
  }

  public String getCdrIcidHostname() {
    return (String) getProperty(CdrProperty.ICID_HOSTNAME);
  }

  public void setCdrRecordSequence(Integer cdrRrecordSequence) {
    setProperty(CdrProperty.RECORD_SEQUENCE, cdrRrecordSequence);
  }

  public Integer getCdrRecordSequence() {
    return (Integer) getProperty(CdrProperty.RECORD_SEQUENCE);
  }

  public void setCdrRecordType(Integer cdrRecordType) {
    setProperty(CdrProperty.RECORD_TYPE, cdrRecordType);
  }

  public Integer getCdrRecordType() {
    return (Integer) getProperty(CdrProperty.RECORD_TYPE);
  }

  public void setCdrGlobalCallReference(GlobalCallReference cdrGlobalCallReference) {
    setProperty(CdrProperty.GLOBAL_CALL_REFERENCE, cdrGlobalCallReference);
  }

  public GlobalCallReference getCdrGlobalCallReference() {
    return (GlobalCallReference) getProperty(CdrProperty.GLOBAL_CALL_REFERENCE);
  }

  public void setCdrOriginalCpn(PartyNumber cdrOriginalCpn) {
    setProperty(CdrProperty.ORIGINAL_CPN, cdrOriginalCpn);
  }

  public PartyNumber getCdrOriginalCpn() {
    return (PartyNumber) getProperty(CdrProperty.ORIGINAL_CPN);
  }

  public void setCdrOwnerNumber(String cdrOwnerNumber) {
    setProperty(CdrProperty.OWNER_NUMBER, cdrOwnerNumber);
  }

  public String getCdrOwnerNumber() {
    return (String) getProperty(CdrProperty.OWNER_NUMBER);
  }

  public void setCdrCalledNumber(String cdrCalledNumber) {
    setProperty(CdrProperty.CALLED_NUMBER, cdrCalledNumber);
  }

  public String getCdrCalledNumber() {
    return (String) getProperty(CdrProperty.CALLED_NUMBER);
  }

  public void setCdrCalledNumberFormated(PartyNumber cdrCalledNumberFormated) {
    setProperty(CdrProperty.CALLED_NUMBER_FORMATTED, cdrCalledNumberFormated);
  }

  public PartyNumber getCdrCalledNumberFormated() {
    return (PartyNumber) getProperty(CdrProperty.CALLED_NUMBER_FORMATTED);
  }

  public void setCdrRedirectingNumber(PartyNumber cdrRedirectingNumber) {
    setProperty(CdrProperty.REDIRECTING_NUMBER, cdrRedirectingNumber);
  }

  public PartyNumber getCdrRedirectingNumber() {
    return (PartyNumber) getProperty(CdrProperty.REDIRECTING_NUMBER);
  }

  public void setCdrConnectedNumber(PartyNumber cdrConnectedNumber) {
    setProperty(CdrProperty.CONNECTED_NUMBER, cdrConnectedNumber);
  }

  public PartyNumber getCdrConnectedNumber() {
    return (PartyNumber) getProperty(CdrProperty.CONNECTED_NUMBER);
  }

  public Integer getCdrCtxGroup() {
    return (Integer) getProperty(CdrProperty.CENTREX_GROUP);
  }

  public void setCdrCtxGroup(Integer cdrCtxGroup) {
    setProperty(CdrProperty.CENTREX_GROUP, cdrCtxGroup);
  }

  public Integer getCdrCtxGroupB() {
    return (Integer) getProperty(CdrProperty.CENTREX_GROUP_B);
  }

  public void setCdrCtxGroupB(Integer cdrCtxGroupB) {
    setProperty(CdrProperty.CENTREX_GROUP_B, cdrCtxGroupB);
  }

  public Integer getCdrCtxCallType() {
    return (Integer) getProperty(CdrProperty.CENTREX_CALL_TYPE_B);
  }

  public void setCdrCtxCallType(Integer cdrCtxGroupB) {
    setProperty(CdrProperty.CENTREX_CALL_TYPE_B, cdrCtxGroupB);
  }

  public void setCdrBusinnesGroup(Integer cdrBusinnesGroup) {
    setProperty(CdrProperty.BUSINESS_GROUP, cdrBusinnesGroup);
  }

  public Integer getCdrBusinnesGroup() {
    return (Integer) getProperty(CdrProperty.BUSINESS_GROUP);
  }

  public void setCdrBusinnesGroupB(Integer cdrBusinnesGroupB) {
    setProperty(CdrProperty.BUSINESS_GROUP_B, cdrBusinnesGroupB);
  }

  public Integer getCdrBusinnesGroupB() {
    return (Integer) getProperty(CdrProperty.BUSINESS_GROUP_B);
  }

  public void setCdrCtxCallingNumber(String cdrCtxCallingNumber) {
    setProperty(CdrProperty.CTX_CALLING_NUMBER, cdrCtxCallingNumber);
  }

  public String getCdrCtxCallingNumber() {
    return (String) getProperty(CdrProperty.CTX_CALLING_NUMBER);
  }

  public void setCdrCtxCalledNumber(String cdrCtxCalledNumber) {
    setProperty(CdrProperty.CTX_CALLED_NUMBER, cdrCtxCalledNumber);
  }

  public String getCdrCtxCalledNumber() {
    return (String) getProperty(CdrProperty.CTX_CALLED_NUMBER);
  }

  public void setCdrCtxRedirectingNumber(String cdrCtxRedirectingNumber) {
    setProperty(CdrProperty.CTX_REDIRECTING_NUMBER, cdrCtxRedirectingNumber);
  }

  public String getCdrCtxRedirectingNumber() {
    return (String) getProperty(CdrProperty.CTX_REDIRECTING_NUMBER);
  }

  public void setCdrCacNumber(String cdrCacNumber) {
    setProperty(CdrProperty.CAC_NUMBER, cdrCacNumber);
  }

  public Integer getCdrCacNumber() {
    return (Integer) getProperty(CdrProperty.CAC_NUMBER);
  }

  public void setCdrCacPrefix(Integer cdrCacPrefix) {
    setProperty(CdrProperty.CAC_PREFIX, cdrCacPrefix);
  }

  public Integer getCdrCacPrefix() {
    return (Integer) getProperty(CdrProperty.CAC_PREFIX);
  }

  public Integer getCdrCacType() {
    return (Integer) getProperty(CdrProperty.CAC_TYPE);
  }

  public void setCdrChargeStatus(Integer cdrChargeStatus) {
    setProperty(CdrProperty.CHARGE_STATUS, cdrChargeStatus);
  }

  public Integer getCdrChargeStatus() {
    return (Integer) getProperty(CdrProperty.CHARGE_STATUS);
  }

  public void setCdrChargeUnits(Long cdrChargeUnits) {
    setProperty(CdrProperty.CHARGE_UNITS, cdrChargeUnits);
  }

  public Long getCdrChargeUnits() {
    return (Long) getProperty(CdrProperty.CHARGE_UNITS);
  }

  public void setCdrCallDuration(Long cdrCallDuration) {
    setProperty(CdrProperty.CALL_DURATION, cdrCallDuration);
  }

  public Long getCdrCallDuration() {
    return (Long) getProperty(CdrProperty.CALL_DURATION);
  }

  public void setCdrTariffDirection(Integer cdrTariffDirection) {
    setProperty(CdrProperty.TARIFF_DIRECTION, cdrTariffDirection);
  }

  public Integer getCdrTariffDirection() {
    return (Integer) getProperty(CdrProperty.TARIFF_DIRECTION);
  }

  public void setCdrCallReleaseValue(Integer cdrCallReleaseValue) {
    setProperty(CdrProperty.CALL_RELEASE_VALUE, cdrCallReleaseValue);
  }

  public Integer getCdrCallReleaseValue() {
    return (Integer) getProperty(CdrProperty.CALL_RELEASE_VALUE);
  }

  public void setCdrCallStartTime(Date cdrCallStartTime) {
    setProperty(CdrProperty.CALL_START_TIME, cdrCallStartTime);
  }

  public Date getCdrCallStartTime() {
    return (Date) getProperty(CdrProperty.CALL_START_TIME);
  }

  public void setCdrCallStopTime(Date cdrCallStopTime) {
    setProperty(CdrProperty.CALL_STOP_TIME, cdrCallStopTime);
  }

  public Date getCdrCallStopTime() {
    return (Date) getProperty(CdrProperty.CALL_STOP_TIME);
  }

  public void setCdrTimeZoneName(String cdrTimeZoneName) {
    if (cdrTimeZoneName != null && !cdrTimeZoneName.isEmpty())
      setProperty(CdrProperty.TIME_ZONE_NAME, cdrTimeZoneName);
  }

  public String getCdrTimeZoneName() {
    return (String) getProperty(CdrProperty.TIME_ZONE_NAME);
  }

  public void setCdrSuppServInfo(SupplementaryService cdrSuppServInfo) {
    setProperty(CdrProperty.SUPPLEMENTARY_SERVICE_INFO, cdrSuppServInfo);
  }

  public SupplementaryService getCdrSuppServInfo() {
    return (SupplementaryService) getProperty(CdrProperty.SUPPLEMENTARY_SERVICE_INFO);
  }

  public void setCdrSuppServInfoCalled(SupplementaryService cdrSuppServInfoCalled) {
    setProperty(CdrProperty.SUPPLEMENTARY_SERVICE_INFO_CALLED, cdrSuppServInfoCalled);
  }

  public SupplementaryService getCdrSuppServInfoCalled() {
    return (SupplementaryService) getProperty(CdrProperty.SUPPLEMENTARY_SERVICE_INFO_CALLED);
  }

  public void setCdrSuppServInfoSci(SupplementaryService cdrSuppServInfoSci) {
    setProperty(CdrProperty.SUPPLEMENTARY_SERVICE_INFO_SCI, cdrSuppServInfoSci);
  }

  public SupplementaryService getCdrSuppServInfoSci() {
    return (SupplementaryService) getProperty(CdrProperty.SUPPLEMENTARY_SERVICE_INFO_SCI);
  }

  public void setCdrDialedDigits(String cdrDialedDigits) {
    setProperty(CdrProperty.DIGIT_STRING, cdrDialedDigits);
  }

  public String getCdrDialedDigits() {
    return (String) getProperty(CdrProperty.DIGIT_STRING);
  }

  public Integer getCallingSubscriberGroup() {
    return (Integer) getProperty(CdrProperty.CALLING_SUBSCRIBER_GROUP);
  }

  public Integer getCalledSubscriberGroup() {
    return (Integer) getProperty(CdrProperty.CALLED_SUBSCRIBER_GROUP);
  }

  public Integer getOrigSideSndLineType() {
    return (Integer) getProperty(CdrProperty.ORIG_SIDE_SND_LINE_TYPE);
  }

  public Integer getTermSideSndLineType() {
    return (Integer) getProperty(CdrProperty.TERM_SIDE_SND_LINE_TYPE);
  }

  public Integer getCallReleasingSide() {
    return (Integer) getProperty(CdrProperty.CALL_RELEASING_SIDE);
  }

  public void setCdrInTrunkGroupId(Integer cdrInTrunkGroupId) {
    setProperty(CdrProperty.INTRUNK_GROUP_ID, cdrInTrunkGroupId);
  }

  public Integer getCdrInTrunkGroupId() {
    return (Integer) getProperty(CdrProperty.INTRUNK_GROUP_ID);
  }

  public void setCdrInTrunkId(Integer cdrInTrunkGroupId) {
    setProperty(CdrProperty.INTRUNK_ID, cdrInTrunkGroupId);
  }

  public Integer getCdrInTrunkId() {
    return (Integer) getProperty(CdrProperty.INTRUNK_ID);
  }

  public Integer getCdrInTrunkIdIE144() {
    return (Integer) getProperty(CdrProperty.IE144_Incoming_TD);
  }

  public String getCdrInTrunkGroupNameIE144() {
    return (String) getProperty(CdrProperty.IE144_Incoming_Trunk_Group_Name);
  }

  public void setCdrOutTrunkGroupId(Integer cdrOutTrunkGroupId) {
    setProperty(CdrProperty.OUTTRUNK_GROUP_ID, cdrOutTrunkGroupId);
  }

  public Integer getCdrOutTrunkGroupId() {
    return (Integer) getProperty(CdrProperty.OUTTRUNK_GROUP_ID);
  }

  public void setCdrOutTrunkId(Integer cdrOutTrunkGroupId) {
    setProperty(CdrProperty.OUTTRUNK_ID, cdrOutTrunkGroupId);
  }

  public Integer getCdrOutTrunkId() {
    return (Integer) getProperty(CdrProperty.OUTTRUNK_ID);
  }

  public Integer getCdrOutTrunkIdIE145() {
    return (Integer) getProperty(CdrProperty.IE145_Outgoing_TD);
  }

  public String getCdrOutTrunkGroupNameIE145() {
    return (String) getProperty(CdrProperty.IE145_Outgoing_Trunk_Group_Name);
  }

  public void setCdrTimeBeforeRinging(Integer cdrTimeBeforeRinging) {
    setProperty(CdrProperty.TIME_BEFORE_RINGING, cdrTimeBeforeRinging);
  }

  public Integer getCdrTimeBeforeRinging() {
    return (Integer) getProperty(CdrProperty.TIME_BEFORE_RINGING);
  }

  public void setCdrRingingTimeBeforeAnsw(Integer cdrRingingTimeBeforeAnsw) {
    setProperty(CdrProperty.RINGING_TIME_BEFORE_ANSWER, cdrRingingTimeBeforeAnsw);
  }

  public Integer getCdrRingingTimeBeforeAnsw() {
    return (Integer) getProperty(CdrProperty.RINGING_TIME_BEFORE_ANSWER);
  }

  public Integer getVoipRxCodecType() {
    return (Integer) getProperty(CdrProperty.VOIP_RX_CODEC_TYPE);
  }

  public Integer getVoipTxCodecType() {
    return (Integer) getProperty(CdrProperty.VOIP_TX_CODEC_TYPE);
  }

  public Integer getVoipCallType() {
    return (Integer) getProperty(CdrProperty.VOIP_CALL_TYPE);
  }

  public Integer getVoipSide() {
    return (Integer) getProperty(CdrProperty.VOIP_SIDE);
  }

  public Long getVoipRxPackets() {
    return (Long) getProperty(CdrProperty.VOIP_RX_PACKETS);
  }

  public Long getVoipTxPackets() {
    return (Long) getProperty(CdrProperty.VOIP_TX_PACKETS);
  }

  public Long getVoipRxOctets() {
    return (Long) getProperty(CdrProperty.VOIP_RX_OCTETS);
  }

  public Long getVoipTxOctets() {
    return (Long) getProperty(CdrProperty.VOIP_TX_OCTETS);
  }

  public Long getVoipPacketsLost() {
    return (Long) getProperty(CdrProperty.VOIP_PACKETS_LOST);
  }

  public Integer getVoipAverageJitter() {
    return (Integer) getProperty(CdrProperty.VOIP_AVERAGE_JITTER);
  }

  public Integer getVoipAverageLatency() {
    return (Integer) getProperty(CdrProperty.VOIP_AVERAGE_LATENCY);
  }
  
  public Integer getVoipEchoReturnLoss() {
    return (Integer) getProperty(CdrProperty.VOIP_ECHO_RETURN_LOSS);
  }
  
  public Long getVoipPacketsSentAndLost() {
    return (Long) getProperty(CdrProperty.VOIP_PACKETS_SENT_AND_LOST);
  }
  
  public Integer getVoipMaxPacketsLostInBurst() {
    return (Integer) getProperty(CdrProperty.VOIP_MAX_PACKETS_LOST_IN_BURST);
  }
  
  public Integer getVoipMaxJitter() {
    return (Integer) getProperty(CdrProperty.VOIP_MAX_JITTER);
  }
  
  public Integer getVoipMinJitter() {
    return (Integer) getProperty(CdrProperty.VOIP_MIN_JITTER);
  }
  
  public Integer getVoipRxMos() {
    return (Integer) getProperty(CdrProperty.VOIP_RX_MOS);
  }
  
  public Integer getVoipTxMos() {
    return (Integer) getProperty(CdrProperty.VOIP_TX_MOS);
  }
  
  public Integer getVoipFaxModulationType() {
    return (Integer) getProperty(CdrProperty.VOIP_FAX_MODULATION_TYPE);
  }
  
  public Integer getVoipFaxTransferRate() {
    return (Integer) getProperty(CdrProperty.VOIP_FAX_TRANSFER_RATE);
  }
  
  public Integer getVoipFaxModemRetrains() {
    return (Integer) getProperty(CdrProperty.VOIP_FAX_MODEM_RETRAINS);
  }
  
  public Integer getVoipFaxPagesTransferred() {
    return (Integer) getProperty(CdrProperty.VOIP_FAX_PAGES_TRANSFERRED);
  }
  
  public Integer getVoipFaxPagesRepeated() {
    return (Integer) getProperty(CdrProperty.VOIP_FAX_PAGES_REPEATED);
  }

  public void setCdrFlagF1(Object cdrFlagF1) {
    setProperty(CdrProperty.FLAG_F1, cdrFlagF1);
  }

  public Boolean isCdrFlagF1() {
    return (Boolean) getProperty(CdrProperty.FLAG_F1);
  }

  public void setCdrFlagF2(Boolean cdrFlagF2) {
    setProperty(CdrProperty.FLAG_F2, cdrFlagF2);
  }

  public Boolean isCdrFlagF2() {
    return (Boolean) getProperty(CdrProperty.FLAG_F2);
  }

  public void setCdrFlagF3(Boolean cdrFlagF3) {
    setProperty(CdrProperty.FLAG_F3, cdrFlagF3);
  }

  public Boolean isCdrFlagF3() {
    return (Boolean) getProperty(CdrProperty.FLAG_F3);
  }

  public void setCdrFlagF4(Boolean cdrFlagF4) {
    setProperty(CdrProperty.FLAG_F4, cdrFlagF4);
  }

  public Boolean isCdrFlagF4() {
    return (Boolean) getProperty(CdrProperty.FLAG_F4);
  }

  public void setCdrFlagF6(Boolean cdrFlagF6) {
    setProperty(CdrProperty.FLAG_F6, cdrFlagF6);
  }

  public Boolean isCdrFlagF6() {
    return (Boolean) getProperty(CdrProperty.FLAG_F6);
  }

  public void setCdrFlagF17(Boolean cdrFlagF17) {
    setProperty(CdrProperty.FLAG_F17, cdrFlagF17);
  }

  public Boolean isCdrFlagF17() {
    return (Boolean) getProperty(CdrProperty.FLAG_F17);
  }
}
