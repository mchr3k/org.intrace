package org.intrace.output;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
//import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

import org.intrace.agent.server.AgentClientConnection;
import org.intrace.shared.SerializationHelper;

public class NetworkDataSenderThread extends InstruRunnable {
	private long traceEventSequence = 0;
	private static final int EST_EVENT_COUNT_PER_BATCH = 16 * 1024;
	private static final int BURST_SIZE = 16 * 1024;
	private static final int DRAIN_INTERVAL_MS = 1000;
	/**
	 * All BatchQueueEle instances will point to this singleton, to avoid
	 * unnecessary GC overhead. I made this public so that in the future, the
	 * parameters could be changed on the fly.
	 */
	public static IBatchSchedulerConfig batchSchedulerConfig = new IBatchSchedulerConfig() {
		@Override
		public int getDrainInterval() {
			return DRAIN_INTERVAL_MS;
		}

		@Override
		public int getDrainIntervalMultiplier() {
			return 1;
		}
	};

	/**
	 * An internal wrapper class for an individual trace event. Enables use of
	 * java.util.concurrent.DelayQueue to schedule multiples of these to be
	 * shipped to the client in a single batch, to avoid previous chatty and
	 * poorly performing behavior of a single socket write per trace event.
	 * 
	 * Because of the excess GC required, I'm not thrilled about needing this
	 * wrapper instance for every trace event. However, the DelayQueue solution
	 * is otherwise pretty elegant.
	 * 
	 * @author erikostermueller
	 *
	 */
	public static class TraceEventForBatch implements Delayed {
		private Object traceEventText = null;
		private long arrivalTimeMillis = 0L;
		private long traceEventSequence = 0L;
		private BatchScheduler batchScheduler = null;

		public TraceEventForBatch(Object val, long arrivalTimeMillis,
				long traceEventSequence) {
			this.traceEventText = val;
			this.batchScheduler = new BatchScheduler(arrivalTimeMillis,
					batchSchedulerConfig);
			this.traceEventSequence = traceEventSequence;
		}

		public Object getTraceEvent() {
			return this.traceEventText;
		}

		@Override
		public int compareTo(Delayed o) {
			long rc = this.getDelay(TimeUnit.MILLISECONDS)
					- o.getDelay(TimeUnit.MILLISECONDS);
			return (int) rc;
		}

		public long getTraceEventSequence() {
			return this.traceEventSequence;
		}

		/**
		 * "Expiration occurs when an element's getDelay(TimeUnit.NANOSECONDS) method returns a value less than or equal to zero."
		 * ....taken from here:
		 * https://docs.oracle.com/javase/7/docs/api/java/util
		 * /concurrent/DelayQueue.html
		 */
		@Override
		public long getDelay(TimeUnit unit) {
			long delay = this.batchScheduler.getDepartureTimeMillis()
					- System.currentTimeMillis();
			return unit.convert(delay, TimeUnit.MILLISECONDS);
		}

	}

	private boolean alive = true;
	private final ServerSocket networkSocket;
	private Socket traceSendingSocket = null;
	// private final BlockingQueue<Object> outgoingData = new
	// LinkedTransferQueue<Object>();
	private final BlockingQueue<TraceEventForBatch> outgoingData = new DelayQueue<TraceEventForBatch>();
	private Map<NetworkDataSenderThread, Object> set = new HashMap<NetworkDataSenderThread, Object>();
	private final AgentClientConnection connection;

	public NetworkDataSenderThread(AgentClientConnection connection,
			ServerSocket networkSocket) {
		this.connection = connection;
		this.networkSocket = networkSocket;
	}

	public void start(Map<NetworkDataSenderThread, Object> set) {
		this.set = set;

		Thread networkThread = new Thread(this);
		networkThread.setDaemon(true);
		networkThread.setName(Thread.currentThread().getName()
				+ " - Network Data Sender");
		networkThread.start();
	}

	private void stop() {
		try {
			if (connection != null) {
				connection.setTraceConnEstablished(false);
			}
			alive = false;
			networkSocket.close();
			if (traceSendingSocket != null) {
				traceSendingSocket.close();
			}
		} catch (IOException e) {
			// Throw away
		}
		set.remove(this);
		// System.out.println("## Trace Connection Disconnected");
	}

	public void queueData(Object data) {
		TraceEventForBatch dataWrapper = new TraceEventForBatch(data,
				System.currentTimeMillis(), traceEventSequence++);
		if (alive)
			outgoingData.offer(dataWrapper);
	}

	public void runMethod_OLD() {
		final int HEARTBEAT_TIME_SECONDS = 5;
		try {
			traceSendingSocket = networkSocket.accept();
			// System.out.println("## Trace Connection Established");
			traceSendingSocket.setKeepAlive(true);

			if (connection != null) {
				connection.setTraceConnEstablished(true);
			}

			ObjectOutputStream traceWriter = new ObjectOutputStream(
					traceSendingSocket.getOutputStream());
			// Ready to handle data
			set.put(this, new Object());

			List<String> traceEventsForSingleBatch = new ArrayList<String>(
					EST_EVENT_COUNT_PER_BATCH);
			List<TraceEventForBatch> tmp = new ArrayList<TraceEventForBatch>(
					EST_EVENT_COUNT_PER_BATCH);
			while (true) {
				TraceEventForBatch traceLineWrapper = outgoingData.poll(
						HEARTBEAT_TIME_SECONDS, TimeUnit.SECONDS);
				if (traceLineWrapper != null) {
					if (traceLineWrapper.getTraceEvent() instanceof String) {
						traceEventsForSingleBatch.clear();
						tmp.clear();
						traceEventsForSingleBatch.add((String) traceLineWrapper
								.getTraceEvent());
						outgoingData.drainTo(tmp, BURST_SIZE);
						Collections.sort(tmp,
								new Comparator<TraceEventForBatch>() { // It all
																		// ends
																		// badly
																		// without
																		// this
																		// sort.
									@Override
									public int compare(TraceEventForBatch o1,
											TraceEventForBatch o2) {
										return (int) (o1
												.getTraceEventSequence() - o2
												.getTraceEventSequence());
									}
								});
						/**
						 * This copy from-TraceEventForBatch-to-string-array is
						 * required to avoid 2 things: -- wire format where each
						 * trace event has the extra overhead/space of one long
						 * timestamp (the one used for DelayQueue.getDelay() ).
						 * -- Requiring TraceEventForBatch .class to be in
						 * client JVM. This will avoid versioning conflicts.
						 */
						for (TraceEventForBatch tefb : tmp)
							traceEventsForSingleBatch.add((String) tefb
									.getTraceEvent());

						String[] eventsForOneBatch = new String[traceEventsForSingleBatch
								.size()];
						traceEventsForSingleBatch.toArray(eventsForOneBatch);
						byte[] wireData = SerializationHelper
								.toWire(eventsForOneBatch);
						traceWriter.writeObject(wireData);
					}
				} else {
					traceWriter.writeObject("NOOP"); // If no events in the last
														// HEARTBEAT_TIME_SECONDS,
														// then send a NOOP to
														// keep the tcp
														// connection alive.
				}
				traceWriter.flush();
				traceWriter.reset();
			}
		} catch (InterruptedException ex) {
			stop();
		} catch (IOException ex) {
			stop();
		}
	}

	public void runMethod() {
		final int HEARTBEAT_TIME_SECONDS = 5;
		try {
			traceSendingSocket = networkSocket.accept();
			// System.out.println("## Trace Connection Established");
			traceSendingSocket.setKeepAlive(true);

			if (connection != null) {
				connection.setTraceConnEstablished(true);
			}

			ObjectOutputStream traceWriter = new ObjectOutputStream(
					traceSendingSocket.getOutputStream());
			// Ready to handle data
			set.put(this, new Object());

			while (true) {
				TraceEventForBatch traceLineWrapper = outgoingData.poll(
						HEARTBEAT_TIME_SECONDS, TimeUnit.SECONDS);
				if (traceLineWrapper != null) {
					if (traceLineWrapper.getTraceEvent() instanceof String) {
						List<TraceEventForBatch> typedAndSortedList = new ArrayList<TraceEventForBatch>(
								EST_EVENT_COUNT_PER_BATCH);
						typedAndSortedList.add(traceLineWrapper);
						while (outgoingData.drainTo(typedAndSortedList, BURST_SIZE) > 0)
							;
						// It all ends badly without this sort
						Collections.sort(typedAndSortedList,
								new Comparator<TraceEventForBatch>() { 
									@Override
									public int compare(TraceEventForBatch o1,
											TraceEventForBatch o2) {
										return (int) (o1
												.getTraceEventSequence() - o2
												.getTraceEventSequence());
									}
								});
						transmitBatch(traceWriter, typedAndSortedList, BURST_SIZE);
					} else {
						throw new RuntimeException(
								"Found unsupported trace event of type ["
										+ traceLineWrapper.getTraceEvent()
												.getClass().getName() + "]");
					}
				} else {
					traceWriter.writeObject("NOOP"); // If no events in the last
														// HEARTBEAT_TIME_SECONDS,
														// then send a NOOP to
														// keep the tcp
														// connection alive.
				}
				traceWriter.flush();
				traceWriter.reset();
			}
		} catch (InterruptedException ex) {
			stop();
		} catch (IOException ex) {
			stop();
		}
	}

	/**
	 * Using the give ObjectOutputStream, transmit the given list of events
	 * in one or more successive bursts of no more than burstSize events.
	 * 
	 * This copy from-TraceEventForBatch-to-string-array is required to
	 * avoid 2 things: 
	 * -- Wire format where each trace event has the extra
	 * 		overhead/space of one long timestamp (the one used for
	 * 		DelayQueue.getDelay() ). 
	 * -- More complicated wire format that will
	 * 		diminish the effectiveness of compression 
	 * -- Requiring TraceEventForBatch .class to be in client JVM. 
	 * 		This will avoid versioning conflicts.
	 * @param traceWriter
	 * @param sortedAndTypedEventList
	 * @throws IOException
	 */
	public int transmitBatch(ObjectOutputStream traceWriter,
			List<TraceEventForBatch> sortedAndTypedEventList, int burstSize)
			throws IOException {
		List<String> eventsForSingleBurst = new ArrayList<String>(
				burstSize);

		int remaining = sortedAndTypedEventList.size();
		int burstCount = 0;
		for (TraceEventForBatch tefb : sortedAndTypedEventList) {
			eventsForSingleBurst.add( (String) tefb.getTraceEvent() );
			remaining--;

			if (eventsForSingleBurst.size() % burstSize == 0 
					|| remaining ==0 ) {
				String[] aryEventsForSingleBurst = new String[eventsForSingleBurst.size()];
				eventsForSingleBurst.toArray(aryEventsForSingleBurst);
				byte[] wireData = SerializationHelper.toWire(aryEventsForSingleBurst);
				traceWriter.writeObject(wireData);
				burstCount++;
				eventsForSingleBurst.clear();
			}
		}
		System.out.println("stranded items [" + remaining + "]");		
		return burstCount;
	}

	public void gracefulShutdown() {
		while (!outgoingData.isEmpty()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
	}
}
