
package si.iskratel.cdr.parser;

public class VariablePartPpdr {

  private static final int TRUNK_GROUP_DATA_IE = 155;

  public static PpdrBean parse(PpdrBean ppdrBean, byte[] recordBytes) {
    int index = 19; // At index 19 starts variable part
    boolean parsingComplete = false;
    int length = recordBytes.length;
    while (!parsingComplete) {
      switch (toUnsignedInt(recordBytes[index])) {
      case TRUNK_GROUP_DATA_IE:
        index = parseIE155_9B(ppdrBean, recordBytes, index);
        parsingComplete = isParsingCompleted(length, index);
        break;
      default:
        index = getIndexOfNextIE(recordBytes, index);
        parsingComplete = isParsingCompleted(length, index);
        break;
      }
    }
    return ppdrBean;
  }

  private static boolean isParsingCompleted(int length, int index) {
    return index >= (length - 1);
  }

  private static int getIndexOfNextIE(byte[] recordBytes, int index) {
    return index + lengthOfCurrentIE(recordBytes, index);
  }

  private static int lengthOfCurrentIE(byte[] recordBytes, int index) {
    return recordBytes[index + 1] & 0xff;
  }

  private static int parseIE155_9B(PpdrBean ppdrBean, byte[] recordBytes, int index) {
    int tmpIndex = index;
    int length = toUnsignedInt(recordBytes[++tmpIndex]);
    tmpIndex++;
    int[] flags = getFlags(recordBytes, tmpIndex);
    int[] trunkGroupsData = new int[6];
    String trunkGroupName = null;
    for (int i = 0; i < flags.length; i++) {
      if (flags[i] == 1) {
        if (i == 0) {
          trunkGroupsData[i] = 65536 * toUnsignedInt(recordBytes[++tmpIndex]) + 256 * toUnsignedInt(recordBytes[++tmpIndex])
              + toUnsignedInt(recordBytes[++tmpIndex]);
        } else if (i == 1) {
          int trunkGroupNameLength = toUnsignedInt(recordBytes[++tmpIndex]);
          StringBuilder trGroupNameBuilder = new StringBuilder(trunkGroupNameLength);
          for (int j = 0; j < trunkGroupNameLength; j++) {
            int tmpInt = recordBytes[++tmpIndex];
            trGroupNameBuilder.append((char) tmpInt);
          }
          trunkGroupName = trGroupNameBuilder.toString();
        } else if (i == 2 || i == 3) {
          trunkGroupsData[i] = 16777216 * toUnsignedInt(recordBytes[++tmpIndex]) + 65536 * toUnsignedInt(recordBytes[++tmpIndex])
              + 256 * toUnsignedInt(recordBytes[++tmpIndex]) + toUnsignedInt(recordBytes[++tmpIndex]);
        } else if (i == 4) {
          trunkGroupsData[i] = toUnsignedInt(recordBytes[++tmpIndex]);
        }
      }
    }
    ppdrBean.setTrunkGroupId(trunkGroupsData[0]);
    ppdrBean.setTrunkGroupName(trunkGroupName);
    ppdrBean.setNumberOfAllTrunks(trunkGroupsData[2]);
    ppdrBean.setNumberOfOutOfServiceTrunks(trunkGroupsData[3]);
    ppdrBean.setTrunkGroupOperatingMode(trunkGroupsData[4]);
    index += length;
    return index;
  }

  private static int toUnsignedInt(byte b) {
    return b & 0xff;
  }

  private static int[] getFlags(byte[] recordBytes, int tmpIndex) {
    int flags[] = new int[5];
    flags[0] = (recordBytes[tmpIndex] & 0x01); // Trunk Group Id
    flags[1] = (recordBytes[tmpIndex] & 0x02) >> 1; // Trunk Group Name
    flags[2] = (recordBytes[tmpIndex] & 0x04) >> 2; // No. of all Trunks in Trunk Group
    flags[3] = (recordBytes[tmpIndex] & 0x08) >> 3; // No. of out of service Trunks in Trunk Group
    flags[4] = (recordBytes[tmpIndex] & 0x10) >> 4; // Trunk Group Operating Mode
    return flags;
  }

}
