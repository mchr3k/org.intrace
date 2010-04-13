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
 * Static implementation of the {@link IInstrumentationHandler} interface
 */
public class AgentHelper
{
  // Set of output handlers
  public static final Map<IInstrumentationHandler, Object> instrumentationHandlers = new ConcurrentHashMap<IInstrumentationHandler, Object>();

  // Output Settings
  private static OutputSettings outputSettings = new OutputSettings("");

  // Flag to indicate whether file output is currently going to file1 or file2
  private static boolean file1Active = true;

  // Variable for tracking the number of bytes written to the output files
  private static int writtenChars = 0;
  private static final int MAX_CHARS_PER_FILE = 100 * 1000; // 100kb

  // Set of active network output threads
  private static final Map<NetworkDataSenderThread, Object> networkOutputThreads = new ConcurrentHashMap<NetworkDataSenderThread, Object>();

  /**
   * @param agentArgs
   * @return A List of responses from all of the {@link IInstrumentationHandler}
   *         s and the {@link AgentHelper} itself.
   */
  public static List<String> getResponses(String agentArgs)
  {
    List<String> responses = new ArrayList<String>();

    // Get the response from the AgentHelper
    String response = getResponse(agentArgs);
    if (response != null)
    {
      responses.add(response);
    }

    // Get responses from all of the IInstrumentationHandlers
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      response = outputHandler.getResponse(agentArgs);
      if (response != null)
      {
        responses.add(response);
      }
    }
    return responses;
  }

  /**
   * @param args
   * @return The response to the given args or null if no response is required.
   *         The only response currently implemented is sending back the local
   *         port for a new network data connection.
   */
  private static String getResponse(String args)
  {
    boolean oldStdOutEnabled = outputSettings.isStdoutTraceOutputEnabled();
    boolean oldFileOutEnabled = outputSettings.isFileTraceOutputEnabled();
    outputSettings.parseArgs(args);

    if ((oldStdOutEnabled != outputSettings.isStdoutTraceOutputEnabled())
        || (oldFileOutEnabled != outputSettings.isFileTraceOutputEnabled()))
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
        NetworkDataSenderThread networkOutputThread = new NetworkDataSenderThread(
                                                                                  networkSocket);

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

  /**
   * @return All of the currently active settings for the {@link AgentHelper}
   *         along with all of the active {@link IInstrumentationHandler}s
   */
  public static Map<String, String> getSettings()
  {
    Map<String, String> settings = new HashMap<String, String>();
    settings.putAll(outputSettings.getSettingsMap());
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      settings.putAll(outputHandler.getSettingsMap());
    }
    return settings;
  }

  /**
   * Write output to zero or more of the following.
   * <ul>
   * <li>StdOut
   * <li>FileOut
   * <li>NetworkOut
   * </ul>
   * 
   * @param xiOutput
   */
  public static void writeOutput(String xiOutput)
  {
    SimpleDateFormat dateFormat = new SimpleDateFormat();
    long threadID = Thread.currentThread().getId();
    String traceString = "[" + dateFormat.format(new Date()) + "]:[" + threadID
                         + "]:" + xiOutput;
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
   * Write data output to all network data connections.
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
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.enter(className, methodName, lineNo);
    }
  }

  public static void arg(String className, String methodName, byte byteArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.arg(className, methodName, byteArg);
    }
  }

  public static void arg(String className, String methodName,
                         byte[] byteArrayArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.arg(className, methodName, byteArrayArg);
    }
  }

  public static void arg(String className, String methodName, short shortArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.arg(className, methodName, shortArg);
    }
  }

  public static void arg(String className, String methodName,
                         short[] shortArrayArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.arg(className, methodName, shortArrayArg);
    }
  }

  public static void arg(String className, String methodName, int intArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.arg(className, methodName, intArg);
    }
  }

  public static void arg(String className, String methodName, int[] intArrayArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.arg(className, methodName, intArrayArg);
    }
  }

  public static void arg(String className, String methodName, long longArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.arg(className, methodName, longArg);
    }
  }

  public static void arg(String className, String methodName,
                         long[] longArrayArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.arg(className, methodName, longArrayArg);
    }
  }

  public static void arg(String className, String methodName, float floatArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.arg(className, methodName, floatArg);
    }
  }

  public static void arg(String className, String methodName,
                         float[] floatArrayArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.arg(className, methodName, floatArrayArg);
    }
  }

  public static void arg(String className, String methodName, double doubleArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.arg(className, methodName, doubleArg);
    }
  }

  public static void arg(String className, String methodName,
                         double[] doubleArrayArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.arg(className, methodName, doubleArrayArg);
    }
  }

  public static void arg(String className, String methodName, boolean boolArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.arg(className, methodName, boolArg);
    }
  }

  public static void arg(String className, String methodName,
                         boolean[] boolArrayArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.arg(className, methodName, boolArrayArg);
    }
  }

  public static void arg(String className, String methodName, char charArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.arg(className, methodName, charArg);
    }
  }

  public static void arg(String className, String methodName,
                         char[] charArrayArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.arg(className, methodName, charArrayArg);
    }
  }

  public static void arg(String className, String methodName, Object objArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.arg(className, methodName, objArg);
    }
  }

  public static void arg(String className, String methodName,
                         Object[] objArrayArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.arg(className, methodName, objArrayArg);
    }
  }

  public static void branch(String className, String methodName, int lineNo)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.branch(className, methodName, lineNo);
    }
  }

  public static void exit(String className, String methodName, int lineNo)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.exit(className, methodName, lineNo);
    }
  }
}
