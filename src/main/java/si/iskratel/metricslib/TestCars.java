package si.iskratel.metricslib;

import java.util.Random;

public class TestCars {

    public static String[] cars = {"Volkswagen", "Renault", "Ford", "Toyota", "Fiat", "Peugeot"};
    public static String[] colors = {"Red", "Green", "Blue"};
    public static String[] carTypes = {"Car", "Bus", "Truck"};


    public static void main(String... args) throws Exception {

        MetricsLib.EXPORT_PROMETHEUS_METRICS = true;
        MetricsLib.DEFAULT_ES_HOST = "mcrk-docker-1";
        MetricsLib.DEFAULT_ES_PORT = 9200;
        MetricsLib.init();

        PMetric car_metric = PMetric.build()
                .setName("test_cars_count")
                .setHelp("Counting cars")
                .setLabelNames("car", "color", "type")
                .register("car_statistics");

        EsClient es = new EsClient("mcrk-docker-1", 9200);

        while (true) {

            car_metric.setLabelValues(getRandomCar(), getRandomColor(), getRandomCarType()).inc();
            es.sendBulkPost(car_metric);

            Thread.sleep(10000);
        }

    }


    public static String getRandomCar() {
        return cars[getRandomInRange(0, cars.length - 1)];
    }

    public static String getRandomColor() {
        return colors[getRandomInRange(0, colors.length - 1)];
    }

    public static String getRandomCarType() {
        return carTypes[getRandomInRange(0, carTypes.length - 1)];
    }

    private static int getRandomInRange(int min, int max) {
        // min and max are inclusive
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

}
