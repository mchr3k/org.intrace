package org.intrace.agent;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.jar.JarFile;

import org.intrace.output.AgentHelper;
import org.intrace.output.IInstrumentationHandler;

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

  private static void initialize(String agentArgs, Instrumentation inst)
  {
    try
    {
      // Prepare boot classpath
      String agentPath = Agent.class.getProtectionDomain().getCodeSource()
          .getLocation().getPath();
      System.out.println("Agent path: " + agentPath);
      inst.appendToBootstrapClassLoaderSearch(new JarFile(new File(agentPath)));

      // Class AgentInit in boot classloader
      Class<?> agentInit = Agent.class.getClassLoader().loadClass(
                           "org.intrace.agent.AgentInit");
      Method initMethod = agentInit.getMethod("initialize", String.class, Instrumentation.class);
      initMethod.invoke(null, agentArgs, inst);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }

  }
}