package si.iskratel.metricslib.util;

import si.iskratel.metricslib.MetricsLib;
import si.iskratel.metricslib.PromExporter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String getFormatedTimestamp(long timestamp) {
        if (timestamp == 0) return "n/a";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(cal.getTime());
    }

    public static String convertToDHMSFormat(int secUpTotal) {
        int secUpRemain = secUpTotal % 60;
        int minUpTotal = secUpTotal / 60;
        int minUpRemain = minUpTotal % 60;
        int hourUpTotal = minUpTotal / 60;
        int hourUpRemain = hourUpTotal % 60;
        int dayUpTotal = hourUpTotal / 24;
        int dayUpRemain = hourUpTotal % 24;

        String resp = minUpRemain + "m " + secUpRemain + "s";

        if (dayUpTotal == 0) {
            if (hourUpRemain > 0) {
                resp = hourUpTotal + "h " + resp;
            }
        }

        if (dayUpTotal > 0) {
            resp = dayUpTotal + "d " + dayUpRemain + "h " + resp;
        }

        return resp;
    }

}
