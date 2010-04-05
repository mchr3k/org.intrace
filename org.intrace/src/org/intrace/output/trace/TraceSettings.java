package org.intrace.output.trace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Args Format:
 *   "[arg1[arg2[arg3"
 *   
 * where argx is of the form
 *   value-parameter
 */
public class TraceSettings
{
  private boolean entryExitTraceEnabled = true;
  private boolean branchTraceEnabled = false;
  private boolean argTraceEnabled = false;
  
  private boolean stdoutTraceOutputEnabled = true;
  private boolean fileTraceOutputEnabled = false;
  private File file1;
  private File file2;
  private PrintWriter file1TraceWriter;
  private PrintWriter file2TraceWriter;
  
  public boolean networkTraceOutputRequested = false;

  public TraceSettings(TraceSettings oldSettings)
  {
    entryExitTraceEnabled = oldSettings.entryExitTraceEnabled;
    branchTraceEnabled = oldSettings.branchTraceEnabled;
    argTraceEnabled = oldSettings.argTraceEnabled;
    
    stdoutTraceOutputEnabled = oldSettings.stdoutTraceOutputEnabled;
    fileTraceOutputEnabled = oldSettings.fileTraceOutputEnabled;
  }
  
  public TraceSettings(String args)
  {    
    file1 = new File("trc1.txt");
    file2 = new File("trc2.txt");
    resetTraceFiles(true, true);   
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
    else if (arg.equals("trace-stdout-true"))
    {
      stdoutTraceOutputEnabled = true;
    }
    else if (arg.equals("trace-stdout-false"))
    {
      stdoutTraceOutputEnabled = false;
    }
    else if (arg.equals("trace-file-true"))
    {
      fileTraceOutputEnabled = true;
    }
    else if (arg.equals("trace-file-false"))
    {
      fileTraceOutputEnabled = false;
    }
    else if (arg.startsWith("trace-file1-"))
    {
      String file1Name = arg.replace("trace-file1-", "");      
      file1 = new File(file1Name);
      resetTraceFiles(true, false);     
    }
    else if (arg.startsWith("trace-file2-"))
    {
      String file2Name = arg.replace("trace-file2-", "");
      file2 = new File(file2Name);
      resetTraceFiles(false, true);
    }
    else if (arg.equals("trace-network"))
    {
      networkTraceOutputRequested = true;
    }
  }
  
  public void resetTraceFiles(boolean resetFile1, boolean resetFile2)
  {
    try
    {
      if (resetFile1)
      {
        file1.delete();
        file1TraceWriter = new PrintWriter(new FileWriter(file1));
      }
      if (resetFile2)
      {
        file2.delete();
        file2TraceWriter = new PrintWriter(new FileWriter(file2));
      }
    }
    catch (IOException e)
    {
      // Throw away
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

  public boolean isStdoutTraceOutputEnabled()
  {
    return stdoutTraceOutputEnabled;
  }
  
  public boolean isFileTraceOutputEnabled()
  {
    return fileTraceOutputEnabled;
  }

  public PrintWriter getFile1TraceWriter()
  {
    return file1TraceWriter;
  }

  public PrintWriter getFile2TraceWriter()
  {
    return file2TraceWriter;
  }

  public Map<String,String> getSettingsMap()
  {
    Map<String,String> settingsMap = new HashMap<String, String>();
    settingsMap.put(TraceConfigConstants.ENTRY_EXIT, Boolean.toString(entryExitTraceEnabled));
    settingsMap.put(TraceConfigConstants.BRANCH, Boolean.toString(branchTraceEnabled));
    settingsMap.put(TraceConfigConstants.ARG, Boolean.toString(argTraceEnabled));
    settingsMap.put(TraceConfigConstants.STD_OUT, Boolean.toString(stdoutTraceOutputEnabled));
    settingsMap.put(TraceConfigConstants.FILE_OUT, Boolean.toString(fileTraceOutputEnabled));
    return settingsMap;
  }
}