package si.iskratel.cdr;

import io.prometheus.client.Gauge;
import si.iskratel.cdr.parser.CdrBean;

import java.util.ArrayList;
import java.util.List;

public class EsStoreAggregatedCalls implements IPersistenceClient {

    private boolean running = true;
    private int threadId = 0;
    private int waitInterval = 60 * 1000;
    private long aggregationTimestamp;

    private List<CdrBean> tempList = new ArrayList<>();


    public static final Gauge countByCrc = Gauge.build().name("cdrpr_agg_byCrc_total")
            .labelNames("nodeId", "releaseCause", "incTG", "outTG").help("Number of calls by crc").register();
    public static final Gauge countByDuration = Gauge.build().name("cdrpr_agg_byDuration_total")
            .labelNames("nodeId", "incTG", "outTG").help("Total duration").register();

    public static PMetric m_countByCrc = PMetric.build().setName("m_countByCrc")
            .setLabelNames("nodeId", "cause", "incTG", "outTG").register();
    public static PMetric m_durationByTG = PMetric.build().setName("m_durationByTG")
            .setLabelNames("nodeId", "incTG", "outTG").register();
    public static PMetric m_callsInProgress = PMetric.build().setName("m_callsInProgress")
            .setLabelNames("host").register();

    public EsStoreAggregatedCalls(int id) {
        threadId = id;
    }

    @Override
    public void send() {
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
            m_callsInProgress.setLabelValues(Start.HOSTNAME).setValue(StorageThread.getNumberOfCallsInProgress());
            System.out.println("sending metrics: " + m_countByCrc.getName() + ", size: " + m_countByCrc.getTimeSeriesSize());
            System.out.println("sending metrics: " + m_durationByTG.getName() + ", size: " +  + m_durationByTG.getTimeSeriesSize());
            System.out.println("sending metrics: " + m_callsInProgress.getName() + ", size: " +  + m_callsInProgress.getTimeSeriesSize());

            EsClient.sendBulkPost(m_countByCrc.toJsonString());
            EsClient.sendBulkPost(m_durationByTG.toJsonString());
            EsClient.sendBulkPost(m_callsInProgress.toJsonString());

            tempList.clear();

        }

    }

    private void aggregate(CdrBean cdrBean) {

        // prometheus metrics
        countByCrc.labels(cdrBean.getNodeId(), toCauseString(cdrBean.getCause()), cdrBean.getInTrunkGroupId() + "", cdrBean.getOutTrunkGroupId() + "").inc();
        if (cdrBean.getCause() == 16) {
            countByDuration.labels(cdrBean.getNodeId(), cdrBean.getInTrunkGroupId() + "", cdrBean.getOutTrunkGroupId() + "").inc(cdrBean.getDuration());
        }

        // cdrpr metrics
        m_countByCrc.setLabelValues(cdrBean.getNodeId(), toCauseString(cdrBean.getCause()), cdrBean.getInTrunkGroupId() + "", cdrBean.getOutTrunkGroupId() + "").inc();
        if (cdrBean.getCause() == 16)
            m_durationByTG.setLabelValues(cdrBean.getNodeId(), cdrBean.getInTrunkGroupId() + "", cdrBean.getOutTrunkGroupId() + "").inc(cdrBean.getDuration());

    }


    private String toCauseString(int cause) {
        String newCrc = "";
        switch (cause) {
            case 16:
                newCrc = "Answered";
                break;
            case 17:
                newCrc = "Busy";
                break;
            case 19:
                newCrc = "No answer";
                break;
            case 21:
                newCrc = "Rejected";
                break;
            case 38:
                newCrc = "Network out of order";
                break;
            case 6:
                newCrc = "Channel unacceptable";
                break;
            case 3:
                newCrc = "No route to destination";
                break;
            default:
                newCrc = "Other";
        }
        return newCrc;
    }



}
