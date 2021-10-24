package si.iskratel.simulator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static String toDateString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    public static String toCauseString(int cause) {
        String newCrc = "";
        switch (cause) {
            case 16:
                newCrc = "Answered";
                break;
            case 17:
                newCrc = "Busy";
                break;
            case 19:
                newCrc = "No answer";
                break;
            case 21:
                newCrc = "Rejected";
                break;
            case 38:
                newCrc = "Network out of order";
                break;
            case 6:
                newCrc = "Channel unacceptable";
                break;
            case 3:
                newCrc = "No route to destination";
                break;
            default:
                newCrc = "Other";
        }
        return newCrc;
    }

}
