package org.intrace.client.gui.helper;

import java.util.Map;
import java.util.Map.Entry;

import org.intrace.shared.AgentConfigConstants;
import org.intrace.shared.TraceConfigConstants;

public class ParsedSettingsData
{
  public final String classRegex;
  public final boolean instrEnabled;
  public final boolean saveTracedClassfiles;
  public final boolean verboseMode;
  public final boolean allowJarsToBeTraced;

  public final boolean entryExitEnabled;
  public final boolean branchEnabled;
  public final boolean argsEnabled;
  public final boolean stdOutEnabled;
  public final boolean fileOutEnabled;
  public final boolean netOutEnabled;

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

    if ("true"
              .equals(settingsMap
                                 .get(AgentConfigConstants.SAVE_TRACED_CLASSFILES)))
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

    if ("true"
              .equals(settingsMap
                                 .get(AgentConfigConstants.ALLOW_JARS_TO_BE_TRACED)))
    {
      allowJarsToBeTraced = true;
    }
    else
    {
      allowJarsToBeTraced = false;
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

    if ("true".equals(settingsMap.get(TraceConfigConstants.STD_OUT)))
    {
      stdOutEnabled = true;
    }
    else
    {
      stdOutEnabled = false;
    }

    if ("true".equals(settingsMap.get(TraceConfigConstants.FILE_OUT)))
    {
      fileOutEnabled = true;
    }
    else
    {
      fileOutEnabled = false;
    }

    if ("true".equals(settingsMap.get(TraceConfigConstants.NET_OUT)))
    {
      netOutEnabled = true;
    }
    else
    {
      netOutEnabled = false;
    }
  }

  @Override
  public String toString()
  {
    StringBuffer settingsString = new StringBuffer();
    if (settingsMap.size() > 0)
    {
      for (Entry<String, String> entry : settingsMap.entrySet())
      {
        if (entry.getKey().startsWith("["))
        {
          settingsString.append(entry.getKey());
          settingsString.append(entry.getValue());
        }
      }
    }
    else
    {
      settingsString.append("<unknown>");
    }
    return settingsString.toString();
  }

}
