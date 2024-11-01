package si.matjazcerkvenik.datasims.cdrpr.simulator;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

public class SimulatorMetrics {

	public static final Gauge defaultBulkSize = Gauge.build().name("cdrpr_default_bulk_size")
			.help("Default bulk size.").register();
	public static final Gauge bulkSize = Gauge.build().name("cdrpr_bulk_size")
			.help("Bulk size.").register();
	public static final Gauge bulkCount = Gauge.build().name("cdrpr_bulk_count")
			.help("Bulk count.").register();
	public static final Gauge sendInterval = Gauge.build().name("cdrpr_bulk_send_interval")
			.help("Bulk send interval.").register();
	public static final Counter totalCdrGenerated = Counter.build().name("cdrpr_cdrs_generated_total")
			.labelNames("threadId").help("Number of generated cdrs.").register();
	public static final Gauge queueSize = Gauge.build().name("cdrpr_queue_size")
			.help("Number of cdrs in queue.").register();
	public static final Gauge maxQueueSize = Gauge.build().name("cdrpr_max_queue_size")
			.help("Max number of cdrs in queue.").register();
	public static final Gauge callsInProgress = Gauge.build().name("cdrpr_calls_in_progress")
			.help("Number of calls in progress.").register();
	public static final Counter callsInProgressRemoved = Counter.build().name("cdrpr_calls_in_progress_removed")
			.help("Number of calls removed from progress (call ended).").register();


}