package si.iskratel.simulator;

import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.metricslib.*;

import java.sql.SQLException;

public class AggregatedCalls implements Runnable {

    private boolean running = true;
    private int threadId = 0;

    private PgClient pgClient;
    private EsClient esClient;

    private static final String INDEX_CDR_CALLS_TEST = "pmon_cdr_calls_test_idx";
    private static final String INDEX_CDR_CALLS = "pmon_cdr_calls_idx";
    private static final String INDEX_CDR_CALL_TIMING = "pmon_cdr_call_timings_idx";
    private static final String INDEX_CDR_CALL_DURATION = "pmon_cdr_call_durations_idx";
    private static final String INDEX_CDR_BG = "pmon_cdr_business_group_idx";
    private static final String INDEX_CDR_SUPP_SERVICE = "pmon_cdr_supplementary_service_idx";
    private static final String INDEX_CDR_SG = "pmon_cdr_subscriber_group_idx";
    private static final String INDEX_CDR_VOIP = "pmon_cdr_voip_idx";
    private static final String INDEX_CDR_TRUNK = "pmon_cdr_trunk_calls_idx";
    private static final String INDEX_CDR_TRUNK_DURATION = "pmon_cdr_trunk_durations_idx";

    // metrics
    public static PMetric m_countByCrc = PMetric.build()
            .setName("pmon_cdr_calls_by_cause")
            .setHelp("Count calls by release cause")
            .setLabelNames("node", "cause", "trafficType")
            .register(INDEX_CDR_CALLS);
    public static PMetric m_call_durations = PMetric.build()
            .setName("pmon_cdr_calls_by_duration")
            .setHelp("Total duration of answered calls on node")
            .setLabelNames("node")
            .register(INDEX_CDR_CALL_DURATION);
    public static PMetric m_callsInProgress = PMetric.build()
            .setName("pmon_cdr_calls_in_progress")
            .setHelp("Current number of calls in progress (answered only)")
            .setLabelNames("node")
            .register(INDEX_CDR_CALLS);
    public static PMetric m_timeBeforeRing = PMetric.build()
            .setName("pmon_cdr_time_before_ringing")
            .setHelp("pmon_cdr_time_before_ringing")
            .setLabelNames("node")
            .register(INDEX_CDR_CALL_TIMING);
    public static PMetric m_timeBeforeAns = PMetric.build()
            .setName("pmon_cdr_time_before_answer")
            .setHelp("pmon_cdr_time_before_answer")
            .setLabelNames("node")
            .register(INDEX_CDR_CALL_TIMING);
    public static PMetric m_trunkCalls = PMetric.build()
            .setName("pmon_cdr_trunk_calls")
            .setHelp("pmon_cdr_trunk_calls")
            .setLabelNames("node", "cause", "incTG", "outTG")
            .register(INDEX_CDR_TRUNK);
    public static PMetric m_trunkDuration = PMetric.build()
            .setName("pmon_cdr_trunk_calls_duration")
            .setHelp("pmon_cdr_trunk_calls_duration")
            .setLabelNames("node", "incTG", "outTG")
            .register(INDEX_CDR_TRUNK_DURATION);
    public static PMetric m_bgCalls = PMetric.build()
            .setName("pmon_cdr_bg_calls")
            .setHelp("BG calls")
            .setLabelNames("node", "bgidOrig", "bgidTerm")
            .register(INDEX_CDR_BG);
    public static PMetric m_cgCalls = PMetric.build()
            .setName("pmon_cdr_cg_calls")
            .setHelp("CG calls")
            .setLabelNames("node", "cgidOrig", "cgidTerm", "centrexCallType", "ctxCall")
            .register(INDEX_CDR_BG);
    public static PMetric m_suppServ = PMetric.build()
            .setName("pmon_cdr_supplementary_service")
            .setHelp("Supplementary services")
            .setLabelNames("node", "servId", "servIdOrig", "servIdTerm")
            .register(INDEX_CDR_SUPP_SERVICE);
    public static PMetric m_subscrGrpCalls = PMetric.build()
            .setName("pmon_cdr_subscriber_group_calls")
            .setHelp("Subscriber group calls")
            .setLabelNames("node", "callingSubscrGroup", "calledSubscrGroup")
            .register(INDEX_CDR_SG);
    public static PMetric m_voipRxCodec = PMetric.build()
            .setName("pmon_voip_rx_codec")
            .setHelp("VOIP rx codec")
            .setLabelNames("codec")
            .register(INDEX_CDR_VOIP);
    public static PMetric m_voipTxCodec = PMetric.build()
            .setName("pmon_voip_tx_codec")
            .setHelp("VOIP tx codec")
            .setLabelNames("codec")
            .register(INDEX_CDR_VOIP);
    public static PMetric m_voipTxRxCodec = PMetric.build()
            .setName("pmon_voip_tx_rx_codec")
            .setHelp("VOIP tx/rx codec")
            .setLabelNames("rxCodec", "txCodec")
            .register(INDEX_CDR_VOIP);

    public AggregatedCalls(int id) {
        threadId = id;
        pgClient = new PgClient(Start.PG_URL, Start.PG_USER, Start.PG_PASS);
        esClient = new EsClient(Start.ES_HOST, Start.ES_PORT);
    }


    @Override
    public void run() {

        while (running) {

            try {
                Thread.sleep(Start.SEND_INTERVAL_SEC * 1000);
            } catch (InterruptedException e) {
            }

            // reset metrics: clear all time-series data
//            PMetricRegistry.getRegistry(INDEX_CDR_CALLS_TEST).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_CALLS).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_CALL_TIMING).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_CALL_DURATION).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_BG).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_SUPP_SERVICE).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_SG).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_VOIP).resetMetrics();

            while (Start.getQueueSize() > 0) {

                CdrBean cdr = Start.pollCdr();
                if (cdr != null) {
                    // fill metrics
                    m_trunkCalls.setLabelValues(cdr.getNodeId(), Utils.toCauseString(cdr.getCause()), cdr.getInTrunkGroupId() + "", cdr.getOutTrunkGroupId() + "").inc();
                    m_countByCrc.setLabelValues(cdr.getNodeId(), Utils.toCauseString(cdr.getCause()), cdr.getTrafficType()).inc();
                    if (cdr.getCause() == 16) {
                        m_call_durations.setLabelValues(cdr.getNodeId()).inc(cdr.getDuration());
                        m_trunkDuration.setLabelValues(cdr.getNodeId(), cdr.getInTrunkGroupId() + "", cdr.getOutTrunkGroupId() + "").inc(cdr.getDuration());
                    }
                    m_timeBeforeRing.setLabelValues(cdr.getNodeId()).inc(cdr.getCdrTimeBeforeRinging());
                    m_timeBeforeAns.setLabelValues(cdr.getNodeId()).inc(cdr.getCdrRingingTimeBeforeAnsw());
                    m_bgCalls.setLabelValues(cdr.getNodeId(), cdr.getBgidOrig() + "", cdr.getBgidTerm() + "").inc();
                    m_cgCalls.setLabelValues(cdr.getNodeId(), cdr.getCgidOrig() + "", cdr.getCgidTerm() + "", cdr.getCentrexCallType() + "", cdr.getCtxCall() + "").inc();
                    m_suppServ.setLabelValues(cdr.getNodeId(), cdr.getServId() + "", cdr.getServIdOrig() + "", cdr.getServIdTerm() + "").inc();
                    m_subscrGrpCalls.setLabelValues(cdr.getNodeId(), cdr.getCallingSubscriberGroup() + "", cdr.getCalledSubscriberGroup() + "").inc();
                    m_voipRxCodec.setLabelValues(cdr.getVoipRxCodecType() + "").inc();
                    m_voipTxCodec.setLabelValues(cdr.getVoipTxCodecType() + "").inc();
                    m_voipTxRxCodec.setLabelValues(cdr.getVoipTxCodecType() + "", cdr.getVoipRxCodecType() + "").inc();
                } else {
                    break;
                }

            }

            PrometheusMetrics.bulkCount.set(m_countByCrc.getTimeSeriesSize());

            m_callsInProgress.setLabelValues(Start.HOSTNAME).set(1.0 * StorageThread.getNumberOfCallsInProgress());

            if (Start.SIMULATOR_STORAGE_TYPE.equalsIgnoreCase("ELASTICSEARCH")) {
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_CALLS));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_CALL_TIMING));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_CALL_DURATION));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_BG));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_SUPP_SERVICE));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_SG));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_VOIP));
            }

            if (Start.SIMULATOR_STORAGE_TYPE.equalsIgnoreCase("POSTGRES")) {
                if (Start.PG_CREATE_TABLES_ON_START) {
                    try {
                        pgClient.createTable(m_countByCrc);
                        pgClient.createTable(m_call_durations);
                        pgClient.createTable(m_callsInProgress);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    Start.PG_CREATE_TABLES_ON_START = false;
                }
                pgClient.sendBulk(m_countByCrc);
                pgClient.sendBulk(m_call_durations);
                pgClient.sendBulk(m_callsInProgress);
            }


        }

    }

}
