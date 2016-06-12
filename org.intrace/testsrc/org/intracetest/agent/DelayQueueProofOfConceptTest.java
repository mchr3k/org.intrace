package org.intracetest.agent;


import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Delayed;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicLong;



import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

import org.intrace.output.trace.TraceHandler;
import static org.junit.Assert.assertEquals;

/** This is a 'proof of concept' test that demonstrates how to use a DelayQueue to emit
  * data in batches, instead of individual queue items.  Only jdk code and test code is exercised here.
  *
  * This test sticks 9 items into a DelayQueue.  These items comes out in 3 batches.
  * The delays (in milliseconds) between the batches are defined in the offsetA, offsetB, and offsetC variables.
  */
public class DelayQueueProofOfConceptTest
{
	public static final int POISON_PILL = 3;
	private static long offsetA = 53L;
	private static long offsetB = 109L;
	private static long offsetC = 150L;
	private static long baseline = 0;
	BlockingQueue<Pizza> pizzaQueue = new DelayQueue<Pizza>();
	PizzaEaterRunnable pizzaEater = new PizzaEaterRunnable(pizzaQueue);
	List<Long> expectedArrivalTimestamps = new ArrayList<Long>();
	@Before
	public void setup() throws java.lang.InterruptedException {
		baseline = System.currentTimeMillis();
		
		/**
		 *    B   A   T   C   H          #   1
		 */
		pizzaQueue.put( createTypeOne(baseline) ); expectedArrivalTimestamps.add( new Long(baseline+offsetA) );
		pizzaQueue.put( createTypeOne(baseline) ); expectedArrivalTimestamps.add( new Long(baseline+offsetA) );
		pizzaQueue.put( createTypeOne(baseline) ); expectedArrivalTimestamps.add( new Long(baseline+offsetA) );
		pizzaQueue.put( createTypeOne(baseline) ); expectedArrivalTimestamps.add( new Long(baseline+offsetA) );
		pizzaQueue.put( createTypeOne(baseline) ); expectedArrivalTimestamps.add( new Long(baseline+offsetA) );
		/**
		 *    B   A   T   C   H          #   2
		 */
		pizzaQueue.put( createTypeTwo(baseline) ); expectedArrivalTimestamps.add( new Long(baseline+offsetB) );
		pizzaQueue.put( createTypeTwo(baseline) ); expectedArrivalTimestamps.add( new Long(baseline+offsetB) );
		pizzaQueue.put( createTypeTwo(baseline) ); expectedArrivalTimestamps.add( new Long(baseline+offsetB) );
		/**
		 *    B   A   T   C   H          #   3
		 */
		
		pizzaQueue.put( createTypeThree(baseline) ); expectedArrivalTimestamps.add( new Long(baseline+offsetC) );
	}
	private Pizza createTypeOne(long baseline) {
		Pizza newPizza = new Pizza( baseline + offsetA, 1 );
		return newPizza;
	}

        private Pizza createTypeTwo(long baseline) {
                Pizza newPizza = new Pizza( baseline + offsetB, 2 );
		return newPizza;
        }
        private Pizza createTypeThree(long baseline) {
                Pizza newPizza = new Pizza( baseline + offsetC, POISON_PILL );
                return newPizza;
        }
	@Test 
	public void testDelayQueueBurst() {
		try {
			PizzaEaterRunnable eater = new PizzaEaterRunnable(pizzaQueue);//queue was filled in setup method
			Thread t = new Thread(eater);
			t.start();
			//Time will elapse as DelayQueue emits 
			//a three bursts of pizzas from the queue, 1 second between each burst.
			//The thread will finish when a 'poison pill' is detected by the thread.
			t.join();
			boolean compare = eater.arrivalTimestamps.equals(expectedArrivalTimestamps);
			String actual = eater.arrivalTimestamps.toString();	
			String expected = expectedArrivalTimestamps.toString();
			assertTrue("did not get expected arrival times.   Expected [" + expected + "] Actual [" + actual + "]",compare);
			
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}	
  	}

	public static class Pizza implements Delayed {
			
		private long departureTimestampMillis = 0L;	
		private int type = 0;
		public Pizza(long val, int type) {
			this.departureTimestampMillis = val;
			this.type = type;
			System.out.println("New Pizza [" + this.toString() + "]");
		}
		public String toString() {
			return "Pizza {departureTimestampMillis=" + this.departureTimestampMillis + ",type=" + this.type + "}";
		}
		public int getType() {
			return this.type;
		}
		@Override
		public long getDelay(TimeUnit unit) {
			long ts = System.currentTimeMillis();
			//System.out.println("current [" + ts + "] " + this.toString()) ;
			return this.departureTimestampMillis - ts;
		}

		@Override
		public int compareTo(Delayed o) {
			long rc =  this.getDelay( TimeUnit.MILLISECONDS ) - o.getDelay( TimeUnit.MILLISECONDS );
			return (int)rc;	
		}
	}
	class PizzaEaterRunnable implements Runnable {
	    private static final int batchSize = 1000000;//Only concerned with huge/horrible events, otherwise monitoring is required to tune batch size.
	    public List<Long> arrivalTimestamps = new ArrayList<Long>();
	    BlockingQueue<Pizza> pizzaQueue;
	    public volatile boolean stopRequested = false; 
	    public PizzaEaterRunnable(BlockingQueue<Pizza> val) {
	        this.pizzaQueue = val;
	    }
	    private void eatMany(List<Pizza> manyPizzas) {
		for(Pizza pizza : manyPizzas) 
			eat(pizza);
	    }
	    private void eat(Pizza pizza) {
		arrivalTimestamps.add( new Long(System.currentTimeMillis()) );
		if (pizza.getType() == DelayQueueProofOfConceptTest.POISON_PILL) {
			this.stopRequested = true;
		}
	    }
	
	    @Override
	    public void run() {
		List<Pizza> manyPizzas = new ArrayList<Pizza>();
	        while (!stopRequested) {
	            try {
	                //System.out.println(Thread.currentThread().getName() + " eating up bread ");
	                Pizza pizza = pizzaQueue.poll(100, TimeUnit.MILLISECONDS); 
			if (pizza !=null) {
				manyPizzas.add(pizza);
				pizzaQueue.drainTo(manyPizzas,batchSize);
				eatMany(manyPizzas);
				manyPizzas.clear();
			}
	                     
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	}	
}

