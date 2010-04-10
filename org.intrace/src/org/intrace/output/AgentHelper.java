package org.intrace.output;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;



/**
 * Static implementation of the TraceWriter interface
 */
public class AgentHelper
{
  public static final Map<IOutput,Object> outputHandlers = new ConcurrentHashMap<IOutput,Object>();

  private static OutputSettings outputSettings = new OutputSettings("");
  private static boolean file1Active = true;
  private static int writtenChars = 0;
  private static final int MAX_CHARS_PER_FILE = 100 * 1000; // 100kb

  private static final Map<NetworkDataSenderThread,Object> networkOutputThreads = new ConcurrentHashMap<NetworkDataSenderThread,Object>();

  public static List<String> getResponses(String agentArgs)
  {
    List<String> responses = new ArrayList<String>();
    String response = getResponse(agentArgs);
    if (response != null)
    {
      responses.add(response);
    }
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      response = outputHandler.getResponse(agentArgs);
      if (response != null)
      {
        responses.add(response);
      }
    }
    return responses;
  }

  private static String getResponse(String args)
  {
    boolean oldStdOutEnabled = outputSettings.isStdoutTraceOutputEnabled();
    boolean oldFileOutEnabled = outputSettings.isFileTraceOutputEnabled();
    outputSettings.parseArgs(args);

    if ((oldStdOutEnabled != outputSettings.isStdoutTraceOutputEnabled()) ||
        (oldFileOutEnabled != outputSettings.isFileTraceOutputEnabled()))
    {
      System.out.println("## Output Settings Changed");
    }

    if (outputSettings.networkTraceOutputRequested)
    {
      System.out.println("## Network Output Requested");
      ServerSocket networkSocket;
      try
      {
        networkSocket = new ServerSocket(0);
        NetworkDataSenderThread networkOutputThread = new NetworkDataSenderThread(networkSocket);

        networkOutputThreads.put(networkOutputThread, new Object());

        networkOutputThread.start(networkOutputThreads.keySet());
        outputSettings.networkTraceOutputRequested = false;
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

  public static Map<String, String> getSettings()
  {
    Map<String, String> settings = new HashMap<String, String>();
    settings.putAll(outputSettings.getSettingsMap());
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      settings.putAll(outputHandler.getSettingsMap());
    }
    return settings;
  }

  /**
   * Write output
   * 
   * @param xiTrace
   */
  public static void writeOutput(String xiOutput)
  {
    SimpleDateFormat dateFormat = new SimpleDateFormat();
    long threadID = Thread.currentThread().getId();
    String traceString = "[" + dateFormat.format(new Date()) + "]:[" +
    threadID + "]:" + xiOutput;
    if (outputSettings.isStdoutTraceOutputEnabled())
    {
      System.out.println(traceString);
    }

    if (outputSettings.isFileTraceOutputEnabled())
    {
      writeFileTrace(traceString);
    }

    Set<NetworkDataSenderThread> networkThreads = networkOutputThreads.keySet();
    if (networkThreads.size() > 0)
    {
      for (NetworkDataSenderThread thread : networkThreads)
      {
        thread.queueData(traceString);
      }
    }
  }

  /**
   * Write data output
   * 
   * @param xiTrace
   */
  public static void writeDataOutput(Object xiOutput)
  {
    Set<NetworkDataSenderThread> networkThreads = networkOutputThreads.keySet();
    if (networkThreads.size() > 0)
    {
      for (NetworkDataSenderThread thread : networkThreads)
      {
        thread.queueData(xiOutput);
      }
    }
  }

  private static synchronized void writeFileTrace(String traceString)
  {
    PrintWriter outputWriter;
    if (file1Active)
    {
      outputWriter = outputSettings.getFile1TraceWriter();
    }
    else
    {
      outputWriter = outputSettings.getFile2TraceWriter();
    }
    outputWriter.println(traceString);
    outputWriter.flush();

    // Switch trace files if necessary
    writtenChars += traceString.length();
    if (writtenChars > MAX_CHARS_PER_FILE)
    {
      writtenChars = 0;
      outputSettings.resetTraceFiles(file1Active, !file1Active);
      file1Active = !file1Active;
    }
  }

  /*
   * STATIC IMPLEMENTATION OF IOutput
   */

  public static void enter(String className, String methodName, int lineNo)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.enter(className, methodName, lineNo);
    }
  }

  public static void arg(String className, String methodName, byte byteArg)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.arg(className, methodName, byteArg);
    }
  }

  public static void arg(String className, String methodName, byte[] byteArrayArg)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.arg(className, methodName, byteArrayArg);
    }
  }

  public static void arg(String className, String methodName, short shortArg)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.arg(className, methodName, shortArg);
    }
  }

  public static void arg(String className, String methodName, short[] shortArrayArg)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.arg(className, methodName, shortArrayArg);
    }
  }

  public static void arg(String className, String methodName, int intArg)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.arg(className, methodName, intArg);
    }
  }

  public static void arg(String className, String methodName, int[] intArrayArg)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.arg(className, methodName, intArrayArg);
    }
  }

  public static void arg(String className, String methodName, long longArg)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.arg(className, methodName, longArg);
    }
  }

  public static void arg(String className, String methodName, long[] longArrayArg)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.arg(className, methodName, longArrayArg);
    }
  }

  public static void arg(String className, String methodName, float floatArg)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.arg(className, methodName, floatArg);
    }
  }

  public static void arg(String className, String methodName, float[] floatArrayArg)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.arg(className, methodName, floatArrayArg);
    }
  }

  public static void arg(String className, String methodName, double doubleArg)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.arg(className, methodName, doubleArg);
    }
  }

  public static void arg(String className, String methodName, double[] doubleArrayArg)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.arg(className, methodName, doubleArrayArg);
    }
  }

  public static void arg(String className, String methodName, boolean boolArg)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.arg(className, methodName, boolArg);
    }
  }

  public static void arg(String className, String methodName, boolean[] boolArrayArg)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.arg(className, methodName, boolArrayArg);
    }
  }

  public static void arg(String className, String methodName, char charArg)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.arg(className, methodName, charArg);
    }
  }

  public static void arg(String className, String methodName, char[] charArrayArg)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.arg(className, methodName, charArrayArg);
    }
  }

  public static void arg(String className, String methodName, Object objArg)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.arg(className, methodName, objArg);
    }
  }

  public static void arg(String className, String methodName, Object[] objArrayArg)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.arg(className, methodName, objArrayArg);
    }
  }

  public static void branch(String className, String methodName, int lineNo)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.branch(className, methodName, lineNo);
    }
  }

  public static void exit(String className, String methodName, int lineNo)
  {
    for (IOutput outputHandler : outputHandlers.keySet())
    {
      outputHandler.exit(className, methodName, lineNo);
    }
  }
}
