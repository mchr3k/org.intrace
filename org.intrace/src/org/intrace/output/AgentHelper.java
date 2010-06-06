package org.intrace.output;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
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
    outputSettings.parseArgs(args);

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

  public static void enter(String className, String methodName, int lineNo)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.enter(className, methodName, lineNo);
    }
  }

  public static void val(String desc, String className, String methodName,
                         byte byteArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.val(desc, className, methodName, byteArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         byte[] byteArrayArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.val(desc, className, methodName, byteArrayArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         short shortArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.val(desc, className, methodName, shortArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         short[] shortArrayArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.val(desc, className, methodName, shortArrayArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         int intArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.val(desc, className, methodName, intArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         int[] intArrayArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.val(desc, className, methodName, intArrayArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         long longArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.val(desc, className, methodName, longArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         long[] longArrayArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.val(desc, className, methodName, longArrayArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         float floatArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.val(desc, className, methodName, floatArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         float[] floatArrayArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.val(desc, className, methodName, floatArrayArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         double doubleArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.val(desc, className, methodName, doubleArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         double[] doubleArrayArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.val(desc, className, methodName, doubleArrayArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         boolean boolArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.val(desc, className, methodName, boolArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         boolean[] boolArrayArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.val(desc, className, methodName, boolArrayArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         char charArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.val(desc, className, methodName, charArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         char[] charArrayArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.val(desc, className, methodName, charArrayArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         Object objArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.val(desc, className, methodName, objArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         Object[] objArrayArg)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.val(desc, className, methodName, objArrayArg);
    }
  }

  public static void caught(String className, String methodName, int lineNo,
                            Throwable throwable)
  {
    for (IInstrumentationHandler outputHandler : instrumentationHandlers
                                                                        .keySet())
    {
      outputHandler.caught(className, methodName, lineNo, throwable);
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
