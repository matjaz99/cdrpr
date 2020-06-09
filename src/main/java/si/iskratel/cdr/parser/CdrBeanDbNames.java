package si.iskratel.cdr.parser;

import java.util.HashMap;
import java.util.Map;

public class CdrBeanDbNames {

  public static final Map<String, String> propertyDBNames = new HashMap<String, String>() {
    private static final long serialVersionUID = 1L;

    {
      put("id", "ID");
      put("cdrType", "RECORD_TYPE");
      put("recordSubType", "");
      put("callid", "CALLID");
      put("sequence", "SEQUNECE");
      put("ownerNumber", "OWNER_NUMBER");
      put("callingNumber", "CALLING_NUMBER");
      put("calledNumber", "CALLED_NUMBER");
      put("redirectingNumber", "REDIRECTING_NUMBER");
      put("startTime", "START_TIME");
      put("endTime", "END_TIME");
      put("chgUnits", "CHG_UNITS");
      put("duration", "DURATION");
      put("cause", "CAUSE");
      put("price", "PRICE");
      put("servIdOrig", "SERV_ID_ORIG");
      put("servIdTerm", "SERV_ID_TERM");
      put("bgidOrig", "BGID_ORIG");
      put("bgidTerm", "BGID_TERM");
      put("ctxCall", "CTX_CALL");
      put("binaryRecord", "BINARY_RECORD");
      put("ctxCallingNumber", "CTX_CALLING_NUMBER");
      put("ctxCalledNumber", "CTX_CALLED_NUMBER");
      put("ctxRedirectingNumber", "CTX_REDIRECTING_NUMBER");
      put("trunkInId", "TRUNK_ID");
      put("callingNumberCLIR", "CLIR");
      put("icid", "ICID");
    }
  };

}
