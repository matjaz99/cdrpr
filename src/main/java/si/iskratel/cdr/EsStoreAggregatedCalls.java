package si.iskratel.cdr;

import io.prometheus.client.Gauge;
import okhttp3.*;
import si.iskratel.cdr.parser.CdrBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EsStoreAggregatedCalls implements IPersistenceClient {

    private boolean running = true;
    private int threadId = 0;
    private int waitInterval = 60 * 1000;
    private long aggregationTimestamp;

    private List<CdrBean> tempList = new ArrayList<>();

    private OkHttpClient httpClient = new OkHttpClient();
    private MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    public static final Gauge countByCrc = Gauge.build().name("cdrpr_agg_byCrc_total")
            .labelNames("nodeId", "releaseCause", "incTG", "outTG").help("Number of calls by crc").register();
    public static final Gauge countByDuration = Gauge.build().name("cdrpr_agg_byDuration_total")
            .labelNames("nodeId", "incTG", "outTG").help("Total duration").register();

    public static CdrMetric m_countByCrc = CdrMetric.build().setName("m_countByCrc")
            .setLabelNames("nodeId", "cause", "incTG", "outTG").register();

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

            //CdrMetricRegistry.dumpAllMetrics();
            sendBulkPost(m_countByCrc.toJsonString());

            tempList.clear();

        }

    }

    private void aggregate(CdrBean cdrBean) {

        int crc = cdrBean.getCause();
        String newCrc = "";
        switch (crc) {
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

        // prometheus metrics
        countByCrc.labels(Start.SIMULATOR_NODEID, newCrc, cdrBean.getInTrunkGroupId() + "", cdrBean.getOutTrunkGroupId() + "").inc();
        if (crc == 16) {
            countByDuration.labels(Start.SIMULATOR_NODEID, cdrBean.getInTrunkGroupId() + "", cdrBean.getOutTrunkGroupId() + "").inc(cdrBean.getDuration());
        }

        // cdrpr metrics
        m_countByCrc.setLabelValues(Start.SIMULATOR_NODEID, newCrc, cdrBean.getInTrunkGroupId() + "", cdrBean.getOutTrunkGroupId() + "").inc();

    }

    public void sendBulkPost(String body) {

        System.out.println("sending metrics: " + m_countByCrc.getTimeSeriesSize());
        PrometheusMetrics.bulkCount.set(m_countByCrc.getTimeSeriesSize());

        Request request = new Request.Builder()
                .url(Start.ES_URL)
                .addHeader("User-Agent", "OkHttp Bot")
//                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                .build();

        executeHttpRequest(request);

    }

    private void executeHttpRequest(Request request) {
        try {

            Response response = httpClient.newCall(request).execute();
            while (!response.isSuccessful()) {
                Thread.sleep(1500);
                response = httpClient.newCall(request).execute();
                PrometheusMetrics.elasticPostsResent.labels(threadId + "").inc();
            }
            PrometheusMetrics.elasticPostsSent.labels(threadId + "").inc();

            if (!response.isSuccessful()) System.out.println("EsStoreAggregatedCalls[" + threadId + "]: Unexpected code: " + response);

            response.close();

//        System.out.println(response.body().string());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EsStoreAggregatedCalls[" + threadId + "]: Recursive call.");
            PrometheusMetrics.elasticPostsResent.labels(threadId + "").inc();
            executeHttpRequest(request);
        }
    }

}
