package si.matjazcerkvenik.datasims.cdrpr.simulator;

import si.matjazcerkvenik.datasims.cdrpr.cdr.parser.CdrBean;
import si.matjazcerkvenik.metricslib.*;
import si.matjazcerkvenik.datasims.cdrpr.simulator.generator.StorageThread;

import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Aggregate data as single-value metric. Store data for all nodes in one index.
 */
public class AggCdrsToEs implements Runnable {

    private boolean running = true;
    private int threadId = 0;

    private PgClient pgClient;
    private EsClient esClient;

    private static final String INDEX_CDR_CALLS = Props.ES_INDEX_PREFIX + "pmon_cdraggs_node_calls_idx";
    private static final String INDEX_CDR_KPI_ASR = Props.ES_INDEX_PREFIX + "pmon_cdraggs_node_calls_asr_idx";
    private static final String INDEX_CDR_ACTIVE_CALLS = Props.ES_INDEX_PREFIX + "pmon_cdraggs_node_active_calls_idx";
    private static final String INDEX_CDR_CALL_DURATION = Props.ES_INDEX_PREFIX + "pmon_cdraggs_node_durations_idx";
    private static final String INDEX_CDR_BG = Props.ES_INDEX_PREFIX + "pmon_cdraggs_business_group_idx";
    private static final String INDEX_CDR_SUPP_SERVICE = Props.ES_INDEX_PREFIX + "pmon_cdraggs_supplementary_service_idx";
    private static final String INDEX_CDR_SG = Props.ES_INDEX_PREFIX + "pmon_cdraggs_subscriber_group_idx";
    private static final String INDEX_CDR_VOIP = Props.ES_INDEX_PREFIX + "pmon_cdraggs_voip_idx";
    private static final String INDEX_CDR_TRUNK = Props.ES_INDEX_PREFIX + "pmon_cdraggs_trunk_calls_idx";
    private static final String INDEX_CDR_TRUNK_DURATION = Props.ES_INDEX_PREFIX + "pmon_cdraggs_trunk_durations_idx";

    // metrics
    public static PMetric pmon_cdr_calls_by_cause = PMetric.build()
            .setName("pmon_cdr_calls_by_cause")
            .setHelp("Count calls by release cause")
            .setLabelNames("nodeName", "cause", "trafficType")
            .register(INDEX_CDR_CALLS);
    public static PMetric pmon_cdr_calls_asr = PMetric.build()
            .setName("pmon_cdr_calls_asr")
            .setHelp("Answer to seizure ratio - success rate")
            .setLabelNames("nodeName")
            .register(INDEX_CDR_KPI_ASR);

    public static PMetric pmon_cdr_calls_in_progress = PMetric.build()
            .setName("pmon_cdr_calls_in_progress")
            .setHelp("Current number of calls in progress (answered only)")
            .setLabelNames("nodeName")
            .register(INDEX_CDR_ACTIVE_CALLS);

    public static PMetric pmon_cdr_call_duration = PMetric.build()
            .setName("pmon_cdr_call_duration")
            .setHelp("Total duration of answered calls on node")
            .setLabelNames("nodeName")
            .register(INDEX_CDR_CALL_DURATION);
    public static PMetric pmon_cdr_time_before_ringing = PMetric.build()
            .setName("pmon_cdr_time_before_ringing")
            .setHelp("pmon_cdr_time_before_ringing")
            .setLabelNames("nodeName")
            .register(INDEX_CDR_CALL_DURATION);
    public static PMetric pmon_cdr_time_before_answer = PMetric.build()
            .setName("pmon_cdr_time_before_answer")
            .setHelp("pmon_cdr_time_before_answer")
            .setLabelNames("nodeName")
            .register(INDEX_CDR_CALL_DURATION);
    public static PMetric pmon_cdr_calls_by_trunkgroup = PMetric.build()
            .setName("pmon_cdr_calls_by_trunkgroup")
            .setHelp("pmon_cdr_trunk_calls")
            .setLabelNames("nodeName", "cause", "incTG", "outTG")
            .register(INDEX_CDR_TRUNK);
    public static PMetric pmon_cdr_duration_by_trunkgroup = PMetric.build()
            .setName("pmon_cdr_duration_by_trunkgroup")
            .setHelp("pmon_cdr_trunk_calls_duration")
            .setLabelNames("nodeName", "incTG", "outTG")
            .register(INDEX_CDR_TRUNK_DURATION);
    public static PMetric m_bgCalls = PMetric.build()
            .setName("pmon_cdr_bg_calls")
            .setHelp("BG calls")
            .setLabelNames("nodeName", "bgidOrig", "bgidTerm")
            .register(INDEX_CDR_BG);
    public static PMetric m_cgCalls = PMetric.build()
            .setName("pmon_cdr_cg_calls")
            .setHelp("CG calls")
            .setLabelNames("nodeName", "cgidOrig", "cgidTerm", "centrexCallType", "ctxCall")
            .register(INDEX_CDR_BG);
    public static PMetric m_suppServ = PMetric.build()
            .setName("pmon_cdr_supplementary_service")
            .setHelp("Supplementary services")
            .setLabelNames("nodeName", "servId", "servIdOrig", "servIdTerm")
            .register(INDEX_CDR_SUPP_SERVICE);
    public static PMetric m_subscrGrpCalls = PMetric.build()
            .setName("pmon_cdr_subscriber_group_calls")
            .setHelp("Subscriber group calls")
            .setLabelNames("nodeName", "callingSubscrGroup", "calledSubscrGroup")
            .register(INDEX_CDR_SG);
    public static PMetric m_voipRxCodec = PMetric.build()
            .setName("pmon_voip_rx_codec")
            .setHelp("VOIP rx codec")
            .setLabelNames("nodeName", "codec")
            .register(INDEX_CDR_VOIP);
    public static PMetric m_voipTxCodec = PMetric.build()
            .setName("pmon_voip_tx_codec")
            .setHelp("VOIP tx codec")
            .setLabelNames("nodeName", "codec")
            .register(INDEX_CDR_VOIP);
    public static PMetric m_voipTxRxCodec = PMetric.build()
            .setName("pmon_voip_tx_rx_codec")
            .setHelp("VOIP tx/rx codec")
            .setLabelNames("nodeName", "rxCodec", "txCodec")
            .register(INDEX_CDR_VOIP);
    public static PMetric test_multiply = PMetric.build()
            .setName("pmon_test_multiply")
            .setHelp("test_multiply")
            .setLabelNames("nodeName", "duration")
            .register(INDEX_CDR_KPI_ASR);

    public AggCdrsToEs(int id) {
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
//            PMetricRegistry.getRegistry(INDEX_CDR_CALLS_TEST).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_CALLS).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_KPI_ASR).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_CALL_DURATION).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_BG).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_SUPP_SERVICE).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_SG).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_VOIP).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_TRUNK).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_TRUNK_DURATION).resetMetrics();

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
                    pmon_cdr_time_before_ringing.setLabelValues(cdr.getNodeId()).inc(cdr.getCdrTimeBeforeRinging());
                    pmon_cdr_time_before_answer.setLabelValues(cdr.getNodeId()).inc(cdr.getCdrRingingTimeBeforeAnsw());
                    m_bgCalls.setLabelValues(cdr.getNodeId(), cdr.getBgidOrig() + "", cdr.getBgidTerm() + "").inc();
                    m_cgCalls.setLabelValues(cdr.getNodeId(), cdr.getCgidOrig() + "", cdr.getCgidTerm() + "", cdr.getCentrexCallType() + "", cdr.getCtxCall() + "").inc();
                    m_suppServ.setLabelValues(cdr.getNodeId(), cdr.getServId() + "", cdr.getServIdOrig() + "", cdr.getServIdTerm() + "").inc();
                    m_subscrGrpCalls.setLabelValues(cdr.getNodeId(), cdr.getCallingSubscriberGroup() + "", cdr.getCalledSubscriberGroup() + "").inc();
                    m_voipRxCodec.setLabelValues(cdr.getNodeId(), cdr.getVoipRxCodecType() + "").inc();
                    m_voipTxCodec.setLabelValues(cdr.getNodeId(), cdr.getVoipTxCodecType() + "").inc();
                    m_voipTxRxCodec.setLabelValues(cdr.getNodeId(), cdr.getVoipTxCodecType() + "", cdr.getVoipRxCodecType() + "").inc();
//                    pmon_cdr_calls_asr.setLabelValues(cdr.getNodeId()).set(pmon_cdr_calls_by_cause.getSUM("cause", "Answered") / pmon_cdr_calls_by_cause.getTimeSeriesSize());
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
                pmon_cdr_time_before_ringing.setTimestamp(now - randomOffset);
                pmon_cdr_time_before_answer.setTimestamp(now - randomOffset);
            }

            SimulatorMetrics.bulkCount.set(pmon_cdr_calls_by_cause.getTimeSeriesSize());

            pmon_cdr_calls_in_progress.setLabelValues(Props.HOSTNAME).set(1.0 * StorageThread.getNumberOfCallsInProgress());

            // TEST operations on metrics
            // convert millis to hours
//            pmon_cdr_call_duration.MULTIPLY(1.0/1000 * 1.0/3600);

            if (Props.SIMULATOR_STORAGE_TYPE.equalsIgnoreCase("ELASTICSEARCH")) {
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_CALLS));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_KPI_ASR));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_CALL_DURATION));
//                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_BG));
//                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_SUPP_SERVICE));
//                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_SG));
//                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_VOIP));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_TRUNK));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_TRUNK_DURATION));
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
