
package si.iskratel.cdr.parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

public class CDRReader {

  private static final Logger log = Logger.getLogger(CDRReader.class);

  private static final String IGNORED_NOT_STORED = "). It will be ignored(not stored). ";
  private static final String NOT_CALL_RECORD = "Record is not call record (";

  public static List<DataRecord> readDataRecords(ByteArrayInputStream byteInputStream) {
    List<DataRecord> cdrsList = new LinkedList<DataRecord>();
    try {
      int cdrType;
      while ((cdrType = byteInputStream.read()) != -1) {
        DataRecord dataRecord = readIndividualDataRecord(cdrType, byteInputStream);
        byte[] cdrContent = dataRecord.getDataRecordBytes();
        if ((cdrContent != null) && (cdrContent.length > 2)) {
          cdrsList.add(dataRecord);
        }
      }
    } catch (IOException e) {
      log.error("Exception reading cdr file", e);
    } finally {
      if (byteInputStream != null) {
        try {
          byteInputStream.close();
        } catch (IOException e) {
          log.error("Failed closing byteInputStream", e);
        }
      }
    }
    return cdrsList;
  }

  private static DataRecord readIndividualDataRecord(int cdrType, ByteArrayInputStream byteInputStream) throws IOException {
    DataRecord dataRecord = new DataRecord();
    switch (cdrType) {
    case 200:
    case 220:
      dataRecord.setCdrType(cdrType);
      // CDR length
      byte[] recordLength = new byte[2];
      byteInputStream.read(recordLength);
      // convert from signed byte to unsigned integer
      int firstByte = 0x000000FF & ((int) recordLength[0]);
      int secondByte = 0x000000FF & ((int) recordLength[1]);
      int length = 0;
      length = length | firstByte;
      length = length << 8;
      length = length | secondByte;
      dataRecord.setCdrLength(length);
      // prepare output byte array
      dataRecord.setDataRecordBytes(new byte[length]);
      byte[] cdr = dataRecord.getDataRecordBytes();
      cdr[0] = (byte) cdrType;
      cdr[1] = recordLength[0];
      cdr[2] = recordLength[1];
      byteInputStream.read(cdr, 3, length - 3);
      break;
    case 210:
      byteInputStream.skip(15);
      log.info(NOT_CALL_RECORD + cdrType + IGNORED_NOT_STORED);
      break;
    case 211:
      byteInputStream.skip(18);
      log.info(NOT_CALL_RECORD + cdrType + IGNORED_NOT_STORED);
      break;
    case 212:
      byteInputStream.skip(11);
      log.info(NOT_CALL_RECORD + cdrType + IGNORED_NOT_STORED);
      break;
    default:

      break;
    }
    return dataRecord;
  }

  /*
   * Parses binary CDRs from file and return list of CDRs
   */
  public static List<byte[]> readCDR(File file) throws FileNotFoundException {
    FileInputStream is = new FileInputStream(file);
    List<byte[]> cdrList1 = new LinkedList<byte[]>();
    try {
      int c;
      while ((c = is.read()) != -1) {
        byte[] b = readIndividualCDR(c, is);
        if ((b != null) && (b.length > 2)) {
          cdrList1.add(b);
        }
      }
    } catch (IOException ioe) {
      log.info("", ioe);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          log.info("", e);
        }
      }
    }
    return cdrList1;
  }

  /*
   * Read individual CDR
   */
  private static byte[] readIndividualCDR(int c, FileInputStream is) throws IOException {
    byte[] cdr = null;
    switch (c) {
    case 200:
      // CDR length
      byte[] b = new byte[2];
      is.read(b);
      // convert from signed byte to unsigned integer
      int firstByte = 0x000000FF & ((int) b[0]);
      int secondByte = 0x000000FF & ((int) b[1]);

      int len = 0;
      len = len | firstByte;
      len = len << 8;
      len = len | secondByte;
      // prepare output byte array
      cdr = new byte[len];
      cdr[0] = (byte) c;
      cdr[1] = b[0];
      cdr[2] = b[1];
      is.read(cdr, 3, len - 3);
      break;
    case 210:
      is.skip(15);
      log.info("Record is not call record (" + c + "). It will be ignored(not stored). ");
      break;
    case 211:
      is.skip(18);
      log.info("Record is not call record (" + c + "). It will be ignored(not stored). ");
      break;
    case 212:
      is.skip(11);
      log.info("Record is not call record (" + c + "). It will be ignored(not stored). ");
      break;
    default:

      log.warn("Parsing of CDR record type '" + c + "' not supported) ");
      break;
    }
    return cdr;
  }

}
