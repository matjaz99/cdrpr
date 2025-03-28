package si.matjazcerkvenik.datasims.cdrpr.simulator;

/**
 * This class is intended to generate random data in specified intervals in period from-to.
 * When it finishes, it stops.
 */
public class SeqRandomDataToEs {

    public void generateData() {

        String[] nodeArray = Props.SIMULATOR_NODEID.split(",");
        for (int i = 0; i < nodeArray.length; i++) {

            for (long time = Props.SIMULATOR_START_TIME_MILLIS; time < Props.SIMULATOR_END_TIME_MILLIS; time = time + Props.SIMULATOR_SAMPLING_INTERVAL_SECONDS * 1000) {

            }
        }

    }

}
