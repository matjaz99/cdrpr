package si.iskratel.simulator;

import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.metricslib.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AggregatedCalls implements Runnable {

    private boolean running = true;
    private int threadId = 0;
    private int waitInterval = 5 * 60 * 1000;
    private long aggregationTimestamp;

    private PgClient pgClient;

    private List<CdrBean> tempList = new ArrayList<>();

    // metrics
    public static PMetric m_countByCrc = PMetric.build()
            .setName("m_countByCrc")
            .setHelp("Count calls by release cause")
            .setLabelNames("nodeId", "cause", "incTG", "outTG")
            .register();
    public static PMetric m_durationByTG = PMetric.build()
            .setName("m_durationByTG")
            .setHelp("Total duration of answered calls on trunk groups")
            .setLabelNames("nodeId", "incTG", "outTG")
            .register();
    public static PMetric m_callsInProgress = PMetric.build()
            .setName("m_callsInProgress")
            .setHelp("Current number of calls in progress (answered only)")
            .setLabelNames("host")
            .register();

    public AggregatedCalls(int id) {
        threadId = id;
        pgClient = new PgClient(Start.PG_URL, Start.PG_USER, Start.PG_PASS);
    }


    @Override
    public void run() {

        while (running) {

            try {
                Thread.sleep(waitInterval);
            } catch (InterruptedException e) {
            }

            // TODO reset metrics
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
                    tempList.add(c);
                } else {
                    break;
                }

            }

            for (CdrBean b : tempList) {
                aggregate(b);
            }

            PrometheusMetrics.bulkCount.set(m_countByCrc.getTimeSeriesSize());

            try {
                m_callsInProgress.setLabelValues(Start.HOSTNAME).setValue(1.0 * StorageThread.getNumberOfCallsInProgress());
            } catch (PMetricException e) {
                e.printStackTrace();
            }
            System.out.println("-> sending metrics: " + m_countByCrc.getName() + ", size: " + m_countByCrc.getTimeSeriesSize());
            System.out.println("-> sending metrics: " + m_durationByTG.getName() + ", size: " +  + m_durationByTG.getTimeSeriesSize());
            System.out.println("-> sending metrics: " + m_callsInProgress.getName() + ", size: " +  + m_callsInProgress.getTimeSeriesSize());

            if (Start.SIMULATOR_STORAGE_TYPE.equalsIgnoreCase("ELASTICSEARCH")) {
                EsClient.sendBulkPost(m_countByCrc);
                EsClient.sendBulkPost(m_durationByTG);
                EsClient.sendBulkPost(m_callsInProgress);
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

            tempList.clear();

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
