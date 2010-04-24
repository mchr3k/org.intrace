package org.intrace.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.intrace.shared.OutputConfigConstants;

public class OutputSettings
{
  private boolean stdoutTraceOutputEnabled = true;
  private boolean fileTraceOutputEnabled = false;
  private File file1;
  private File file2;
  private PrintWriter file1TraceWriter;
  private PrintWriter file2TraceWriter;

  public boolean networkTraceOutputRequested = false;

  public OutputSettings(String args)
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
      parseArg("[" + seperateArgs[ii].toLowerCase());
    }
  }

  private void parseArg(String arg)
  {
    if (arg.equals(OutputConfigConstants.STD_OUT + "true"))
    {
      stdoutTraceOutputEnabled = true;
    }
    else if (arg.equals(OutputConfigConstants.STD_OUT + "false"))
    {
      stdoutTraceOutputEnabled = false;
    }
    else if (arg.equals(OutputConfigConstants.FILE_OUT + "true"))
    {
      fileTraceOutputEnabled = true;
    }
    else if (arg.equals(OutputConfigConstants.FILE_OUT + "false"))
    {
      fileTraceOutputEnabled = false;
    }
    else if (arg.startsWith("[out-file1-"))
    {
      String file1Name = arg.replace("[out-file1-", "");
      file1 = new File(file1Name);
      resetTraceFiles(true, false);
    }
    else if (arg.startsWith("[out-file2-"))
    {
      String file2Name = arg.replace("[out-file2-", "");
      file2 = new File(file2Name);
      resetTraceFiles(false, true);
    }
    else if (arg.equals("[out-network"))
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

  public Map<String, String> getSettingsMap()
  {
    Map<String, String> settingsMap = new HashMap<String, String>();
    settingsMap.put(OutputConfigConstants.STD_OUT,
                    Boolean.toString(stdoutTraceOutputEnabled));
    settingsMap.put(OutputConfigConstants.FILE_OUT,
                    Boolean.toString(fileTraceOutputEnabled));
    return settingsMap;
  }
}
