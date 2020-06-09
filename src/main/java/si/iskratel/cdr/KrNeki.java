package si.iskratel.cdr;

import java.util.Random;

public class KrNeki {

    public static void main(String... arg) {

        double x = 0.0;
        while (x < 100) {
            System.out.println("x=" + x + ", y=" + Math.cos(x));
            x += 0.1;
        }

    }

}
