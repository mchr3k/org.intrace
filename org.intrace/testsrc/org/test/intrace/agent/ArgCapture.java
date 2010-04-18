package org.test.intrace.agent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.intrace.output.IInstrumentationHandler;

public class ArgCapture implements IInstrumentationHandler
{
  private final BlockingQueue<String> capturedTrace;

  public ArgCapture(BlockingQueue<String> capturedTrace)
  {
    this.capturedTrace = capturedTrace;
  }

  private void addArg(String arg)
  {
    capturedTrace.add(arg);
  }

  @Override
  public void arg(String className, String methodName, byte byteArg)
  {
    addArg("Arg:##:[" + className + ", " + methodName + ", " + byteArg + "]");
  }

  public void arg(String className, String methodName, byte[] byteArrayArg)
  {
    addArg("Arg:##:[" + className + ", " + methodName + ", "
           + Arrays.toString(byteArrayArg) + "]");
  }

  @Override
  public void arg(String className, String methodName, short shortArg)
  {
    addArg("Arg:##:[" + className + ", " + methodName + ", " + shortArg + "]");
  }

  public void arg(String className, String methodName, short[] shortArrayArg)
  {

    addArg("Arg:##:[" + className + ", " + methodName + ", "
           + Arrays.toString(shortArrayArg) + "]");
  }

  @Override
  public void arg(String className, String methodName, int intArg)
  {
    addArg("Arg:##:[" + className + ", " + methodName + ", " + intArg + "]");
  }

  @Override
  public void arg(String className, String methodName, int[] intArrayArg)
  {
    addArg("Arg:##:[" + className + ", " + methodName + ", "
           + Arrays.toString(intArrayArg) + "]");
  }

  @Override
  public void arg(String className, String methodName, long longArg)
  {
    addArg("Arg:##:[" + className + ", " + methodName + ", " + longArg + "]");
  }

  @Override
  public void arg(String className, String methodName, long[] longArrayArg)
  {
    addArg("Arg:##:[" + className + ", " + methodName + ", "
           + Arrays.toString(longArrayArg) + "]");
  }

  @Override
  public void arg(String className, String methodName, float floatArg)
  {
    addArg("Arg:##:[" + className + ", " + methodName + ", " + floatArg + "]");
  }

  @Override
  public void arg(String className, String methodName, float[] floatArrayArg)
  {
    addArg("Arg:##:[" + className + ", " + methodName + ", "
           + Arrays.toString(floatArrayArg) + "]");
  }

  @Override
  public void arg(String className, String methodName, double doubleArg)
  {
    addArg("Arg:##:[" + className + ", " + methodName + ", " + doubleArg + "]");
  }

  @Override
  public void arg(String className, String methodName, double[] doubleArrayArg)
  {
    addArg("Arg:##:[" + className + ", " + methodName + ", "
           + Arrays.toString(doubleArrayArg) + "]");
  }

  @Override
  public void arg(String className, String methodName, boolean boolArg)
  {
    addArg("Arg:##:[" + className + ", " + methodName + ", " + boolArg + "]");
  }

  @Override
  public void arg(String className, String methodName, boolean[] boolArrayArg)
  {
    addArg("Arg:##:[" + className + ", " + methodName + ", "
           + Arrays.toString(boolArrayArg) + "]");
  }

  @Override
  public void arg(String className, String methodName, char charArg)
  {
    addArg("Arg:##:[" + className + ", " + methodName + ", " + charArg + "]");
  }

  @Override
  public void arg(String className, String methodName, char[] charArrayArg)
  {
    addArg("Arg:##:[" + className + ", " + methodName + ", "
           + Arrays.toString(charArrayArg) + "]");
  }

  @Override
  public void arg(String className, String methodName, Object objArg)
  {
    addArg("Arg:##:[" + className + ", " + methodName + ", " + objArg + "]");
  }

  public void arg(String className, String methodName, Object[] objArrayArg)
  {
    addArg("Arg:##:[" + className + ", " + methodName + ", "
           + Arrays.deepToString(objArrayArg) + "]");
  }

  @Override
  public void branch(String className, String methodName, int lineNo)
  {
    // Do nothing
  }

  @Override
  public void enter(String className, String methodName, int lineNo)
  {
    // Do nothing
  }

  @Override
  public void exit(String className, String methodName, int lineNo)
  {
    // Do nothing
  }

  @Override
  public String getResponse(String args)
  {
    return null;
  }

  @Override
  public Map<String, String> getSettingsMap()
  {
    return new HashMap<String, String>();
  }
}
