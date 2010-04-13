package org.intrace.output;

import java.util.HashMap;
import java.util.Map;

public class IInstrumentationHandlerAdapter implements IInstrumentationHandler
{

  @Override
  public void arg(String className, String methodName, byte byteArg)
  {
    // Do nothing
  }

  @Override
  public void arg(String className, String methodName, byte[] byteArrayArg)
  {
    // Do nothing
  }

  @Override
  public void arg(String className, String methodName, short shortArg)
  {
    // Do nothing
  }

  @Override
  public void arg(String className, String methodName, short[] shortArrayArg)
  {
    // Do nothing
  }

  @Override
  public void arg(String className, String methodName, int intArg)
  {
    // Do nothing
  }

  @Override
  public void arg(String className, String methodName, int[] intArrayArg)
  {
    // Do nothing
  }

  @Override
  public void arg(String className, String methodName, long longArg)
  {
    // Do nothing
  }

  @Override
  public void arg(String className, String methodName, long[] longArrayArg)
  {
    // Do nothing
  }

  @Override
  public void arg(String className, String methodName, float floatArg)
  {
    // Do nothing
  }

  @Override
  public void arg(String className, String methodName, float[] floatArrayArg)
  {
    // Do nothing
  }

  @Override
  public void arg(String className, String methodName, double doubleArg)
  {
    // Do nothing
  }

  @Override
  public void arg(String className, String methodName, double[] doubleArrayArg)
  {
    // Do nothing
  }

  @Override
  public void arg(String className, String methodName, boolean boolArg)
  {
    // Do nothing
  }

  @Override
  public void arg(String className, String methodName, boolean[] boolArrayArg)
  {
    // Do nothing
  }

  @Override
  public void arg(String className, String methodName, char charArg)
  {
    // Do nothing
  }

  @Override
  public void arg(String className, String methodName, char[] charArrayArg)
  {
    // Do nothing
  }

  @Override
  public void arg(String className, String methodName, Object objArg)
  {
    // Do nothing
  }

  @Override
  public void arg(String className, String methodName, Object[] objArrayArg)
  {
    // Do nothing
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
    // Do nothing
    return null;
  }

  @Override
  public Map<String, String> getSettingsMap()
  {
    return new HashMap<String, String>();
  }
}
