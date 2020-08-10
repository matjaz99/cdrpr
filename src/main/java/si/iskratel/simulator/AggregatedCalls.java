package si.iskratel.simulator;

import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.metricslib.*;

import java.sql.SQLException;

public class AggregatedCalls implements Runnable {

    private boolean running = true;
    private int threadId = 0;

    private PgClient pgClient;
    private EsClient esClient;

    // metrics
    public static PMetric m_countByCrc = PMetric.build()
            .setName("pmon_count_by_cause")
            .setHelp("Count calls by release cause")
            .setLabelNames("node", "cause", "incTG", "outTG")
            .register();
    public static PMetric m_durationByTG = PMetric.build()
            .setName("pmon_duration_by_tg")
            .setHelp("Total duration of answered calls on trunk groups")
            .setLabelNames("node", "incTG", "outTG")
            .register();
    public static PMetric m_callsInProgress = PMetric.build()
            .setName("pmon_calls_in_progress")
            .setHelp("Current number of calls in progress (answered only)")
            .setLabelNames("node")
            .register();
    public static PMetric m_timeBeforeRing = PMetric.build()
            .setName("pmon_time_before_ring")
            .setHelp("pmon_time_before_ring")
            .setLabelNames("node")
            .register();
    public static PMetric m_timeBeforeAns = PMetric.build()
            .setName("pmon_time_before_ans")
            .setHelp("pmon_time_before_ans")
            .setLabelNames("node")
            .register();
    public static PMetric m_bgCalls = PMetric.build()
            .setName("pmon_bg_calls")
            .setHelp("BG calls")
            .setLabelNames("node", "bgidOrig", "bgidTerm")
            .register();
    public static PMetric m_cgCalls = PMetric.build()
            .setName("pmon_cg_calls")
            .setHelp("CG calls")
            .setLabelNames("node", "cgidOrig", "cgidTerm", "centrexCallType", "ctxCall")
            .register();

    public AggregatedCalls(int id) {
        threadId = id;
        pgClient = new PgClient(Start.PG_URL, Start.PG_USER, Start.PG_PASS);
        esClient = new EsClient(Start.ES_URL);
    }


    @Override
    public void run() {

        while (running) {

            try {
                Thread.sleep(Start.SEND_INTERVAL_SEC * 1000);
            } catch (InterruptedException e) {
            }

            // reset metrics: clear all time-series data and restart timestamp
            PMetricRegistry.getRegistry("default").clearTimeSeriesInMetrics(System.currentTimeMillis());

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
                esClient.sendBulkPost(m_countByCrc);
                esClient.sendBulkPost(m_durationByTG);
                esClient.sendBulkPost(m_callsInProgress);
                esClient.sendBulkPost(m_timeBeforeRing);
                esClient.sendBulkPost(m_timeBeforeAns);
                esClient.sendBulkPost(m_bgCalls);
                esClient.sendBulkPost(m_cgCalls);
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

    private void aggregate(CdrBean cdrBean) {

        // fill metrics
        try {
            m_countByCrc.setLabelValues(cdrBean.getNodeId(), Utils.toCauseString(cdrBean.getCause()), cdrBean.getInTrunkGroupId() + "", cdrBean.getOutTrunkGroupId() + "").inc();
            if (cdrBean.getCause() == 16)
                m_durationByTG.setLabelValues(cdrBean.getNodeId(), cdrBean.getInTrunkGroupId() + "", cdrBean.getOutTrunkGroupId() + "").inc(cdrBean.getDuration());
            m_timeBeforeRing.setLabelValues(cdrBean.getNodeId()).inc(cdrBean.getCdrTimeBeforeRinging());
            m_timeBeforeAns.setLabelValues(cdrBean.getNodeId()).inc(cdrBean.getCdrRingingTimeBeforeAnsw());
            m_bgCalls.setLabelValues(cdrBean.getNodeId(), cdrBean.getBgidOrig() + "", cdrBean.getBgidTerm() + "").inc();
            m_cgCalls.setLabelValues(cdrBean.getNodeId(), cdrBean.getCgidOrig() + "", cdrBean.getCgidTerm() + "", cdrBean.getCentrexCallType() + "", cdrBean.getCtxCall() + "").inc();
        } catch (PMetricException e) {
            e.printStackTrace();
        }


    }




}
