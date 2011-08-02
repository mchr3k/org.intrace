package org.intrace.client.gui.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    List<String> getIncludePattern();

    List<String> getExcludePattern();

    boolean discardFiltered();
  }

  /**
   * This constant defines the minimum gap in milliseconds between
   * callbacks to the TraceTextHandler.
   */
  private static final long MIN_UPDATE_GAP = 100; // milliseconds
  
  /**
   * System trace is never filtered out
   */
  private static final String SYSTEM_TRACE_PREFIX = "***";

  /**
   * Low memory warning
   */
  private static final String LOW_MEMORY = "Warning: Low memory - no further trace will be collected. StdOut or File output will continue if enabled.";

  /**
   * Match all val
   */
  public static final String MATCH_ALL_VAL = "*";
  
  /**
   * Pattern which matches anything
   */
  public static final List<String> MATCH_ALL = new ArrayList<String>(1);
  static
  {
    MATCH_ALL.add(MATCH_ALL_VAL);
  }

  /**
   * Pattern which matches nothing
   */
  public static final List<String> MATCH_NONE = new ArrayList<String>(0);

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
  private List<String> traceLines = new ArrayList<String>();
  
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
   * Flag to signal that excess trace should be discarded
   */
  private boolean discardExcessTrace = true;

  /**
   * cTor
   * @param mode 
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
  
  public void interrupt()
  {
    thisThread.interrupt();
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

  public void setDiscardExcess(boolean xiDiscardExcess)
  {
    discardExcessTrace = xiDiscardExcess;
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
                                    0.8 * maxMemory); // 90% of Maxmemory
    long charMemLimit = byteMemLimit / 2; // UTF-16 - 2 bytes per char 
    long numChars = 0;
    boolean doClearTrace = false;
    boolean lowMemorySignalled = false;
    TraceFilterProgressHandler patternProgress = null;
    List<String> activeIncludePattern = MATCH_ALL;
    List<String> activeExcludePattern = MATCH_NONE;
    boolean discardFilteredTrace = true;
    
    StringBuilder bufferedText = new StringBuilder();
    long lastTextTime = 0;
    int bufferedTextCount = 0;
    
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
            discardFilteredTrace = patternProgress.discardFiltered();
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

          String newTraceLine = null;
          if (bufferedTextCount > 0)
          {
            newTraceLine = newTraceLines.poll(MIN_UPDATE_GAP, 
                                              TimeUnit.MILLISECONDS);
          }
          else
          {
            newTraceLine = newTraceLines.take();
          }

          long newTextTime = System.currentTimeMillis();
          long timeSinceLastText = newTextTime - lastTextTime;
          
          boolean bufferedTextToWrite = (bufferedTextCount > 0);
          boolean writeBufferedText = (timeSinceLastText > MIN_UPDATE_GAP) && bufferedTextToWrite;
          if (writeBufferedText)
          {
            String bufferedTextStr = bufferedText.toString();
            
            totalLines.addAndGet(bufferedTextCount);
            displayedLines.addAndGet(bufferedTextCount);
            callback.appendText(bufferedTextStr);
            callback.setStatus(displayedLines.get(), totalLines.get());
            
            lastTextTime = newTextTime;
            
            bufferedText.setLength(0);
            bufferedTextCount = 0;
            bufferedTextToWrite = false;
          }
          
          if (newTraceLine != null)
          {
            if (!newTraceLine.startsWith(SYSTEM_TRACE_PREFIX))
            {
              traceLineSemaphore.release();
            }
  
            boolean memoryLimitSafe = (numChars < charMemLimit); 
            if (memoryLimitSafe || discardExcessTrace)
            {
              if (!memoryLimitSafe)
              {
                numChars = discardExcess(activeIncludePattern, activeExcludePattern);
              }
              
              lowMemorySignalled = false;
              boolean matchFilter = newTraceLine.startsWith(SYSTEM_TRACE_PREFIX) ||
                                    (!matches(activeExcludePattern, newTraceLine) &&
                                     matches(activeIncludePattern, newTraceLine));
  
              if (!discardFilteredTrace || matchFilter)
              {
                // I expected a factor of 2 due to trace strings being held by this
                // thread along with another copy held by the UI. However, profiling
                // shows a factor of 40 is necessary. This is because we need to be able
                // to handle entire copies of the active data when adding new strings.
                numChars += (40 * newTraceLine.length());                
    
                traceLines.add(newTraceLine);
              }

              if (matchFilter)
              {
                if (timeSinceLastText > MIN_UPDATE_GAP)
                {
                  totalLines.addAndGet(1);
                  displayedLines.addAndGet(1);
                  callback.appendText(newTraceLine);
                  callback.setStatus(displayedLines.get(), totalLines.get());
                  
                  lastTextTime = newTextTime;
                }
                else
                {
                  bufferedTextCount++;
                  bufferedText.append(newTraceLine);
                }
              }
              else if (!discardExcessTrace)
              {
                totalLines.incrementAndGet();
                callback.setStatus(displayedLines.get(), totalLines.get());
              }
            }            
          }
          
          if (!bufferedTextToWrite &&
              (numChars >= charMemLimit) &&
              !lowMemorySignalled)
          {
            lowMemorySignalled = true;
            String memWarning = SYSTEM_TRACE_PREFIX + " " + LOW_MEMORY;            
            traceLines.add(memWarning);
            totalLines.incrementAndGet();
            displayedLines.incrementAndGet();
            callback.appendText(memWarning);
            callback.setStatus(displayedLines.get(), totalLines.get());
          }                   
        }
        catch (InterruptedException ex)
        {
          doClearTrace = getClearTrace();
          patternProgress = traceFilters.poll(); 
          if ((patternProgress == null) && 
              !doClearTrace)
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
  }
  
  private boolean matches(List<String> strs, String target)
  {
    for (String str : strs)
    {
      if (str.equals(MATCH_ALL_VAL) || 
          target.contains(str))
      {
        return true;
      }
    }
    return false;
  }

  private void applyPattern(TraceFilterProgressHandler progressCallback)
  {
    boolean xiDiscardFiltered = progressCallback.discardFiltered();
    List<String> includePattern = progressCallback.getIncludePattern();
    List<String> excludePattern = progressCallback.getExcludePattern();
    int numLines = traceLines.size();
    int handledLines = 0;
    double lastPercentage = 0;
    double filterPercentage = (xiDiscardFiltered ? 70 : 100);
    StringBuilder traceText = new StringBuilder();
    boolean cancelled = progressCallback.setProgress(0);
    boolean firstUpdate = true;
    displayedLines.set(0);
    if (xiDiscardFiltered)
    {
      totalLines.set(0);
    }
    callback.setStatus(displayedLines.get(), totalLines.get());
    List<Integer> removeLines = new ArrayList<Integer>();
    if (!cancelled)
    {            
      for (int ii = 0; ii < traceLines.size(); ii++)
      {
        String traceLine = traceLines.get(ii);
        if (traceLine.startsWith(SYSTEM_TRACE_PREFIX) ||
            (!matches(excludePattern, traceLine) &&
              matches(includePattern, traceLine)))
        {
          displayedLines.incrementAndGet();
          if (xiDiscardFiltered)
          {
            totalLines.incrementAndGet();
          }
          traceText.append(traceLine);
        }
        else if (xiDiscardFiltered)
        {
          removeLines.add(ii);
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
            cancelled = progressCallback.setProgress(
                           (int) (filterPercentage * roundedPercantage));
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
    if (xiDiscardFiltered)
    {
      handledLines = 0;
      for (Integer removeLine : removeLines)
      {
        traceLines.remove(removeLine - handledLines);
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
            cancelled = progressCallback.setProgress(
                         (int)(filterPercentage) +  
                         (int) ((100 - filterPercentage) * roundedPercantage));
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
  
  private long discardExcess(List<String> includePattern,
                             List<String> excludePattern)
  {    
    int numLines = traceLines.size();
    int discardLines = (int)(0.25 * (double)numLines);
    StringBuilder traceText = new StringBuilder();    
    displayedLines.set(0);
    totalLines.set(0);
    List<String> newTraceLines = new ArrayList<String>((numLines - discardLines) + 10);
    
    // Update counters and recompute text
    for (int ii = discardLines; ii < traceLines.size(); ii++)
    {
      String traceLine = traceLines.get(ii);
      newTraceLines.add(traceLine);
      if (traceLine.startsWith(SYSTEM_TRACE_PREFIX) ||
          (!matches(excludePattern, traceLine) &&
            matches(includePattern, traceLine)))
      {
        displayedLines.incrementAndGet();        
        traceText.append(traceLine);
      }
      totalLines.incrementAndGet();
    }

    // Make callbacks
    traceLines = newTraceLines;
    String text = traceText.toString();
    callback.setText(text);
    callback.setStatus(displayedLines.get(), totalLines.get());
    
    return text.length();
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