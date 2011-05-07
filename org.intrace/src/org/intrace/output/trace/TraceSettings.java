package org.intrace.output.trace;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.HashMap;
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
  private boolean branchTraceEnabled = true;
  private boolean argTraceEnabled = true;

  private boolean stdoutTraceOutputEnabled = false;
  private boolean fileTraceOutputEnabled = false;
  private boolean netTraceOutputEnabled = true;
  private File file1 = new File("trc1.txt");
  private File file2 = new File("trc2.txt");
  private PrintWriter file1TraceWriter = null;
  private PrintWriter file2TraceWriter = null;

  public TraceSettings(TraceSettings oldSettings)
  {
    entryExitTraceEnabled = oldSettings.entryExitTraceEnabled;
    branchTraceEnabled = oldSettings.branchTraceEnabled;
    argTraceEnabled = oldSettings.argTraceEnabled;
    stdoutTraceOutputEnabled = oldSettings.stdoutTraceOutputEnabled;
    fileTraceOutputEnabled = oldSettings.fileTraceOutputEnabled;
    netTraceOutputEnabled = oldSettings.netTraceOutputEnabled;
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
      parseArg("[" + seperateArgs[ii].toLowerCase());
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
    else if (arg.equals(TraceConfigConstants.STD_OUT + "true"))
    {
      stdoutTraceOutputEnabled = true;
    }
    else if (arg.equals(TraceConfigConstants.STD_OUT + "false"))
    {
      stdoutTraceOutputEnabled = false;
    }
    else if (arg.equals(TraceConfigConstants.FILE_OUT + "true"))
    {
      fileTraceOutputEnabled = true;
    }
    else if (arg.equals(TraceConfigConstants.FILE_OUT + "false"))
    {
      fileTraceOutputEnabled = false;
      file1TraceWriter = closeFile(file1TraceWriter);
      file2TraceWriter = closeFile(file2TraceWriter);
    }
    else if (arg.startsWith("[out-file1-"))
    {
      String file1Name = arg.replace("[out-file1-", "");
      file1TraceWriter = closeFile(file1TraceWriter);
      file1 = new File(file1Name);
    }
    else if (arg.startsWith("[out-file2-"))
    {
      String file2Name = arg.replace("[out-file2-", "");
      file2TraceWriter = closeFile(file2TraceWriter);
      file2 = new File(file2Name);
    }
    else if (arg.equals(TraceConfigConstants.NET_OUT + "true"))
    {
      netTraceOutputEnabled = true;
    }
    else if (arg.equals(TraceConfigConstants.NET_OUT + "false"))
    {
      System.setProperty("NET", "OFF");
      netTraceOutputEnabled = false;
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

  public boolean isNetTraceOutputEnabled()
  {
    return netTraceOutputEnabled;
  }

  // Flag to indicate whether file output is currently going to file1 or file2
  private boolean file1Active = true;

  // Variable for tracking the number of bytes written to the output files
  private int writtenLines = 0;
  private static final int MAX_LINES_PER_FILE = 100 * 1000; // 100k lines

  public PrintWriter getFileTraceWriter()
  {
    // Handle rolling over between files
    writtenLines++;
    if (writtenLines > MAX_LINES_PER_FILE)
    {
      writtenLines = 0;
      file1Active = !file1Active;

      if (file1Active)
      {
        file1TraceWriter = resetFile(file1TraceWriter, file1, true);
      }
      else
      {
        file2TraceWriter = resetFile(file2TraceWriter, file2, true);
      }
    }
    if (file1Active)
    {
      if (file1TraceWriter == null)
      {
        file1TraceWriter = resetFile(file1TraceWriter, file1, false);
      }
      return file1TraceWriter;
    }
    else
    {
      if (file2TraceWriter == null)
      {
        file2TraceWriter = resetFile(file2TraceWriter, file2, false);
      }
      return file2TraceWriter;
    }
  }

  private PrintWriter closeFile(PrintWriter printWriter)
  {
    if (printWriter != null)
    {
      printWriter.flush();
      printWriter.close();
    }
    return null;
  }

  private PrintWriter resetFile(PrintWriter printWriter, File file,
                                boolean deleteFile)
  {
    writtenLines = 0;
    PrintWriter ret = null;
    try
    {
      closeFile(printWriter);
      if (deleteFile)
      {
        file.delete();
      }
      else if (file.exists())
      {
        LineNumberReader reader = new LineNumberReader(new FileReader(file));
        while (reader.readLine() != null)
        {
          // Do nothing
        }
        writtenLines = reader.getLineNumber();
        reader.close();
      }
      ret = new PrintWriter(new FileWriter(file, true));
    }
    catch (IOException e)
    {
      // Throw away
    }
    return ret;
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
    settingsMap.put(TraceConfigConstants.STD_OUT,
                    Boolean.toString(stdoutTraceOutputEnabled));
    settingsMap.put(TraceConfigConstants.FILE_OUT,
                    Boolean.toString(fileTraceOutputEnabled));
    settingsMap.put(TraceConfigConstants.NET_OUT,
                    Boolean.toString(netTraceOutputEnabled));
    return settingsMap;
  }
}