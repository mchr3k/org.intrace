package gb.instrument.client.gui;

import gb.instrument.agent.AgentConfigConstants;
import gb.instrument.output.trace.TraceConfigConstants;

import java.util.Map;

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
  public ParsedSettingsData(Map<String, String> settingsMap)
  {
    classRegex = settingsMap.get(AgentConfigConstants.CLASS_REGEX);
    
    if ("true".equals(settingsMap.get(AgentConfigConstants.TRACING_ENABLED)))
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
  }

}
