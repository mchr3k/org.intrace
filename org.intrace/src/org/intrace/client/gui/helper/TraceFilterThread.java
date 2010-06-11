package org.intrace.client.gui.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
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
  }

  /**
   * Filter progress monitor callback
   */
  public static interface TraceFilterProgressHandler
  {
    boolean setProgress(int percent);
  }

  /**
   * Queue of new unprocessed trace lines
   */
  private final BlockingQueue<String> newTraceLines = new LinkedBlockingQueue<String>();
  
  /**
   * Semaphore controlling how many trace line can be added
   */
  private final Semaphore traceLineSemaphore = new Semaphore(5);

  /**
   * List of trace lines saved by this thread from the newTraceLines
   */
  private final List<String> traceLines = new ArrayList<String>();

  /**
   * Reference to this Thread
   */
  private final Thread thisThread;

  /**
   * Callback for filter application progress
   */
  private TraceFilterProgressHandler progressCallback;

  /**
   * Text output callback
   */
  private final TraceTextHandler callback;

  /**
   * Pattern which matches anything
   */
  public static final Pattern NO_PATTERN = Pattern
                                                  .compile(".*", Pattern.DOTALL);

  /**
   * Flag to signal that trace should be cleared
   */
  private boolean clearTrace = false;

  /**
   * Flag to signal that trace should be cleared
   */
  private Pattern tracePattern = null;

  /**
   * System trace is never filtered out
   */
  private static final String SYSTEM_TRACE_PREFIX = "***";

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
    } catch (InterruptedException e)
    {
      // Throw away
    }
    // We don't release the semaphore - the thread does so
    // once it is happy to allow more trace lines in
  }
  
  public void addSystemTraceLine(String traceLine)
  {
    newTraceLines.add(SYSTEM_TRACE_PREFIX + " " + traceLine + "\r\n");
  }

  public void applyFilter(Pattern filter,
                          TraceFilterProgressHandler progressCallback)
  {
    this.progressCallback = progressCallback;
    setTracePattern(filter);
    thisThread.interrupt();
  }
 
  private synchronized void setTracePattern(Pattern filter)
  {
    tracePattern = filter;
  }
  
  private synchronized Pattern getTracePattern()
  {
    return tracePattern;
  }
  
  public void clearTrace()
  {
    setClearTrace();
    thisThread.interrupt();
  }
  
  private synchronized void setClearTrace()
  {
    clearTrace = true;
  }
  
  private synchronized boolean getClearTrace()
  {
    return clearTrace;
  }

  @Override
  public void run()
  {
    boolean doClearTrace = false;
    Pattern applyPattern = null;
    boolean newPattern = false;
    try
    {
      while (true)
      {
        try
        {
          if (newPattern)
          {
            applyPattern(applyPattern);
            newPattern = false;
            tracePattern = null;
          }

          if (doClearTrace)
          {
            doClearTrace = false;
            clearTrace = false;
            traceLines.clear();
            callback.setText("");
          }

          String newTraceLine = newTraceLines.take();
          
          if (!newTraceLine.startsWith(SYSTEM_TRACE_PREFIX))
          {
            traceLineSemaphore.release();
          }
          
          traceLines.add(newTraceLine);
          if (newTraceLine.startsWith(SYSTEM_TRACE_PREFIX) || 
              (applyPattern == null) || 
              applyPattern.matcher(newTraceLine).matches())
          {
            callback.appendText(newTraceLine);
          }
        }
        catch (InterruptedException ex)
        {
          doClearTrace = getClearTrace();
          Pattern oldPattern = applyPattern;
          applyPattern = getTracePattern();
          if (oldPattern != applyPattern)
          {
            newPattern = true;
          }
          if ((applyPattern == null) && !doClearTrace)
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

  private void applyPattern(Pattern pattern)
  {
    int numLines = traceLines.size();
    int handledLines = 0;
    double lastPercentage = 0;
    StringBuilder traceText = new StringBuilder();
    boolean cancelled = progressCallback.setProgress(0);
    if (!cancelled)
    {
      for (String traceLine : traceLines)
      {
        if (traceLine.startsWith(SYSTEM_TRACE_PREFIX)
            || (pattern == NO_PATTERN) || pattern.matcher(traceLine).matches())
        {
          traceText.append(traceLine);
        }
        handledLines++;

        if ((handledLines % 1000) == 0)
        {
          double unroundedPercentage = ((double) handledLines)
                                       / ((double) numLines);
          double roundedPercantage = roundToSignificantFigures(
                                                               unroundedPercentage,
                                                               2);
          if (lastPercentage != roundedPercantage)
          {
            cancelled = progressCallback
                                        .setProgress((int) (100 * roundedPercantage));

            if (cancelled)
            {
              break;
            }
          }
          lastPercentage = roundedPercantage;
        }
      }
    }
    progressCallback.setProgress(100);
    if (!cancelled)
    {
      callback.setText(traceText.toString());
    }
  }

  private static double roundToSignificantFigures(double num, int n)
  {
    if (num == 0)
    {
      return 0;
    }

    final double d = Math.ceil(Math.log10(num < 0 ? -num : num));
    final int power = n - (int) d;

    final double magnitude = Math.pow(10, power);
    final long shifted = Math.round(num * magnitude);
    return shifted / magnitude;
  }
}