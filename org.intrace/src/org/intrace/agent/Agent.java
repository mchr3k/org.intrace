package org.intrace.agent;

import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.intrace.agent.server.AgentClientConnection;
import org.intrace.agent.server.AgentServer;
import org.intrace.output.AgentHelper;
import org.intrace.output.IInstrumentationHandler;
import org.intrace.output.trace.TraceHandler;

/**
 * InTrace Agent: Installs a Class Transformer to instrument class bytecode. The
 * Instrumentation adds calls to {@link AgentHelper} which allows for
 * {@link IInstrumentationHandler}s to generate output.
 */
public class Agent
{
  /**
   * Entry point when loaded using -agent command line arg.
   * 
   * @param agentArgs
   * @param inst
   */
  public static void premain(String agentArgs, Instrumentation inst)
  {
    initialize(agentArgs, inst);
  }

  /**
   * Entry point when loaded into running JVM.
   * 
   * @param agentArgs
   * @param inst
   */
  public static void agentmain(String agentArgs, Instrumentation inst)
  {
    initialize(agentArgs, inst);
  }

  /**
   * Common init function.
   * 
   * @param agentArgs
   * @param inst
   */
  private static void initialize(String agentArgs, Instrumentation inst)
  {
    System.out.println("## Loaded InTrace Agent.");

    if (agentArgs == null)
    {
      agentArgs = "";
    }

    // Setup the trace instrumentation handler
    AgentHelper.instrumentationHandler = TraceHandler.INSTANCE;

    // Parse startup args
    AgentSettings args = new AgentSettings(agentArgs);
    AgentHelper.getResponses(null, agentArgs);

    // Construct Transformer
    ClassTransformer t = new ClassTransformer(inst, args);
    inst.addTransformer(t, true);

    // Ensure loaded classes are traced
    t.instrumentKlasses(t.getLoadedClassesForModification());
    
    // Start Server thread
    new AgentServer(t, args.getServerPort()).start();
    
    // Store server port
    waitForServerPort();
    args.setActualServerPort(serverPort);
    
    // Wait for callback connection
    if (args.getCallbackPort() > -1)
    {
      System.out.println("## Establishing Callback Connection...");
      doCallbackConnection(args.getCallbackPort(), t);
    }
    
    // Wait for startup
    if (args.isWaitStart())
    {
      try
      {
        System.out.println("## Program Paused");
        AgentServer.waitForStartSignal();
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
    }
  }
  
  private static int serverPort = -1;
  
  public static synchronized void setServerPort(int xiServerPort)
  {
    serverPort = xiServerPort;
    Agent.class.notifyAll();
  }
  
  private static synchronized void waitForServerPort()  
  {
    try
    {
      Agent.class.wait();
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  private static void doCallbackConnection(int callbackPort, ClassTransformer t)
  {
    try
    {
      Socket callback = new Socket();
      callback.connect(new InetSocketAddress("localhost", callbackPort));
      AgentClientConnection clientConnection = new AgentClientConnection(
                                                                         callback,
                                                                         t);
      AgentServer.addClientConnection(clientConnection);
      clientConnection.start(1);
      clientConnection.waitForTraceConn();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}