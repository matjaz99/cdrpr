package si.iskratel.metricslib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.iskratel.metricslib.util.StateLog;

import java.io.File;
import java.util.Date;

public class EsHealthcheckThread implements Runnable {

    private Logger logger = LoggerFactory.getLogger(EsHealthcheckThread.class);

    private boolean ilmPolicyAlreadyCreated = false;

    @Override
    public void run() {

        // read cluster.json and ilm_policy files if they exist
        String cluster_json = FileClient.readFile(new File(MetricsLib.ES_CLUSTER_FILE));
        if (cluster_json == null) {
            cluster_json = PMetricFormatter.getDefaultClusterSettings();
            StateLog.addToStateLog(MetricsLib.ES_CLUSTER_FILE, "File not found. Using defaults.");
        }
        String ilm_json = FileClient.readFile(new File(MetricsLib.ES_ILM_POLICY_FILE));
        if (ilm_json == null) {
            ilm_json = PMetricFormatter.getDefaultIlmPolicy();
            StateLog.addToStateLog(MetricsLib.ES_ILM_POLICY_FILE,"File not found. Using defaults.");
        }

        EsClient es = new EsClient(MetricsLib.ES_DEFAULT_SCHEMA, MetricsLib.ES_DEFAULT_HOST, MetricsLib.ES_DEFAULT_PORT);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"name\":\"metricslib\",\"api_version\":\"").append(MetricsLib.METRICSLIB_API_VERSION).append("\",").append("\"date\":\"").append(new Date().toString()).append("\"}");

        boolean succ = false;
        while (!succ) {
            succ = es.sendPost("/metricslib/_doc/m37r1c5l1b4b0ut", sb.toString()).success;
            logger.info("Waiting for ElasticSearch...");
            if (succ) break;
            StateLog.addToStateLog("ES_IS_READY", Boolean.toString(EsClient.ES_IS_READY));
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
            }
        }

        while (true) {

            succ = false;
            int failCount = 0;
            while (!succ) {
                succ = es.sendGet("/metricslib/_doc/m37r1c5l1b4b0ut").success;
                if (succ) break;
                failCount ++;
                if (failCount == 3) {
                    EsClient.ES_IS_READY = false;
                }
                logger.info("ES_IS_READY=false");
                StateLog.addToStateLog("ES_IS_READY", Boolean.toString(EsClient.ES_IS_READY));
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {
                }
            }

            if (!ilmPolicyAlreadyCreated) {

                succ = false;
                while (!succ) {
                    succ = es.sendPut("/_cluster/settings", cluster_json).success;
                    if (succ) break;
                    try {
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                    }
                }
                logger.info("Cluster settings configured");

                succ = false;
                while (!succ) {
                    succ = es.sendPut("/_ilm/policy/" + MetricsLib.ES_ILM_POLICY_NAME, ilm_json).success;
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
            logger.info("ES_IS_READY=true");
            StateLog.addToStateLog("ES_IS_READY", Boolean.toString(EsClient.ES_IS_READY));

            try {
                Thread.sleep(MetricsLib.ES_HEALTHCHECK_INTERVAL * 1000);
            } catch (InterruptedException e) {
            }

        }

    }
}
