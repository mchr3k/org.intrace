package org.intracetest.agent;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Delayed;

import org.intrace.output.BatchScheduler;
import org.intrace.output.IBatchSchedulerConfig;
import org.junit.Test;

public class DelayQueueBatchTest {
	
	@Test 
	public void canCalculateDepartureTimes_currentInterval() {
		
		final int INTERVAL = 250;			// I  N  T  E  R  V  A  L =======>  Z  E  R  O      t o    2  4  9
		final int DEPARTURE_TIME_MILLIS = INTERVAL;
		IBatchSchedulerConfig config1 = new IBatchSchedulerConfig() {
			@Override
			public int getDrainInterval() {return INTERVAL;}
			@Override
			public int getDrainIntervalMultiplier() { return 1; }
		};
		
		//First milli of the interval
		BatchScheduler batchScheduler = new BatchScheduler(0L, config1);
		assertEquals("Departure time was not calculated correctcly", DEPARTURE_TIME_MILLIS, batchScheduler.getDepartureTimeMillis() );
		
		//Last milli of the interval
		batchScheduler = new BatchScheduler( (INTERVAL-1), config1);
		assertEquals("Departure time was not calculated correctcly", DEPARTURE_TIME_MILLIS, batchScheduler.getDepartureTimeMillis() );
		
		//First milli of subsequent interval
		batchScheduler = new BatchScheduler( INTERVAL, config1);
		assertEquals("Departure time was not calculated correctcly", 2*DEPARTURE_TIME_MILLIS, batchScheduler.getDepartureTimeMillis() );

	}
	@Test 
	public void canCalculateDepartureTimes_subsequentInterval() {
		
		final int INTERVAL = 250;			// I  N  T  E  R  V  A  L =======>  Z  E  R  O      t o    2  4  9
		final int DEPARTURE = INTERVAL;
		final int MULTIPLIER = 2;
		IBatchSchedulerConfig config1 = new IBatchSchedulerConfig() {
			@Override
			public int getDrainInterval() {return INTERVAL;}
			@Override
			public int getDrainIntervalMultiplier() { return MULTIPLIER; }
		};
		
		BatchScheduler batchScheduler = new BatchScheduler(0L, config1);
		assertEquals("Departure time was not calculated correctcly", MULTIPLIER*DEPARTURE, batchScheduler.getDepartureTimeMillis() );
		
		batchScheduler = new BatchScheduler( (INTERVAL-1), config1);
		assertEquals("Departure time was not calculated correctcly", MULTIPLIER*DEPARTURE, batchScheduler.getDepartureTimeMillis() );
		
		batchScheduler = new BatchScheduler( INTERVAL, config1);
		assertEquals("Departure time was not calculated correctcly", MULTIPLIER*DEPARTURE+INTERVAL, batchScheduler.getDepartureTimeMillis() );

	}
	
	IBatchSchedulerConfig config2 = new IBatchSchedulerConfig() {
		@Override
		public int getDrainInterval() {return 1000;}
		@Override
		public int getDrainIntervalMultiplier() { return 2; }
	};
	/**
	 * First num goes in one batch, 2nd two numbers go in the subsequent batch.
	 */
	@Test 
	public void canCalculateSubsequentBatchedDepartureTimes() {
		BatchScheduler batchScheduler = new BatchScheduler(2352819999L, this.config2);
		assertEquals("Departure time was not calculated correctcly", 2352821000L, batchScheduler.getDepartureTimeMillis() );
		batchScheduler = new BatchScheduler(2352820000L, this.config2);
		assertEquals("Departure time was not calculated correctcly", 2352822000L, batchScheduler.getDepartureTimeMillis() );
		batchScheduler = new BatchScheduler(2352820001L, this.config2);
		assertEquals("Departure time was not calculated correctcly", 2352822000L, batchScheduler.getDepartureTimeMillis() );

	}
	
	@Test 
	public void canCalculateBatchedDepartureTimes() {
		BatchScheduler batchScheduler = new BatchScheduler(899079228, this.config2);
		assertEquals("Departure time was not calculated correctcly", 899081000L, batchScheduler.getDepartureTimeMillis() );
		batchScheduler = new BatchScheduler(899079473L, this.config2);
		assertEquals("Departure time was not calculated correctcly", 899081000L, batchScheduler.getDepartureTimeMillis() );
		batchScheduler = new BatchScheduler(2293479377L, this.config2);
		assertEquals("Departure time was not calculated correctcly", 2293481000L, batchScheduler.getDepartureTimeMillis() );
		batchScheduler = new BatchScheduler(902521357L, this.config2);
		assertEquals("Departure time was not calculated correctcly", 902523000L, batchScheduler.getDepartureTimeMillis() );
		batchScheduler = new BatchScheduler(902756592L, this.config2);
		assertEquals("Departure time was not calculated correctcly", 902758000L, batchScheduler.getDepartureTimeMillis() );
		batchScheduler = new BatchScheduler(2294063215L, this.config2);
		assertEquals("Departure time was not calculated correctcly", 2294065000L, batchScheduler.getDepartureTimeMillis() );
		
	}
	/**
	 * First num goes in one batch, 2nd two numbers go in the subsequent batch.
	 * 



	 */
	@Test 
	public void canCalculateSubsequentBatchedDepartureTimes_smallerInterval() {
		IBatchSchedulerConfig config1 = new IBatchSchedulerConfig() {
			@Override
			public int getDrainInterval() {return 250;}
			@Override
			public int getDrainIntervalMultiplier() { return 2; }
		};
		
		BatchScheduler batchScheduler = new BatchScheduler(2352819999L, config1);
		assertEquals("Departure time was not calculated correctcly", 2352820250L, batchScheduler.getDepartureTimeMillis() );
		batchScheduler = new BatchScheduler(2352820000L, config1);
		assertEquals("Departure time was not calculated correctcly", 2352820500L, batchScheduler.getDepartureTimeMillis() );
		batchScheduler = new BatchScheduler(2352820001L, config1);
		assertEquals("Departure time was not calculated correctcly", 2352820500L, batchScheduler.getDepartureTimeMillis() );

	}

}
