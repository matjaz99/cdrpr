package si.iskratel.simulator;

import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.metricslib.*;

import java.sql.SQLException;

public class AggregatedCalls implements Runnable {

    private boolean running = true;
    private int threadId = 0;

    private PgClient pgClient;
    private EsClient esClient;

    private static final String INDEX_CDR_CALLS = Start.ES_INDEX_PREFIX + "pmon_cdr_node_calls_idx";
    private static final String INDEX_CDR_ACTIVE_CALLS = Start.ES_INDEX_PREFIX + "pmon_cdr_node_active_calls_idx";
    private static final String INDEX_CDR_CALL_DURATION = Start.ES_INDEX_PREFIX + "pmon_cdr_node_durations_idx";
    private static final String INDEX_CDR_BG = Start.ES_INDEX_PREFIX + "pmon_cdr_business_group_idx";
    private static final String INDEX_CDR_SUPP_SERVICE = Start.ES_INDEX_PREFIX + "pmon_cdr_supplementary_service_idx";
    private static final String INDEX_CDR_SG = Start.ES_INDEX_PREFIX + "pmon_cdr_subscriber_group_idx";
    private static final String INDEX_CDR_VOIP = Start.ES_INDEX_PREFIX + "pmon_cdr_voip_idx";
    private static final String INDEX_CDR_TRUNK = Start.ES_INDEX_PREFIX + "pmon_cdr_trunk_calls_idx";
    private static final String INDEX_CDR_TRUNK_DURATION = Start.ES_INDEX_PREFIX + "pmon_cdr_trunk_durations_idx";

    // metrics
    public static PMetric pmon_cdr_calls_by_cause = PMetric.build()
            .setName("pmon_cdr_calls_by_cause")
            .setHelp("Count calls by release cause")
            .setLabelNames("nodeName", "cause", "trafficType")
            .register(INDEX_CDR_CALLS);
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
            PMetricRegistry.getRegistry(INDEX_CDR_CALL_DURATION).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_BG).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_SUPP_SERVICE).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_SG).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_VOIP).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_TRUNK).resetMetrics();
            PMetricRegistry.getRegistry(INDEX_CDR_TRUNK_DURATION).resetMetrics();

            while (Start.getQueueSize() > 0) {

                CdrBean cdr = Start.pollCdr();
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
                } else {
                    break;
                }

            }

            PrometheusMetrics.bulkCount.set(pmon_cdr_calls_by_cause.getTimeSeriesSize());

            pmon_cdr_calls_in_progress.setLabelValues(Start.HOSTNAME).set(1.0 * StorageThread.getNumberOfCallsInProgress());

            if (Start.SIMULATOR_STORAGE_TYPE.equalsIgnoreCase("ELASTICSEARCH")) {
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_CALLS));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_CALL_DURATION));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_BG));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_SUPP_SERVICE));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_SG));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_VOIP));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_TRUNK));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_TRUNK_DURATION));
            }

            if (Start.SIMULATOR_STORAGE_TYPE.equalsIgnoreCase("POSTGRES")) {
                if (Start.PG_CREATE_TABLES_ON_START) {
                    try {
                        pgClient.createTable(pmon_cdr_calls_by_cause);
                        pgClient.createTable(pmon_cdr_call_duration);
                        pgClient.createTable(pmon_cdr_calls_in_progress);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    Start.PG_CREATE_TABLES_ON_START = false;
                }
                pgClient.sendBulk(pmon_cdr_calls_by_cause);
                pgClient.sendBulk(pmon_cdr_call_duration);
                pgClient.sendBulk(pmon_cdr_calls_in_progress);
            }


        }

    }

}
