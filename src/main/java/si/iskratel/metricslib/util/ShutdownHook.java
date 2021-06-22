package si.iskratel.metricslib.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.iskratel.metricslib.FileClient;
import si.iskratel.metricslib.MetricsLib;

public class ShutdownHook extends Thread {

    private static Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

    public ShutdownHook() {
        super();
        setName("ShutdownHook");
    }

    @Override
    public void run() {

        logger.info("Shutdown hook activated");

        logger.info("Removing pid file");
        FileClient.removeFile(MetricsLib.METRICSLIB_PID_FILE);

        logger.info("Stopped\n");

    }
}
