package si.iskratel.metricslib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class EsHealthcheckThread implements Runnable {

    private Logger logger = LoggerFactory.getLogger(EsHealthcheckThread.class);

    @Override
    public void run() {

        EsClient es = new EsClient(MetricsLib.ES_DEFAULT_SCHEMA, MetricsLib.ES_DEFAULT_HOST, MetricsLib.ES_DEFAULT_PORT);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"name\":\"metricslib\",\"api_version\":\"").append(MetricsLib.METRICSLIB_API_VERSION).append("\",").append("\"date\":\"").append(new Date().toString()).append("\"}");

        boolean succ = false;
        while (!succ) {
            logger.info("Healthcheck:  ES schema: " + MetricsLib.ES_DEFAULT_SCHEMA + " ES host: " + MetricsLib.ES_DEFAULT_HOST + " ES port: " + MetricsLib.ES_DEFAULT_PORT);
            succ = es.sendPost("/metricslib/_doc/m37r1c5l1b4b0ut", sb.toString()).success;
            logger.info("Healthcheck: Waiting for ElasticSearch...");
            if (succ) break;
            try {
                Thread.sleep(MetricsLib.ES_HEALTHCHECK_INTERVAL * 1000);
            } catch (InterruptedException e) {
            }
        }
        EsClient.ES_IS_READY = true;
        logger.info("Healthcheck:  ElasticSearch is ready");

    }
}
