package org.intrace.output.trace;

import java.util.HashMap;
import java.util.Map;

import org.intrace.shared.TraceConfigConstants;

/**
 * Args Format:
 *   "[arg1[arg2[arg3"
 * 
 * where argx is of the form
 *   value-parameter
 */
public class TraceSettings
{
  private boolean entryExitTraceEnabled = false;
  private boolean branchTraceEnabled = false;
  private boolean argTraceEnabled = false;

  public TraceSettings(TraceSettings oldSettings)
  {
    entryExitTraceEnabled = oldSettings.entryExitTraceEnabled;
    branchTraceEnabled = oldSettings.branchTraceEnabled;
    argTraceEnabled = oldSettings.argTraceEnabled;
  }

  public TraceSettings(String args)
  {
    parseArgs(args);
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
    if (arg.equals("trace-ee-false"))
    {
      entryExitTraceEnabled = false;
    }
    else if (arg.equals("trace-ee-true"))
    {
      entryExitTraceEnabled = true;
    }
    else if (arg.equals("trace-branch-true"))
    {
      branchTraceEnabled = true;
    }
    else if (arg.equals("trace-branch-false"))
    {
      branchTraceEnabled = false;
    }
    else if (arg.equals("trace-args-true"))
    {
      argTraceEnabled = true;
    }
    else if (arg.equals("trace-args-false"))
    {
      argTraceEnabled = false;
    }
  }

  public boolean isEntryExitTraceEnabled()
  {
    return entryExitTraceEnabled;
  }

  public boolean isBranchTraceEnabled()
  {
    return branchTraceEnabled;
  }

  public boolean isArgTraceEnabled()
  {
    return argTraceEnabled;
  }

  public Map<String,String> getSettingsMap()
  {
    Map<String,String> settingsMap = new HashMap<String, String>();
    settingsMap.put(TraceConfigConstants.ENTRY_EXIT, Boolean.toString(entryExitTraceEnabled));
    settingsMap.put(TraceConfigConstants.BRANCH, Boolean.toString(branchTraceEnabled));
    settingsMap.put(TraceConfigConstants.ARG, Boolean.toString(argTraceEnabled));
    return settingsMap;
  }
}