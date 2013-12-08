package org.intrace.output.trace;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.intrace.shared.TraceConfigConstants;

/**
 * Args Format: "[arg1[arg2[arg3"
 * 
 * where argx is of the form value-parameter
 */
public class TraceSettings
{
  private boolean entryExitTraceEnabled = true;
  private boolean branchTraceEnabled = false;
  private boolean argTraceEnabled = true;
  private boolean truncateArraysEnabled = true;
  
  /**
   * If true, append the 'current' stack trace to the text of the exit trace event.
   * This is helpful for discovering who is invoking a particular line of code.
   * Here is an example of the trace output:
   * <PRE>
   * [07:53:15.509]:[1]:example.FirstTraceExample:intArrayMethod: }:70~java.lang.Thread.getStackTrace(Thread.java:1567),example.FirstTraceExample.intArrayMethod(FirstTraceExample.java:70),example.FirstTraceExample.workMethod(FirstTraceExample.java:38),example.FirstTraceExample.otherMain(FirstTraceExample.java:29),example.FirstTraceExample.main(FirstTraceExample.java:16)
   * </PRE>
   */
  private boolean exitStackTrace = false;

  public TraceSettings(TraceSettings oldSettings)
  {
    entryExitTraceEnabled = oldSettings.entryExitTraceEnabled;
    branchTraceEnabled = oldSettings.branchTraceEnabled;
    argTraceEnabled = oldSettings.argTraceEnabled; 
    truncateArraysEnabled = oldSettings.truncateArraysEnabled;
    exitStackTrace = oldSettings.exitStackTrace;
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
      parseArg("[" + seperateArgs[ii].toLowerCase(Locale.ROOT));
    }
  }

  private void parseArg(String arg)
  {
    if (arg.equals(TraceConfigConstants.ENTRY_EXIT + "false"))
    {
      entryExitTraceEnabled = false;
    }
    else if (arg.equals(TraceConfigConstants.ENTRY_EXIT + "true"))
    {
      entryExitTraceEnabled = true;
    }
    else if (arg.equals(TraceConfigConstants.BRANCH + "true"))
    {
      branchTraceEnabled = true;
    }
    else if (arg.equals(TraceConfigConstants.BRANCH + "false"))
    {
      branchTraceEnabled = false;
    }
    else if (arg.equals(TraceConfigConstants.ARG + "true"))
    {
      argTraceEnabled = true;
    }
    else if (arg.equals(TraceConfigConstants.ARG + "false"))
    {
      argTraceEnabled = false;
    }
    else if (arg.equals(TraceConfigConstants.ARRAYS + "true"))
    {
      truncateArraysEnabled = true;
    }
    else if (arg.equals(TraceConfigConstants.ARRAYS + "false"))
    {
      truncateArraysEnabled = false;
    }
    else if (arg.equals(TraceConfigConstants.EXIT_STACK_TRACE + "false"))
    {
      exitStackTrace = false;
    }
    else if (arg.equals(TraceConfigConstants.EXIT_STACK_TRACE + "true"))
    {
      exitStackTrace = true;
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
  
  public boolean isTruncateArraysEnabled()
  {
    return truncateArraysEnabled;
  }
  public boolean isExitStackTraceEnabled() {
	  return exitStackTrace;
  }

  public Map<String, String> getSettingsMap()
  {
    Map<String, String> settingsMap = new HashMap<String, String>();
    settingsMap.put(TraceConfigConstants.ENTRY_EXIT,
                    Boolean.toString(entryExitTraceEnabled));
    settingsMap.put(TraceConfigConstants.BRANCH,
                    Boolean.toString(branchTraceEnabled));
    settingsMap
               .put(TraceConfigConstants.ARG, Boolean.toString(argTraceEnabled));
    settingsMap
    .put(TraceConfigConstants.ARRAYS, Boolean.toString(truncateArraysEnabled));
    
    settingsMap.put(TraceConfigConstants.EXIT_STACK_TRACE,
            Boolean.toString(exitStackTrace));
    return settingsMap;
  }
}