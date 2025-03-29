package si.matjazcerkvenik.datasims.cdrpr.simulator;

import si.matjazcerkvenik.metricslib.EsClient;
import si.matjazcerkvenik.metricslib.PMetric;
import si.matjazcerkvenik.metricslib.PMetricRegistry;

import java.util.Random;

/**
 * This class is intended to generate random data in specified intervals in period from-to.
 * When it finishes, it stops.
 */
public class SeqRandomDataToEs {

    private EsClient esClient;

    public void generateData() {

        esClient = new EsClient(Props.ES_SCHEMA, Props.ES_HOST, Props.ES_PORT);

        String[] nodeArray = Props.SIMULATOR_NODEID.split(",");
        for (int i = 0; i < nodeArray.length; i++) {

            String node_calls_idx = "pmon_seqdata_node_calls_" + nodeArray[i].trim().toLowerCase() + "_idx";
            String node_durations_idx = "pmon_seqdata_node_durations_" + nodeArray[i].trim().toLowerCase() + "_idx";
            String tg_calls_idx = "pmon_seqdata_tg_calls_" + nodeArray[i].trim().toLowerCase() + "_idx";
            String tg_durations_idx = "pmon_seqdata_tg_durations_" + nodeArray[i].trim().toLowerCase() + "_idx";

            PMetric pmon_cdr_calls_by_cause = PMetric.build()
                    .setName("pmon_cdr_calls_by_cause")
                    .setHelp("Count calls by release cause")
                    .setLabelNames("nodeName", "cause", "trafficType")
                    .register(node_calls_idx);

            PMetric pmon_cdr_call_duration = PMetric.build()
                    .setName("pmon_cdr_call_duration")
                    .setHelp("Total duration of answered calls on node")
                    .setLabelNames("nodeName")
                    .register(node_durations_idx);

            PMetric pmon_cdr_calls_by_trunkgroup = PMetric.build()
                    .setName("pmon_cdr_calls_by_trunkgroup")
                    .setHelp("pmon_cdr_trunk_calls")
                    .setLabelNames("nodeName", "cause", "incTG", "outTG")
                    .register(tg_calls_idx);

            PMetric pmon_cdr_duration_by_trunkgroup = PMetric.build()
                    .setName("pmon_cdr_duration_by_trunkgroup")
                    .setHelp("pmon_cdr_trunk_calls_duration")
                    .setLabelNames("nodeName", "incTG", "outTG")
                    .register(tg_durations_idx);

            int answered = 1000;
            int busy = 100;
            int noReply = 80;
            int other = 10;
            int duration = 5000000;
            Random r = new Random();

            for (long time = Props.SIMULATOR_START_TIME_SECONDS; time < Props.SIMULATOR_END_TIME_SECONDS; time = time + Props.SIMULATOR_SAMPLING_INTERVAL_SECONDS) {

                // simulate values

                answered = Math.abs(answered + (int)(r.nextGaussian() * 2));
                busy = Math.abs(busy + (int)(r.nextGaussian() * 2));
                noReply = Math.abs(noReply + (int)(r.nextGaussian() * 2));
                other = Math.abs(answered + (int)(r.nextGaussian() * 2));

                duration = Math.abs(duration + (int)(r.nextGaussian() * 10));


                // fill metrics

                pmon_cdr_calls_by_cause.setLabelValues(nodeArray[i], "Answered", "trafficType").set(answered);
                pmon_cdr_calls_by_cause.setLabelValues(nodeArray[i], "Busy", "trafficType").set(busy);
                pmon_cdr_calls_by_cause.setLabelValues(nodeArray[i], "No reply", "trafficType").set(noReply);
                pmon_cdr_calls_by_cause.setLabelValues(nodeArray[i], "Other", "trafficType").set(other);

                pmon_cdr_call_duration.setLabelValues(nodeArray[i]).set(duration);

                for (int j = 1000; j < 1020; j++) {
                    for (int k = 4000; k < 4020; k++) {
                        pmon_cdr_calls_by_trunkgroup.setLabelValues(nodeArray[i], "Answered", "TG-" + j, "TG-" + k).set((int)answered * 1.0 / 400);
                        pmon_cdr_calls_by_trunkgroup.setLabelValues(nodeArray[i], "Busy", "TG-" + j, "TG-" + k).set((int)busy * 1.0 / 400);
                        pmon_cdr_calls_by_trunkgroup.setLabelValues(nodeArray[i], "No reply", "TG-" + j, "TG-" + k).set((int)noReply * 1.0 / 400);
                        pmon_cdr_calls_by_trunkgroup.setLabelValues(nodeArray[i], "Other", "TG-" + j, "TG-" + k).set((int)other * 1.0 / 400);
                        pmon_cdr_duration_by_trunkgroup.setLabelValues(nodeArray[i], "TG-" + j, "TG-" + k).set((int)duration * 1.0 / 400);
                    }

                }


                // set timestamp and store to DB

                pmon_cdr_calls_by_cause.setTimestamp(time * 1000);
                pmon_cdr_call_duration.setTimestamp(time * 1000);
                pmon_cdr_calls_by_trunkgroup.setTimestamp(time * 1000);
                pmon_cdr_duration_by_trunkgroup.setTimestamp(time * 1000);

                esClient.sendBulkPost(PMetricRegistry.getRegistry(node_calls_idx));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(node_durations_idx));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(tg_calls_idx));
                esClient.sendBulkPost(PMetricRegistry.getRegistry(tg_durations_idx));

//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }

                PMetricRegistry.getRegistry(node_calls_idx).resetMetrics();
                PMetricRegistry.getRegistry(node_durations_idx).resetMetrics();
                PMetricRegistry.getRegistry(tg_calls_idx).resetMetrics();
                PMetricRegistry.getRegistry(tg_durations_idx).resetMetrics();

            }
        }

    }

}
