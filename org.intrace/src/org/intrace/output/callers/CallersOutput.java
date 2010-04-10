package org.intrace.output.callers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.intrace.output.AgentHelper;
import org.intrace.output.IOutputAdapter;
import org.intrace.shared.CallersConfigConstants;

public class CallersOutput extends IOutputAdapter
{
  private final CallersSettings callersSettings = new CallersSettings("");
  private final Map<String, Object> recordedData = new ConcurrentHashMap<String, Object>();

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
        recordedData.put(CallersConfigConstants.MAP_ID, CallersConfigConstants.MAP_ID);
      }
      else
      {
        System.out.println("## Callers Analysis Ended");
        AgentHelper.writeDataOutput(new ConcurrentHashMap<String, Object>(recordedData));
      }
    }

    return null;
  }

  @Override
  public Map<String, String> getSettingsMap()
  {
    return callersSettings.getSettingsMap();
  }

  @Override
  public void enter(String className, String methodName)
  {
    if (callersSettings.isCallersEnabled()
        && callersSettings.getMethodRegex().matcher(methodName).matches())
    {
      recordCall();
    }
  }

  @SuppressWarnings("unchecked")
  private synchronized void recordCall()
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
        + ((element.getLineNumber() > -1) ? element.getLineNumber() : "unknown") ;

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
}
