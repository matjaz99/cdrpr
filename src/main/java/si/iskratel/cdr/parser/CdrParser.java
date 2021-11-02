
package si.iskratel.cdr.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.BitSet;
import java.util.Hashtable;
import si.iskratel.cdr.manager.BadCdrRecordException;

public class CdrParser {

  private static final Hashtable<Integer, Integer> FIELDS_LENGTH = new Hashtable<Integer, Integer>();;
  private static final BitSet FIELDS_TO_PARSE = new BitSet(50);

  static {
    setFieldsLength();
    setFieldsToParse();
  }

  public static CdrObject parseCDR(byte[] cdrBytes) throws BadCdrRecordException {
    CdrObject cdrObject = null;
    FixedPartCdr fixPartCdr = new FixedPartCdr();
    try {
      InputStream cdrStream = new ByteArrayInputStream(cdrBytes);
      cdrObject = fixPartCdr.parse200record(cdrStream, FIELDS_LENGTH, FIELDS_TO_PARSE);
      cdrObject.setProperty(CdrProperty.CDR_BYTES, cdrBytes);
    } catch (BadCdrRecordException be) {
      throw be;
    } catch (Exception ex) {
      BadCdrRecordException bex = new BadCdrRecordException("Bad binary CDR received.", ex);
      throw bex;
    }
    return cdrObject;
  }
  
  public static PpdrBean parsePPDR(DataRecord dataRecord) {
    FixedPartPpdr fixedPartPpdr = new FixedPartPpdr();
    return fixedPartPpdr.parse220record(dataRecord);
  }

  private static void setFieldsToParse() {
    FIELDS_TO_PARSE.set(0); // 100 - Called number
    FIELDS_TO_PARSE.set(1); // 101 - Call accepting party number
    FIELDS_TO_PARSE.set(2); // 102 - Start date and time
    FIELDS_TO_PARSE.set(3); // 103 - End date and time
    FIELDS_TO_PARSE.set(4); // 104 - Number of charging units
    FIELDS_TO_PARSE.set(6); // 106 - Supplementary service used by calling subscriber
    FIELDS_TO_PARSE.set(7); // 107 - Supplementary service used by called subscriber
    FIELDS_TO_PARSE.set(8); // 108 - Subscribers control input
    FIELDS_TO_PARSE.set(9); // 109 - Dialed digits
    FIELDS_TO_PARSE.set(11); // 111 - Tariff direction
    FIELDS_TO_PARSE.set(13); // 113 - Incoming trunk data
    FIELDS_TO_PARSE.set(14); // 114 - Incoming trunk data
    FIELDS_TO_PARSE.set(15); // 115 - Call/service duration
    FIELDS_TO_PARSE.set(17); // 117 - Business and centrex group id
    FIELDS_TO_PARSE.set(18); // 118 - Carrier Access Code (CAC)
    FIELDS_TO_PARSE.set(19); // 119 - ORIGINAL CPN
    FIELDS_TO_PARSE.set(21); // 121 - Call release cause
    FIELDS_TO_PARSE.set(22); // 122 - Charge Band Number (CBNO)
    FIELDS_TO_PARSE.set(24); // 124 - Durations before answer
    FIELDS_TO_PARSE.set(28); // 128 - VoIP Info
    FIELDS_TO_PARSE.set(29); // 129 - Amount of transferred data
    FIELDS_TO_PARSE.set(31); // 131 - New Destination Number
    FIELDS_TO_PARSE.set(32); // 132 - VoIP Quality Of Service Data
    FIELDS_TO_PARSE.set(33); // 133 - Centrex data
    FIELDS_TO_PARSE.set(34); // 134 - Additional Statistics Data
    FIELDS_TO_PARSE.set(35); // 135 - IMS charging identifier - ICID
    FIELDS_TO_PARSE.set(37); // 137 - Supplementary service additional info
    FIELDS_TO_PARSE.set(38); // 138 - Calling party number
    FIELDS_TO_PARSE.set(39); // 139 - Additional Calling number
    FIELDS_TO_PARSE.set(40); // 140 - Called party number
    FIELDS_TO_PARSE.set(42); // 142 - Third party number
    FIELDS_TO_PARSE.set(43); // 143 - Redirecting party number
    FIELDS_TO_PARSE.set(44); // 144 - Incoming Trunk Data
    FIELDS_TO_PARSE.set(45); // 145 - Outgoing Trunk Data
    FIELDS_TO_PARSE.set(46); // 146 - Node Info
    FIELDS_TO_PARSE.set(47); // 147 - Global call reference
    FIELDS_TO_PARSE.set(50); // 150 - Received called party number
    FIELDS_TO_PARSE.set(51); // 151 - Call Type (Traffic type)
    FIELDS_TO_PARSE.set(56); // 156 - Additional Numbers (Connected number, Received calling party number)
    FIELDS_TO_PARSE.set(59); // 159 - Centrex Numbers
    FIELDS_TO_PARSE.set(60); // 160 - Time Zone
  }

  private static void setFieldsLength() {
    FIELDS_LENGTH.put(102, 9);
    FIELDS_LENGTH.put(103, 9);
    FIELDS_LENGTH.put(104, 4);
    FIELDS_LENGTH.put(105, 3);
    FIELDS_LENGTH.put(106, 2);
    FIELDS_LENGTH.put(107, 2);
    FIELDS_LENGTH.put(108, 3);
    FIELDS_LENGTH.put(110, 2);
    FIELDS_LENGTH.put(111, 2);
    FIELDS_LENGTH.put(112, 2);
    FIELDS_LENGTH.put(113, 9);
    FIELDS_LENGTH.put(114, 9);
    FIELDS_LENGTH.put(115, 5);
    FIELDS_LENGTH.put(117, 6);
    FIELDS_LENGTH.put(122, 5);
    FIELDS_LENGTH.put(124, 10);
    FIELDS_LENGTH.put(133, 12);
  }

}