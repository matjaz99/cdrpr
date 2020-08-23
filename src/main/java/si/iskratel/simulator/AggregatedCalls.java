package si.iskratel.simulator;

import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.metricslib.*;

import java.sql.SQLException;

public class AggregatedCalls implements Runnable {

    private boolean running = true;
    private int threadId = 0;

    private PgClient pgClient;
    private EsClient esClient;

    private static final String INDEX_CDR = "pmon_cdr_aggs";
    private static final String INDEX_CDR_VOIP = "pmon_cdr_voip";
    private static final String INDEX_XML = "pmon_xml";

    // metrics
    public static PMetric m_countByCrc = PMetric.build()
            .setName("pmon_count_by_cause")
            .setHelp("Count calls by release cause")
            .setLabelNames("node", "cause", "incTG", "outTG")
            .register(INDEX_CDR);
    public static PMetric m_durationByTG = PMetric.build()
            .setName("pmon_duration_by_tg")
            .setHelp("Total duration of answered calls on trunk groups")
            .setLabelNames("node", "incTG", "outTG")
            .register(INDEX_CDR);
    public static PMetric m_callsInProgress = PMetric.build()
            .setName("pmon_calls_in_progress")
            .setHelp("Current number of calls in progress (answered only)")
            .setLabelNames("node")
            .register(INDEX_CDR);
    public static PMetric m_timeBeforeRing = PMetric.build()
            .setName("pmon_time_before_ring")
            .setHelp("pmon_time_before_ring")
            .setLabelNames("node")
            .register(INDEX_CDR);
    public static PMetric m_timeBeforeAns = PMetric.build()
            .setName("pmon_time_before_ans")
            .setHelp("pmon_time_before_ans")
            .setLabelNames("node")
            .register(INDEX_CDR);
    public static PMetric m_bgCalls = PMetric.build()
            .setName("pmon_bg_calls")
            .setHelp("BG calls")
            .setLabelNames("node", "bgidOrig", "bgidTerm")
            .register(INDEX_CDR);
    public static PMetric m_cgCalls = PMetric.build()
            .setName("pmon_cg_calls")
            .setHelp("CG calls")
            .setLabelNames("node", "cgidOrig", "cgidTerm", "centrexCallType", "ctxCall")
            .register(INDEX_CDR);
    public static PMetric m_suppServ = PMetric.build()
            .setName("pmon_supp_service")
            .setHelp("Supplementary services")
            .setLabelNames("node", "servId", "servIdOrig", "servIdTerm")
            .register(INDEX_CDR);
    public static PMetric m_subscrGrpCalls = PMetric.build()
            .setName("pmon_subscriber_group_calls")
            .setHelp("Subscriber group calls")
            .setLabelNames("node", "callingSubscrGroup", "calledSubscrGroup")
            .register(INDEX_CDR);
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
            .setHelp("VOIP tx codec")
            .setLabelNames("rxCodec", "txCodec")
            .register(INDEX_CDR_VOIP);
    public static PMetric m_xml_test_1 = PMetric.build()
            .setName("pmon_xml_test_1")
            .setHelp("xml_test_1")
            .setLabelNames("elementType", "node")
            .register(INDEX_XML);

    public AggregatedCalls(int id) {
        threadId = id;
        pgClient = new PgClient(Start.PG_URL, Start.PG_USER, Start.PG_PASS);
//        esClient = new EsClient(Start.ES_URL);
        esClient = new EsClient(Start.ES_HOST, Start.ES_PORT, Start.ES_INDEX);
    }


    @Override
    public void run() {

        while (running) {

            try {
                Thread.sleep(Start.SEND_INTERVAL_SEC * 1000);
            } catch (InterruptedException e) {
            }

            // reset metrics: clear all time-series data and restart timestamp
            PMetricRegistry.getRegistry(INDEX_CDR).clearTimeSeriesInMetrics(System.currentTimeMillis());
            PMetricRegistry.getRegistry(INDEX_CDR_VOIP).clearTimeSeriesInMetrics(System.currentTimeMillis());
            PMetricRegistry.getRegistry(INDEX_XML).clearTimeSeriesInMetrics(System.currentTimeMillis());

            while (Start.getQueueSize() > 0) {

                CdrBean c = Start.pollCdr();
                if (c != null) {
                    aggregate(c);
                } else {
                    break;
                }

            }

            PrometheusMetrics.bulkCount.set(m_countByCrc.getTimeSeriesSize());

            try {
                m_callsInProgress.setLabelValues(Start.HOSTNAME).set(1.0 * StorageThread.getNumberOfCallsInProgress());
            } catch (PMetricException e) {
                e.printStackTrace();
            }

            if (Start.SIMULATOR_STORAGE_TYPE.equalsIgnoreCase("ELASTICSEARCH")) {
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_CDR_VOIP));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(INDEX_XML));
            }

            if (Start.SIMULATOR_STORAGE_TYPE.equalsIgnoreCase("POSTGRES")) {
                if (Start.PG_CREATE_TABLES_ON_START) {
                    try {
                        pgClient.createTable(m_countByCrc);
                        pgClient.createTable(m_durationByTG);
                        pgClient.createTable(m_callsInProgress);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    Start.PG_CREATE_TABLES_ON_START = false;
                }
                pgClient.sendBulk(m_countByCrc);
                pgClient.sendBulk(m_durationByTG);
                pgClient.sendBulk(m_callsInProgress);
            }


        }

    }

    private void aggregate(CdrBean cdr) {

        // fill metrics
        try {
            m_countByCrc.setLabelValues(cdr.getNodeId(), Utils.toCauseString(cdr.getCause()), cdr.getInTrunkGroupId() + "", cdr.getOutTrunkGroupId() + "").inc();
            if (cdr.getCause() == 16)
                m_durationByTG.setLabelValues(cdr.getNodeId(), cdr.getInTrunkGroupId() + "", cdr.getOutTrunkGroupId() + "").inc(cdr.getDuration());
            m_timeBeforeRing.setLabelValues(cdr.getNodeId()).inc(cdr.getCdrTimeBeforeRinging());
            m_timeBeforeAns.setLabelValues(cdr.getNodeId()).inc(cdr.getCdrRingingTimeBeforeAnsw());
            m_bgCalls.setLabelValues(cdr.getNodeId(), cdr.getBgidOrig() + "", cdr.getBgidTerm() + "").inc();
            m_cgCalls.setLabelValues(cdr.getNodeId(), cdr.getCgidOrig() + "", cdr.getCgidTerm() + "", cdr.getCentrexCallType() + "", cdr.getCtxCall() + "").inc();
            m_suppServ.setLabelValues(cdr.getNodeId(), cdr.getServId() + "", cdr.getServIdOrig() + "", cdr.getServIdTerm() + "").inc();
            m_subscrGrpCalls.setLabelValues(cdr.getNodeId(), cdr.getCallingSubscriberGroup() + "", cdr.getCalledSubscriberGroup() + "").inc();
            m_voipRxCodec.setLabelValues(cdr.getVoipRxCodecType() + "").inc();
            m_voipTxCodec.setLabelValues(cdr.getVoipTxCodecType() + "").inc();
            m_voipTxRxCodec.setLabelValues(cdr.getVoipTxCodecType() + "", cdr.getVoipRxCodecType() + "");
        } catch (PMetricException e) {
            e.printStackTrace();
        }


    }




}
