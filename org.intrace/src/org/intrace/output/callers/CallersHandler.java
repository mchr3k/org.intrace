package org.intrace.output.callers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.intrace.output.AgentHelper;
import org.intrace.output.IInstrumentationHandlerAdapter;
import org.intrace.shared.CallersConfigConstants;

public class CallersHandler extends IInstrumentationHandlerAdapter
{
  private static final long INTERMEDIATE_SEND_DELAY = 1000;

  private final Map<Long, CallersCapture> activeCaptures = new HashMap<Long, CallersCapture>();
  private long callersId;

  @Override
  public synchronized String getResponse(String args)
  {
    String[] seperateArgs = args.split("\\[");
    for (int ii = 0; ii < seperateArgs.length; ii++)
    {
      parseArg("[" + seperateArgs[ii].toLowerCase());
    }

    return null;
  }

  private synchronized String parseArg(String arg)
  {
    if (arg.startsWith("[callers-start-"))
    {
      String argRegex = arg.replace("[callers-start-", "");
      Pattern argPattern = Pattern.compile(argRegex);
      callersId++;
      CallersCapture newCallersCapture = new CallersCapture(this, argPattern,
                                                            callersId);
      newCallersCapture.start();
      activeCaptures.put(callersId, newCallersCapture);

      System.out.println("## New Callers Analysis Started, ID: " + callersId
                         + ", Pattern: " + argRegex);

      return "[new-callers-id-" + callersId;
    }
    else if (arg.startsWith("[callers-end-"))
    {
      String argIDStr = arg.replace("[callers-end-", "");
      Long argID = Long.parseLong(argIDStr);
      System.out.println("## Callers Analysis Ended, ID: " + argID);
      CallersCapture oldCallersCapture = activeCaptures.get(argID);
      oldCallersCapture.running = false;
      oldCallersCapture.thread.interrupt();
      oldCallersCapture.sendFinalData();
    }
    return null;
  }

  void writeData(Map<String, Object> dataToSend)
  {
    AgentHelper.writeDataOutput(dataToSend);
  }

  @Override
  public Map<String, String> getSettingsMap()
  {
    return new HashMap<String, String>();
  }

  @Override
  public void enter(String className, String methodName, int lineNo)
  {
    if ((activeCaptures.size() > 0))
    {
      for (CallersCapture cc : activeCaptures.values())
      {
        cc.recordCall(methodName, lineNo);
      }
    }
  }

  private static class CallersCapture implements Runnable
  {
    private boolean running = true;
    private Thread thread;
    private final CallersHandler callersRef;
    private final Map<String, Object> recordedData = new ConcurrentHashMap<String, Object>();
    private final Pattern pattern;
    private final long id;

    public CallersCapture(CallersHandler callersOutput, Pattern pattern,
        long callersId)
    {
      callersRef = callersOutput;
      this.pattern = pattern;
      id = callersId;
    }

    public void sendFinalData()
    {
      sendData(true);
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
          sendData(false);
          Thread.sleep(INTERMEDIATE_SEND_DELAY);
        }
        catch (InterruptedException e)
        {
          // Throw away
        }
      }
    }

    private void sendData(boolean finalData)
    {
      Map<String, Object> dataToSend = new HashMap<String, Object>(recordedData);
      dataToSend.put(CallersConfigConstants.FINAL, Boolean.toString(finalData));
      dataToSend.put(CallersConfigConstants.PATTERN, pattern.pattern());
      dataToSend.put(CallersConfigConstants.ID, id);
      callersRef.writeData(dataToSend);
    }

    @SuppressWarnings("unchecked")
    private synchronized void recordCall(String methodName, int lineNo)
    {
      if (pattern.matcher(methodName).matches())
      {
        StackTraceElement[] stackTrace = new Exception().getStackTrace();
        if ((stackTrace != null) && (stackTrace.length > 4))
        {
          Map<String, Object> treeElement = recordedData;
          for (int ii = 4; ii < stackTrace.length; ii++)
          {
            StackTraceElement element = stackTrace[ii];
            String stackLine = element.getClassName()
                               + "#"
                               + element.getMethodName()
                               + ":"
                               + ((element.getLineNumber() > -1) ? element
                                                                          .getLineNumber()
                                                                : ((lineNo > -1) ? lineNo
                                                                                : "unknown"));

            Object treeElementObj = treeElement.get(stackLine);
            if (treeElementObj == null)
            {
              treeElementObj = new ConcurrentHashMap<String, Object>();
              treeElement.put(stackLine, treeElementObj);
            }
            treeElement = (Map<String, Object>) treeElementObj;
          }
        }
      }
    }

  }
}
