package org.intrace.agent;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.intrace.shared.AgentConfigConstants;

/**
 * Args Format: "[arg1[arg2[arg3"
 *
 * where argx is of the form value-parameter
 */
public class AgentSettings
{
  private Pattern classRegex = Pattern.compile(".*");
  private boolean instruEnabled = false;
  private boolean saveTracedClassfiles = false;
  private boolean verboseMode = false;
  private boolean allowJarsToBeTraced = false;

  public AgentSettings(String args)
  {
    parseArgs(args);
  }

  public AgentSettings(AgentSettings oldInstance)
  {
    classRegex = oldInstance.getClassRegex();
    instruEnabled = oldInstance.isInstrumentationEnabled();
    saveTracedClassfiles = oldInstance.saveTracedClassfiles();
    verboseMode = oldInstance.isVerboseMode();
    allowJarsToBeTraced = oldInstance.allowJarsToBeTraced();
  }

  public void parseArgs(String args)
  {
    String[] seperateArgs = args.split("\\[");
    for (int ii = 0; ii < seperateArgs.length; ii++)
    {
      parseArg("[" + seperateArgs[ii]);
    }
  }

  private void parseArg(String arg)
  {
    if (arg.toLowerCase().equals(AgentConfigConstants.VERBOSE_MODE + "true"))
    {
      verboseMode = true;
    }
    else if (arg.toLowerCase().equals(
                                      AgentConfigConstants.VERBOSE_MODE
                                          + "false"))
    {
      verboseMode = false;
    }
    else if (arg.toLowerCase().equals(
                                      AgentConfigConstants.INSTRU_ENABLED
                                          + "true"))
    {
      instruEnabled = true;
    }
    else if (arg.toLowerCase().equals(
                                      AgentConfigConstants.INSTRU_ENABLED
                                          + "false"))
    {
      instruEnabled = false;
    }
    else if (arg.toLowerCase()
                .equals(AgentConfigConstants.SAVE_TRACED_CLASSFILES + "true"))
    {
      saveTracedClassfiles = true;
    }
    else if (arg.toLowerCase()
                .equals(AgentConfigConstants.SAVE_TRACED_CLASSFILES + "false"))
    {
      saveTracedClassfiles = false;
    }
    else if (arg.toLowerCase()
                .equals(AgentConfigConstants.ALLOW_JARS_TO_BE_TRACED + "true"))
    {
      allowJarsToBeTraced = true;
    }
    else if (arg.toLowerCase()
                .equals(AgentConfigConstants.ALLOW_JARS_TO_BE_TRACED + "false"))
    {
      allowJarsToBeTraced = false;
    }
    else if (arg.startsWith(AgentConfigConstants.CLASS_REGEX))
    {
      String classRegexStr = arg.replace(AgentConfigConstants.CLASS_REGEX, "");
      classRegex = Pattern.compile(classRegexStr);
    }
  }

  public Pattern getClassRegex()
  {
    return classRegex;
  }

  public boolean isInstrumentationEnabled()
  {
    return instruEnabled;
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
    currentSettings += "Tracing Enabled            : " + instruEnabled + "\n";
    currentSettings += "Save Traced Class Files    : " + saveTracedClassfiles
                       + "\n";
    currentSettings += "Trace Classes in JAR Files : " + allowJarsToBeTraced
                       + "\n";
    return currentSettings;
  }

  public Map<String, String> getSettingsMap()
  {
    Map<String, String> settingsMap = new HashMap<String, String>();
    settingsMap.put(AgentConfigConstants.INSTRU_ENABLED,
                    Boolean.toString(instruEnabled));
    settingsMap.put(AgentConfigConstants.CLASS_REGEX, classRegex.pattern());
    settingsMap.put(AgentConfigConstants.ALLOW_JARS_TO_BE_TRACED,
                    Boolean.toString(allowJarsToBeTraced));
    settingsMap.put(AgentConfigConstants.VERBOSE_MODE,
                    Boolean.toString(verboseMode));
    settingsMap.put(AgentConfigConstants.SAVE_TRACED_CLASSFILES,
                    Boolean.toString(saveTracedClassfiles));
    return settingsMap;
  }
}
