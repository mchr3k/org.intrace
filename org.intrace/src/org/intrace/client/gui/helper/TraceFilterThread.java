package org.intrace.client.gui.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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
  public static final String SYSTEM_TRACE_PREFIX = "***";

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
    newTraceLines.add(traceLine + "\r\n");
  }

  public void applyFilter(Pattern filter,
                          TraceFilterProgressHandler progressCallback)
  {
    this.progressCallback = progressCallback;
    tracePattern = filter;
    thisThread.interrupt();
  }

  public void clearTrace()
  {
    clearTrace = true;
    thisThread.interrupt();
  }

  @Override
  public void run()
  {
    try
    {
      while (true)
      {
        try
        {
          Pattern pattern = tracePattern;
          if (pattern != null)
          {
            tracePattern = null;
            applyPattern(pattern);
          }

          if (clearTrace)
          {
            clearTrace = false;
            traceLines.clear();
            callback.setText("");
          }

          String newTraceLine = newTraceLines.take();
          traceLines.add(newTraceLine);
          if (newTraceLine.startsWith("***") || (tracePattern == null)
              || tracePattern.matcher(newTraceLine).matches())
          {
            callback.appendText(newTraceLine);
          }
        }
        catch (InterruptedException ex)
        {
          if ((tracePattern == null) && !clearTrace)
          {
            // Time to quit
            break;
          }
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
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