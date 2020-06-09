
package si.iskratel.cdr.parser;

import java.util.Arrays;

public class DataRecord {

  private int cdrType;
  private int cdrLength;
  private byte[] dataRecordBytes;

  public int getCdrType() {
    return cdrType;
  }

  public void setCdrType(int cdrType) {
    this.cdrType = cdrType;
  }

  public int getCdrLength() {
    return cdrLength;
  }

  public void setCdrLength(int cdrLength) {
    this.cdrLength = cdrLength;
  }

  public byte[] getDataRecordBytes() {
    return dataRecordBytes;
  }

  public void setDataRecordBytes(byte[] dataRecordBytes) {
    this.dataRecordBytes = dataRecordBytes;
  }

  @Override
  public String toString() {
    return "DataRecord [cdrType=" + cdrType + ", cdrLength=" + cdrLength + ", dataRecordBytes=" + Arrays.toString(dataRecordBytes) + "]";
  }
  
}