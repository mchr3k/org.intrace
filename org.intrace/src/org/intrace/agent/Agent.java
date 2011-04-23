package org.intrace.agent;

import java.lang.instrument.Instrumentation;

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

    // Setup the output handlers
    AgentHelper.instrumentationHandlers.put(new TraceHandler(), new Object());

    // Parse startup args
    AgentSettings args = new AgentSettings(agentArgs);
    AgentHelper.getResponses(agentArgs);

    // Construct Transformer
    ClassTransformer t = new ClassTransformer(inst, args);
    inst.addTransformer(t, true);

    // Ensure loaded classes are traced
    t.instrumentKlasses(t.getLoadedClassesForModification());

    // Start Server thread
    new AgentServer(t).start();
  }
}