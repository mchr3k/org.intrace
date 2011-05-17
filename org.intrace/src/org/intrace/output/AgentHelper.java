package org.intrace.output;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.intrace.agent.server.AgentClientConnection;

/**
 * Static implementation of the {@link IInstrumentationHandler} interface
 */
public class AgentHelper
{
  // Instrumentation handler
  public static IInstrumentationHandler instrumentationHandler;

  // Output Settings
  public static OutputSettings outputSettings = new OutputSettings("");

  // Set of active network output threads
  private static final Map<NetworkDataSenderThread, Object> networkOutputThreads = new ConcurrentHashMap<NetworkDataSenderThread, Object>();

  /**
   * @param connection 
   * @param agentArgs
   * @return A List of responses from all of the {@link IInstrumentationHandler}
   *         s and the {@link AgentHelper} itself.
   */
  public static List<String> getResponses(AgentClientConnection connection, String agentArgs)
  {
    List<String> responses = new ArrayList<String>();

    // Get the response from the AgentHelper
    String response = getResponse(connection, agentArgs);
    if (response != null)
    {
      responses.add(response);
    }

    // Get responses from all of the IInstrumentationHandlers
    if (instrumentationHandler != null)
    {
      response = instrumentationHandler.getResponse(agentArgs);
      if (response != null)
      {
        responses.add(response);
      }
    }
    return responses;
  }

  /**
   * @param connection 
   * @param args
   * @return The response to the given args or null if no response is required.
   *         The only response currently implemented is sending back the local
   *         port for a new network data connection.
   */
  private static String getResponse(AgentClientConnection connection, String args)
  {
    OutputSettings oldSettings = new OutputSettings(outputSettings);
    outputSettings.parseArgs(args);

    if ((oldSettings.isStdoutOutputEnabled() != outputSettings.isStdoutOutputEnabled())
        || (oldSettings.isFileOutputEnabled() != outputSettings
                                                                   .isFileOutputEnabled())
        || (oldSettings.isNetOutputEnabled() != outputSettings
                                                                  .isNetOutputEnabled()))
    {
      System.out.println("## Trace Settings Changed");
    }

    if (outputSettings.networkTraceOutputRequested)
    {
      if ((connection == null) || !connection.isTraceConnEstablished())
      {
        System.out.println("## Network Output Requested");
        ServerSocket networkSocket;
        try
        {
          networkSocket = new ServerSocket(0);
          NetworkDataSenderThread networkOutputThread = new NetworkDataSenderThread(connection,
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
        System.out.println("## Network Output Already Connected");
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
    if (instrumentationHandler != null)
    {
      settings.putAll(instrumentationHandler.getSettingsMap());
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
    if (instrumentationHandler != null)
    {
      instrumentationHandler.enter(className, methodName, lineNo);
    }
  }

  public static void val(String desc, String className, String methodName,
                         byte byteArg)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, byteArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         byte[] byteArrayArg)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, byteArrayArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         short shortArg)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, shortArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         short[] shortArrayArg)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, shortArrayArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         int intArg)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, intArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         int[] intArrayArg)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, intArrayArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         long longArg)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, longArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         long[] longArrayArg)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, longArrayArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         float floatArg)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, floatArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         float[] floatArrayArg)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, floatArrayArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         double doubleArg)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, doubleArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         double[] doubleArrayArg)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, doubleArrayArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         boolean boolArg)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, boolArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         boolean[] boolArrayArg)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, boolArrayArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         char charArg)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, charArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         char[] charArrayArg)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, charArrayArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         Object objArg)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, objArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         Object[] objArrayArg)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, objArrayArg);
    }
  }

  public static void val(String desc, String className, String methodName,
                         int lineNo, Throwable throwable)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.val(desc, className, methodName, lineNo, throwable);
    }
  }

  public static void branch(String className, String methodName, int lineNo)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.branch(className, methodName, lineNo);
    }
  }

  public static void exit(String className, String methodName, int lineNo)
  {
    if (instrumentationHandler != null)
    {
      instrumentationHandler.exit(className, methodName, lineNo);
    }
  }
}
