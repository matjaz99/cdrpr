
package si.matjazcerkvenik.datasims.cdrpr.cdr.parser;

import java.util.Date;

public class PpdrBean {

  private int recordLength;
  private long recordIndex;
  private Date recordTime;
  private int trunkGroupId;
  private String trunkGroupName;
  private int numberOfAllTrunks;
  private int numberOfOutOfServiceTrunks;
  private int trunkGroupOperatingMode;

  public int getRecordLength() {
    return recordLength;
  }

  public void setRecordLength(int recordLength) {
    this.recordLength = recordLength;
  }

  public long getRecordIndex() {
    return recordIndex;
  }

  public void setRecordIndex(long recordIndex) {
    this.recordIndex = recordIndex;
  }

  public Date getRecordTime() {
    return recordTime;
  }

  public void setRecordTime(Date recordTime) {
    this.recordTime = recordTime;
  }

  public int getTrunkGroupId() {
    return trunkGroupId;
  }

  public void setTrunkGroupId(int trunkGroupId) {
    this.trunkGroupId = trunkGroupId;
  }

  public String getTrunkGroupName() {
    return trunkGroupName;
  }

  public void setTrunkGroupName(String trunkGroupName) {
    this.trunkGroupName = trunkGroupName;
  }

  public int getNumberOfAllTrunks() {
    return numberOfAllTrunks;
  }

  public void setNumberOfAllTrunks(int numberOfAllTrunks) {
    this.numberOfAllTrunks = numberOfAllTrunks;
  }

  public int getNumberOfOutOfServiceTrunks() {
    return numberOfOutOfServiceTrunks;
  }

  public void setNumberOfOutOfServiceTrunks(int numberOfOutOfServiceTrunks) {
    this.numberOfOutOfServiceTrunks = numberOfOutOfServiceTrunks;
  }

  public int getTrunkGroupOperatingMode() {
    return trunkGroupOperatingMode;
  }

  public void setTrunkGroupOperatingMode(int trunkGroupOperatingMode) {
    this.trunkGroupOperatingMode = trunkGroupOperatingMode;
  }

  @Override
  public String toString() {
    return "PpdrBean [recordLength=" + recordLength + ", recordIndex=" + recordIndex + ", recordTime=" + recordTime + ", trunkGroupId="
        + trunkGroupId + ", trunkGroupName=" + trunkGroupName + ", numberOfAllTrunks=" + numberOfAllTrunks + ", numberOfOutOfServiceTrunks="
        + numberOfOutOfServiceTrunks + ", trunkGroupOperatingMode=" + trunkGroupOperatingMode + "]";
  }

}