package si.iskratel.simulator;

import io.prometheus.client.Gauge;
import si.iskratel.cdr.parser.CdrBean;
import si.iskratel.monitoring.EsClient;
import si.iskratel.monitoring.PMetricException;
import si.iskratel.monitoring.PgClient;
import si.iskratel.monitoring.PMetric;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AggregatedCalls implements Runnable {

    private boolean running = true;
    private int threadId = 0;
    private int waitInterval = 60 * 1000;
    private long aggregationTimestamp;

    private PgClient pgClient;

    private List<CdrBean> tempList = new ArrayList<>();


    // prometheus metrics
    public static final Gauge countByCrc = Gauge.build().name("cdrpr_agg_byCrc_total")
            .labelNames("nodeId", "releaseCause", "incTG", "outTG").help("Number of calls by crc").register();
    public static final Gauge countByDuration = Gauge.build().name("cdrpr_agg_byDuration_total")
            .labelNames("nodeId", "incTG", "outTG").help("Total duration").register();

    // pmetrics
    public static PMetric m_countByCrc = PMetric.build().name("m_countByCrc")
            .labelNames("nodeId", "cause", "incTG", "outTG").register();
    public static PMetric m_durationByTG = PMetric.build().name("m_durationByTG")
            .labelNames("nodeId", "incTG", "outTG").register();
    public static PMetric m_callsInProgress = PMetric.build().name("m_callsInProgress")
            .labelNames("host").register();

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

            //CdrMetricRegistry.dumpAllMetrics();
            try {
                m_callsInProgress.labelValues(Start.HOSTNAME).setValue(1.0 * StorageThread.getNumberOfCallsInProgress());
            } catch (PMetricException e) {
                e.printStackTrace();
            }
            System.out.println("sending metrics: " + m_countByCrc.getName() + ", size: " + m_countByCrc.getTimeSeriesSize());
            System.out.println("sending metrics: " + m_durationByTG.getName() + ", size: " +  + m_durationByTG.getTimeSeriesSize());
            System.out.println("sending metrics: " + m_callsInProgress.getName() + ", size: " +  + m_callsInProgress.getTimeSeriesSize());

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

        // prometheus metrics
        countByCrc.labels(cdrBean.getNodeId(), Utils.toCauseString(cdrBean.getCause()), cdrBean.getInTrunkGroupId() + "", cdrBean.getOutTrunkGroupId() + "").inc();
        if (cdrBean.getCause() == 16) {
            countByDuration.labels(cdrBean.getNodeId(), cdrBean.getInTrunkGroupId() + "", cdrBean.getOutTrunkGroupId() + "").inc(cdrBean.getDuration());
        }

        // cdrpr metrics
        try {
            m_countByCrc.labelValues(cdrBean.getNodeId(), Utils.toCauseString(cdrBean.getCause()), cdrBean.getInTrunkGroupId() + "", cdrBean.getOutTrunkGroupId() + "").inc();
            if (cdrBean.getCause() == 16)
                m_durationByTG.labelValues(cdrBean.getNodeId(), cdrBean.getInTrunkGroupId() + "", cdrBean.getOutTrunkGroupId() + "").inc(cdrBean.getDuration());
        } catch (PMetricException e) {
            e.printStackTrace();
        }


    }




}
