package si.iskratel.simulator;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class KrNeki {

    public static void main(String... arg) throws Exception {

        setTimeFromFilename("1000020200907141523.si2");
        setTimeFromFilename("1000020200907021523.si2");
        System.out.println("===========================================================================");

    }

    public static void playWithCosinus() throws InterruptedException {
        double x = 0.0;
        while (x < 100) {
            System.out.println("x=" + x + ", y=" + Math.cos(x));
            x += 0.1;
        }
    }

    public static void playWithCos() throws InterruptedException {
        while (true) {
            Thread.sleep(2000);
            long t = System.currentTimeMillis() / 10000;
            System.out.println("t=" + t + ", y=" + Math.cos(t));
        }
    }

    public static void playWithGauss() throws InterruptedException {
        int mean = 10;
        int dev = 10;
        while (true) {
            Thread.sleep(2000);
            Random r = new Random();
            double gauss = r.nextGaussian();
            System.out.println(mean + gauss * dev);
        }
    }

    public static void setTimeFromFilename(String filename) {

        filename = filename.replace(".si2", "");

        int x = filename.length();
        String second = filename.substring(x-2, x);
        String minute = filename.substring(x-4, x-2);
        String hour = filename.substring(x-6, x-4);
        String day = filename.substring(x-8, x-6);
        String month = filename.substring(x-10, x-8);
        String year = filename.substring(x-14, x-10);

        System.out.println(filename);
        System.out.println(second);
        System.out.println(minute);
        System.out.println(hour);
        System.out.println(day);
        System.out.println(month);
        System.out.println(year);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(year));
        cal.set(Calendar.MONTH, Integer.parseInt(month));
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
        cal.set(Calendar.MINUTE, Integer.parseInt(minute));
        cal.set(Calendar.SECOND, Integer.parseInt(second));
        cal.set(Calendar.MILLISECOND, 0);

        Date d = cal.getTime();
        System.out.println(Utils.toDateString(d) + " --> " + d.getTime());

    }


}
