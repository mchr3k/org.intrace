package org.intrace.output;

import java.util.HashMap;
import java.util.Map;

public class OutputSettings
{
  public boolean networkTraceOutputRequested = false;

  public OutputSettings(String args)
  {
    parseArgs(args);
  }

  public void parseArgs(String args)
  {
    String[] seperateArgs = args.split("\\[");
    for (int ii = 0; ii < seperateArgs.length; ii++)
    {
      parseArg("[" + seperateArgs[ii].toLowerCase());
    }
  }

  private void parseArg(String arg)
  {
    if (arg.equals("[out-network"))
    {
      networkTraceOutputRequested = true;
    }
  }

  public Map<String, String> getSettingsMap()
  {
    Map<String, String> settingsMap = new HashMap<String, String>();
    return settingsMap;
  }
}
