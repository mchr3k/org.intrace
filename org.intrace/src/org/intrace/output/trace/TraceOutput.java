package org.intrace.output.trace;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.intrace.output.IOutput;

/**
 * Implements Standard Output Tracing
 */
public class TraceOutput implements IOutput
{
  private boolean entryExitTrace = true;
  private boolean branchTrace = false;
  private boolean argTrace = false;

  private boolean stdoutTrace = true;
  private boolean fileoutTrace = false;
  private boolean file1Active = true;
  private int writtenChars = 0;
  private final int MAX_CHARS_PER_FILE = 100 * 1000; // 100kb

  final TraceSettings traceSettings = new TraceSettings("");
  
  private Map<NetworkTraceThread,Object> traceThreads = new ConcurrentHashMap<NetworkTraceThread,Object>();
  
  public String getResponse(String args)
  {
    TraceSettings oldSettings = new TraceSettings(traceSettings);
    traceSettings.parseArgs(args);
    
    if ((oldSettings.isEntryExitTraceEnabled() != traceSettings.isEntryExitTraceEnabled()) ||
        (oldSettings.isBranchTraceEnabled() != traceSettings.isBranchTraceEnabled()) ||
        (oldSettings.isArgTraceEnabled() != traceSettings.isArgTraceEnabled()) ||
        (oldSettings.isStdoutTraceOutputEnabled() != traceSettings.isStdoutTraceOutputEnabled()) ||
        (oldSettings.isFileTraceOutputEnabled() != traceSettings.isFileTraceOutputEnabled()))
    {
      System.out.println("## Trace Settings Changed");
    }
    
    entryExitTrace = traceSettings.isEntryExitTraceEnabled();
    branchTrace = traceSettings.isBranchTraceEnabled();
    argTrace = traceSettings.isArgTraceEnabled();
    stdoutTrace = traceSettings.isStdoutTraceOutputEnabled();
    fileoutTrace = traceSettings.isFileTraceOutputEnabled();
    
    if (traceSettings.networkTraceOutputRequested)
    {
      System.out.println("## Network Trace Requested");
      ServerSocket networkSocket;
      try
      {
        networkSocket = new ServerSocket(0);
        NetworkTraceThread traceThread = new NetworkTraceThread(networkSocket);
        
        traceThreads.put(traceThread, new Object());
        
        traceThread.start(traceThreads.keySet());
        traceSettings.networkTraceOutputRequested = false;
        return Integer.toString(networkSocket.getLocalPort());
      }
      catch (IOException e)
      {
        // Do nothing
        return null;
      }      
    }
    else
    {
      return null;
    }
  }
  
  public Map<String,String> getSettingsMap()
  {
    return traceSettings.getSettingsMap();
  }
  
  /**
   * Write trace output
   * 
   * @param xiTrace
   */
  private void writeTrace(String xiTrace)
  {
    SimpleDateFormat dateFormat = new SimpleDateFormat();
    long threadID = Thread.currentThread().getId();
    String traceString = "[" + dateFormat.format(new Date()) + "]:[" + 
                         threadID + "]:" + xiTrace;
    if (stdoutTrace)
    {
      System.out.println(traceString);
    }
    
    if (fileoutTrace)
    {
      writeFileTrace(traceString);
    }
    
    Set<NetworkTraceThread> networkThreads = traceThreads.keySet();
    if (networkThreads.size() > 0)
    {
      for (NetworkTraceThread thread : networkThreads)
      {
        thread.queueTrace(traceString);
      }
    }
  }

  private synchronized void writeFileTrace(String traceString)
  {
    PrintWriter outputWriter;
    if (file1Active)
    {
      outputWriter = traceSettings.getFile1TraceWriter();
    }
    else
    {
      outputWriter = traceSettings.getFile2TraceWriter();
    }
    outputWriter.println(traceString);
    outputWriter.flush();
    
    // Switch trace files if necessary
    writtenChars += traceString.length();
    if (writtenChars > MAX_CHARS_PER_FILE)
    {
      writtenChars = 0;
      traceSettings.resetTraceFiles(file1Active, !file1Active);
      file1Active = !file1Active;
    }
  }

  @Override
  public void arg(String className, String methodName, byte byteArg)
  {
    if (argTrace)
    {
      writeTrace(className + ":" + methodName + ": Arg: " + byteArg);
    }
  }

  public void arg(String className, String methodName, byte[] byteArrayArg)
  {
    if (argTrace)
    {
      writeTrace(className + ":" + methodName + ": Arg: "
          + Arrays.toString(byteArrayArg));
    }
  }

  @Override
  public void arg(String className, String methodName, short shortArg)
  {
    if (argTrace)
    {
      writeTrace(className + ":" + methodName + ": Arg: " + shortArg);
    }
  }

  public void arg(String className, String methodName, short[] shortArrayArg)
  {
    if (argTrace)
    {
      writeTrace(className + ":" + methodName + ": Arg: "
          + Arrays.toString(shortArrayArg));
    }
  }

  @Override
  public void arg(String className, String methodName, int intArg)
  {
    if (argTrace)
    {
      writeTrace(className + ":" + methodName + ": Arg: " + intArg);
    }
  }

  @Override
  public void arg(String className, String methodName, int[] intArrayArg)
  {
    if (argTrace)
    {
      writeTrace(className + ":" + methodName + ": Arg: "
          + Arrays.toString(intArrayArg));
    }
  }

  @Override
  public void arg(String className, String methodName, long longArg)
  {
    if (argTrace)
    {
      writeTrace(className + ":" + methodName + ": Arg: " + longArg);
    }
  }

  @Override
  public void arg(String className, String methodName, long[] longArrayArg)
  {
    if (argTrace)
    {
      writeTrace(className + ":" + methodName + ": Arg: "
          + Arrays.toString(longArrayArg));
    }
  }

  @Override
  public void arg(String className, String methodName, float floatArg)
  {
    if (argTrace)
    {
      writeTrace(className + ":" + methodName + ": Arg: " + floatArg);
    }
  }

  @Override
  public void arg(String className, String methodName, float[] floatArrayArg)
  {
    if (argTrace)
    {
      writeTrace(className + ":" + methodName + ": Arg: "
          + Arrays.toString(floatArrayArg));
    }
  }

  @Override
  public void arg(String className, String methodName, double doubleArg)
  {
    if (argTrace)
    {
      writeTrace(className + ":" + methodName + ": Arg: " + doubleArg);
    }
  }

  @Override
  public void arg(String className, String methodName, double[] doubleArrayArg)
  {
    if (argTrace)
    {
      writeTrace(className + ":" + methodName + ": Arg: "
          + Arrays.toString(doubleArrayArg));
    }
  }

  @Override
  public void arg(String className, String methodName, boolean boolArg)
  {
    if (argTrace)
    {
      writeTrace(className + ":" + methodName + ": Arg: " + boolArg);
    }
  }

  @Override
  public void arg(String className, String methodName, boolean[] boolArrayArg)
  {
    if (argTrace)
    {
      writeTrace(className + ":" + methodName + ": Arg: "
          + Arrays.toString(boolArrayArg));
    }
  }

  @Override
  public void arg(String className, String methodName, char charArg)
  {
    if (argTrace)
    {
      writeTrace(className + ":" + methodName + ": Arg: " + charArg);
    }
  }

  @Override
  public void arg(String className, String methodName, char[] charArrayArg)
  {
    if (argTrace)
    {
      writeTrace(className + ":" + methodName + ": Arg: "
          + Arrays.toString(charArrayArg));
    }
  }

  @Override
  public void arg(String className, String methodName, Object objArg)
  {
    if (argTrace)
    {
      writeTrace(className + ":" + methodName + ": Arg: " + objArg);
    }
  }

  public void arg(String className, String methodName, Object[] objArrayArg)
  {
    if (argTrace)
    {
      writeTrace(className + ":" + methodName + ": Arg: "
          + Arrays.toString(objArrayArg));
    }
  }

  @Override
  public void branch(String className, String methodName, int lineNo)
  {
    if (branchTrace)
    {
      writeTrace(className + ":" + methodName + ": /:" + lineNo);
    }
  }

  @Override
  public void enter(String className, String methodName)
  {
    if (entryExitTrace)
    {
      writeTrace(className + ":" + methodName + ": {");
    }
  }

  @Override
  public void exit(String className, String methodName, int lineNo)
  {
    if (entryExitTrace)
    {
      writeTrace(className + ":" + methodName + ": }:" + lineNo);
    }
  }
}
