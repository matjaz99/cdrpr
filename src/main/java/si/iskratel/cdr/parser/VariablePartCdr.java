/* ###################################################################### */
/* # # */
/* # # */
/* # Copyright (c) 2001 ISKRATEL # */
/* # # */
/* # # */
/* # Name : VariabledPartCDR.java # */
/* # # */
/* # Decription : Defines variabled part data in call record. # */
/* # # */
/* # Code : GQSD - XAE5503 # */
/* # # */
/* # Date : July, 2001 # */
/* # # */
/* # Author : Vidic Ales, RDSM3 # */
/* # # */
/* # Translation : # */
/* # # */
/* # Remarks : # */
/* # # */
/* ###################################################################### */
package si.iskratel.cdr.parser;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import org.apache.log4j.Logger;
import si.iskratel.cdr.manager.BadCdrRecordException;

/**
 * The VariabledPartCDR class implements methods for putting values and keys
 * into Hashtable. It define only variabled part of call record.
 * 
 * @see CdrObject
 * @since JDK 1.3
 * @version 1.0, April,2001
 */
public class VariablePartCdr {

  /** Current counter. */
  private int sizeCounter;

  /** Informational element length. */
  private int typeLength;

  /** Temporary integer. */
  private int temp;

  /** Buffer of integers in which converted bytes are stored. */
  private int[] intBuf = null;

  /** Provide cdr object. */
  public CdrObject cdrObject = null;

  /** Represents method name (setType100(), setType101(),...) */
  private String methodName = new String("");

  /** CDR record size */
  private int cdrSize = 0;

  private static Logger log = Logger.getLogger(VariablePartCdr.class);

  /**
   * The <code>VariabledPartCDR()</code> is class constructor.
   */
  public VariablePartCdr() {
  }

  /**
   * Obtains the cdr Object of the provided array of integers. Determines all
   * recorded values in variable part of CDR record.
   * 
   * @param acdrObject1
   *          Object to complete.
   * @param acdrSize1
   *          Represents size of cdr record.
   * @param pointer
   *          Points, where to start in intBuf.
   * @param intBuf1
   *          Aray of integers, from which variabled part of cdr record should
   *          be extracted.
   * @param fieldsToParse 
   * @param fieldLengths 
   * @return Complete object describing cdr record
   * @throws BadCdrRecordException
   *           if bad cdr record occurs.
   */
  public CdrObject variableHandler(CdrObject acdrObject1, int acdrSize1, long pointer, int[] intBuf1, Hashtable<Integer, Integer> fieldLengths, BitSet fieldsToParse) throws BadCdrRecordException {
    this.cdrObject = acdrObject1;
    this.intBuf = intBuf1;
    int infoNumber;
    this.cdrSize = acdrSize1;
    this.sizeCounter = 16 + (int) pointer;
    while (this.sizeCounter < this.cdrSize) {
      infoNumber = intBuf1[this.sizeCounter];
      if (infoNumber < 100) {
        // Illegal IE
        throw new BadCdrRecordException("Info Element '" + infoNumber + "' not valid ");
      }
      if ((infoNumber == 100) || (infoNumber == 109)) {
        readNumberLength(sizeCounter);
      } else if (infoNumber == 101) {
        read101Length(sizeCounter);
      } else if ((infoNumber < 116) || (infoNumber == 117) || (infoNumber == 124)) {
        // older information elements don't include length
        getOlderLength(infoNumber, fieldLengths);
      } else {
        readIELength(sizeCounter);
      }
//      System.out.println(infoNumber);
      if (shouldSkipIE(infoNumber, fieldsToParse)) {
        if (typeLength == 0) {
          throw new BadCdrRecordException("Info Element '" + infoNumber + "' size: 0");
        }
        this.sizeCounter += typeLength;
      } else {
        this.methodName = "setType" + infoNumber;
//         System.out.println(this.methodName);
        this.invokeMethod(this.methodName, this);
      }
    }
    return this.cdrObject;
  }
  
  boolean shouldSkipIE(int infoNumber, BitSet fieldsToParse) {
    return !(fieldsToParse.get(infoNumber - 100));
  }

  private void getOlderLength(int infoNumber, Hashtable<Integer, Integer> fieldLengths) throws BadCdrRecordException {
    if (fieldLengths.get(infoNumber) == null) {
      throw new BadCdrRecordException("Wrong info element " + infoNumber);
    } else {
      typeLength = fieldLengths.get(infoNumber);
    }
  }

  private void read101Length(int sizeCounter2) throws BadCdrRecordException {
    typeLength = intBuf[sizeCounter2 + 2];
  }

  /**
   * Reads number length without IE number. Applied for IE100 and IE109
   * 
   * @param sizeCounter2
   * @throws BadCdrRecordException
   *           if length is < 0
   */
  private void readNumberLength(int sizeCounter2) throws BadCdrRecordException {
    typeLength = intBuf[sizeCounter2 + 1];
    if (typeLength < 0) { // IE100 and IE109 my have length 0, but they should not be less then 0
      int ie = intBuf[sizeCounter2];
      log.debug("IE " + ie + " length is " + typeLength);
      throw new BadCdrRecordException("IE " + ie + " length is " + typeLength);
    }
  }

  /**
   * Reads IE length
   * 
   * @param sizeCounter2
   * @throws BadCdrRecordException
   *           if length is 0
   */
  private void readIELength(int sizeCounter2) throws BadCdrRecordException {
    typeLength = intBuf[sizeCounter2 + 1];
    if (typeLength < 2) { // IE length can not be 0
      int ie = intBuf[sizeCounter2];
      log.debug("IE " + ie + " length is " + typeLength);
      throw new BadCdrRecordException("IE " + ie + " length is " + typeLength);
    }
  }

  /**
   * From array of integers obtains the String which is saved in hashtable.
   * 
   * @see CdrProperty
   */
  public void setType100() {
    this.sizeCounter = this.sizeCounter + 2;
    temp = typeLength / 2 + typeLength % 2;
    StringBuilder numPartyInConn = new StringBuilder(typeLength);
    for (int i = 0; i < temp; i++) {
      // handling of FCC in the number
      int tmpInt = intBuf[this.sizeCounter] >> 4;
      // Handle hex number
      if (tmpInt < 10) {
        // add number
        numPartyInConn.append(tmpInt);
      } else {
        // convert hex number to appropriate character (hex A to char A)
        char c = (char) (tmpInt + 55);
        numPartyInConn.append(c);
      }
      if (i * 2 + 2 <= typeLength) {
        int tmpInt2 = intBuf[this.sizeCounter] & 0x0F;
        // convert hex number to appropriate number or character
        if (tmpInt2 < 10) {
          // handle number
          numPartyInConn.append(tmpInt2);
        } else {
          // convert hex number to appropriate character (hex A to char A)
          char c = (char) (tmpInt2 + 55);
          numPartyInConn.append(c);
        }
      }
      this.sizeCounter++;
    }
    this.cdrObject.setProperty(CdrProperty.CALLED_NUMBER, numPartyInConn.toString());
  }

  /**
   * From array of integers obtains the String which is saved in hashtable. It
   * also obtain a flag and saved in hashtable. It is saved as an Integer.
   * 
   * @see CdrProperty
   */
  public void setType101() {
    int flag101 = intBuf[this.sizeCounter + 1];

    this.sizeCounter = this.sizeCounter + 3;
    temp = typeLength / 2 + typeLength % 2;

    StringBuilder numPartyTakeOver = new StringBuilder(typeLength);
    for (int i = 0; i < temp; i++) {
      numPartyTakeOver.append(intBuf[this.sizeCounter] >> 4);
      if (i * 2 + 2 <= typeLength)
        numPartyTakeOver.append(intBuf[this.sizeCounter] & 0x0F);
      this.sizeCounter++;
    }
    this.cdrObject.setProperty(CdrProperty.TRANSFERRED_TO_DN, numPartyTakeOver.toString());
    this.cdrObject.setProperty(CdrProperty.FLAG_101, flag101 == 1);
  }

  /**
   * From array of integers obtains two Strings (start time and start date). Both
   * are saved in hashtable. It also obtain a flag and saved in hashtable as an
   * Integer.
   * 
   * @see CdrProperty
   */
  public void setType102() {
    this.sizeCounter++;
    Date callStartDate = getDateTime(intBuf, this.sizeCounter);
    int pos = this.sizeCounter + 7;
    int F1 = (intBuf[pos] & 0x01);
    int F2 = ((intBuf[pos] & 0x02) >> 1);
    int F3 = ((intBuf[pos] & 0x04) >> 2);
    int F4 = ((intBuf[pos] & 0x08) >> 3);
    this.sizeCounter = this.sizeCounter + 8;
    this.cdrObject.setProperty(CdrProperty.FLAG_102, F1 == 1);
    this.cdrObject.setProperty(CdrProperty.ORIG_TIME_ZONE, F2 == 1);
    this.cdrObject.setProperty(CdrProperty.TERM_TIME_ZONE, F3 == 1);
    this.cdrObject.setProperty(CdrProperty.UTC_TIME_ZONE, F4 == 1);
    this.cdrObject.setProperty(CdrProperty.CALL_START_TIME, callStartDate);
    // prevent empty stop time
    if (this.cdrObject.getProperty(CdrProperty.CALL_STOP_TIME) == null) {
      this.cdrObject.setProperty(CdrProperty.CALL_STOP_TIME, callStartDate);
    }
  }

  /**
   * From array of integers obtains two Strings (stop time and stop date). Both
   * are saved in hashtable. It also obtain a flag and saved in the hashtable as
   * an Integer.
   * 
   * @see CdrProperty
   */
  public void setType103() {
    this.sizeCounter++;
    Date callEndDate = getDateTime(intBuf, this.sizeCounter);
    int flag103 = (intBuf[this.sizeCounter + 7] & 0x01);
    this.sizeCounter = this.sizeCounter + 8;
    this.cdrObject.setProperty(CdrProperty.CALL_STOP_TIME, callEndDate);
    this.cdrObject.setProperty(CdrProperty.FLAG_103, flag103 == 1);
  }

  /**
   * From array of integers obtains Long (tariff). It also saves value in the
   * hashtable.
   * 
   * @see CdrProperty
   */
  public void setType104() {
    this.sizeCounter++;
    long tariff = 65536 * intBuf[this.sizeCounter] + 256 * intBuf[this.sizeCounter + 1] + intBuf[this.sizeCounter + 2];
    this.sizeCounter = this.sizeCounter + 3;
    this.cdrObject.setProperty(CdrProperty.CHARGE_UNITS, new Long(tariff));
  }

  /**
   * From array of integers obtains two Strings (bearer service and teleservice).
   * Both are saved in the hashtable.
   * 
   * @see CdrProperty
   */
  public void setType105() {
    this.sizeCounter++;
    int bearerService = (intBuf[this.sizeCounter]);
    int teleservice = (intBuf[this.sizeCounter + 1]);
    this.sizeCounter = this.sizeCounter + 2;
    this.cdrObject.setProperty(CdrProperty.BEARER_SERVICE, bearerService);
    this.cdrObject.setProperty(CdrProperty.TELESERVICE, teleservice);
  }

  /**
   * From array of integers obtains String (source subscribe number). It also
   * saves this value in the hashtable.
   * 
   * @see CdrProperty
   */
  public void setType106() {
    this.sizeCounter++;
    // int sourceSubscNum = intBuf[this.sizeCounter];
    short serviceId = (short) intBuf[this.sizeCounter];
    this.sizeCounter++;

    SupplementaryService ss = new SupplementaryService();
    ss.setServiceId(serviceId);
    this.cdrObject.setProperty(CdrProperty.SUPPLEMENTARY_SERVICE_INFO, ss);
    /*
     * this.cdrObject.setProperty(CdrProperty.ORIG_SUPPLEMENTARY_SERVICE,
     * sourceSubscNum);
     */
  }

  /**
   * From array of integers obtains String (sink subscribe number). It also saves
   * this value in the hashtable.
   * 
   * @see CdrProperty
   */
  public void setType107() {
    this.sizeCounter++;
    // int sinkSubscNum = intBuf[this.sizeCounter];
    short serviceId = (short) intBuf[this.sizeCounter];
    this.sizeCounter++;
    /*
     * this.cdrObject.setProperty(CdrProperty.TERM_SUPPLEMENTARY_SERVICE,
     * sinkSubscNum);
     */
    SupplementaryService ss = new SupplementaryService();
    ss.setServiceId(serviceId);
    this.cdrObject.setProperty(CdrProperty.SUPPLEMENTARY_SERVICE_INFO_CALLED, ss);
  }

  /**
   * From array of integers obtains two Integers (input type and supplemental
   * service). Both values are saved in the hashtable.
   * 
   * @see CdrProperty
   */
  public void setType108() {
    this.sizeCounter++;
    char[] infoType = new char[1];
    infoType[0] = (char) intBuf[this.sizeCounter];
    int inputType = (int) intBuf[this.sizeCounter] & 0xFF;
    short serviceId = -1;
    serviceId = (short) intBuf[this.sizeCounter + 1];
    this.sizeCounter = this.sizeCounter + 2;
    // this.cdrObject.setProperty(CdrProperty.TYPE_OF_INPUT, inputType);

    SupplementaryService ss = new SupplementaryService();
    ss.setServiceId(serviceId);
    ss.setServiceInfo(infoType);
    ss.setInputType(inputType);
    this.cdrObject.setProperty(CdrProperty.SUPPLEMENTARY_SERVICE_INFO_SCI, ss);
    /*
     * this.cdrObject.setProperty(CdrProperty.FACILITY_INPUT,
     * supplementalService);
     */
  }

  /**
   * From array of integers obtains a String (character sequence). Value is saved
   * in the hashtable.
   * 
   * @see CdrProperty
   */
  public void setType109() {
    this.sizeCounter += 2;
    temp = typeLength / 2 + typeLength % 2;

    StringBuilder charSeq = new StringBuilder(typeLength);
    for (int i = 0; i < temp; i++) {
      charSeq.append(intBuf[this.sizeCounter] >> 4);
      if (i * 2 + 2 <= typeLength)
        charSeq.append(intBuf[this.sizeCounter] & 0x0F);
      this.sizeCounter++;
    }

    this.cdrObject.setProperty(CdrProperty.DIGIT_STRING, charSeq.toString());
  }

  /**
   * From array of integers obtains an Integer (source category). Value is saved
   * in the hashtable.
   * 
   * @see CdrProperty
   */
  public void setType110() {
    this.sizeCounter++;
    int sourceCat = intBuf[this.sizeCounter];
    this.sizeCounter++;
    this.cdrObject.setProperty(CdrProperty.ORIGIN_CATEGORY, sourceCat);
  }

  /**
   * From array of integers obtains an Integer (charging course). Value is saved
   * in the hashtable.
   * 
   * @see CdrProperty
   * @see CdrObject#setProperty(String, Object)
   */
  public void setType111() {
    this.sizeCounter++;
    int tariffCourse = intBuf[this.sizeCounter];
    this.sizeCounter++;
    this.cdrObject.setProperty(CdrProperty.TARIFF_DIRECTION, tariffCourse);
  }

  /**
   * From array of integers obtains an Integer (failure cause). Value is saved in
   * the hashtable.
   * 
   * @see CdrProperty
   */
  public void setType112() {
    this.sizeCounter++;
    int failureCause = (intBuf[this.sizeCounter]);
    if ((failureCause >= 9) && (failureCause <= 15))
      failureCause = 9;
    this.sizeCounter++;
    this.cdrObject.setProperty(CdrProperty.FAILURE_CAUSE, failureCause);
  }

  /**
   * From array of integers obtains five Integers (entry transfer group, entry
   * transfer, entry module, entry port and entry channel). All values are also
   * saved in the hashtable as an Integer.
   * 
   * @see CdrProperty
   */
  public void setType113() {
    this.sizeCounter++;

    int transGroup113 = 256 * intBuf[this.sizeCounter] + intBuf[this.sizeCounter + 1];
    int trans113 = 256 * intBuf[this.sizeCounter + 2] + intBuf[this.sizeCounter + 3];
    int module113 = intBuf[this.sizeCounter + 4];
    int port113 = 256 * intBuf[this.sizeCounter + 5] + intBuf[this.sizeCounter + 6];
    int channel113 = intBuf[this.sizeCounter + 7];
    this.sizeCounter = this.sizeCounter + 8;

    this.cdrObject.setProperty(CdrProperty.INTRUNK_GROUP_ID, transGroup113);
    this.cdrObject.setProperty(CdrProperty.INTRUNK_ID, trans113);
    this.cdrObject.setProperty(CdrProperty.INMODULE_ID, module113);
    this.cdrObject.setProperty(CdrProperty.INPORT_ID, port113);
    this.cdrObject.setProperty(CdrProperty.INCHANNEL_ID, channel113);
  }

  /**
   * From array of integers obtains five Integers (exit transfer group, exit
   * transfer, exit module, exit port and exit channel). All values are also saved
   * in the hashtable as an Integer.
   * 
   * @see CdrProperty
   */
  public void setType114() {
    this.sizeCounter++;
    int transGroup114 = 256 * intBuf[this.sizeCounter] + intBuf[this.sizeCounter + 1];
    int trans114 = 256 * intBuf[this.sizeCounter + 2] + intBuf[this.sizeCounter + 3];
    int module114 = intBuf[this.sizeCounter + 4];
    int port114 = 256 * intBuf[this.sizeCounter + 5] + intBuf[this.sizeCounter + 6];
    int channel114 = intBuf[this.sizeCounter + 7];
    this.sizeCounter = this.sizeCounter + 8;

    this.cdrObject.setProperty(CdrProperty.OUTTRUNK_GROUP_ID, new Integer(transGroup114));
    this.cdrObject.setProperty(CdrProperty.OUTTRUNK_ID, trans114);
    this.cdrObject.setProperty(CdrProperty.OUTMODULE_ID, module114);
    this.cdrObject.setProperty(CdrProperty.OUTPORT_ID, port114);
    this.cdrObject.setProperty(CdrProperty.OUTCHANNEL_ID, channel114);
  }

  /**
   * From array of integers obtains a Long (duration). Value is saved in the
   * hashtable.
   * 
   * @see CdrProperty
   * @see CdrObject#setProperty(String, Object)
   */
  public void setType115() {
    this.sizeCounter++;
    long duration = 16777216 * intBuf[this.sizeCounter] + 65536 * intBuf[this.sizeCounter + 1] + 256 * intBuf[this.sizeCounter + 2]
        + intBuf[this.sizeCounter + 3];
    if (intBuf[sizeCounter] < 0 || intBuf[sizeCounter + 1] < 0 || intBuf[sizeCounter + 2] < 0 || intBuf[sizeCounter + 3] < 0) {
      System.out.println("intBuf[0]: " + intBuf[sizeCounter]);
      System.out.println("intBuf[1]: " + intBuf[sizeCounter] + 1);
      System.out.println("intBuf[2]: " + intBuf[sizeCounter] + 2);
      System.out.println("intBuf[3]: " + intBuf[sizeCounter] + 3);
      System.out.println("Duration: " + duration);
    }
    this.sizeCounter = this.sizeCounter + 4;
    this.cdrObject.setProperty(CdrProperty.CALL_DURATION, duration);
  }

  /**
   * From array of integers obtains a String (control addition). Value is saved in
   * the hashtable.
   * 
   * @see CdrProperty
   * @see CdrObject#setProperty(String, Object)
   */
  public void setType116() {
    this.sizeCounter = this.sizeCounter + 4;
  }

  /**
   * From array of integers obtains two Integers (BGID and CGID). Both values are
   * saved in hashtable.
   * 
   * @see CdrProperty
   */
  public void setType117() {
    this.sizeCounter += 2;
    int BGID = 16777216 * intBuf[this.sizeCounter] + 65536 * intBuf[this.sizeCounter + 1] + 256 * intBuf[this.sizeCounter + 2] + intBuf[this.sizeCounter + 3];
    this.sizeCounter = this.sizeCounter + 4;
    int CGID = 16777216 * intBuf[this.sizeCounter] + 65536 * intBuf[this.sizeCounter + 1] + 256 * intBuf[this.sizeCounter + 2] + intBuf[this.sizeCounter + 3];
    this.sizeCounter = this.sizeCounter + 4;
    this.cdrObject.setProperty(CdrProperty.CENTREX_GROUP, CGID);
    this.cdrObject.setProperty(CdrProperty.BUSINESS_GROUP, BGID);
  }

  /**
   * From array of integers obtains two values. One as String (CAC number) and
   * other as Integer (CAC type). Both values are saved in the hashtable.
   * 
   * @see CdrProperty
   */
  public void setType118() {
    int cacSizeOctets = intBuf[sizeCounter + 1] - 3;
    this.sizeCounter += 2;
    int cacType = ((intBuf[this.sizeCounter] & 0xE0) >> 5);
    int cacPref = (intBuf[this.sizeCounter] & 0x18) >> 3;
    int digNum = (intBuf[this.sizeCounter] & 0x07);
    this.sizeCounter++;
    
    int cacNumber = 0;
    int[] sizeDifference = {1, 256, 65536, 16777216};
    for (int i = 1; i <= cacSizeOctets; i++) {
      cacNumber += intBuf[sizeCounter++] * sizeDifference[cacSizeOctets - i];
    }
    
    int cacPrefixNumber = cacNumber / (int) Math.pow(10, digNum - cacPref);
    
    this.cdrObject.setProperty(CdrProperty.CAC_PREFIX, cacPrefixNumber);
    this.cdrObject.setProperty(CdrProperty.CAC_TYPE, cacType);
    this.cdrObject.setProperty(CdrProperty.CAC_NUMBER, cacNumber);
  }

  /**
   * From array of integers obtains a String (original calling party number).
   * Value is saved in the hashtable.
   * 
   * @see CdrProperty
   */
  public void setType119() {
    this.sizeCounter++;
    int numLen = intBuf[this.sizeCounter + 1];
    this.sizeCounter += 2;
    StringBuilder callPartyNum = new StringBuilder(numLen);
    temp = numLen / 2 + numLen % 2;
    for (int i = 0; i < temp; i++) {

      int tmpInt = intBuf[sizeCounter] >> 4;
      if (tmpInt < 10) {
        callPartyNum.append(tmpInt);
      } else {
        char c = (char) (tmpInt + 55);
        callPartyNum.append(c);
      }
      if (i * 2 + 2 <= numLen) {
        int tmpInt2 = intBuf[sizeCounter] & 0x0F;
        if (tmpInt2 < 10) {
          callPartyNum.append(tmpInt2);
        } else {
          char c = (char) (tmpInt2 + 55);
          callPartyNum.append(c);
        }
      }
      this.sizeCounter++;
    }
    PartyNumber pn = new PartyNumber();
    pn.setNumber(callPartyNum.toString());
    this.cdrObject.setProperty(CdrProperty.ORIGINAL_CPN, pn);
  }

  /**
   * From array of integers obtains five Integers (exit transfer group, exit
   * transfer, exit module, exit port and exit channel). All values are also saved
   * in the hashtable as Integers.
   * 
   * @see CdrProperty
   */
  public void setType120() {
    this.sizeCounter = this.sizeCounter + 2;
    int typeReq = intBuf[this.sizeCounter];
    this.sizeCounter++;
    long int2longNum = intBuf[this.sizeCounter];
    long tariffUnitNum = 16777216 * int2longNum + 65536 * intBuf[this.sizeCounter + 1] + 256 * intBuf[this.sizeCounter + 2] + intBuf[this.sizeCounter + 3];
    this.sizeCounter = this.sizeCounter + 4;
    // long int2longState = intBuf[this.sizeCounter];
    long newState = 16777216 * int2longNum + 65536 * intBuf[this.sizeCounter + 1] + 256 * intBuf[this.sizeCounter + 2] + intBuf[this.sizeCounter + 3];
    this.sizeCounter = this.sizeCounter + 4;
    String newDate = "" + (intBuf[this.sizeCounter]) + (intBuf[this.sizeCounter + 1]) + "-" + (intBuf[this.sizeCounter + 2]) + "-"
        + (intBuf[this.sizeCounter + 3]);
    this.sizeCounter = this.sizeCounter + 4;
    this.cdrObject.setProperty(CdrProperty.REQUEST_TYPE, new Integer(typeReq));
    this.cdrObject.setProperty(CdrProperty.ADDED_CHARGE_UNITS, new Long(tariffUnitNum));
    this.cdrObject.setProperty(CdrProperty.NEW_BALANCE, new Long(newState));
    this.cdrObject.setProperty(CdrProperty.NEW_EXPIRED_DATE, newDate);
  }

  /**
   * From array of integers obtains an Integer (call release cause). Value is
   * saved in the hashtable.
   * 
   * @see CdrProperty
   */
  public void setType121() {
    this.sizeCounter += 2;
    int releaseValue = 256 * intBuf[this.sizeCounter] + intBuf[this.sizeCounter + 1];
    this.sizeCounter += 2;
    int t = (intBuf[this.sizeCounter]);
    int standard = (t >> 5) & 0x3;
    int location = t & 0xF;
    this.sizeCounter++;

    this.cdrObject.setProperty(CdrProperty.CALL_RELEASE_VALUE, releaseValue);
    this.cdrObject.setProperty(CdrProperty.CALL_RELEASE_STANDARD, standard);
    this.cdrObject.setProperty(CdrProperty.CALL_RELEASE_LOCATION, location);
  }

  public void setType122() {
    this.sizeCounter += 2;
    int cbno = 256 * intBuf[this.sizeCounter] + intBuf[this.sizeCounter + 1];
    this.sizeCounter += 2;
    this.sizeCounter++;
    this.cdrObject.setProperty(CdrProperty.IE122_CBNO, cbno);
  }

  /**
   * From array of integers obtains two Integers (Time before ringing and Ringing
   * time before accept). Both values are saved in hashtable.
   * 
   * @see CdrProperty
   */
  public void setType124() {
    this.sizeCounter += 2;
    int TBR = 16777216 * intBuf[this.sizeCounter] + 65536 * intBuf[this.sizeCounter + 1] + 256 * intBuf[this.sizeCounter + 2] + intBuf[this.sizeCounter + 3];
    this.sizeCounter = this.sizeCounter + 4;
    int RTBA = 16777216 * intBuf[this.sizeCounter] + 65536 * intBuf[this.sizeCounter + 1] + 256 * intBuf[this.sizeCounter + 2] + intBuf[this.sizeCounter + 3];
    this.sizeCounter = this.sizeCounter + 4;
    this.cdrObject.setProperty(CdrProperty.TIME_BEFORE_RINGING, TBR);
    this.cdrObject.setProperty(CdrProperty.RINGING_TIME_BEFORE_ANSWER, RTBA);
  }
  
  public void setType128() {
    int length = intBuf[sizeCounter + 1];
    int rxCodecType = intBuf[sizeCounter + 2] & 0xff;
    int txCodecType = intBuf[sizeCounter + 3] & 0xff;
    int side = intBuf[sizeCounter + 12] >> 7;
    int callType = intBuf[sizeCounter + 12] & 0x7f;
    sizeCounter += length;
    cdrObject.setProperty(CdrProperty.VOIP_RX_CODEC_TYPE, rxCodecType);
    cdrObject.setProperty(CdrProperty.VOIP_TX_CODEC_TYPE, txCodecType);
    cdrObject.setProperty(CdrProperty.VOIP_SIDE, side);
    cdrObject.setProperty(CdrProperty.VOIP_CALL_TYPE, callType);
  }
  
  public void setType129() {
    int length = intBuf[sizeCounter + 1];
    long rxPackets = 16777216 * intBuf[sizeCounter + 3] + 65536 * intBuf[sizeCounter + 4] + 256 * intBuf[sizeCounter + 5] + intBuf[sizeCounter + 6];
    long txPackets = 16777216 * intBuf[sizeCounter + 7] + 65536 * intBuf[sizeCounter + 8] + 256 * intBuf[sizeCounter + 9] + intBuf[sizeCounter + 10];
    long rxOctets = 16777216 * intBuf[sizeCounter + 11] + 65536 * intBuf[sizeCounter + 12] + 256 * intBuf[sizeCounter + 13] + intBuf[sizeCounter + 14];
    long txOctets = 16777216 * intBuf[sizeCounter + 15] + 65536 * intBuf[sizeCounter + 16] + 256 * intBuf[sizeCounter + 17] + intBuf[sizeCounter + 18];
    long packetsLost = 16777216 * intBuf[sizeCounter + 19] + 65536 * intBuf[sizeCounter + 20] + 256 * intBuf[sizeCounter + 21] + intBuf[sizeCounter + 22];
    int averageJitter = intBuf[sizeCounter + 23];
    int averageLatency = intBuf[sizeCounter + 24];
    sizeCounter += length;
    cdrObject.setProperty(CdrProperty.VOIP_RX_PACKETS, rxPackets);
    cdrObject.setProperty(CdrProperty.VOIP_TX_PACKETS, txPackets);
    cdrObject.setProperty(CdrProperty.VOIP_RX_OCTETS, rxOctets);
    cdrObject.setProperty(CdrProperty.VOIP_TX_OCTETS, txOctets);
    cdrObject.setProperty(CdrProperty.VOIP_PACKETS_LOST, packetsLost);
    cdrObject.setProperty(CdrProperty.VOIP_AVERAGE_JITTER, averageJitter);
    cdrObject.setProperty(CdrProperty.VOIP_AVERAGE_LATENCY, averageLatency);
  }

  public void setType131() {
    int start = this.sizeCounter;
    this.sizeCounter += 1;
    int numLen = intBuf[this.sizeCounter];
    this.sizeCounter += 3;
    int lengthOfNumber = intBuf[this.sizeCounter];
    this.sizeCounter += 1;
    StringBuilder ndn = new StringBuilder(lengthOfNumber);
    temp = lengthOfNumber / 2 + lengthOfNumber % 2;
    for (int i = 0; i < temp; i++) {
      int tmpInt = intBuf[sizeCounter + i] >> 4;
      if (tmpInt < 10) {
        ndn.append(tmpInt);
      } else {
        char c = (char) (tmpInt + 55);
        ndn.append(c);
      }
      if (i * 1 + 2 <= temp) {
        int tmpInt2 = intBuf[sizeCounter + i] & 0x0F;
        if (tmpInt2 < 10) {
          ndn.append(tmpInt2);
        } else {
          char c = (char) (tmpInt2 + 55);
          ndn.append(c);
        }
      }
    }
    if (numLen != 0) {
      this.sizeCounter = start + numLen;
    }
    this.cdrObject.setProperty(CdrProperty.IE131_NDN, ndn.toString());
  }
  
  public void setType132() {
    int length = intBuf[sizeCounter + 1];
    int echoReturnLoss = intBuf[sizeCounter + 3];
    long packetsSentAndLost = 16777216 * intBuf[sizeCounter + 4] + 65536 * intBuf[sizeCounter + 5] + 256 * intBuf[sizeCounter + 6] + intBuf[sizeCounter + 7];
    int maxPacketsLostInBurst = 256 * intBuf[sizeCounter + 8] + intBuf[sizeCounter + 9];
    int maxJitter = intBuf[sizeCounter + 10] & 0xff;
    int minJitter = intBuf[sizeCounter + 11] & 0xff;
    int rxMos = intBuf[sizeCounter + 12] & 0xff;
    int txMos = intBuf[sizeCounter + 13] & 0xff;
    int faxModulationType = intBuf[sizeCounter + 14] & 0xff;
    int faxTransferRate = intBuf[sizeCounter + 15] & 0xff;
    int faxModemRetrains = intBuf[sizeCounter + 16] & 0xff;
    int faxPagesTransfered = 256 * intBuf[sizeCounter + 17] + intBuf[sizeCounter + 18];
    int faxPagesRepeated = 256 * intBuf[sizeCounter + 19] + intBuf[sizeCounter + 20];
    sizeCounter += length;
    cdrObject.setProperty(CdrProperty.VOIP_ECHO_RETURN_LOSS, echoReturnLoss);
    cdrObject.setProperty(CdrProperty.VOIP_PACKETS_SENT_AND_LOST, packetsSentAndLost);
    cdrObject.setProperty(CdrProperty.VOIP_MAX_PACKETS_LOST_IN_BURST, maxPacketsLostInBurst);
    cdrObject.setProperty(CdrProperty.VOIP_MAX_JITTER, maxJitter);
    cdrObject.setProperty(CdrProperty.VOIP_MIN_JITTER, minJitter);
    cdrObject.setProperty(CdrProperty.VOIP_RX_MOS, rxMos);
    cdrObject.setProperty(CdrProperty.VOIP_TX_MOS, txMos);
    cdrObject.setProperty(CdrProperty.VOIP_FAX_MODULATION_TYPE, faxModulationType);
    cdrObject.setProperty(CdrProperty.VOIP_FAX_TRANSFER_RATE, faxTransferRate);
    cdrObject.setProperty(CdrProperty.VOIP_FAX_MODEM_RETRAINS, faxModemRetrains);
    cdrObject.setProperty(CdrProperty.VOIP_FAX_PAGES_TRANSFERRED, faxPagesTransfered);
    cdrObject.setProperty(CdrProperty.VOIP_FAX_PAGES_REPEATED, faxPagesRepeated);
  }

  public void setType133() {
    this.sizeCounter += 2;
    int BGID = 16777216 * intBuf[this.sizeCounter] + 65536 * intBuf[this.sizeCounter + 1] + 256 * intBuf[this.sizeCounter + 2] + intBuf[this.sizeCounter + 3];
    this.sizeCounter = this.sizeCounter + 4;
    int CGID = 16777216 * intBuf[this.sizeCounter] + 65536 * intBuf[this.sizeCounter + 1] + 256 * intBuf[this.sizeCounter + 2] + intBuf[this.sizeCounter + 3];
    this.sizeCounter = this.sizeCounter + 4;
    int CCT = intBuf[this.sizeCounter];
    this.sizeCounter = this.sizeCounter + 2;
    this.cdrObject.setProperty(CdrProperty.CENTREX_GROUP_B, CGID);
    this.cdrObject.setProperty(CdrProperty.BUSINESS_GROUP_B, BGID);
    this.cdrObject.setProperty(CdrProperty.CENTREX_CALL_TYPE_B, CCT);
  }
  
  public void setType134() {
    int startIndex = sizeCounter;
    ++sizeCounter;
    int length = intBuf[sizeCounter] & 0xff;
    Integer[] additionalStatData = new Integer[5];
    ++sizeCounter;
    int flag[] = new int[8];
    flag[0] = (intBuf[sizeCounter] & 0x01);
    flag[1] = ((intBuf[sizeCounter] & 0x02) >> 1);
    flag[2] = ((intBuf[sizeCounter] & 0x04) >> 2);
    flag[3] = ((intBuf[sizeCounter] & 0x08) >> 3);
    flag[4] = ((intBuf[sizeCounter] & 0x10) >> 4);
    flag[5] = ((intBuf[sizeCounter] & 0x20) >> 5);
    flag[6] = ((intBuf[sizeCounter] & 0x40) >> 6);
    flag[7] = (intBuf[sizeCounter] >> 7);
    for (int i = 0; i < flag.length; i++) {
      if (flag[i] == 1) {
        if (i == 0 || i == 1) {
          additionalStatData[i] = 256 * intBuf[++sizeCounter] + intBuf[++sizeCounter];
        } else {
          additionalStatData[i] = 1 * intBuf[++sizeCounter];
        }
      }
    }
    sizeCounter = startIndex + length;
    if (additionalStatData[0] != null) {
      cdrObject.setProperty(CdrProperty.CALLING_SUBSCRIBER_GROUP, additionalStatData[0]);
    }
    if (additionalStatData[1] != null) {
      cdrObject.setProperty(CdrProperty.CALLED_SUBSCRIBER_GROUP, additionalStatData[1]);
    }
    if (additionalStatData[2] != null) {
      cdrObject.setProperty(CdrProperty.ORIG_SIDE_SND_LINE_TYPE, additionalStatData[2]);
    }
    if (additionalStatData[3] != null) {
      cdrObject.setProperty(CdrProperty.TERM_SIDE_SND_LINE_TYPE, additionalStatData[3]);
    }
    if (additionalStatData[4] != null) {
      cdrObject.setProperty(CdrProperty.CALL_RELEASING_SIDE, additionalStatData[4]);
    }
  }

  public void setType135() {
    int start = this.sizeCounter;
    this.sizeCounter += 1;
    int numLen = intBuf[this.sizeCounter];
    this.sizeCounter += 1;
    int lengthOfICID = intBuf[this.sizeCounter];
    byte[] ICID = new byte[lengthOfICID];
    this.sizeCounter += 1;
    for (int i = 0; i < lengthOfICID; i++) {
      ICID[i] = (byte) intBuf[sizeCounter + i];
    }
    this.sizeCounter += lengthOfICID;
    int lengthOfHostname = intBuf[this.sizeCounter];
    byte[] HOSTNAME = new byte[lengthOfHostname];
    this.sizeCounter += 1;
    for (int i = 0; i < lengthOfHostname; i++) {
      HOSTNAME[i] = (byte) intBuf[sizeCounter + i];
    }
    if (numLen != 0) {
      this.sizeCounter = start + numLen;
    }
    try {
      String icid = new String(ICID, "ASCII");
      String hostname = new String(HOSTNAME, "ASCII");
      this.cdrObject.setProperty(CdrProperty.ICID, icid);
      this.cdrObject.setProperty(CdrProperty.ICID_HOSTNAME, hostname);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

  }

  // Supplementary service additional info
  public void setType137() {
    int ieLength = intBuf[this.sizeCounter + 1];
    int infoLength = ieLength - 4;
    // int serviceId = intBuf[this.sizeCounter + 2] *256 +
    // intBuf[this.sizeCounter + 3];
    short serviceId = (short) (intBuf[this.sizeCounter + 2] * 256 + intBuf[this.sizeCounter + 3]);
    /*
     * StringBuilder sb = new StringBuilder(); boolean skipFirst = true; for (int i
     * = 0; i < infoLength; i++) { if (skipFirst) skipFirst = false; else
     * sb.append(", "); sb.append(this.intBuf[this.sizeCounter + 4 + i]); } String
     * info = sb.toString();
     */

    char[] info = new char[infoLength];
    for (int i = 0; i < info.length; i++) {
      info[i] = (char) intBuf[this.sizeCounter + 4 + i];
    }

    SupplementaryService ss = new SupplementaryService();
    ss.setServiceId(serviceId);
    ss.setServiceInfo(info);
    if (ieLength == 0) {
      // TODO throw exception
    }
    this.sizeCounter += ieLength;
    this.cdrObject.setProperty(CdrProperty.SUPPLEMENTARY_SERVICE_INFO, ss);
  }

  public void setType138() {
    int start = sizeCounter;
    int length = intBuf[this.sizeCounter + 1];
    PartyNumber pn = new PartyNumber();
    // Nature of address indicator
    this.sizeCounter += 2;
    pn.setNatureOfAddress(intBuf[this.sizeCounter]);
    // Numbering plan indicator
    this.sizeCounter++;
    pn.setNumberingPlan(intBuf[this.sizeCounter] & 0xFF);
    // Presentation indicator
    this.sizeCounter++;
    pn.setPresentationInd((intBuf[this.sizeCounter] & 0xF0) >> 4);
    // Screening indicator NOT handled
    pn.setScreeningInd((intBuf[this.sizeCounter] & 0x0F));
    // LAC handling
    this.sizeCounter++;
    int lacLen = ((intBuf[this.sizeCounter] & 0xE0) >> 5);
    pn.setLacLength(lacLen);
    // DN handling
    int wholeNumLen = intBuf[sizeCounter] & 0x1F;
    this.sizeCounter += 1;
    temp = wholeNumLen / 2 + wholeNumLen % 2;
    StringBuilder subscriberNumber = new StringBuilder(temp);
    for (int i = 0; i < temp; i++) {
      int tmpInt = intBuf[sizeCounter + i] >> 4;
      if (tmpInt < 10) {
        subscriberNumber.append(tmpInt);
      } else {
        char c = (char) (tmpInt + 55);
        subscriberNumber.append(c);
      }
      if (i * 2 + 2 <= wholeNumLen) {
        int tmpInt2 = intBuf[sizeCounter + i] & 0x0F;
        if (tmpInt2 < 10) {
          subscriberNumber.append(tmpInt2);
        } else {
          char c = (char) (tmpInt2 + 55);
          subscriberNumber.append(c);
        }
      }
    }
    if (length != 0) {
      sizeCounter = start + length;
    }
    pn.setNumber(subscriberNumber.toString());
    this.cdrObject.setProperty(CdrProperty.ORIGINAL_CPN, pn);
  }

  public void setType139() {
    int start = sizeCounter;
    int length = intBuf[this.sizeCounter + 1];
    this.sizeCounter += 5;
//    int LAC = ((intBuf[this.sizeCounter] & 0xE0) >> 5); // LAC length
    temp = (intBuf[this.sizeCounter] & 0x1F);
    this.sizeCounter += 1;
    StringBuilder subscriberNumber = new StringBuilder(temp);
    temp = temp / 2 + temp % 2;
    for (int i = 0; i < temp; i++) {
      int tmpInt = intBuf[sizeCounter + i] >> 4;
      if (tmpInt < 10) {
        subscriberNumber.append(tmpInt);
      } else {
        char c = (char) (tmpInt + 55);
        subscriberNumber.append(c);
      }
      if (i * 2 + 2 <= temp) {
        int tmpInt2 = intBuf[sizeCounter + i] & 0x0F;
        if (tmpInt2 < 10) {
          subscriberNumber.append(tmpInt2);
        } else {
          char c = (char) (tmpInt2 + 55);
          subscriberNumber.append(c);
        }
      }
    }
    if (length != 0) {
      sizeCounter = start + length;
    }
    this.cdrObject.setProperty(CdrProperty.IE139_ADDITIONAL_CGN, subscriberNumber.toString());
  }

  // Called party number
  public void setType140() {
    int start = this.sizeCounter;
    this.sizeCounter += 2;

    PartyNumber pn = new PartyNumber();
    pn.setNatureOfAddress(intBuf[this.sizeCounter]);
    this.sizeCounter++;

    pn.setNumberingPlan(intBuf[this.sizeCounter] & 0xFF);
    this.sizeCounter++;

    int networkNumLen = intBuf[sizeCounter] >> 5;
    pn.setLacLength(networkNumLen);
    int wholeNumLen = intBuf[sizeCounter] & 0x1F;
    this.sizeCounter++;

    StringBuilder subscriberNumber = new StringBuilder(networkNumLen + wholeNumLen);
    temp = (wholeNumLen) / 2 + (wholeNumLen) % 2;

    for (int i = 0; i < temp; i++) {
      // handling of FCC in the number
      int tmpInt = intBuf[sizeCounter + i] >> 4;
      // if hex number contains (A,B,C,D,E,F) -> convert it to character
      if (tmpInt < 10) {
        // handle number
        subscriberNumber.append(tmpInt);
      } else {
        // convert hex number to appropriate character (hex A to char A)
        char c = (char) (tmpInt + 55);
        subscriberNumber.append(c);
      }

      if (i * 2 + 2 <= wholeNumLen) {
        int tmpInt2 = intBuf[sizeCounter + i] & 0x0F;
        // convert hex number to appropriate number or character
        if (tmpInt2 < 10) {
          // handle number
          subscriberNumber.append(tmpInt2);
        } else {
          // handle character
          // int 55 + A (hex) = 65 -> in ASCII table for "A"
          char c = (char) (tmpInt2 + 55);
          subscriberNumber.append(c);
        }
      }
    }
    pn.setNumber(subscriberNumber.toString());
    if (typeLength != 0) {
      this.sizeCounter = start + typeLength;
    }
    this.cdrObject.setProperty(CdrProperty.CALLED_NUMBER_FORMATTED, pn);
  }

  private long decodeBinary(int pos, int numOfBytes) {
    if (numOfBytes < 1) {
      return 0;
    }
    long value = 0;
    String binNum = "";
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < numOfBytes; i++) {
      binNum = Integer.toBinaryString(this.intBuf[pos + i]);
      // add leading zeros
      for (int j = 0; j < 8 - binNum.length(); j++) {
        sb.append("0");
      }
      sb.append(binNum);
    }
    binNum = sb.toString();

    value = Long.parseLong(binNum, 2);
    return value;
  }


  /**
   * Decode binary arrays with MSB in last filed.
   * 
   * @param pos
   * @param numOfBytes
   * @return
   */
  private long decodeBinaryFirstByteLsb(int pos, int numOfBytes) {
    if (numOfBytes < 1) {
      return 0;
    }
    long value = 0;
    String binNum = "";
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < numOfBytes; i++) {
      binNum = Integer.toBinaryString(this.intBuf[pos + i]);
      // add leading zeros
      for (int j = 0; j < 8 - binNum.length(); j++) {
        sb.append("0");
      }
      sb.insert(0, binNum);
    }
    binNum = sb.toString();
    value = Long.parseLong(binNum, 2);
    return value;
  }


  public void setType142() {
    int start = this.sizeCounter;
    this.sizeCounter += 1;
    int numLen = intBuf[this.sizeCounter];
    this.sizeCounter += 1;
    int callType = ((intBuf[this.sizeCounter] & 0xF0) >> 4);
    int thirdPartyType = ((intBuf[this.sizeCounter] & 0x0F));
    this.sizeCounter += 1;
    int natureOdAddr = intBuf[this.sizeCounter];
    this.sizeCounter += 1;
    int numberingPlan = ((intBuf[this.sizeCounter] & 0x0F));
    this.sizeCounter += 1;
    int lengthOfNumber = intBuf[this.sizeCounter];
    StringBuilder ie142num = new StringBuilder(lengthOfNumber);
    temp = lengthOfNumber / 2 + lengthOfNumber % 2;
    this.sizeCounter += 1;
    for (int i = 0; i < temp; i++) {
      int tmpInt = intBuf[sizeCounter + i] >> 4;
      if (tmpInt < 10) {
        ie142num.append(tmpInt);
      } else {
        char c = (char) (tmpInt + 55);
        ie142num.append(c);
      }
      if (i * 1 + 2 <= temp) {
        int tmpInt2 = intBuf[sizeCounter + i] & 0x0F;
        if (tmpInt2 < 10) {
          ie142num.append(tmpInt2);
        } else {
          char c = (char) (tmpInt2 + 55);
          ie142num.append(c);
        }
      }
    }
    if (numLen != 0) {
      this.sizeCounter = start + numLen;
    }
    this.cdrObject.setProperty(CdrProperty.CALL_TYPE, callType);
    this.cdrObject.setProperty(CdrProperty.THIRD_PARTY_TYPE, thirdPartyType);
    this.cdrObject.setProperty(CdrProperty.NATURE_OF_ADDR, natureOdAddr);
    this.cdrObject.setProperty(CdrProperty.NUMBERING_PLAN, numberingPlan);
    this.cdrObject.setProperty(CdrProperty.LENGTH_OF_NUMBER, lengthOfNumber);
    this.cdrObject.setProperty(CdrProperty.IE142_NUMBER, ie142num.toString());
  }

  public void setType143() {
    int start = this.sizeCounter;
    this.sizeCounter += 2;

    PartyNumber pn = new PartyNumber();
    pn.setNatureOfAddress(intBuf[this.sizeCounter]);
    this.sizeCounter++;

    pn.setNumberingPlan(intBuf[this.sizeCounter] & 0xFF);
    this.sizeCounter++;

    int networkNumLen = intBuf[sizeCounter] >> 5;
    pn.setLacLength(networkNumLen);
    int wholeNumLen = intBuf[sizeCounter] & 0x1F;
    this.sizeCounter++;

    StringBuilder subscriberNumber = new StringBuilder(networkNumLen + wholeNumLen);
    temp = (wholeNumLen) / 2 + (wholeNumLen) % 2;

    for (int i = 0; i < temp; i++) {
      // handling of FCC in the number
      int tmpInt = intBuf[sizeCounter + i] >> 4;
      // if hex number contains (A,B,C,D,E,F) -> convert it to character
      if (tmpInt < 10) {
        // handle number
        subscriberNumber.append(tmpInt);
      } else {
        // convert hex number to appropriate character (hex A to char A)
        char c = (char) (tmpInt + 55);
        subscriberNumber.append(c);
      }

      if (i * 2 + 2 <= wholeNumLen) {
        int tmpInt2 = intBuf[sizeCounter + i] & 0x0F;
        // convert hex number to appropriate number or character
        if (tmpInt2 < 10) {
          // handle number
          subscriberNumber.append(tmpInt2);
        } else {
          // handle character
          // int 55 + A (hex) = 65 -> in ASCII table for "A"
          char c = (char) (tmpInt2 + 55);
          subscriberNumber.append(c);
        }
      }
    }
    pn.setNumber(subscriberNumber.toString());
    if (typeLength != 0) {
      this.sizeCounter = start + typeLength;
    }
    this.cdrObject.setProperty(CdrProperty.REDIRECTING_NUMBER, pn);
  }

  public void setType144() {
    int start = this.sizeCounter;
    this.sizeCounter += 1;
    int numLen = intBuf[this.sizeCounter];
    Integer trunkId = 65536 * intBuf[this.sizeCounter + 1] + 256 * intBuf[this.sizeCounter + 2] + intBuf[this.sizeCounter + 3];
    this.sizeCounter += 8;
    int trunkGroupNameLength = intBuf[this.sizeCounter];
    StringBuilder itd = new StringBuilder(trunkGroupNameLength);
    temp = trunkGroupNameLength / 2 + trunkGroupNameLength % 2;
    this.sizeCounter += 1;
    for (int i = 0; i < trunkGroupNameLength; i++) {
      int tmpInt = intBuf[sizeCounter + i];
      itd.append((char) tmpInt);
    }
    if (numLen != 0) {
      this.sizeCounter = start + numLen;
    }
    this.cdrObject.setProperty(CdrProperty.IE144_Incoming_TD, trunkId);
    this.cdrObject.setProperty(CdrProperty.IE144_Incoming_Trunk_Group_Name, itd.toString());
  }

  public void setType145() {
    int start = this.sizeCounter;
    this.sizeCounter += 1;
    int numLen = intBuf[this.sizeCounter];
    Integer trunkId = 65536 + intBuf[this.sizeCounter + 1] + 256 + intBuf[this.sizeCounter + 2] + intBuf[this.sizeCounter + 3];
    this.sizeCounter += 8;
    int trunkGroupNameLength = intBuf[this.sizeCounter];
    StringBuilder itd = new StringBuilder(trunkGroupNameLength);
    temp = trunkGroupNameLength / 2 + trunkGroupNameLength % 2;
    this.sizeCounter += 1;
    for (int i = 0; i < trunkGroupNameLength; i++) {
      int tmpInt = intBuf[sizeCounter + i];
      itd.append((char) tmpInt);
    }
    if (numLen != 0) {
      this.sizeCounter = start + numLen;
    }
    this.cdrObject.setProperty(CdrProperty.IE145_Outgoing_TD, trunkId);
    this.cdrObject.setProperty(CdrProperty.IE145_Outgoing_Trunk_Group_Name, itd.toString());
  }

  // Global Call Reference
  public void setType147() {
    int len = this.typeLength;
    int startCounter = this.sizeCounter;

    this.sizeCounter += 2;

    GlobalCallReference gcr = new GlobalCallReference();
    gcr.setReceived((intBuf[this.sizeCounter] & 0x1) == 1);
    // if F2=true -> GCR starts with LSB according to ITU
    boolean gcrOrderLsbFirst = (intBuf[this.sizeCounter] & 0x2) == 2;
    this.sizeCounter++;

    int netIdLen = intBuf[this.sizeCounter];
    this.sizeCounter++;
    long networkId = decodeBinary(sizeCounter, netIdLen);
    gcr.setNetworkId(networkId);
    this.sizeCounter = this.sizeCounter + netIdLen;

    int nodeIdLen = intBuf[this.sizeCounter];
    this.sizeCounter++;
    ;
    // long nodeId = decodeBinary(sizeCounter, nodeIdLen);
    long nodeId = decodeBinary(sizeCounter, nodeIdLen);
    gcr.setNodeId(nodeId);
    this.sizeCounter = this.sizeCounter + nodeIdLen;

    int crefLen = intBuf[this.sizeCounter];
    this.sizeCounter++;
    long callRef;
    if (gcrOrderLsbFirst) {
      // the MSB is in last filed
      callRef = decodeBinaryFirstByteLsb(sizeCounter, crefLen);
    } else {
      // the MSB is in first field
      callRef = decodeBinary(sizeCounter, crefLen);
    }
    gcr.setCallReference(callRef);
    this.sizeCounter = this.sizeCounter + crefLen;

    if (len != 0) {
      this.sizeCounter = startCounter + len; // TSK01449969
    }
    this.cdrObject.setProperty(CdrProperty.GLOBAL_CALL_REFERENCE, gcr);
  }
  
  public void setType151() throws BadCdrRecordException {
    this.sizeCounter = this.sizeCounter + 2;
    int callType = intBuf[this.sizeCounter];
    this.cdrObject.setProperty(CdrProperty.IE151_CALL_TYPE, callType);
    this.sizeCounter++;
  }

  public void setType156() throws BadCdrRecordException {
    int start = this.sizeCounter;
    this.sizeCounter++;
    int numLen = intBuf[this.sizeCounter];
    this.sizeCounter++;
    int numberCount = intBuf[this.sizeCounter]; // quantity of additional numbers
    this.sizeCounter++;
    for (int i = 0; i < numberCount; i++) {
      int numberType = intBuf[this.sizeCounter]; // additional number type
      this.sizeCounter++;
      int natureOfAddrInd = intBuf[this.sizeCounter]; // Nature of Address Indicator
      this.sizeCounter++;
      int numberingPlanInd = intBuf[this.sizeCounter]; // numbering Plan Indicator
      this.sizeCounter += 2; // jump over reserved
      int lengthOfNumber = intBuf[this.sizeCounter];
      String number = readBCD(lengthOfNumber);
      this.sizeCounter += temp;
      switch (numberType) {
      case 0: // TODO Spare
        break;
      case 1: // TODO Recieved Calling Party Number
        break;
      case 2: // TODO Called User's IMSI (International Mobile Subscriber Identity)
        break;
      case 3: // TODO Calling User's IMSI (International Mobile Subscriber Identity)
        break;
      case 4:
        PartyNumber pn = new PartyNumber();
        pn.setNatureOfAddress(natureOfAddrInd);
        pn.setNumberingPlan(numberingPlanInd);
        pn.setNumber(number);
        this.cdrObject.setProperty(CdrProperty.CONNECTED_NUMBER, pn);
        break;
      case 5: // RESERVED
        break;
      default:
        log.debug("ERROR: IE156 Additional number type is wrong");
        throw new BadCdrRecordException("ERROR: IE156 Additional number type is wrong");
      }
    }
    if (this.sizeCounter != start + numLen) {
      log.debug("ERROR: Length of IE156 is wrong");
      throw new BadCdrRecordException("ERROR: Length of IE156 is wrong");
    }
  }

  // Centrex Numbers
  public void setType159() throws BadCdrRecordException {
    int len = this.typeLength;
    int startCounter = this.sizeCounter;

    this.sizeCounter += 2;

    int callingUsr = (intBuf[this.sizeCounter] & 0x01);
    int calledUsr = ((intBuf[this.sizeCounter] & 0x02) >> 1);
    int redirectUsr = ((intBuf[this.sizeCounter] & 0x04) >> 2);
    int numOfCenUsr = callingUsr + calledUsr + redirectUsr;
    this.sizeCounter += 1;
    if (numOfCenUsr == 0 && intBuf[this.sizeCounter] == 0) {
      this.sizeCounter += 1;
    }
    for (int j = 0; j < numOfCenUsr; j++) {
      // handling of FCC in the number
      int cenQualifier = intBuf[this.sizeCounter];
      this.sizeCounter += 1;
      int lengthOfNumber = intBuf[this.sizeCounter];
      String centrexNumber = readBCD(lengthOfNumber);

      this.sizeCounter += temp;
      switch (cenQualifier) {
      case 1:
        this.cdrObject.setProperty(CdrProperty.CTX_CALLING_NUMBER, centrexNumber);
        break;
      case 2:
        this.cdrObject.setProperty(CdrProperty.CTX_CALLED_NUMBER, centrexNumber);
        break;
      case 3:
        this.cdrObject.setProperty(CdrProperty.CTX_REDIRECTING_NUMBER, centrexNumber);
        break;
      default:
        log.debug("ERROR: IE159 Centrex Number Qualifier is wrong");
        throw new BadCdrRecordException("ERROR: IE159 Centrex Number Qualifier is wrong");
      }
    }
    if (this.sizeCounter != startCounter + len) {
      log.debug("ERROR: Length of IE159 is wrong");
      throw new BadCdrRecordException("ERROR: Length of IE159 is wrong");
    }
  }

  // Time Zone
  public void setType160() throws BadCdrRecordException {
    int start = this.sizeCounter;
    this.sizeCounter += 1;
    int lengthOfIE = intBuf[this.sizeCounter];
    this.sizeCounter += 1;
    int TZReference = intBuf[this.sizeCounter];
    this.sizeCounter += 1;
    int TZOffset = intBuf[this.sizeCounter];
    this.sizeCounter += 1;
    int TZLength = intBuf[this.sizeCounter];
    this.sizeCounter += 1;
    byte[] TZ = new byte[TZLength];
    for (int i = 0; i < TZLength; i++) {
      TZ[i] = (byte) intBuf[sizeCounter + i];
    }
    try {
      String timeZone = new String(TZ, "UTF8");
      this.cdrObject.setProperty(CdrProperty.TIME_ZONE_NAME, timeZone);
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    this.sizeCounter += TZLength;
    this.cdrObject.setProperty(CdrProperty.TIME_ZONE_REFERENCE, TZReference);
    this.cdrObject.setProperty(CdrProperty.TIME_ZONE_OFFSET, TZOffset);
    if (this.sizeCounter != start + lengthOfIE) {
      log.debug("ERROR: Length of IE160 is wrong");
      throw new BadCdrRecordException("ERROR: Length of IE160 is wrong");
    }
  }

  private String readBCD(int length) {
    StringBuilder number = new StringBuilder(length);
    temp = length / 2 + length % 2;
    this.sizeCounter += 1;
    for (int i = 0; i < temp; i++) {
      int tmpInt = intBuf[sizeCounter + i] >> 4;
      // if hex number contains (A,B,C,D,E,F) -> convert it to character
      if (tmpInt < 10) {
        // handle number
        number.append(tmpInt);
      } else {
        // convert hex number to appropriate character (hex A to char A)
        char c = (char) (tmpInt + 55);
        number.append(c);
      }
      // if hex number contains (A,B,C,D,E,F) -> convert it to character
      if (i * 2 + 2 <= length) {
        int tmpInt2 = intBuf[sizeCounter + i] & 0x0F;
        if (tmpInt2 < 10) {
          // handle number
          number.append(tmpInt2);
        } else {
          // handle character
          // int 55 + A (hex) = 65 -> in ASCII table for "A"
          char c = (char) (tmpInt2 + 55);
          number.append(c);
        }
      }
    }
    return number.toString();

  }

  /**
   * Gets a Method object that reflects the specified member method and invokes it
   * class with no arguments.
   * 
   * @param methodName1
   *          The <i>methodName</i> parameter is a String specifying the simple
   *          name the desired method.
   * @param classRef
   *          Class reference.
   * @throws BadCdrRecordException
   *           if bad cdr record occurs.
   */
  private void invokeMethod(String methodName1, Object classRef) throws BadCdrRecordException {
    try {
      // System.out.println("invokeMethod: " + methodName1);
      Method method = this.getClass().getMethod(methodName1);
      method.invoke(classRef);
    } catch (Exception methodException) {
      log.debug("ERROR_METHOD " + methodName);
      methodException.printStackTrace();
      throw new BadCdrRecordException(methodException);
      // try {
      // this.unknownType();
      // } catch (BadCdrRecordException badCDRrecordException) {
      // throw badCDRrecordException;
      // }
    }
  }

  // /**
  // * Method is to handle unknown types in variabled part of CDR record. If CDR
  // * record is bad then BadCDRrecordException is thrown.
  // *
  // * @throws BadCdrRecordException
  // * if bad cdr record occurs.
  // */
  // private void unknownType() throws BadCdrRecordException {
  // int unknownTypeLength = 0;
  // this.sizeCounter = this.sizeCounter + 1;
  // unknownTypeLength = intBuf[this.sizeCounter];
  // if (unknownTypeLength == 0)
  // unknownTypeLength = 2;
  // this.sizeCounter = this.sizeCounter + unknownTypeLength - 1;
  // if (this.sizeCounter > this.cdrSize - 3)
  // throw new BadCdrRecordException();
  // }
  //
  /**
   * Gets date from provided data.
   * 
   * @param intBuffer
   *          buffer to resolve
   * @param counter
   *          length of contents to resolve
   * @return The cdr date (format: day/month/year)
   */
  public Date getDateTime(int[] intBuffer, int counter) {
    int year;
    int day = intBuffer[counter + 2];
    int month = intBuffer[counter + 1] - 1; // GregorianCalender finta
    int t = intBuffer[counter];
    if ((t > 90) && (t < 100))
      year = t + 1990;
    else
      year = t + 2000;

    int hour = intBuffer[counter + 3];
    int min = intBuffer[counter + 4];
    int sec = intBuffer[counter + 5];
    int hundreds = intBuffer[counter + 6];

    Calendar c = new GregorianCalendar(year, month, day, hour, min, sec);
    c.add(Calendar.MILLISECOND, hundreds * 100);
    return c.getTime();
  }

  public String getDateTimeString(int[] intBuffer, int counter) {
    int year;
    int day = intBuffer[counter + 2];
    int month = intBuffer[counter + 1]; // GregorianCalender finta
    int t = intBuffer[counter];
    if ((t > 90) && (t < 100))
      year = t + 1990;
    else
      year = t + 2000;

    int hour = intBuffer[counter + 3];
    int min = intBuffer[counter + 4];
    int sec = intBuffer[counter + 5];
    int hundreds = intBuffer[counter + 6];
    String time = day + "." + month + "." + year + " " + hour + ":" + min + ":" + sec + ":" + hundreds * 100;
    return time;
  }
}
