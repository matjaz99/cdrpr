package si.matjazcerkvenik.datasims.cdrpr.simulator;

import si.matjazcerkvenik.datasims.cdrpr.cdr.parser.CdrBean;
import si.matjazcerkvenik.datasims.cdrpr.simulator.generator.StorageThread;
import si.matjazcerkvenik.metricslib.EsClient;
import si.matjazcerkvenik.metricslib.PMetric;
import si.matjazcerkvenik.metricslib.PMetricRegistry;
import si.matjazcerkvenik.metricslib.PgClient;

import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Write metrics for each node in separate index.
 */
public class AggCdrsToEsByNode implements Runnable {

    private boolean running = true;
    private int threadId = 0;

    private PgClient pgClient;
    private EsClient esClient;

    private static final String INDEX_NODEAGGS_CALLS = Props.ES_INDEX_PREFIX + "pmon_nodeaggs_node_calls_idx";
    private static final String INDEX_NODEAGGS_KPI_ASR = Props.ES_INDEX_PREFIX + "pmon_cdraggs_node_calls_asr_idx";
    private static final String INDEX_NODEAGGS_ACTIVE_CALLS = Props.ES_INDEX_PREFIX + "pmon_cdraggs_node_active_calls_idx";
    private static final String INDEX_NODEAGGS_CALL_DURATION = Props.ES_INDEX_PREFIX + "pmon_cdraggs_node_durations_idx";
    private static final String INDEX_TGAGGS_CALLS = Props.ES_INDEX_PREFIX + "pmon_cdraggs_trunk_calls_idx";
    private static final String INDEX_TGAGGS_TRUNK_DURATION = Props.ES_INDEX_PREFIX + "pmon_cdraggs_trunk_durations_idx";

    // metrics
    public static PMetric pmon_cdr_calls_by_cause = PMetric.build()
            .setName("pmon_cdr_calls_by_cause")
            .setHelp("Count calls by release cause")
            .setLabelNames("nodeName", "cause", "trafficType")
            .register(INDEX_NODEAGGS_CALLS);
    public static PMetric pmon_cdr_calls_asr = PMetric.build()
            .setName("pmon_cdr_calls_asr")
            .setHelp("Answer to seizure ratio - success rate")
            .setLabelNames("nodeName")
            .register(INDEX_NODEAGGS_KPI_ASR);

    public static PMetric pmon_cdr_calls_in_progress = PMetric.build()
            .setName("pmon_cdr_calls_in_progress")
            .setHelp("Current number of calls in progress (answered only)")
            .setLabelNames("nodeName")
            .register(INDEX_NODEAGGS_ACTIVE_CALLS);

    public static PMetric pmon_cdr_call_duration = PMetric.build()
            .setName("pmon_cdr_call_duration")
            .setHelp("Total duration of answered calls on node")
            .setLabelNames("nodeName")
            .register(INDEX_NODEAGGS_CALL_DURATION);
    public static PMetric pmon_cdr_calls_by_trunkgroup = PMetric.build()
            .setName("pmon_cdr_calls_by_trunkgroup")
            .setHelp("pmon_cdr_trunk_calls")
            .setLabelNames("nodeName", "cause", "incTG", "outTG")
            .register(INDEX_TGAGGS_CALLS);
    public static PMetric pmon_cdr_duration_by_trunkgroup = PMetric.build()
            .setName("pmon_cdr_duration_by_trunkgroup")
            .setHelp("pmon_cdr_trunk_calls_duration")
            .setLabelNames("nodeName", "incTG", "outTG")
            .register(INDEX_TGAGGS_TRUNK_DURATION);

    public AggCdrsToEsByNode(int id) {
        threadId = id;
        pgClient = new PgClient(Props.PG_URL, Props.PG_USER, Props.PG_PASS);
        esClient = new EsClient(Props.ES_SCHEMA, Props.ES_HOST, Props.ES_PORT);
    }


    @Override
    public void run() {

        while (running) {

            try {
                Thread.sleep(Props.SEND_INTERVAL_SEC * 1000);
            } catch (InterruptedException e) {
            }

            // reset metrics: clear all time-series data
            PMetricRegistry.getRegistry(INDEX_NODEAGGS_CALLS).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_NODEAGGS_KPI_ASR).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_NODEAGGS_CALL_DURATION).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_TGAGGS_CALLS).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_TGAGGS_TRUNK_DURATION).resetMetrics();

            while (StorageThread.getQueueSize() > 0) {

                CdrBean cdr = StorageThread.pollCdr();
                if (cdr != null) {
                    // fill metrics
                    pmon_cdr_calls_by_trunkgroup.setLabelValues(cdr.getNodeId(), Utils.toCauseString(cdr.getCause()), cdr.getInTrunkGroupId() + "", cdr.getOutTrunkGroupId() + "").inc();
                    pmon_cdr_calls_by_cause.setLabelValues(cdr.getNodeId(), Utils.toCauseString(cdr.getCause()), cdr.getTrafficType()).inc();
                    if (cdr.getCause() == 16) {
                        pmon_cdr_call_duration.setLabelValues(cdr.getNodeId()).inc(cdr.getDuration());
                        pmon_cdr_duration_by_trunkgroup.setLabelValues(cdr.getNodeId(), cdr.getInTrunkGroupId() + "", cdr.getOutTrunkGroupId() + "").inc(cdr.getDuration());
                    }
//                    pmon_cdr_calls_asr.setLabelValues(cdr.getNodeId()).set(pmon_cdr_calls_by_cause.getSUM("cause", "Answered") / pmon_cdr_calls_by_cause.getTimeSeriesSize());
                    PMetricRegistry.getRegistry(INDEX_TGAGGS_CALLS).getMetric("pmon_cdr_calls_by_trunkgroup");
                } else {
                    break;
                }

            }


            if (Props.SIMULATOR_TIME_OFFSET_MONTHS > 0) {
                long now = System.currentTimeMillis();
                long randomOffset = 0L;
                // rnd offset between now and then
                randomOffset = ThreadLocalRandom.current().nextLong(Props.SIMULATOR_TIME_OFFSET_MONTHS * 30 * 24 * 3600 * 1000);
                pmon_cdr_calls_by_trunkgroup.setTimestamp(now - randomOffset);
                pmon_cdr_calls_by_cause.setTimestamp(now - randomOffset);
                pmon_cdr_call_duration.setTimestamp(now - randomOffset);
                pmon_cdr_duration_by_trunkgroup.setTimestamp(now - randomOffset);
            }

            SimulatorMetrics.bulkCount.set(pmon_cdr_calls_by_cause.getTimeSeriesSize());

            pmon_cdr_calls_in_progress.setLabelValues(Props.HOSTNAME).set(1.0 * StorageThread.getNumberOfCallsInProgress());

            // TEST operations on metrics
            // convert millis to hours
//            pmon_cdr_call_duration.MULTIPLY(1.0/1000 * 1.0/3600);

            if (Props.SIMULATOR_STORAGE_TYPE.equalsIgnoreCase("ELASTICSEARCH")) {
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_NODEAGGS_CALLS));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_NODEAGGS_KPI_ASR));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_NODEAGGS_CALL_DURATION));
//                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_BG));
//                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_SUPP_SERVICE));
//                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_SG));
//                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_VOIP));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_TGAGGS_CALLS));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_TGAGGS_TRUNK_DURATION));
            }

            if (Props.SIMULATOR_STORAGE_TYPE.equalsIgnoreCase("POSTGRES")) {
                if (Props.PG_CREATE_TABLES_ON_START) {
                    try {
                        pgClient.createTable(pmon_cdr_calls_by_cause);
                        pgClient.createTable(pmon_cdr_call_duration);
                        pgClient.createTable(pmon_cdr_calls_in_progress);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    Props.PG_CREATE_TABLES_ON_START = false;
                }
                pgClient.sendBulk(pmon_cdr_calls_by_cause);
                pgClient.sendBulk(pmon_cdr_call_duration);
                pgClient.sendBulk(pmon_cdr_calls_in_progress);
            }


        }

    }

}
