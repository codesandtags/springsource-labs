package rewards.jms.client;

import java.util.List;

import rewards.Dining;

/**
 * A batch processor that sends dining event notifications via JMS.
 */
public class JmsDiningBatchProcessor implements DiningBatchProcessor {

	// TODO-03: Provide a JmsTemplate field

	public void processBatch(List<Dining> batch) {
		// TODO-04: Send each Dining instance to the queue
	}
}
