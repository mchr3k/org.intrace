package org.intrace.client.gui.helper;

import java.util.Map;
import java.util.Map.Entry;

import org.intrace.shared.AgentConfigConstants;
import org.intrace.shared.TraceConfigConstants;

public class ParsedSettingsData
{
  public String classRegex;
  public String classExcludeRegex;
  public boolean instrEnabled;
  public boolean saveTracedClassfiles;
  public boolean verboseMode;

  public boolean entryExitEnabled;
  public boolean branchEnabled;
  public boolean argsEnabled;
  public boolean stdOutEnabled;
  public boolean fileOutEnabled;
  public boolean netOutEnabled;

  public final int instruClasses;
  public final int totalClasses;
  
  public final int actualServerPort;
  
  public final boolean waitStart;

  private final Map<String, String> settingsMap;

  public ParsedSettingsData(Map<String, String> settingsMap)
  {
    this.settingsMap = settingsMap;

    classRegex = settingsMap.get(AgentConfigConstants.CLASS_REGEX);
    classExcludeRegex = settingsMap
                                   .get(AgentConfigConstants.EXCLUDE_CLASS_REGEX);

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

    String numInstrStr = settingsMap
                                    .get(AgentConfigConstants.STINST);
    if (numInstrStr != null)
    {
      instruClasses = Integer.parseInt(numInstrStr);
    }
    else
    {
      instruClasses = 0;
    }

    String numTotalStr = settingsMap
                                    .get(AgentConfigConstants.STCLS);
    if (numTotalStr != null)
    {
      totalClasses = Integer.parseInt(numTotalStr);
    }
    else
    {
      totalClasses = 0;
    }
    
    String actualServerPortStr = settingsMap.get(AgentConfigConstants.SERVER_PORT);
    if (actualServerPortStr != null)
    {
      actualServerPort = Integer.parseInt(actualServerPortStr);
    }
    else
    {
      actualServerPort = 9123;
    }
    
    if ("true".equals(settingsMap.get(AgentConfigConstants.START_WAIT)))
    {
      waitStart = true;
    }
    else
    {
      waitStart = false;
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
