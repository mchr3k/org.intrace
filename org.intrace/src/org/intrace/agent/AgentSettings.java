package org.intrace.agent;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Args Format:
 *   "[arg1[arg2[arg3"
 *   
 * where argx is of the form
 *   value-parameter
 */
public class AgentSettings
{
  private Pattern classRegex = Pattern.compile("");
  private boolean tracingEnabled = false;
  private boolean saveTracedClassfiles = false;
  private boolean verboseMode = false;
  private boolean allowJarsToBeTraced = false;
    
  public AgentSettings(String args)
  {
    parseArg(args);
  }
  
  public AgentSettings(AgentSettings oldInstance)
  {
    classRegex = oldInstance.getClassRegex();
    tracingEnabled = oldInstance.isTracingEnabled();
    saveTracedClassfiles = oldInstance.saveTracedClassfiles();
    verboseMode = oldInstance.isVerboseMode();
    allowJarsToBeTraced = oldInstance.allowJarsToBeTraced();
  }

  public void parseArgs(String args)
  {
    String[] seperateArgs = args.split("\\[");
    for (int ii = 0; ii < seperateArgs.length; ii++)
    {
      parseArg(seperateArgs[ii].toLowerCase());
    }
  }
  
  private void parseArg(String arg)
  {
    if (arg.equals("verbose-true"))
    {
      verboseMode = true;
    }
    else if (arg.equals("verbose-false"))
    {
      verboseMode = false;
    }
    else if (arg.equals("instru-true"))
    {
      tracingEnabled = true;
    }
    else if (arg.equals("instru-false"))
    {
      tracingEnabled = false;
    }
    else if (arg.equals("saveinstru-true"))
    {
      saveTracedClassfiles = true;
    }
    else if (arg.equals("saveinstru-false"))
    {
      saveTracedClassfiles = false;
    }
    else if (arg.equals("instrujars-true"))
    {
      allowJarsToBeTraced = true;
    }
    else if (arg.equals("instrujars-false"))
    {
      allowJarsToBeTraced = false;
    }
    else if (arg.startsWith("regex-"))
    {
      String classRegexStr = arg.replace("regex-", "");
      classRegex = Pattern.compile(classRegexStr);
    }
  }

  public Pattern getClassRegex()
  {
    return classRegex;
  }

  public boolean isTracingEnabled()
  {
    return tracingEnabled;
  }

  public boolean saveTracedClassfiles()
  {
    return saveTracedClassfiles;
  }

  public boolean isVerboseMode()
  {
    return verboseMode;
  }

  public boolean allowJarsToBeTraced()
  {
    return allowJarsToBeTraced;
  }
  
  @Override
  public String toString()
  {
    String currentSettings = "";
    currentSettings += "Class Regex                : " + classRegex + "\n";
    currentSettings += "Tracing Enabled            : " + tracingEnabled + "\n";
    currentSettings += "Save Traced Class Files    : " + saveTracedClassfiles + "\n";
    currentSettings += "Trace Classes in JAR Files : " + allowJarsToBeTraced + "\n";
    return currentSettings;
  }
  
  public Map<String,String> getSettingsMap()
  {
    Map<String,String> settingsMap = new HashMap<String, String>();
    settingsMap.put(AgentConfigConstants.TRACING_ENABLED, Boolean.toString(tracingEnabled));
    settingsMap.put(AgentConfigConstants.CLASS_REGEX, classRegex.pattern());
    settingsMap.put(AgentConfigConstants.ALLOW_JARS_TO_BE_TRACED, Boolean.toString(allowJarsToBeTraced));
    settingsMap.put(AgentConfigConstants.VERBOSE_MODE, Boolean.toString(verboseMode));
    settingsMap.put(AgentConfigConstants.SAVE_TRACED_CLASSFILES, Boolean.toString(saveTracedClassfiles));
    return settingsMap;
  }
}
