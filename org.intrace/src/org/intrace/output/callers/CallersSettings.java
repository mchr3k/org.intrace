package org.intrace.output.callers;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.intrace.shared.CallersConfigConstants;

public class CallersSettings
{
  private boolean callersEnabled = false;
  private Pattern methodsPattern = Pattern.compile(".*");

  public CallersSettings(CallersSettings oldSettings)
  {
    callersEnabled = oldSettings.callersEnabled;
    methodsPattern = oldSettings.methodsPattern;
  }

  public CallersSettings(String args)
  {
    parseArgs(args);
  }

  public void parseArgs(String args)
  {
    String[] seperateArgs = args.split("\\[");
    for (int ii = 0; ii < seperateArgs.length; ii++)
    {
      parseArg(seperateArgs[ii]);
    }
  }

  private void parseArg(String arg)
  {
    if (arg.toLowerCase().equals("callers-enabled-false"))
    {
      callersEnabled = false;
    }
    else if (arg.toLowerCase().equals("callers-enabled-true"))
    {
      callersEnabled = true;
    }
    else if (arg.startsWith("callers-regex-"))
    {
      String methodsRegexStr = arg.replace("callers-regex-", "");
      methodsPattern = Pattern.compile(methodsRegexStr);
    }
  }

  public boolean isCallersEnabled()
  {
    return callersEnabled;
  }

  public Pattern getMethodRegex()
  {
    return methodsPattern;
  }

  public Map<String,String> getSettingsMap()
  {
    Map<String,String> settingsMap = new HashMap<String, String>();
    settingsMap.put(CallersConfigConstants.CALLERS_ENABLED, Boolean.toString(callersEnabled));
    settingsMap.put(CallersConfigConstants.PATTERN, methodsPattern.pattern());
    return settingsMap;
  }
}
