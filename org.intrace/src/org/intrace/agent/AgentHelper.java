package org.intrace.agent;

import org.intrace.output.IOutput;
import org.intrace.output.trace.TraceOutput;


/**
 * Static implementation of the TraceWriter interface
 */
public class AgentHelper
{
  private static IOutput outputHandler = new TraceOutput();
  
  public static IOutput getActiveOutputHandler()
  {
    return outputHandler;
  }
  
  public static void enter(String className, String methodName)
  {
    outputHandler.enter(className, methodName);
  }

  public static void arg(String className, String methodName, byte byteArg)
  {
    outputHandler.arg(className, methodName, byteArg);
  }
  
  public static void arg(String className, String methodName, byte[] byteArrayArg)
  {
    outputHandler.arg(className, methodName, byteArrayArg);
  }

  public static void arg(String className, String methodName, short shortArg)
  {
    outputHandler.arg(className, methodName, shortArg);
  }
  
  public static void arg(String className, String methodName, short[] shortArrayArg)
  {
    outputHandler.arg(className, methodName, shortArrayArg);
  }

  public static void arg(String className, String methodName, int intArg)
  {
    outputHandler.arg(className, methodName, intArg);
  }
  
  public static void arg(String className, String methodName, int[] intArrayArg)
  {
    outputHandler.arg(className, methodName, intArrayArg);
  }

  public static void arg(String className, String methodName, long longArg)
  {
    outputHandler.arg(className, methodName, longArg);
  }
  
  public static void arg(String className, String methodName, long[] longArrayArg)
  {
    outputHandler.arg(className, methodName, longArrayArg);
  }

  public static void arg(String className, String methodName, float floatArg)
  {
    outputHandler.arg(className, methodName, floatArg);
  }
  
  public static void arg(String className, String methodName, float[] floatArrayArg)
  {
    outputHandler.arg(className, methodName, floatArrayArg);
  }

  public static void arg(String className, String methodName, double doubleArg)
  {
    outputHandler.arg(className, methodName, doubleArg);
  }
  
  public static void arg(String className, String methodName, double[] doubleArrayArg)
  {
    outputHandler.arg(className, methodName, doubleArrayArg);
  }

  public static void arg(String className, String methodName, boolean boolArg)
  {
    outputHandler.arg(className, methodName, boolArg);
  }
  
  public static void arg(String className, String methodName, boolean[] boolArrayArg)
  {
    outputHandler.arg(className, methodName, boolArrayArg);
  }

  public static void arg(String className, String methodName, char charArg)
  {
    outputHandler.arg(className, methodName, charArg);
  }

  public static void arg(String className, String methodName, char[] charArrayArg)
  {
    outputHandler.arg(className, methodName, charArrayArg);
  }
  
  public static void arg(String className, String methodName, Object objArg)
  {
    outputHandler.arg(className, methodName, objArg);
  }
  
  public static void arg(String className, String methodName, Object[] objArrayArg)
  {
    outputHandler.arg(className, methodName, objArrayArg);
  }

  public static void branch(String className, String methodName, int lineNo)
  {
    outputHandler.branch(className, methodName, lineNo);
  }
  
  public static void exit(String className, String methodName, int lineNo)
  {
    outputHandler.exit(className, methodName, lineNo);
  }
}
