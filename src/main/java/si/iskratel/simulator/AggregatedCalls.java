package si.iskratel.simulator;

import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.metricslib.*;

import java.sql.SQLException;

public class AggregatedCalls implements Runnable {

    private boolean running = true;
    private int threadId = 0;
    private long aggregationTimestamp;

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
            aggregationTimestamp = System.currentTimeMillis();
            m_countByCrc.setTimestamp(aggregationTimestamp);
            m_countByCrc.clear();
            m_durationByTG.setTimestamp(aggregationTimestamp);
            m_durationByTG.clear();
            m_callsInProgress.setTimestamp(aggregationTimestamp);
            m_callsInProgress.clear();

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
            System.out.println("-> sending metrics: " + m_countByCrc.getName() + ", size: " + m_countByCrc.getTimeSeriesSize());
            System.out.println("-> sending metrics: " + m_durationByTG.getName() + ", size: " +  + m_durationByTG.getTimeSeriesSize());
            System.out.println("-> sending metrics: " + m_callsInProgress.getName() + ", size: " +  + m_callsInProgress.getTimeSeriesSize());

            if (Start.SIMULATOR_STORAGE_TYPE.equalsIgnoreCase("ELASTICSEARCH")) {
                esClient.sendBulkPost(m_countByCrc);
                esClient.sendBulkPost(m_durationByTG);
                esClient.sendBulkPost(m_callsInProgress);
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
        } catch (PMetricException e) {
            e.printStackTrace();
        }


    }




}
