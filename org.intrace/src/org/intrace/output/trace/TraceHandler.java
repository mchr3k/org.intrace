package org.intrace.output.trace;

import java.util.Arrays;
import java.util.Map;

import org.intrace.output.AgentHelper;
import org.intrace.output.IInstrumentationHandler;

/**
 * Implements Standard Output Tracing
 */
public class TraceHandler implements IInstrumentationHandler
{
  private boolean entryExitTrace = true;
  private boolean branchTrace = false;
  private boolean argTrace = false;

  final TraceSettings traceSettings = new TraceSettings("");

  public String getResponse(String args)
  {
    TraceSettings oldSettings = new TraceSettings(traceSettings);
    traceSettings.parseArgs(args);

    if ((oldSettings.isEntryExitTraceEnabled() != traceSettings
                                                               .isEntryExitTraceEnabled())
        || (oldSettings.isBranchTraceEnabled() != traceSettings
                                                               .isBranchTraceEnabled())
        || (oldSettings.isArgTraceEnabled() != traceSettings
                                                            .isArgTraceEnabled()))
    {
      System.out.println("## Trace Settings Changed");
    }

    entryExitTrace = traceSettings.isEntryExitTraceEnabled();
    branchTrace = traceSettings.isBranchTraceEnabled();
    argTrace = traceSettings.isArgTraceEnabled();

    return null;
  }

  public Map<String, String> getSettingsMap()
  {
    return traceSettings.getSettingsMap();
  }

  @Override
  public void val(String desc, String className, String methodName, byte byteArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": " + desc + ":"
                              + byteArg);
    }
  }

  public void val(String desc, String className, String methodName, byte[] byteArrayArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": " + desc + ":"
                              + Arrays.toString(byteArrayArg));
    }
  }

  @Override
  public void val(String desc, String className, String methodName, short shortArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": " + desc + ":"
                              + shortArg);
    }
  }

  public void val(String desc, String className, String methodName, short[] shortArrayArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": " + desc + ":"
                              + Arrays.toString(shortArrayArg));
    }
  }

  @Override
  public void val(String desc, String className, String methodName, int intArg)
  {
    if (argTrace)
    {
      AgentHelper
                 .writeOutput(className + ":" + methodName + ": " + desc + ":" + intArg);
    }
  }

  @Override
  public void val(String desc, String className, String methodName, int[] intArrayArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": " + desc + ":"
                              + Arrays.toString(intArrayArg));
    }
  }

  @Override
  public void val(String desc, String className, String methodName, long longArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": " + desc + ":"
                              + longArg);
    }
  }

  @Override
  public void val(String desc, String className, String methodName, long[] longArrayArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": " + desc + ":"
                              + Arrays.toString(longArrayArg));
    }
  }

  @Override
  public void val(String desc, String className, String methodName, float floatArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": " + desc + ":"
                              + floatArg);
    }
  }

  @Override
  public void val(String desc, String className, String methodName, float[] floatArrayArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": " + desc + ":"
                              + Arrays.toString(floatArrayArg));
    }
  }

  @Override
  public void val(String desc, String className, String methodName, double doubleArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": " + desc + ":"
                              + doubleArg);
    }
  }

  @Override
  public void val(String desc, String className, String methodName, double[] doubleArrayArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": " + desc + ":"
                              + Arrays.toString(doubleArrayArg));
    }
  }

  @Override
  public void val(String desc, String className, String methodName, boolean boolArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": " + desc + ":"
                              + boolArg);
    }
  }

  @Override
  public void val(String desc, String className, String methodName, boolean[] boolArrayArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": " + desc + ":"
                              + Arrays.toString(boolArrayArg));
    }
  }

  @Override
  public void val(String desc, String className, String methodName, char charArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": " + desc + ":"
                              + charArg);
    }
  }

  @Override
  public void val(String desc, String className, String methodName, char[] charArrayArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": " + desc + ":"
                              + Arrays.toString(charArrayArg));
    }
  }

  @Override
  public void val(String desc, String className, String methodName, Object objArg)
  {
    // Array return values pass through this arm so we must do something
    // a bit special - use Arrays.deepToString and discard the surrounding
    // [] that we add.
    if (argTrace)
    {
      String objStr = Arrays.deepToString(new Object[] {objArg});
      objStr = objStr.substring(1, objStr.length() - 1);
      AgentHelper
                 .writeOutput(className + ":" + methodName + ": " + desc + ":" + objStr);
    }
  }

  public void val(String desc, String className, String methodName, Object[] objArrayArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": " + desc + ":"
                              + Arrays.deepToString(objArrayArg));
    }
  }

  @Override
  public void branch(String className, String methodName, int lineNo)
  {
    if (branchTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": /:" + lineNo);
    }
  }

  @Override
  public void enter(String className, String methodName, int lineNo)
  {
    if (entryExitTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": {:" + lineNo);
    }
  }

  @Override
  public void exit(String className, String methodName, int lineNo)
  {
    if (entryExitTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": }:" + lineNo);
    }
  }
}
