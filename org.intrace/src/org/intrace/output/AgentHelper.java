package org.intrace.output;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
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
  private static IInstrumentationHandler instrumentationHandler;
  
  public static void setInstrumentationHandler(IInstrumentationHandler handler)
  {
    instrumentationHandler = handler;
  }

  // Output Settings
  private static OutputSettings outputSettings = new OutputSettings("");
  
  public static OutputSettings getOutputSettings()
  {
    return outputSettings;
  }

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
//    OutputSettings oldSettings = new OutputSettings(outputSettings);
    outputSettings.parseArgs(args);

//    if ((oldSettings.isStdoutOutputEnabled() != outputSettings.isStdoutOutputEnabled())
//        || (oldSettings.isFileOutputEnabled() != outputSettings
//                                                                   .isFileOutputEnabled())
//        || (oldSettings.isNetOutputEnabled() != outputSettings
//                                                                  .isNetOutputEnabled()))
//    {
////      System.out.println("## Trace Settings Changed");
//    }

    if (outputSettings.networkTraceOutputRequested)
    {
      if ((connection == null) || !connection.isTraceConnEstablished())
      {
        ServerSocket networkSocket;
        try
        {
          networkSocket = new ServerSocket(0);
          NetworkDataSenderThread networkOutputThread = new NetworkDataSenderThread(connection,
                                                                                    networkSocket);  

          networkOutputThread.start(networkOutputThreads);
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
//        System.out.println("## Network Output Already Connected");
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
  
  /**
   * Allow any network output to gracefully shutdown
   */
  public static void gracefulShutdown()
  {
    Set<NetworkDataSenderThread> networkThreads = networkOutputThreads.keySet();
    if (networkThreads.size() > 0)
    {
      for (NetworkDataSenderThread thread : networkThreads)
      {
        thread.gracefulShutdown();
      }
    }
  }

  public static final CriticalBlock INSTRU_CRITICAL_BLOCK = new CriticalBlock();
  private static class CriticalBlock implements Thread.UncaughtExceptionHandler
  {
    @Override
    public void uncaughtException(Thread t, Throwable e)
    {
      // Do nothing
    }
  }
  
  public static void enter(String className, String methodName, int lineNo)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.enter(className, methodName, lineNo);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }    
  }

  public static void val(String desc, String className, String methodName,
                         byte byteArg)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.val(desc, className, methodName, byteArg);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void val(String desc, String className, String methodName,
                         byte[] byteArrayArg)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.val(desc, className, methodName, byteArrayArg);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void val(String desc, String className, String methodName,
                         short shortArg)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.val(desc, className, methodName, shortArg);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);      
      }
    }
  }

  public static void val(String desc, String className, String methodName,
                         short[] shortArrayArg)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.val(desc, className, methodName, shortArrayArg);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void val(String desc, String className, String methodName,
                         int intArg)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.val(desc, className, methodName, intArg);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void val(String desc, String className, String methodName,
                         int[] intArrayArg)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.val(desc, className, methodName, intArrayArg);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void val(String desc, String className, String methodName,
                         long longArg)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.val(desc, className, methodName, longArg);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void val(String desc, String className, String methodName,
                         long[] longArrayArg)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.val(desc, className, methodName, longArrayArg);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void val(String desc, String className, String methodName,
                         float floatArg)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.val(desc, className, methodName, floatArg);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void val(String desc, String className, String methodName,
                         float[] floatArrayArg)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.val(desc, className, methodName, floatArrayArg);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void val(String desc, String className, String methodName,
                         double doubleArg)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.val(desc, className, methodName, doubleArg);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void val(String desc, String className, String methodName,
                         double[] doubleArrayArg)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.val(desc, className, methodName, doubleArrayArg);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void val(String desc, String className, String methodName,
                         boolean boolArg)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.val(desc, className, methodName, boolArg);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void val(String desc, String className, String methodName,
                         boolean[] boolArrayArg)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.val(desc, className, methodName, boolArrayArg);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void val(String desc, String className, String methodName,
                         char charArg)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.val(desc, className, methodName, charArg);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void val(String desc, String className, String methodName,
                         char[] charArrayArg)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.val(desc, className, methodName, charArrayArg);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void val(String desc, String className, String methodName,
                         Object objArg)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);
      try
      {
        if (instrumentationHandler != null)
        {
          try
          {
            instrumentationHandler.val(desc, className, methodName, objArg);
          }
          catch (Throwable th)
          {
            instrumentationHandler.val(desc, className, methodName, "<InTrace: Exception thrown from toString() on Object arg: " + th.toString() + ">");
          }
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void val(String desc, String className, String methodName,
                         Object[] objArrayArg)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {  
        if (instrumentationHandler != null)
        {
          try
          {
            instrumentationHandler.val(desc, className, methodName, objArrayArg);
          }
          catch (Throwable th)
          {
            instrumentationHandler.val(desc, className, methodName, "<Exception thrown: " + th.toString() + ">");
          }
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void val(String desc, String className, String methodName,
                         int lineNo, Throwable throwable)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.val(desc, className, methodName, lineNo, throwable);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void branch(String className, String methodName, int lineNo)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.branch(className, methodName, lineNo);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }

  public static void exit(String className, String methodName, int lineNo)
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    if (handler != INSTRU_CRITICAL_BLOCK)
    {
      // Allow instrumentation call to proceed
      currentTh.setUncaughtExceptionHandler(INSTRU_CRITICAL_BLOCK);      
      try
      {      
        if (instrumentationHandler != null)
        {
          instrumentationHandler.exit(className, methodName, lineNo);
        }
      }
      finally
      {
        currentTh.setUncaughtExceptionHandler(handler);
      }
    }
  }
}
