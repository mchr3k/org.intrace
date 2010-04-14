package org.intrace.agentbug.agent;

import java.lang.instrument.Instrumentation;

/**
 * Test Agent
 */
public class Agent
{
  /**
   * Agent called on JVM init.
   * @param agentArgs
   * @param inst
   * @throws IOException
   */
  public static void premain(String agentArgs, Instrumentation inst)
  {
    System.out.println("Loaded Agent.");
  }
}

