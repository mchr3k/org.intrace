package org.intrace.client.gui.helper;


import java.util.Map;
import java.util.Map.Entry;

import org.intrace.shared.AgentConfigConstants;
import org.intrace.shared.CallersConfigConstants;
import org.intrace.shared.OutputConfigConstants;
import org.intrace.shared.TraceConfigConstants;

public class ParsedSettingsData
{
  public final String classRegex;
  public final boolean instrEnabled;
  public final boolean saveTracedClassfiles;
  public final boolean verboseMode;
  public final boolean allowJarsToBeTraced;

  public final boolean callersCaptureInProgress;

  public final boolean entryExitEnabled;
  public final boolean branchEnabled;
  public final boolean argsEnabled;
  public final boolean stdOutEnabled;
  public final boolean fileOutEnabled;
  public final String callersRegex;

  private final Map<String, String> settingsMap;

  public ParsedSettingsData(Map<String, String> settingsMap)
  {
    this.settingsMap = settingsMap;

    classRegex = settingsMap.get(AgentConfigConstants.CLASS_REGEX);

    if ("true".equals(settingsMap.get(AgentConfigConstants.INSTRU_ENABLED)))
    {
      instrEnabled = true;
    }
    else
    {
      instrEnabled = false;
    }

    if ("true".equals(settingsMap.get(AgentConfigConstants.SAVE_TRACED_CLASSFILES)))
    {
      saveTracedClassfiles = true;
    }
    else
    {
      saveTracedClassfiles = false;
    }

    if ("true".equals(settingsMap.get(AgentConfigConstants.VERBOSE_MODE)))
    {
      verboseMode = true;
    }
    else
    {
      verboseMode = false;
    }

    if ("true".equals(settingsMap.get(AgentConfigConstants.ALLOW_JARS_TO_BE_TRACED)))
    {
      allowJarsToBeTraced = true;
    }
    else
    {
      allowJarsToBeTraced = false;
    }

    callersRegex = settingsMap.get(CallersConfigConstants.PATTERN);

    if ("true".equals(settingsMap.get(CallersConfigConstants.CALLERS_ENABLED)))
    {
      callersCaptureInProgress = true;
    }
    else
    {
      callersCaptureInProgress = false;
    }

    if ("true".equals(settingsMap.get(TraceConfigConstants.ENTRY_EXIT)))
    {
      entryExitEnabled = true;
    }
    else
    {
      entryExitEnabled = false;
    }

    if ("true".equals(settingsMap.get(TraceConfigConstants.BRANCH)))
    {
      branchEnabled = true;
    }
    else
    {
      branchEnabled = false;
    }

    if ("true".equals(settingsMap.get(TraceConfigConstants.ARG)))
    {
      argsEnabled = true;
    }
    else
    {
      argsEnabled = false;
    }

    if ("true".equals(settingsMap.get(OutputConfigConstants.STD_OUT)))
    {
      stdOutEnabled = true;
    }
    else
    {
      stdOutEnabled = false;
    }

    if ("true".equals(settingsMap.get(OutputConfigConstants.FILE_OUT)))
    {
      fileOutEnabled = true;
    }
    else
    {
      fileOutEnabled = false;
    }
  }

  public String dumpSettings()
  {
    StringBuffer settingsString = new StringBuffer();
    for (Entry<String,String> entry : settingsMap.entrySet())
    {
      if (entry.getKey().startsWith("["))
      {
        settingsString.append(entry.getKey());
        settingsString.append(entry.getValue());
      }
    }
    return settingsString.toString();
  }

}
