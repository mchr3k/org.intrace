package org.intrace.output.trace;


import java.util.Arrays;
import java.util.Map;

import org.intrace.output.AgentHelper;
import org.intrace.output.IOutput;

/**
 * Implements Standard Output Tracing
 */
public class TraceOutput implements IOutput
{
  private boolean entryExitTrace = true;
  private boolean branchTrace = false;
  private boolean argTrace = false;

  final TraceSettings traceSettings = new TraceSettings("");

  public String getResponse(String args)
  {
    TraceSettings oldSettings = new TraceSettings(traceSettings);
    traceSettings.parseArgs(args);

    if ((oldSettings.isEntryExitTraceEnabled() != traceSettings.isEntryExitTraceEnabled()) ||
        (oldSettings.isBranchTraceEnabled() != traceSettings.isBranchTraceEnabled()) ||
        (oldSettings.isArgTraceEnabled() != traceSettings.isArgTraceEnabled()))
    {
      System.out.println("## Trace Settings Changed");
    }

    entryExitTrace = traceSettings.isEntryExitTraceEnabled();
    branchTrace = traceSettings.isBranchTraceEnabled();
    argTrace = traceSettings.isArgTraceEnabled();

    return null;
  }

  public Map<String,String> getSettingsMap()
  {
    return traceSettings.getSettingsMap();
  }

  @Override
  public void arg(String className, String methodName, byte byteArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": Arg: " + byteArg);
    }
  }

  public void arg(String className, String methodName, byte[] byteArrayArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": Arg: "
                              + Arrays.toString(byteArrayArg));
    }
  }

  @Override
  public void arg(String className, String methodName, short shortArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": Arg: " + shortArg);
    }
  }

  public void arg(String className, String methodName, short[] shortArrayArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": Arg: "
                              + Arrays.toString(shortArrayArg));
    }
  }

  @Override
  public void arg(String className, String methodName, int intArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": Arg: " + intArg);
    }
  }

  @Override
  public void arg(String className, String methodName, int[] intArrayArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": Arg: "
                              + Arrays.toString(intArrayArg));
    }
  }

  @Override
  public void arg(String className, String methodName, long longArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": Arg: " + longArg);
    }
  }

  @Override
  public void arg(String className, String methodName, long[] longArrayArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": Arg: "
                              + Arrays.toString(longArrayArg));
    }
  }

  @Override
  public void arg(String className, String methodName, float floatArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": Arg: " + floatArg);
    }
  }

  @Override
  public void arg(String className, String methodName, float[] floatArrayArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": Arg: "
                              + Arrays.toString(floatArrayArg));
    }
  }

  @Override
  public void arg(String className, String methodName, double doubleArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": Arg: " + doubleArg);
    }
  }

  @Override
  public void arg(String className, String methodName, double[] doubleArrayArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": Arg: "
                              + Arrays.toString(doubleArrayArg));
    }
  }

  @Override
  public void arg(String className, String methodName, boolean boolArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": Arg: " + boolArg);
    }
  }

  @Override
  public void arg(String className, String methodName, boolean[] boolArrayArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": Arg: "
                              + Arrays.toString(boolArrayArg));
    }
  }

  @Override
  public void arg(String className, String methodName, char charArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": Arg: " + charArg);
    }
  }

  @Override
  public void arg(String className, String methodName, char[] charArrayArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": Arg: "
                              + Arrays.toString(charArrayArg));
    }
  }

  @Override
  public void arg(String className, String methodName, Object objArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": Arg: " + objArg);
    }
  }

  public void arg(String className, String methodName, Object[] objArrayArg)
  {
    if (argTrace)
    {
      AgentHelper.writeOutput(className + ":" + methodName + ": Arg: "
                              + Arrays.toString(objArrayArg));
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
