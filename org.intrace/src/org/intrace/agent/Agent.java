package org.intrace.agent;


import java.io.IOException;
import java.lang.instrument.Instrumentation;

import org.intrace.agent.server.AgentServer;
import org.intrace.output.AgentHelper;
import org.intrace.output.callers.CallersOutput;
import org.intrace.output.trace.TraceOutput;

/**
 * Trace Agent: Installs a Class Transformer to add trace lines.
 */
public class Agent
{
  /**
   * Agent called on JVM init.
   * @param agentArgs
   * @param inst
   * @throws IOException
   */
  public static void premain(String agentArgs, Instrumentation inst) throws IOException
  {
    initialize(agentArgs, inst);
  }

  /**
   * Agent called after JVM init.
   * @param agentArgs
   * @param inst
   * @throws IOException
   */
  public static void agentmain(String agentArgs, Instrumentation inst) throws IOException
  {
    initialize(agentArgs, inst);
  }

  /**
   * Common init function.
   * @param agentArgs
   * @param inst
   * @throws IOException
   */
  private static void initialize(String agentArgs, Instrumentation inst) throws IOException
  {
    System.out.println("Loaded Tracing Agent.");

    if (agentArgs == null) agentArgs = "";

    AgentHelper.outputHandlers.put(new TraceOutput(), new Object());
    AgentHelper.outputHandlers.put(new CallersOutput(), new Object());

    AgentSettings args = new AgentSettings(agentArgs);
    AgentHelper.getResponses(agentArgs);

    ClassTransformer t = new ClassTransformer(inst, args);
    inst.addTransformer(t, true);
    t.traceLoadedClasses();

    Thread traceServer = new Thread(new AgentServer(t));
    traceServer.setName("TraceServer");
    traceServer.setDaemon(true);
    traceServer.start();
  }
}
