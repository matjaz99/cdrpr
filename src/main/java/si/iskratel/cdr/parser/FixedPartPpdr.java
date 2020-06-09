
package si.iskratel.cdr.parser;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class FixedPartPpdr {

  public PpdrBean parse220record(DataRecord dataRecord) {
    byte[] recordBytes = dataRecord.getDataRecordBytes();
    PpdrBean ppdrBean = new PpdrBean();
    ppdrBean.setRecordLength(dataRecord.getCdrLength());
    ppdrBean.setRecordIndex(getRecordIndex(recordBytes));
    ppdrBean.setRecordTime(getDateTime(recordBytes, 6));
    ppdrBean = VariablePartPpdr.parse(ppdrBean, recordBytes);
    return ppdrBean;
  }

  private Date getDateTime(byte[] recordBytes, int position) {
    int year;
    int lastTwoDigitsOfYear = recordBytes[position + 1];
    if ((lastTwoDigitsOfYear > 90) && (lastTwoDigitsOfYear < 100))
      year = lastTwoDigitsOfYear + 1990;
    else
      year = lastTwoDigitsOfYear + 2000;
    int month = recordBytes[position + 2] - 1; // GregorianCalender finta
    int day = recordBytes[position + 3];
    int hour = recordBytes[position + 4];
    int min = recordBytes[position + 5];
    int sec = recordBytes[position + 6];
    int hundreds = recordBytes[position + 7];
    Calendar calendar = new GregorianCalendar(year, month, day, hour, min, sec);
    calendar.add(Calendar.MILLISECOND, hundreds * 100);
    return calendar.getTime();
  }

  private long getRecordIndex(byte[] recordBytes) {
    long firstByte = 0x000000FF & ((long) recordBytes[3]);
    long secondByte = 0x000000FF & ((long) recordBytes[4]);
    long thirdByte = 0x000000FF & ((long) recordBytes[5]);
    long fourthByte = 0x000000FF & ((long) recordBytes[6]);
    long index = 0;
    index = index | firstByte;
    index = index << 8;
    index = index | secondByte;
    index = index << 8;
    index = index | thirdByte;
    index = index << 8;
    index = index | fourthByte;
    return index;
  }

}