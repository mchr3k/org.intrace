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
  public void val(String desc, String className, String methodName, byte byteArg)
  {
    addArg(desc + ":##:[" + className + ", " + methodName + ", " + byteArg + "]");
  }

  public void val(String desc, String className, String methodName, byte[] byteArrayArg)
  {
    addArg(desc + ":##:[" + className + ", " + methodName + ", "
           + Arrays.toString(byteArrayArg) + "]");
  }

  @Override
  public void val(String desc, String className, String methodName, short shortArg)
  {
    addArg(desc + ":##:[" + className + ", " + methodName + ", " + shortArg + "]");
  }

  public void val(String desc, String className, String methodName, short[] shortArrayArg)
  {

    addArg(desc + ":##:[" + className + ", " + methodName + ", "
           + Arrays.toString(shortArrayArg) + "]");
  }

  @Override
  public void val(String desc, String className, String methodName, int intArg)
  {
    addArg(desc + ":##:[" + className + ", " + methodName + ", " + intArg + "]");
  }

  @Override
  public void val(String desc, String className, String methodName, int[] intArrayArg)
  {
    addArg(desc + ":##:[" + className + ", " + methodName + ", "
           + Arrays.toString(intArrayArg) + "]");
  }

  @Override
  public void val(String desc, String className, String methodName, long longArg)
  {
    addArg(desc + ":##:[" + className + ", " + methodName + ", " + longArg + "]");
  }

  @Override
  public void val(String desc, String className, String methodName, long[] longArrayArg)
  {
    addArg(desc + ":##:[" + className + ", " + methodName + ", "
           + Arrays.toString(longArrayArg) + "]");
  }

  @Override
  public void val(String desc, String className, String methodName, float floatArg)
  {
    addArg(desc + ":##:[" + className + ", " + methodName + ", " + floatArg + "]");
  }

  @Override
  public void val(String desc, String className, String methodName, float[] floatArrayArg)
  {
    addArg(desc + ":##:[" + className + ", " + methodName + ", "
           + Arrays.toString(floatArrayArg) + "]");
  }

  @Override
  public void val(String desc, String className, String methodName, double doubleArg)
  {
    addArg(desc + ":##:[" + className + ", " + methodName + ", " + doubleArg + "]");
  }

  @Override
  public void val(String desc, String className, String methodName, double[] doubleArrayArg)
  {
    addArg(desc + ":##:[" + className + ", " + methodName + ", "
           + Arrays.toString(doubleArrayArg) + "]");
  }

  @Override
  public void val(String desc, String className, String methodName, boolean boolArg)
  {
    addArg(desc + ":##:[" + className + ", " + methodName + ", " + boolArg + "]");
  }

  @Override
  public void val(String desc, String className, String methodName, boolean[] boolArrayArg)
  {
    addArg(desc + ":##:[" + className + ", " + methodName + ", "
           + Arrays.toString(boolArrayArg) + "]");
  }

  @Override
  public void val(String desc, String className, String methodName, char charArg)
  {
    addArg(desc + ":##:[" + className + ", " + methodName + ", " + charArg + "]");
  }

  @Override
  public void val(String desc, String className, String methodName, char[] charArrayArg)
  {
    addArg(desc + ":##:[" + className + ", " + methodName + ", "
           + Arrays.toString(charArrayArg) + "]");
  }

  @Override
  public void val(String desc, String className, String methodName, Object objArg)
  {
    addArg(desc + ":##:[" + className + ", " + methodName + ", " + objArg + "]");
  }

  public void val(String desc, String className, String methodName, Object[] objArrayArg)
  {
    addArg(desc + ":##:[" + className + ", " + methodName + ", "
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
