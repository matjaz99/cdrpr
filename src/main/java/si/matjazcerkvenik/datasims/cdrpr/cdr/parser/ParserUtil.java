package si.matjazcerkvenik.datasims.cdrpr.cdr.parser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class ParserUtil {

  public static Date getCdrDate(Date time, String timeZone) {
    Calendar calendar = new GregorianCalendar();
    int timeZDifference = 0;
    if (timeZone != null && !timeZone.equals("")) {
      TimeZone tz = TimeZone.getTimeZone(timeZone); // producer TZ
      int tzDST = tz.getDSTSavings(); // producer TZ daysavings in miliseconds (if timezone supports daysavings, otherwise is 0)
      TimeZone localTz = calendar.getTimeZone();
      int localTzDst = localTz.getDSTSavings();// local daysavings in miliseconds (if timezone supports daysavings, otherwise is 0)
      timeZDifference = (tz.getRawOffset() + tzDST) - (calendar.getTimeZone().getRawOffset() + localTzDst);
    }
    calendar.setTime(time);
    if (timeZDifference != 0) {
      calendar.setTimeInMillis(calendar.getTimeInMillis() - timeZDifference);
    }
    return calendar.getTime();
  }

  // -------------------------


  public static Date getCdrDate(String time) throws ParseException {
    DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.US);
    Date date = dateFormat.parse(time);
    return date;
  }
  
  public static Date getCdrDate(String endTime, long duration) throws ParseException {
    Date date1 = getCdrDate(endTime);
    Date date2 = new Date(date1.getTime() - duration);
    return date2;
  }
}
