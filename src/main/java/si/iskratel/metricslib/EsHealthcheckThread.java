package si.iskratel.metricslib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class EsHealthcheckThread implements Runnable {

    private Logger logger = LoggerFactory.getLogger(EsHealthcheckThread.class);

    private boolean ilmPolicyAlreadyCreated = false;

    @Override
    public void run() {

        EsClient es = new EsClient(MetricsLib.ES_DEFAULT_SCHEMA, MetricsLib.ES_DEFAULT_HOST, MetricsLib.ES_DEFAULT_PORT);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"name\":\"metricslib\",\"api_version\":\"").append(MetricsLib.METRICSLIB_API_VERSION).append("\",").append("\"date\":\"").append(new Date().toString()).append("\"}");

        while (true) {

            boolean succ = false;
            while (!succ) {
                logger.info("ES schema: " + MetricsLib.ES_DEFAULT_SCHEMA + " ES host: " + MetricsLib.ES_DEFAULT_HOST + " ES port: " + MetricsLib.ES_DEFAULT_PORT);
                succ = es.sendPost("/metricslib/_doc/m37r1c5l1b4b0ut", sb.toString()).success;
                logger.info("Waiting for ElasticSearch...");
                if (succ) break;
                try {
                    Thread.sleep(MetricsLib.ES_HEALTHCHECK_INTERVAL * 1000);
                } catch (InterruptedException e) {
                }
            }

            if (!ilmPolicyAlreadyCreated) {

                succ = false;
                while (!succ) {
                    succ = es.sendPut("/_cluster/settings", PMetricFormatter.getDefaultClusterSettings()).success;
                    if (succ) break;
                    try {
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                    }
                }
                logger.info("Cluster settings configured");

                succ = false;
                while (!succ) {
                    succ = es.sendPut("/_ilm/policy/" + MetricsLib.ES_ILM_POLICY_NAME, PMetricFormatter.getDefaultIlmPolicy()).success;
                    if (succ) break;
                    try {
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                    }
                }
                logger.info("ILM policy configured");
                ilmPolicyAlreadyCreated = true;

            }

            EsClient.ES_IS_READY = true;
            logger.info("ElasticSearch is ready");

            try {
                Thread.sleep(MetricsLib.ES_HEALTHCHECK_INTERVAL * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }
}
