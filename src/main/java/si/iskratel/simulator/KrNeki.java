package si.iskratel.simulator;

import java.util.Random;

public class KrNeki {

    public static void main(String... arg) throws Exception {

        double x = 0.0;
        while (x < 100) {
            System.out.println("x=" + x + ", y=" + Math.cos(x));
            x += 0.1;
        }

        System.out.println("===========================================================================");

        playWithGauss();

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


}
