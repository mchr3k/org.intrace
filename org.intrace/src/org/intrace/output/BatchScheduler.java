package org.intrace.output;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Given an arrival time (millis), calculate departure time (millis).
 * An instance of this object that arrives in a particular time window will depart in the 
 * -- current window, if BatchWindowMultiplier = 1
 * -- subsequent window , if BatchWindowMultiplier = 2
 * where window size is defined by the given IBatchSchedulerConfig.
 * 
 * Designed to work with java.util.concurrent.DelayQueue
 * https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/DelayQueue.html
 * @author erikostermueller
 *
 */
public class BatchScheduler {

	IBatchSchedulerConfig config = null;
	private long arrivalTimeMillis = 0L;
	public BatchScheduler(long val, IBatchSchedulerConfig config) {
		this.arrivalTimeMillis = val;
		this.config = config;
	}


	public long getArrivalTimeMillis() {
		return this.arrivalTimeMillis;
	}

	public long getDepartureTimeMillis() {
		
		long aLittleTooFarIntoFuture = 
				this.getArrivalTimeMillis() + 
				(this.config.getDrainInterval() * this.config.getDrainIntervalMultiplier() );
		
		long offset = aLittleTooFarIntoFuture % this.config.getDrainInterval();
		return aLittleTooFarIntoFuture - offset;
	}

}
