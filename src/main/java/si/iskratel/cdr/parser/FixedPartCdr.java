/* ###################################################################### */
/* # # */
/* # # */
/* # Copyright (c) 2001 ISKRATEL # */
/* # # */
/* # # */
/* # Name : FixedPartCDR.java # */
/* # # */
/* # Decription : Defines fiksed part data in cdr call record. # */
/* # # */
/* # Code : GQSB - XAE5501 # */
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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import org.apache.log4j.Logger;
import si.iskratel.cdr.manager.BadCdrRecordException;

/**
 * The FixedPartCDR class implements methods for putting values and keys into
 * Hashtable. It define only fixed part of call record.
 * 
 * @see CdrObject
 * @since JDK 1.3
 * @author Vidic Ales
 * @version 1.0, April,2001
 */

public class FixedPartCdr {

  public static int LENGTH_POS = 1;

  public static int CDRINDEX_POS = 3;

  public static int CALLID_POS = 7;

  public static int FLAG_POS = 11;

  public static int RECORDSEQ_POS = 14;

  public static int LACLEN_POS = 15;

  public static int NUM_POS = 16;

  private int MAX_CDR_LEN = 4096;

  public static final int CDR_TYPE_CALL = 200;
  public static final int CDR_TYPE_DATE_CHG = 210;// CDR type for changed date
  public static final int CDR_TYPE_LOST_REC = 211; // CDR for Lost records
  public static final int CDR_TYPE_CS_RESTART = 212; // CDR type for CS restart

  /** Temporary integer. */
  private long temp;

  /**
   * The array buffer into which the integers are saved. It will be used as array
   * from wich informations should be extracted.
   */
  private int intBuf[] = new int[MAX_CDR_LEN];

  /**
   * The array buffer into which the bytes are saved. It will be used as array
   * from wich integer buffer should be extracted.
   */
  private byte byteBuf[] = new byte[MAX_CDR_LEN];

  /** CDR record size. */
  public int cdrSize;

  /** A CDRobject obtain object with all properties. */
  private CdrObject cdrObject = null;

  /** A VariabledPartCDR obtain variable part of call record. */
  private VariablePartCdr varCDR = null;

  public boolean seekSet = false;

  /** Parameters for prefix handling **/
  private boolean prefixHandlingActive = false;

  private String nationalPrefix;

  // private String internationalPrefix; currently only national prefix is
  // supported

  private static final Logger log = Logger.getLogger(FixedPartCdr.class);

  /**
   * The <code>FixedPartCDR()</code> is class constructor who also set current
   * language.
   */
  public FixedPartCdr() {
    varCDR = new VariablePartCdr();
    this.cdrObject = new CdrObject();
  }

  /**
   * Obtains the cdr Object of the provided stream. Sets all flags in fixed part
   * of CDR record. Determines city/area code and local number of record's owner.
   * Also determines variabled part of CDR record.
   * 
   * @param cdrStream
   *          the input stream from which information should be extracted.
   * @param fieldsToParse 
   * @param fieldLengths 
   * @return Object describing fixed part of cdr record.
   * @throws BadCdrRecordException
   *           if bad cdr record occurs.
   * @throws IOException
   *           if an I/O error occurs.
   */
  public CdrObject parse200record(InputStream cdrStream, Hashtable<Integer, Integer> fieldLengths, BitSet fieldsToParse) throws BadCdrRecordException {
    cdrObject.clearAllProperties();
    int dataRead = 0;
    try {
      cdrObject.clearAllProperties();
      cdrStream.mark(300);
      dataRead = cdrStream.read(this.byteBuf, 0, 3);
      if (dataRead > 0) {
        // determine CDR type (CALL| )
        int cdrType = this.byteBuf[0] & 0xff;
        cdrObject.setProperty(CdrProperty.RECORD_TYPE, cdrType);
        if (cdrType == CDR_TYPE_DATE_CHG || cdrType == CDR_TYPE_LOST_REC || cdrType == CDR_TYPE_CS_RESTART) {
          // These are known CDR types, but they are not handed by CDR server
          log.info("CDR is not of type CALL ('" + cdrType + "'). It will be not stored. ");
          return cdrObject;
        }
        if (cdrType != CDR_TYPE_CALL) {
          // Unknown CDR
          throw new BadCdrRecordException("Unknown CDR type '" + cdrType + "'");
        }
        // handle CDR of type CALL(200)
        this.parseSize(this.byteBuf);
        cdrStream.reset();
        dataRead = cdrStream.read(this.byteBuf, 0, this.cdrSize);
        // parse CDR data
        this.parseRecordType(this.byteBuf);
        this.parseCallId();
        this.parseFlags();
        this.parseSubscriberNumber();
        this.varCDR.variableHandler(cdrObject, cdrSize, temp, this.intBuf, fieldLengths, fieldsToParse);
        if (!cdrObject.containKey(CdrProperty.SUPPLEMENTARY_SERVICE_INFO)) {
          // put default Service information (0=Call)
          cdrObject.setProperty(CdrProperty.SUPPLEMENTARY_SERVICE_INFO, new SupplementaryService());
        }
        return cdrObject;
      }
    } catch (IOException ioEx) {
      throw new BadCdrRecordException(ioEx);
    }
    return null;
  }

  // public CdrObject parse200record(byte[] cdrStream) throws
  // BadCdrRecordException {
  // cdrObject.clearAllProperties();
  // if (cdrStream.length > 2) {
  // this.byteBuf[0] = cdrStream[0];
  // this.byteBuf[1] = cdrStream[1];
  // this.byteBuf[2] = cdrStream[2];
  // this.parseSize(this.byteBuf);
  // cdrObject.setCdrLength(cdrSize);
  // for (int i = 0; i < cdrStream.length; i++) {
  // byteBuf[i] = cdrStream[i];
  // }
  // this.parseRecordType(this.byteBuf);
  // this.parseCallId();
  // this.parseFlags();
  // this.parseSubscriberNumber();
  // this.varCDR.variableHandler(cdrObject, cdrSize, temp, this.intBuf);
  // return cdrObject;
  // }
  // return null;
  // }

  public byte[] encode200Record(CdrObject acdrObject) {
    this.cdrObject = acdrObject;
    if (this.cdrObject == null)
      return null;
    this.byteBuf = new byte[256];

    int pos = 0;
    this.encodeRecordType();
    this.encodeCallId();
    this.encodeFlags();
    pos = this.encodeSubscriberNumber();
    pos = encodeType100(pos);
    pos = encodeType102(pos);
    pos = encodeType103(pos);
    pos = encodeType104(pos);
    pos = encodeType111(pos);
    pos = encodeType115(pos);
    pos = encodeType117(pos);
    pos = encodeType119(pos);
    pos = encodeType121(pos);
    pos = encodeType124(pos);
    pos = encodeType133(pos);
    pos = encodeType135(pos);
    pos = encodeType137(pos);
    // pos = encodeType138(pos);
    this.cdrSize = pos - 1;
    this.encodeSize();

    byte[] res = new byte[cdrSize];
    for (int i = 0; i < res.length; i++) {
      res[i] = byteBuf[i];
    }

    return res;
  }

  /**
   * Set the size of one cdr record.
   * 
   * @param byteBuf1
   *          byte buffer from which size should be extracted.
   */
  private void parseSize(byte[] byteBuf1) {
    for (int i = 1; i < 3; i++) {
      if (byteBuf1[i] < 0)
        intBuf[i] = byteBuf1[i] + 256;
      else
        intBuf[i] = byteBuf1[i];
    }
    this.cdrSize = 256 * intBuf[1] + intBuf[2];
  }

  private void encodeSize() {
    /*
     * byte high, low; high = (byte)(cdrSize / 256); low = (byte)(cdrSize - high);
     * this.byteBuf[1] = high; this.byteBuf[2] = low;
     */
    encodeBinary(cdrSize, 1, 2);
  }

  /**
   * From array of bytes obtains array of integers. It also put under hashtable
   * key RECORD_TYPE value 200.
   * 
   * @param byteBuf1
   *          byte buffer from which array of integers should be extracted.
   * @see CdrProperty
   */
  private void parseRecordType(byte[] byteBuf1) {
    int recordLength = this.cdrSize;
    for (int i = 0; i < recordLength; i++) {
      if (byteBuf1[i] < 0)
        intBuf[i] = 256 + byteBuf1[i];
      else
        intBuf[i] = byteBuf1[i];
    }
    this.cdrObject.setProperty(CdrProperty.RECORD_TYPE, new Integer(intBuf[0]));
  }

  private void encodeRecordType() {
    this.byteBuf[0] = (byte) 200;
  }

  /**
   * Determines sequence call number and process id, and both values saves in
   * hastables as Long.
   */
  private void parseCallId() {
    long cdrIndex = -1;
    long callId = -1;
    int pos = CDRINDEX_POS;
    cdrIndex = 16777216 * intBuf[pos] + 65536 * intBuf[pos + 1] + 256 * intBuf[pos + 2] + intBuf[pos + 3];

    pos = CALLID_POS;
    callId = 16777216 * intBuf[pos] + 65536 * intBuf[pos + 1] + 256 * intBuf[pos + 2] + intBuf[pos + 3];
    this.cdrObject.setProperty(CdrProperty.CDR_INDEX, new Long(cdrIndex));
    this.cdrObject.setProperty(CdrProperty.CALL_ID, new Long(callId));
  }

  private void encodeCallId() {
    long cdrIndex = (Long) this.cdrObject.getProperty(CdrProperty.CDR_INDEX);
    long callId = (Long) this.cdrObject.getProperty(CdrProperty.CALL_ID);

    encodeBinary(cdrIndex, CDRINDEX_POS, 4);
    encodeBinary(callId, CALLID_POS, 4);
  }

  /**
   * From array of integers determine flags. Each value of the flag is saved in
   * hashtable under each flag's key. Values are saved as Strings. It also
   * determines value of record sequence and saved in hashtable as a String.
   * 
   * @see CdrProperty
   */
  private void parseFlags() {
    int flag[] = new int[18];
    int sequence = -1;
    int chargeStatus = -1;
    int pos = FLAG_POS;
    flag[0] = (intBuf[pos] & 0x01);
    flag[1] = ((intBuf[pos] & 0x02) >> 1);
    flag[2] = ((intBuf[pos] & 0x04) >> 2);
    flag[3] = ((intBuf[pos] & 0x08) >> 3);
    flag[4] = ((intBuf[pos] & 0x10) >> 4);
    flag[5] = ((intBuf[pos] & 0x20) >> 5);
    flag[6] = ((intBuf[pos] & 0x40) >> 6);
    flag[7] = (intBuf[pos] >> 7);
    flag[8] = (intBuf[pos + 1] & 0x01);
    flag[9] = ((intBuf[pos + 1] & 0x02) >> 1);
    flag[10] = ((intBuf[pos + 1] & 0x04) >> 2);
    flag[11] = ((intBuf[pos + 1] & 0x08) >> 3);
    flag[12] = ((intBuf[pos + 1] & 0x10) >> 4);
    flag[13] = ((intBuf[pos + 1] & 0x20) >> 5);
    flag[14] = ((intBuf[pos + 1] & 0x40) >> 6);
    flag[15] = (intBuf[pos + 1] >> 7);
    flag[16] = (intBuf[pos + 2] & 0x01);
    flag[17] = ((intBuf[pos + 2] & 0x02) >> 1);

    // TODO: flags 18,19,20
    sequence = (intBuf[RECORDSEQ_POS] >> 4);
    chargeStatus = (intBuf[RECORDSEQ_POS] & 0xF);

    this.cdrObject.setProperty(CdrProperty.FLAG_F1, flag[0] == 1);
    this.cdrObject.setProperty(CdrProperty.FLAG_F2, flag[1] == 1);
    this.cdrObject.setProperty(CdrProperty.FLAG_F3, flag[2] == 1);
    this.cdrObject.setProperty(CdrProperty.FLAG_F4, flag[3] == 1);
    this.cdrObject.setProperty(CdrProperty.FLAG_F5, flag[4] == 1);
    this.cdrObject.setProperty(CdrProperty.FLAG_F6, flag[5] == 1);
    this.cdrObject.setProperty(CdrProperty.FLAG_F7, flag[6] == 1);
    this.cdrObject.setProperty(CdrProperty.FLAG_F8, flag[7] == 1);
    this.cdrObject.setProperty(CdrProperty.FLAG_F9, flag[8] == 1);
    this.cdrObject.setProperty(CdrProperty.FLAG_F10, flag[9] == 1);
    this.cdrObject.setProperty(CdrProperty.FLAG_F11, flag[10] == 1);
    this.cdrObject.setProperty(CdrProperty.FLAG_F12, flag[11] == 1);
    this.cdrObject.setProperty(CdrProperty.FLAG_F13, flag[12] == 1);
    this.cdrObject.setProperty(CdrProperty.FLAG_F14, flag[13] == 1);
    this.cdrObject.setProperty(CdrProperty.FLAG_F15, flag[14] == 1);
    this.cdrObject.setProperty(CdrProperty.FLAG_F16, flag[15] == 1);
    this.cdrObject.setProperty(CdrProperty.FLAG_F17, flag[16] == 1);
    this.cdrObject.setProperty(CdrProperty.FLAG_F18, flag[17] == 1);

    this.cdrObject.setProperty(CdrProperty.RECORD_SEQUENCE, sequence);
    this.cdrObject.setProperty(CdrProperty.CHARGE_STATUS, chargeStatus);
  }

  private int encodeFlags() {
    byte b = 0;
    long mask = 1;
    for (int i = 0; i < 8; i++) {
      String flagName = "F" + (i + 1);
      try {
        if ((Boolean) cdrObject.getProperty(flagName))
          b = (byte) (b + mask);
      } catch (NullPointerException e) {
        // Ok, the flag is not set
      }
      mask = mask << 1;
    }
    this.byteBuf[11] = b;

    b = 0;
    for (int i = 8; i < 16; i++) {
      if (cdrObject.getProperty("F" + i + 1) != null)
        b = (byte) (b + mask);
      mask = mask << 1;
    }
    this.byteBuf[12] = b;

    b = 0;
    for (int i = 16; i < 20; i++) {
      if (cdrObject.getProperty("F" + i + 1) != null)
        b = (byte) (b + mask);
      mask = mask << 1;
    }
    this.byteBuf[13] = b;

    int sequence = (Integer) cdrObject.getProperty(CdrProperty.RECORD_SEQUENCE);
    int chargeStatus = (Integer) cdrObject.getProperty(CdrProperty.CHARGE_STATUS);

    b = (byte) ((sequence << 4) + chargeStatus);
    this.byteBuf[RECORDSEQ_POS] = b;
    return RECORDSEQ_POS + 3;
  }

  /**
   * From array of integers determine city/area code and local number of record's
   * owner. The result is String, saved in hashtable.
   * 
   * @see CdrProperty
   */
  private void parseSubscriberNumber() {
    int networkNumLen;
    int subscriberNumLen;

    int pos = LACLEN_POS;
    networkNumLen = (intBuf[pos] >> 5);
    subscriberNumLen = (intBuf[pos] & 0x1F);
    StringBuilder subscriberNumber = new StringBuilder(networkNumLen + subscriberNumLen);
    temp = (networkNumLen + subscriberNumLen) / 2 + (networkNumLen + subscriberNumLen) % 2;

    pos = NUM_POS;
    for (int i = 0; i < temp; i++) {
      // start
      int tmpInt = intBuf[pos + i] >> 4;
      // if number is > 9 than number contains hex (A,B,C,D,E,F) than
      // convert it
      // to character
      if (tmpInt < 10) {
        // handle number
        subscriberNumber.append(tmpInt);
      } else {
        // convert hex number to appropriate character hex A to char A
        char c = (char) (tmpInt + 55);
        subscriberNumber.append(c);
      }
      // end
      // subscriberNumber.append(intBuf[pos + i] >> 4);
      /*
       * if ((networkNumLen == i 2 + 1) && (networkNumLen != 0))
       * subscriberNumber.append("-");
       */
      // start
      if (i * 2 + 2 <= networkNumLen + subscriberNumLen) {
        int tmpInt2 = intBuf[pos + i] & 0x0F;
        if (tmpInt2 < 10) {
          subscriberNumber.append(tmpInt2);
        } else {
          char c = (char) (tmpInt2 + 55);// int 55 + A (hex) = 65 -> in ASCII table for "A"
          subscriberNumber.append(c);
        }
        // subscriberNumber.append(intBuf[pos + i] & 0x0F);
      }
      // end
      /*
       * if (networkNumLen == i 2 + 2) subscriberNumber.append("-");
       */
    }
    // add national prefix (escape code) to the owner number if LAC length
    // exist
    String subNumber = subscriberNumber.toString();
    if (prefixHandlingActive) {
      if (networkNumLen > 0) {
        // add prefix before subscriber number
        subNumber = nationalPrefix + subNumber;
      }
    }
    this.cdrObject.setProperty(CdrProperty.OWNER_NUMBER, subNumber);
  }

  private int encodeSubscriberNumber() {
    String snum = (String) this.cdrObject.getProperty(CdrProperty.OWNER_NUMBER);
    int laclen = Math.max(snum.indexOf('-'), 0);
    String lac = snum.substring(0, laclen);
    String num = snum.substring(laclen);
    int numlen = num.length();
    snum = lac + num;

    this.byteBuf[LACLEN_POS] = (byte) ((laclen << 5) + numlen);

    return encodeBcd(snum, NUM_POS);
  }

  public int encodeType100(int pos) {
    String snum = (String) this.cdrObject.getProperty(CdrProperty.CALLED_NUMBER);
    this.byteBuf[pos] = 100;
    this.byteBuf[pos + 1] = (byte) snum.length();
    return encodeBcd(snum, pos + 2);
  }

  public int encodeType102(int pos) {
    Date callStartDate = (Date) this.cdrObject.getProperty(CdrProperty.CALL_START_TIME);

    Boolean flag102 = (Boolean) this.cdrObject.getProperty(CdrProperty.FLAG_102);

    this.byteBuf[pos] = 102;
    int res = this.encodeDateTime(callStartDate, pos + 1);

    if ((flag102 != null) && flag102) {
      byteBuf[res + 1] = 1;
    }
    return res + 2;
  }

  public int encodeDateTime(Date date, int pos) {
    Calendar c = new GregorianCalendar();
    c.setTime(date);
    int year = c.get(Calendar.YEAR);
    if (year < 2000)
      year -= 1990;
    else
      year -= 2000;

    int day = c.get(Calendar.DAY_OF_MONTH);
    int month = c.get(Calendar.MONTH) + 1; // GregorianCalendar finta
    int hour = c.get(Calendar.HOUR_OF_DAY);
    int min = c.get(Calendar.MINUTE);
    int sec = c.get(Calendar.SECOND);
    int hundreds = c.get(Calendar.MILLISECOND) / 100;

    this.byteBuf[pos] = (byte) year;
    this.byteBuf[++pos] = (byte) month;
    this.byteBuf[++pos] = (byte) day;
    this.byteBuf[++pos] = (byte) hour;
    this.byteBuf[++pos] = (byte) min;
    this.byteBuf[++pos] = (byte) sec;
    this.byteBuf[++pos] = (byte) hundreds;

    return pos;
  }

  public int encodeType103(int pos) {
    Date callEndDate = (Date) this.cdrObject.getProperty(CdrProperty.CALL_STOP_TIME);
    Boolean flag103 = (Boolean) this.cdrObject.getProperty(CdrProperty.FLAG_103);

    this.byteBuf[pos] = 103;
    int res = this.encodeDateTime(callEndDate, pos + 1);

    if ((flag103 != null) && flag103) {
      byteBuf[res + 1] = 1;
    }
    return res + 2;
  }

  public int encodeType104(int pos) {
    long tariff = ((Long) this.cdrObject.getProperty(CdrProperty.CHARGE_UNITS)).longValue();

    this.byteBuf[pos] = 104;
    return encodeBinary(tariff, pos + 1, 3);
  }

  public int encodeType111(int pos) {
    int tariffCourse = ((Integer) cdrObject.getProperty(CdrProperty.TARIFF_DIRECTION)).intValue();

    this.byteBuf[pos] = 111;
    return encodeBinary(tariffCourse, pos + 1, 1);
  }

  public int encodeType115(int pos) {
    Long duration = (Long) cdrObject.getProperty(CdrProperty.CALL_DURATION);

    this.byteBuf[pos] = 115;
    return encodeBinary(duration.longValue(), pos + 1, 4);
  }

  public int encodeType117(int pos) {
    int bgid = 0;
    if (cdrObject.getProperty(CdrProperty.BUSINESS_GROUP) != null) {
      bgid = (Integer) cdrObject.getProperty(CdrProperty.BUSINESS_GROUP);
    }
    int cgid = 0;
    if (cdrObject.getProperty(CdrProperty.CENTREX_GROUP) != null) {
      cgid = (Integer) cdrObject.getProperty(CdrProperty.CENTREX_GROUP);
    }
    this.byteBuf[pos] = 117;
    this.byteBuf[pos + 1] = (byte) 10;
    pos = encodeBinary(bgid, pos + 2, 4);
    return encodeBinary(cgid, pos, 4);// cgid
  }

  public int encodeType119(int pos) {
    String num = (String) this.cdrObject.getProperty(CdrProperty.ORIGINAL_CPN);
    this.byteBuf[pos] = (byte) 119;
    this.byteBuf[pos + 2] = (byte) num.length();
    int len = encodeBcd(num, pos + 3);
    int pakLen = len - pos;
    this.byteBuf[pos + 1] = (byte) (pakLen);
    return len;
  }

  public int encodeType121(int pos) {
    Integer releaseValue = (Integer) cdrObject.getProperty(CdrProperty.CALL_RELEASE_VALUE);

    Integer standard = (Integer) cdrObject.getProperty(CdrProperty.CALL_RELEASE_STANDARD);
    Integer location = (Integer) cdrObject.getProperty(CdrProperty.CALL_RELEASE_LOCATION);

    int t = (standard.intValue() << 5) & location.intValue();

    this.byteBuf[pos] = 121;
    this.byteBuf[pos + 1] = (byte) 5;
    pos = encodeBinary(releaseValue.longValue(), pos + 2, 2);
    this.byteBuf[pos + 1] = (byte) t;
    return pos + 1;
  }

  public int encodeType124(int pos) {
    Integer TBR = (Integer) cdrObject.getProperty(CdrProperty.TIME_BEFORE_RINGING);
    Integer RTBA = (Integer) cdrObject.getProperty(CdrProperty.RINGING_TIME_BEFORE_ANSWER);
    this.byteBuf[pos] = 124;
    this.byteBuf[pos + 1] = (byte) 10;
    pos = encodeBinary(TBR.longValue(), pos + 2, 4);
    pos = encodeBinary(RTBA.longValue(), pos, 4);
    return pos;
  }

  public int encodeType133(int pos) {
    int bgid = 0;
    if (cdrObject.getProperty(CdrProperty.BUSINESS_GROUP_B) != null) {
      bgid = (Integer) cdrObject.getProperty(CdrProperty.BUSINESS_GROUP_B);
    }
    int cgid = 0;
    if (cdrObject.getProperty(CdrProperty.CENTREX_GROUP_B) != null) {
      cgid = (Integer) cdrObject.getProperty(CdrProperty.CENTREX_GROUP_B);
    }
    int ctxct = 0;
    if (cdrObject.getProperty(CdrProperty.CENTREX_CALL_TYPE_B) != null) {
      cgid = (Integer) cdrObject.getProperty(CdrProperty.CENTREX_CALL_TYPE_B);
    }

    this.byteBuf[pos] = (byte) 133;
    this.byteBuf[pos + 1] = (byte) 12;
    pos = encodeBinary(bgid, pos + 2, 4);// Called Subscriber Bussines Group
    pos = encodeBinary(cgid, pos, 4);// Called Subscriber Centrex Group
    return encodeBinary(ctxct, pos, 1) + 1;// Centrex Call Type
  }

  public int encodeType135(int pos) {
    String icid = "";
    int lengthIcid = 0;
    int lengthHost = 0;
    if (cdrObject.getProperty(CdrProperty.ICID) != null) {
      icid = (String) cdrObject.getProperty(CdrProperty.ICID);
      lengthIcid = icid.length();
    }
    String hostname = "";
    if (cdrObject.getProperty(CdrProperty.ICID_HOSTNAME) != null) {
      hostname = (String) cdrObject.getProperty(CdrProperty.ICID_HOSTNAME);
      lengthHost = hostname.length();
    }

    this.byteBuf[pos] = (byte) 135;
    this.byteBuf[pos + 1] = (byte) (lengthHost + lengthIcid + 4);
    this.byteBuf[pos + 2] = (byte) (lengthIcid);
    pos = pos + 2;
    byte[] ICID;
    try {
      ICID = icid.getBytes("ASCII");
      for (int i = 1; i <= lengthIcid; i++) {
        byteBuf[pos + i] = ICID[i - 1];
      }
    } catch (UnsupportedEncodingException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    pos = pos + 1 + lengthIcid;
    this.byteBuf[pos] = (byte) (lengthHost);
    byte[] HOSTNAME;
    try {
      HOSTNAME = hostname.getBytes("ASCII");
      for (int i = 1; i <= lengthHost; i++) {
        byteBuf[pos + i] = HOSTNAME[i - 1];
      }
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return pos + 1 + lengthHost;

  }

  public int encodeType137(int pos) {
    SupplementaryService supService = (SupplementaryService) this.cdrObject.getProperty(CdrProperty.SUPPLEMENTARY_SERVICE_INFO);
    this.byteBuf[pos] = (byte) 137;
    int infoLength = 0;
    if (supService == null) {
      supService = new SupplementaryService();
    } else {
      infoLength = supService.getServiceInfo().length;
    }
    this.byteBuf[pos + 1] = (byte) (infoLength + 4);
    pos = encodeBinary(supService.getServiceId(), pos + 2, 2);
    char[] info = supService.getServiceInfo();
    for (int i = 0; i < infoLength; i++) {
      this.byteBuf[pos + i] = (byte) (info[i]);
    }
    return pos + infoLength + 1;
  }

  public int encodeType138(int pos) {
    String snum = (String) this.cdrObject.getProperty(CdrProperty.ORIGINAL_CPN);
    this.byteBuf[pos] = (byte) 138;
    // this.byteBuf[pos + 1] = (byte)(snum.length() + 6);
    int len = encodeBcd(snum, pos + 6);
    int pakLen = len - pos;
    this.byteBuf[pos + 1] = (byte) (pakLen);
    return len;
  }

  public int encodeType140(int pos) {
    String snum = (String) this.cdrObject.getProperty(CdrProperty.CALLED_NUMBER);
    this.byteBuf[pos] = (byte) 140;
    this.byteBuf[pos + 1] = (byte) snum.length();
    return encodeBcd(snum, pos + 2);
  }

  private int encodeBinary(long value, int pos, int numOfBytes) {
    for (int i = 0; i < numOfBytes; i++) {
      long mask = (long) Math.pow(256, numOfBytes - i - 1);
      this.byteBuf[pos + i] = (byte) ((value / mask));
      value = value & (mask - 1);
    }
    return pos + numOfBytes;
  }

  private int encodeBcd(String snum, int pos) {
    int bytelen = snum.length() / 2 + snum.length() % 2;
    for (int i = 0; i < bytelen; i++) {
      int bpos = i * 2;
      if (snum.length() > bpos + 1) {
        byteBuf[pos + i] = (byte) ((encodeChar(snum.charAt(bpos)) << 4) + (encodeChar(snum.charAt(bpos + 1)) & 0x0F));
      } else {
        byteBuf[pos + i] = (byte) (encodeChar(snum.charAt(bpos)) << 4);
      }
    }
    return pos + bytelen;
  }

  private int encodeChar(char x) {
    if ((byte) x < 65) {
      return x;
    } else {
      return (byte) x - 55;
    }
  }
}
