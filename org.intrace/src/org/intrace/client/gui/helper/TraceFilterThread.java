package org.intrace.client.gui.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Threaded owner of the trace lines. Allows a Pattern to be used to filter
 * trace text.
 */
public class TraceFilterThread implements Runnable
{
  /**
   * Trace text output callback
   */
  public static interface TraceTextHandler
  {
    void setText(String traceText);

    void appendText(String traceText);
    
    public void setStatus(final int displayed, final int total);
  }

  /**
   * Filter progress monitor callback
   */
  public static interface TraceFilterProgressHandler
  {
    boolean setProgress(int percent);

    Pattern getIncludePattern();

    Pattern getExcludePattern();
  }

  /**
   * System trace is never filtered out
   */
  private static final String SYSTEM_TRACE_PREFIX = "***";

  /**
   * Low memory warning
   */
  private static final String LOW_MEMORY = "Warning: Low memory - no further trace will be collected.";

  /**
   * Pattern which matches anything
   */
  public static final Pattern MATCH_ALL = Pattern.compile(".*", Pattern.DOTALL);

  /**
   * Pattern which matches nothing
   */
  public static final Pattern MATCH_NONE = Pattern
                                                  .compile("^$", Pattern.DOTALL);

  /**
   * Queue of filters to apply - this should usually only contain zero or one
   * entry
   */
  private final Queue<TraceFilterProgressHandler> traceFilters = new ConcurrentLinkedQueue<TraceFilterProgressHandler>();

  /**
   * Queue of new unprocessed trace lines
   */
  private final BlockingQueue<String> newTraceLines = new LinkedBlockingQueue<String>();

  /**
   * Semaphore controlling how many trace line can be added
   */
  private final Semaphore traceLineSemaphore = new Semaphore(30);

  /**
   * List of trace lines saved by this thread from the newTraceLines
   */
  private final List<String> traceLines = new ArrayList<String>();
  
  /**
   * Number of displayed lines
   */  
  private final AtomicInteger displayedLines = new AtomicInteger();
  
  /**
   * Total number of lines
   */
  private final AtomicInteger totalLines = new AtomicInteger();

  /**
   * Reference to this Thread
   */
  private final Thread thisThread;

  /**
   * Text output callback
   */
  private final TraceTextHandler callback;

  /**
   * Flag to signal that trace should be cleared
   */
  private boolean clearTrace = false;

  /**
   * cTor
   * 
   * @param callback
   * @param progressCallback
   */
  public TraceFilterThread(TraceTextHandler callback)
  {
    this.callback = callback;

    thisThread = new Thread(this);
    thisThread.setDaemon(true);
    thisThread.setName("Trace Filter");
    thisThread.start();
  }

  public void addTraceLine(String traceLine)
  {
    try
    {
      traceLineSemaphore.acquire();
      newTraceLines.add(traceLine + "\r\n");
    }
    catch (InterruptedException e)
    {
      // Restore interrupted flag
      Thread.interrupted();
    }
    // We don't release the semaphore here - the trace appender 
    // thread does so once it is happy to allow more trace lines 
    // in
  }

  public void addSystemTraceLine(String traceLine)
  {
    newTraceLines.add(SYSTEM_TRACE_PREFIX + " " + traceLine + "\r\n");
  }

  public void applyFilter(TraceFilterProgressHandler progressCallback)
  {
    traceFilters.add(progressCallback);
    thisThread.interrupt();
  }

  public synchronized void setClearTrace()
  {
    clearTrace = true;
    thisThread.interrupt();
  }

  private synchronized boolean getClearTrace()
  {
    boolean retClearTrace = clearTrace;
    clearTrace = false;
    return retClearTrace;
  }

  @Override
  public void run()
  {
    long maxMemory = Runtime.getRuntime().maxMemory();
    long byteMemLimit = (long) Math.max(maxMemory - (10 * 1000 * 1000), // Maxmemory
                                    // - 10mb
                                    0.9 * maxMemory); // 90% of Maxmemory
    long charMemLimit = byteMemLimit / 2; // UTF-16 - 2 bytes per char 
    long numChars = 0;
    boolean doClearTrace = false;
    boolean lowMemorySignalled = false;
    TraceFilterProgressHandler patternProgress = null;
    Pattern activeIncludePattern = MATCH_ALL;
    Pattern activeExcludePattern = MATCH_NONE;
    try
    {
      while (true)
      {
        try
        {
          if (patternProgress != null)
          {
            activeIncludePattern = patternProgress.getIncludePattern();
            activeExcludePattern = patternProgress.getExcludePattern();
            applyPattern(patternProgress);
            patternProgress = null;
          }

          if (doClearTrace)
          {
            doClearTrace = false;
            traceLines.clear();
            callback.setText("");
            displayedLines.set(0);
            totalLines.set(0);
            callback.setStatus(displayedLines.get(), totalLines.get());
            numChars = 0;
          }

          String newTraceLine = newTraceLines.take();          

          if (!newTraceLine.startsWith(SYSTEM_TRACE_PREFIX))
          {
            traceLineSemaphore.release();
          }

          if (numChars < charMemLimit)
          {
            lowMemorySignalled = false;

            // I expected a factor of 2 due to trace strings being held by this
            // thread along with another copy held by the UI. However, profiling
            // shows a factor of 18 is necessary. This is because we need to be able
            // to handle entire copies of the active data when adding new strings.
            numChars += (18 * newTraceLine.length());

            traceLines.add(newTraceLine);
            totalLines.incrementAndGet();
            callback.setStatus(displayedLines.get(), totalLines.get());
            if (newTraceLine.startsWith(SYSTEM_TRACE_PREFIX)
                || (!activeExcludePattern.matcher(newTraceLine).matches() && activeIncludePattern
                    .matcher(newTraceLine).matches()))
            {
              displayedLines.incrementAndGet();
              callback.appendText(newTraceLine);
              callback.setStatus(displayedLines.get(), totalLines.get());
            }
          }
          else if (!lowMemorySignalled)
          {
            lowMemorySignalled = true;
            String memWarning = SYSTEM_TRACE_PREFIX + " " + LOW_MEMORY;            
            traceLines.add(memWarning);
            totalLines.incrementAndGet();
            callback.appendText(memWarning);
            callback.setStatus(displayedLines.get(), totalLines.get());
          }
        }
        catch (InterruptedException ex)
        {
          doClearTrace = getClearTrace();
          patternProgress = traceFilters.poll();
          if ((patternProgress == null) && !doClearTrace)
          {
            // Time to quit
            break;
          }
        }
      }
    }
    catch (Throwable ex)
    {
      ex.printStackTrace();
    }
    System.out.println("Filter thread quitting");
  }

  private void applyPattern(TraceFilterProgressHandler progressCallback)
  {
    Pattern includePattern = progressCallback.getIncludePattern();
    Pattern excludePattern = progressCallback.getExcludePattern();
    int numLines = traceLines.size();
    int handledLines = 0;
    double lastPercentage = 0;
    StringBuilder traceText = new StringBuilder();
    boolean cancelled = progressCallback.setProgress(0);
    boolean firstUpdate = true;
    displayedLines.set(0);
    callback.setStatus(displayedLines.get(), totalLines.get());
    if (!cancelled)
    {
      for (String traceLine : traceLines)
      {
        if (traceLine.startsWith(SYSTEM_TRACE_PREFIX)
            || (!excludePattern.matcher(traceLine).matches() && includePattern
                                                                              .matcher(
                                                                                       traceLine)
                                                                              .matches()))
        {
          displayedLines.incrementAndGet();
          traceText.append(traceLine);
        }
        handledLines++;

        if ((handledLines % 10000) == 0)
        {
          double unroundedPercentage = ((double) handledLines)
                                       / ((double) numLines);
          double roundedPercantage = roundToSignificantFigures(
                                                               unroundedPercentage,
                                                               2);
          if (lastPercentage != roundedPercantage)
          {
            // Try and ensure that we are GC-ing regularly
            System.gc();
            cancelled = progressCallback
                                        .setProgress((int) (100 * roundedPercantage));
            if (cancelled)
            {
              break;
            }
            else
            {
              if (firstUpdate)
              {
                callback.setText(traceText.toString());
                callback.setStatus(displayedLines.get(), totalLines.get());
                traceText = new StringBuilder();
                firstUpdate = false;
              }
              else
              {                
                callback.appendText(traceText.toString());
                callback.setStatus(displayedLines.get(), totalLines.get());
              }
            }
          }
          lastPercentage = roundedPercantage;
        }
      }
    }
    progressCallback.setProgress(100);
    if (!cancelled)
    {
      if (firstUpdate)
      {
        callback.setText(traceText.toString());
        callback.setStatus(displayedLines.get(), totalLines.get());
      }
      else
      {
        callback.appendText(traceText.toString());
        callback.setStatus(displayedLines.get(), totalLines.get());
      }
    }
  }

  private static double roundToSignificantFigures(double num, int n)
  {
    if (num == 0)
    {
      return 0;
    }

    final double d = Math.ceil(Math.log10(num < 0 ? -num
                                                 : num));
    final int power = n - (int) d;

    final double magnitude = Math.pow(10, power);
    final long shifted = Math.round(num * magnitude);
    return shifted / magnitude;
  }
}