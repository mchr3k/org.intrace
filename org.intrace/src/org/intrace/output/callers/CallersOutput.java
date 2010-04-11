package org.intrace.output.callers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.intrace.output.AgentHelper;
import org.intrace.output.IOutputAdapter;
import org.intrace.shared.CallersConfigConstants;

public class CallersOutput extends IOutputAdapter
{
  private final CallersSettings callersSettings = new CallersSettings("");
  private final Map<String, Object> recordedData = new ConcurrentHashMap<String, Object>();
  private CaptureInProgress inProgress;

  @Override
  public synchronized String getResponse(String args)
  {
    CallersSettings oldSettings = new CallersSettings(callersSettings);
    callersSettings.parseArgs(args);

    if (oldSettings.isCallersEnabled() != callersSettings.isCallersEnabled())
    {
      if (callersSettings.isCallersEnabled())
      {
        System.out.println("## Callers Analysis Started");
        recordedData.clear();
        inProgress = new CaptureInProgress(this);
        inProgress.start();
      }
      else
      {
        System.out.println("## Callers Analysis Ended");
        writeData(true);
        inProgress.running = false;
        inProgress.thread.interrupt();
      }
    }

    return null;
  }

  void writeData(boolean finalWrite)
  {
    Map<String, Object> dataToSend = new HashMap<String, Object>(recordedData);
    dataToSend.put(CallersConfigConstants.FINAL, Boolean.toString(finalWrite));
    dataToSend.put(CallersConfigConstants.PATTERN, callersSettings.getMethodRegex().pattern());
    AgentHelper.writeDataOutput(dataToSend);
  }

  @Override
  public Map<String, String> getSettingsMap()
  {
    return callersSettings.getSettingsMap();
  }

  @Override
  public void enter(String className, String methodName, int lineNo)
  {
    if (callersSettings.isCallersEnabled()
        && callersSettings.getMethodRegex().matcher(methodName).matches())
    {
      recordCall(lineNo);
    }
  }

  @SuppressWarnings("unchecked")
  private synchronized void recordCall(int lineNo)
  {
    StackTraceElement[] stackTrace = new Exception().getStackTrace();
    if ((stackTrace != null) && (stackTrace.length > 3))
    {
      Map<String, Object> treeElement = recordedData;
      for (int ii = 3; ii < stackTrace.length; ii++)
      {
        StackTraceElement element = stackTrace[ii];
        String stackLine = element.getClassName() + "#"
        + element.getMethodName() + ":"
        + ((element.getLineNumber() > -1) ?
                                           element.getLineNumber() :
                                             ((lineNo > -1) ? lineNo : "unknown"));

        Object treeElementObj = treeElement.get(stackLine);
        if (treeElementObj == null)
        {
          treeElementObj = new ConcurrentHashMap<String, Object>();
          treeElement.put(stackLine, treeElementObj);
        }
        treeElement = (Map<String, Object>)treeElementObj;
      }
    }
  }

  private static class CaptureInProgress implements Runnable
  {
    private boolean running = true;
    private Thread thread;
    private final CallersOutput callersRef;
    public CaptureInProgress(CallersOutput callersOutput)
    {
      callersRef = callersOutput;
    }

    public void start()
    {
      thread = new Thread(this);
      thread.setDaemon(true);
      thread.setName("CaptureInProgress");
      thread.start();
    }

    @Override
    public void run()
    {
      while (running)
      {
        try
        {
          callersRef.writeData(false);
          Thread.sleep(5000);
        }
        catch (InterruptedException e)
        {
          // Throw away
        }
      }
    }

  }
}
