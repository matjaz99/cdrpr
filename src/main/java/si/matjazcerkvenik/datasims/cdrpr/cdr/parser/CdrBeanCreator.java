
package si.matjazcerkvenik.datasims.cdrpr.cdr.parser;

import si.matjazcerkvenik.datasims.cdrpr.cdr.manager.BadCdrRecordException;

public abstract class CdrBeanCreator {

  public static final int CALL = 1;
  public static final int FAU = 2;
  public static final int FAIS = 3;
  public static final int CALL_FAU = 12; // Cdr type of call, witch is caused by supplementary service(eg. LH)
  public static final int SINGLE_CDR = 1;// Sequence
  public static final int MULTIPLE_CDR = 2;

  public static final int SUPPLEMENTARY_SERVICE_LH = 12;

  public CdrBeanCreator() {
  }

  public CdrBean parseBinaryCdr(byte[] cdrBytes, String timeZone) throws BadCdrRecordException {
    CdrObject cdrObj = CdrParser.parseCDR(cdrBytes);
    return createCdrBean(cdrObj, timeZone);
  }

  public PpdrBean parseBinaryPpdr(DataRecord dataRecord) {
    return CdrParser.parsePPDR(dataRecord);
  }

  public CdrBean createCdrBean(CdrObject cdrObj, String timeZone) throws BadCdrRecordException {
    if (cdrObj == null)
      throw new BadCdrRecordException("Missing input data");
    CdrBean cdrBean = new CdrBean();
    setBeanValues(cdrObj, cdrBean, timeZone);
    return cdrBean;
  }

  public abstract void setSpecificBeanValues(CdrObject cdrObj, CdrBean cdrBean);

  private void setBeanValues(CdrObject cdrObj, CdrBean cdrBean, String timeZone) {
    if (isCallRecord(cdrObj)) {
      setId(cdrObj, cdrBean);
      setCallId(cdrObj, cdrBean);
      setIcid(cdrObj, cdrBean);
      setCdrType(cdrObj, cdrBean);
      setRecordSubType(cdrObj, cdrBean);
      setSequence(cdrObj, cdrBean);
      setOwnerNumber(cdrObj, cdrBean);
      setCallingNumber(cdrObj, cdrBean);
      setCallingNumberCLIR(cdrObj, cdrBean);
      setCalledNumber(cdrObj, cdrBean);
      setRedirectingNumber(cdrObj, cdrBean);
      setConnectedNumber(cdrObj, cdrBean);
      setStartTime(cdrObj, cdrBean, timeZone);
      setEndTime(cdrObj, cdrBean, timeZone);
      setChgUnits(cdrObj, cdrBean);
      setDuration(cdrObj, cdrBean);
      setCause(cdrObj, cdrBean);
      setPrice(cdrObj, cdrBean);
      setServIdOrig(cdrObj, cdrBean);
      setServIdTerm(cdrObj, cdrBean);
      setServId(cdrObj, cdrBean);
      setSuppServSucc(cdrObj, cdrBean);
      setInputType(cdrObj, cdrBean);
      setBgidOrig(cdrObj, cdrBean);
      setbBgidTerm(cdrObj, cdrBean);
      setCgidOrig(cdrObj, cdrBean);
      setCgidTerm(cdrObj, cdrBean);
      setCtxCallType(cdrObj, cdrBean);
      setCtxCall(cdrObj, cdrBean);
      setCtxCallingNumber(cdrObj, cdrBean);
      setCtxCalledNumber(cdrObj, cdrBean);
      setCtxRedirectingNumber(cdrObj, cdrBean);
      setCallType(cdrObj, cdrBean);
      setBinaryRecord(cdrObj, cdrBean);
      setInTrunkGroupId(cdrObj, cdrBean);
      setInTrunkId(cdrObj, cdrBean);
      setInTrunkIdIE144(cdrObj, cdrBean);
      setInTrunkNameIE144(cdrObj, cdrBean);
      setOutTrunkGroupId(cdrObj, cdrBean);
      setOutTrunkId(cdrObj, cdrBean);
      setOutTrunkIdIE145(cdrObj, cdrBean);
      setOutTrunkNameIE145(cdrObj, cdrBean);
      setSpecificBeanValues(cdrObj, cdrBean);
      setCallingSubscriberGroup(cdrObj, cdrBean);
      setCalledSubscriberGroup(cdrObj, cdrBean);
      setOrigSideSndLineType(cdrObj, cdrBean);
      setTermSideSndLineType(cdrObj, cdrBean);
      setCallReleasingSide(cdrObj, cdrBean);
      setCdrTimeBeforeRinging(cdrObj, cdrBean);
      setCdrRingingTimeBeforeAnsw(cdrObj, cdrBean);
      setCdrCacNumber(cdrObj, cdrBean);
      setCdrCacPrefix(cdrObj, cdrBean);
      setCdrCacType(cdrObj, cdrBean);
      setVoipRxCodecType(cdrObj, cdrBean);
      setVoipTxCodecType(cdrObj, cdrBean);
      setVoipSide(cdrObj, cdrBean);
      setVoipCallType(cdrObj, cdrBean);
      setVoipRxPackets(cdrObj, cdrBean);
      setVoipTxPackets(cdrObj, cdrBean);
      setVoipRxOctets(cdrObj, cdrBean);
      setVoipTxOctets(cdrObj, cdrBean);
      setVoipPacketsLost(cdrObj, cdrBean);
      setVoipAverageJitter(cdrObj, cdrBean);
      setVoipAverageLatency(cdrObj, cdrBean);
      setVoipEchoReturnLoss(cdrObj, cdrBean);
      setVoipPacketsSentAndLost(cdrObj, cdrBean);
      setVoipMaxPacketsLostInBurst(cdrObj, cdrBean);
      setVoipMaxJitter(cdrObj, cdrBean);
      setVoipMinJitter(cdrObj, cdrBean);
      setVoipRxMos(cdrObj, cdrBean);
      setVoipTxMos(cdrObj, cdrBean);
      setVoipFaxModulationType(cdrObj, cdrBean);
      setVoipFaxTransferRate(cdrObj, cdrBean);
      setVoipFaxModemRetrains(cdrObj, cdrBean);
      setVoipFaxPagesTransferred(cdrObj, cdrBean);
      setVoipFaxPagesRepeated(cdrObj, cdrBean);
    }
  }

  private boolean isCallRecord(CdrObject cdrObj) {
    if (cdrObj.getCdrRecordType() != null)
      return cdrObj.getCdrRecordType()
          .equals(FixedPartCdr.CDR_TYPE_CALL);
    return false;

  }

  private short determineCallRecordSubType(CdrObject cdrObj) {
    if (cdrObj.isCdrFlagF1()) // Call
      return (short) CALL;
    if (cdrObj.isCdrFlagF2()) // Key defines is record of performing supplement service (FAU - Facility
      // Usage).
      return ((short) FAU);
    if (cdrObj.isCdrFlagF3()) // Key defines is record of Facility Input by Subscriber (FAIS).
      return ((short) FAIS);
    return 0;
  }

  private boolean isForwarding(SupplementaryService supplementaryService) {
    if (supplementaryService != null) {
      Short suppServiceId = supplementaryService.getServiceId();
      if (suppServiceId.equals(new Short("9")) || suppServiceId.equals(new Short("10")) || suppServiceId.equals(new Short("11"))
          || suppServiceId.equals(new Short("28")))
        return true;
    }
    return false;
  }

  // ---------------------------------------------------------------------

  private void setId(CdrObject cdrObj, CdrBean cdrBean) {
  }

  private void setCallId(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getCdrCallId() != null)
      cdrBean.setCallid(cdrObj.getCdrCallId());
  }

  private void setIcid(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getCdrIcid() != null)
      cdrBean.setIcid(cdrObj.getCdrIcid());
  }

  private void setCdrType(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setCdrType(cdrObj.getCdrRecordType());
  }

  private void setRecordSubType(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setRecordSubType(determineCallRecordSubType(cdrObj));
  }

  private void setSequence(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setSequence(cdrObj.getCdrRecordSequence());
  }

  private void setOwnerNumber(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setOwnerNumber(cdrObj.getCdrOwnerNumber());
  }

  private void setCallingNumber(CdrObject cdrObj, CdrBean cdrBean) {
    PartyNumber pn = cdrObj.getCdrOriginalCpn();
    if (pn != null) {
      cdrBean.setCallingNumber(pn.getNumber());
    } else {
      cdrBean.setCallingNumber(cdrObj.getCdrOwnerNumber());
    }
  }

  private void setCallingNumberCLIR(CdrObject cdrObj, CdrBean cdrBean) {
    PartyNumber pn = cdrObj.getCdrOriginalCpn();
    if (pn != null) {
      cdrBean.setCallingNumberCLIR(pn.getPresentationInd());
    }
  }

  private void setConnectedNumber(CdrObject cdrObj, CdrBean cdrBean) {
    PartyNumber pn = cdrObj.getCdrConnectedNumber();
    if (pn != null) {
      cdrBean.setConnectedNumber(pn.getNumber());
    }
  }

  private void setCalledNumber(CdrObject cdrObj, CdrBean cdrBean) {
    PartyNumber pn = cdrObj.getCdrCalledNumberFormated();
    if (pn != null) {
      cdrBean.setCalledNumber(pn.getNumber());
    } else {
      if (cdrObj.getCdrCalledNumber() != null) {
        cdrBean.setCalledNumber(cdrObj.getCdrCalledNumber());
      }
    }
  }

  private void setRedirectingNumber(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getCdrRedirectingNumber() != null) {
      cdrBean.setRedirectingNumber(cdrObj.getCdrRedirectingNumber()
          .getNumber());
    } else {
      if (isForwarding(((SupplementaryService) cdrObj.getCdrSuppServInfo())) && cdrObj.getCdrOriginalCpn() != null
          && cdrObj.getCdrOwnerNumber() != null && !(cdrObj.getCdrOriginalCpn()).getNumber()
              .equals(cdrObj.getCdrOwnerNumber()))
        cdrBean.setRedirectingNumber(cdrObj.getCdrOwnerNumber());
    }
  }

  private void setStartTime(CdrObject cdrObj, CdrBean cdrBean, String timeZone) {
    if (cdrObj.getCdrTimeZoneName() != null) {// use timezone from IE160
      timeZone = cdrObj.getCdrTimeZoneName();
    }
    if (cdrObj.getCdrCallStartTime() != null)
      cdrBean.setStartTime(ParserUtil.getCdrDate(cdrObj.getCdrCallStartTime(), timeZone));
  }

  private void setEndTime(CdrObject cdrObj, CdrBean cdrBean, String timeZone) {
    if (cdrObj.getCdrTimeZoneName() != null) {// use timezone from IE160
      timeZone = cdrObj.getCdrTimeZoneName();
    }
    if (cdrObj.getCdrCallStopTime() != null) {
      cdrBean.setEndTime(ParserUtil.getCdrDate(cdrObj.getCdrCallStopTime(), timeZone));
    } else {
      if (cdrObj.getCdrCallStartTime() != null)
        cdrBean.setEndTime(ParserUtil.getCdrDate(cdrObj.getCdrCallStartTime(), timeZone));
    }
  }

  private void setChgUnits(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getCdrChargeUnits() != null) {
      cdrBean.setChgUnits(cdrObj.getCdrChargeUnits());
    }
  }

  private void setDuration(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getCdrCallDuration() != null) {
      cdrBean.setDuration(cdrObj.getCdrCallDuration());
    }
  }

  private void setCause(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getCdrCallReleaseValue() == null) {
      cdrBean.setCause(0);
    } else {
      cdrBean.setCause(cdrObj.getCdrCallReleaseValue());
    }
  }

  private void setPrice(CdrObject cdrObj, CdrBean cdrBean) {
  }

  private void setServIdOrig(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getCdrSuppServInfo() != null) {
      cdrBean.setServIdOrig(cdrObj.getCdrSuppServInfo()
          .getServiceId());
    }
  }

  private void setServIdTerm(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getCdrSuppServInfoCalled() != null) {
      cdrBean.setServIdTerm(cdrObj.getCdrSuppServInfoCalled()
          .getServiceId());
    }
  }

  private void setServId(CdrObject cdrObject, CdrBean cdrBean) {
    if (cdrObject.getCdrSuppServInfoSci() != null) {
      cdrBean.setServId(cdrObject.getCdrSuppServInfoSci()
          .getServiceId());
    }
  }

  private void setInputType(CdrObject cdrObject, CdrBean cdrBean) {
    if (cdrObject.getCdrSuppServInfoSci() != null) {
      cdrBean.setSupplementaryInputType(cdrObject.getCdrSuppServInfoSci()
          .getInputType());
    }
  }

  private void setBgidOrig(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getCdrBusinnesGroup() != null) {
      cdrBean.setBgidOrig(cdrObj.getCdrBusinnesGroup());
    }
  }

  private void setbBgidTerm(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getCdrBusinnesGroupB() != null) {
      cdrBean.setBgidTerm(cdrObj.getCdrBusinnesGroupB());
    }
  }

  private void setCgidOrig(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getCdrCtxGroup() != null) {
      cdrBean.setCgidOrig(cdrObj.getCdrCtxGroup());
    }
  }

  private void setCgidTerm(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getCdrCtxGroupB() != null) {
      cdrBean.setCgidTerm(cdrObj.getCdrCtxGroupB());
    }
  }

  private void setCtxCallType(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getCdrCtxCallType() != null) {
      cdrBean.setCentrexCallType(cdrObj.getCdrCtxCallType());
    }
  }

  private void setCtxCall(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.isCdrFlagF17()) {
      cdrBean.setCtxCall(1);
    }
  }

  private void setCallType(CdrObject cdrObj, CdrBean cdrBean){
    if(cdrObj.getProperty(CdrProperty.IE151_CALL_TYPE) != null) {
      cdrBean.setCallType((Integer) cdrObj.getProperty(CdrProperty.IE151_CALL_TYPE));
    }
  }

  private void setSuppServSucc(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.isCdrFlagF4() != null) {
      cdrBean.setServSucc(cdrObj.isCdrFlagF4());
    }
  }
  
  private void setVoipRxCodecType(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipRxCodecType() != null) {
      cdrBean.setVoipRxCodecType(cdrObj.getVoipRxCodecType());
    }
  }
  
  private void setVoipTxCodecType(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipTxCodecType() != null) {
      cdrBean.setVoipTxCodecType(cdrObj.getVoipTxCodecType());
    }
  }
  
  private void setVoipSide(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipSide() != null) {
      cdrBean.setVoipSide(cdrObj.getVoipSide());
    }
  }
  
  private void setVoipRxPackets(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipRxPackets() != null) {
      cdrBean.setVoipRxPackets(cdrObj.getVoipRxPackets());
    }
  }
  
  private void setVoipTxPackets(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipTxPackets() != null) {
      cdrBean.setVoipTxPackets(cdrObj.getVoipTxPackets());
    }
  }
  
  private void setVoipRxOctets(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipRxOctets() != null) {
      cdrBean.setVoipRxOctets(cdrObj.getVoipRxOctets());
    }
  }
  
  private void setVoipTxOctets(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipTxOctets() != null) {
      cdrBean.setVoipTxOctets(cdrObj.getVoipTxOctets());
    }
  }
  
  private void setVoipPacketsLost(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipPacketsLost() != null) {
      cdrBean.setVoipPacketsLost(cdrObj.getVoipPacketsLost());
    }
  }
  
  private void setVoipAverageJitter(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipAverageJitter() != null) {
      cdrBean.setVoipAverageJitter(cdrObj.getVoipAverageJitter());
    }
  }
  
  private void setVoipAverageLatency(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipAverageLatency() != null) {
      cdrBean.setVoipAverageLatency(cdrObj.getVoipAverageLatency());
    }
  }
  
  private void setVoipCallType(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipCallType() != null) {
      cdrBean.setVoipCallType(cdrObj.getVoipCallType());
    }
  }
  
  private void setVoipEchoReturnLoss(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipEchoReturnLoss() != null) {
      cdrBean.setVoipEchoReturnLoss(cdrObj.getVoipEchoReturnLoss());
    }
  }
  
  private void setVoipPacketsSentAndLost(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipPacketsSentAndLost() != null) {
      cdrBean.setVoipPacketsSentAndLost(cdrObj.getVoipPacketsSentAndLost());
    }
  }
  
  private void setVoipMaxPacketsLostInBurst(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipMaxPacketsLostInBurst() != null) {
      cdrBean.setVoipMaxPacketsLostInBurst(cdrObj.getVoipMaxPacketsLostInBurst());
    }
  }
  
  private void setVoipMaxJitter(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipMaxJitter() != null) {
      cdrBean.setVoipMaxJitter(cdrObj.getVoipMaxJitter());
    }
  }
  
  private void setVoipMinJitter(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipMinJitter() != null) {
      cdrBean.setVoipMinJitter(cdrObj.getVoipMinJitter());
    }
  }
  
  private void setVoipRxMos(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipRxMos() != null) {
      cdrBean.setVoipRxMos(cdrObj.getVoipRxMos());
    }
  }
  
  private void setVoipTxMos(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipTxMos() != null) {
      cdrBean.setVoipTxMos(cdrObj.getVoipTxMos());
    }
  }
  
  private void setVoipFaxModulationType(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipFaxModulationType() != null) {
      cdrBean.setVoipFaxModulationType(cdrObj.getVoipFaxModulationType());
    }
  }
  
  private void setVoipFaxTransferRate(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipFaxTransferRate() != null) {
      cdrBean.setVoipFaxTransferRate(cdrObj.getVoipFaxTransferRate());
    }
  }
  
  private void setVoipFaxModemRetrains(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipFaxModemRetrains() != null) {
      cdrBean.setVoipFaxModemRetrains(cdrObj.getVoipFaxModemRetrains());
    }
  }
  
  private void setVoipFaxPagesTransferred(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipFaxPagesTransferred() != null) {
      cdrBean.setVoipFaxPagesTransferred(cdrObj.getVoipFaxPagesTransferred());
    }
  }
  
  private void setVoipFaxPagesRepeated(CdrObject cdrObj, CdrBean cdrBean) {
    if (cdrObj.getVoipFaxPagesRepeated() != null) {
      cdrBean.setVoipFaxPagesRepeated(cdrObj.getVoipFaxPagesRepeated());
    }
  }

  private void setBinaryRecord(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setBinaryRecord(cdrObj.getCdrBytes());
  }

  private void setCtxCallingNumber(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setCtxCallingNumber(cdrObj.getCdrCtxCallingNumber());
  }

  private void setCtxCalledNumber(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setCtxCalledNumber(cdrObj.getCdrCtxCalledNumber());
  }

  private void setCtxRedirectingNumber(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setCtxRedirectingNumber(cdrObj.getCdrCtxRedirectingNumber());
  }

  private void setInTrunkGroupId(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setInTrunkGroupId(cdrObj.getCdrInTrunkGroupId());
  }

  private void setInTrunkId(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setInTrunkId(cdrObj.getCdrInTrunkId());
  }

  private void setInTrunkIdIE144(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setInTrunkIdIE144(cdrObj.getCdrInTrunkIdIE144());
  }

  private void setInTrunkNameIE144(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setInTrunkGroupNameIE144(cdrObj.getCdrInTrunkGroupNameIE144());
  }

  private void setOutTrunkGroupId(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setOutTrunkGroupId(cdrObj.getCdrOutTrunkGroupId());
  }

  private void setOutTrunkId(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setOutTrunkId(cdrObj.getCdrOutTrunkId());
  }

  private void setOutTrunkIdIE145(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setOutTrunkIdIE145(cdrObj.getCdrOutTrunkIdIE145());
  }

  private void setOutTrunkNameIE145(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setOutTrunkGroupNameIE145(cdrObj.getCdrOutTrunkGroupNameIE145());
  }

  private void setCallingSubscriberGroup(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setCallingSubscriberGroup(cdrObj.getCallingSubscriberGroup());
  }

  private void setCalledSubscriberGroup(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setCalledSubscriberGroup(cdrObj.getCalledSubscriberGroup());
  }

  private void setOrigSideSndLineType(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setOrigSideSndLineType(cdrObj.getOrigSideSndLineType());
  }

  private void setTermSideSndLineType(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setTermSideSndLineType(cdrObj.getTermSideSndLineType());
  }

  private void setCallReleasingSide(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setCallReleasingSide(cdrObj.getCallReleasingSide());
  }

  private void setCdrTimeBeforeRinging(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setCdrTimeBeforeRinging(cdrObj.getCdrTimeBeforeRinging());
  }

  private void setCdrRingingTimeBeforeAnsw(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setCdrRingingTimeBeforeAnsw(cdrObj.getCdrRingingTimeBeforeAnsw());
  }

  private void setCdrCacNumber(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setCdrCacNumber(cdrObj.getCdrCacNumber());
  }

  private void setCdrCacPrefix(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setCacPrefix(cdrObj.getCdrCacPrefix());
  }

  private void setCdrCacType(CdrObject cdrObj, CdrBean cdrBean) {
    cdrBean.setCacType(cdrObj.getCdrCacType());
  }
}
