package org.intrace.output.trace;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import org.intrace.output.AgentHelper;
import org.intrace.output.IInstrumentationHandler;

/**
 * Implements Standard Output Tracing
 */
public class TraceHandler implements IInstrumentationHandler
{
  public static final TraceHandler INSTANCE = new TraceHandler();
  private TraceHandler()
  {
    // Private constructor
  }
  
  private boolean entryExitTrace = true;
  private boolean branchTrace = false;
  private boolean argTrace = false;

  private static final TraceSettings traceSettings = new TraceSettings("");

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

  private String getArrayLenStr(Object array)
  {
    String lRet = "";
    if (array != null)
    {
      lRet = "Len:" + Array.getLength(array) + " ";
    }
    return lRet;
  }
  
  @Override
  public void val(String desc, String className, String methodName, byte byteArg)
  {
    if (argTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + byteArg);
    }
  }

  public void val(String desc, String className, String methodName,
                  byte[] byteArrayArg)
  {
    if (argTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + getArrayLenStr(byteArrayArg) + Arrays.toString(byteArrayArg));
    }
  }

  @Override
  public void val(String desc, String className, String methodName,
                  short shortArg)
  {
    if (argTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + shortArg);
    }
  }

  public void val(String desc, String className, String methodName,
                  short[] shortArrayArg)
  {
    if (argTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + getArrayLenStr(shortArrayArg) + Arrays.toString(shortArrayArg));
    }
  }

  @Override
  public void val(String desc, String className, String methodName, int intArg)
  {
    if (argTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + intArg);
    }
  }

  @Override
  public void val(String desc, String className, String methodName,
                  int[] intArrayArg)
  {
    if (argTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + getArrayLenStr(intArrayArg) + Arrays.toString(intArrayArg));
    }
  }

  @Override
  public void val(String desc, String className, String methodName, long longArg)
  {
    if (argTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + longArg);
    }
  }

  @Override
  public void val(String desc, String className, String methodName,
                  long[] longArrayArg)
  {
    if (argTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + getArrayLenStr(longArrayArg) + Arrays.toString(longArrayArg));
    }
  }

  @Override
  public void val(String desc, String className, String methodName,
                  float floatArg)
  {
    if (argTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + floatArg);
    }
  }

  @Override
  public void val(String desc, String className, String methodName,
                  float[] floatArrayArg)
  {
    if (argTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + getArrayLenStr(floatArrayArg) + Arrays.toString(floatArrayArg));
    }
  }

  @Override
  public void val(String desc, String className, String methodName,
                  double doubleArg)
  {
    if (argTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + doubleArg);
    }
  }

  @Override
  public void val(String desc, String className, String methodName,
                  double[] doubleArrayArg)
  {
    if (argTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + getArrayLenStr(doubleArrayArg) + Arrays.toString(doubleArrayArg));
    }
  }

  @Override
  public void val(String desc, String className, String methodName,
                  boolean boolArg)
  {
    if (argTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + boolArg);
    }
  }

  @Override
  public void val(String desc, String className, String methodName,
                  boolean[] boolArrayArg)
  {
    if (argTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + getArrayLenStr(boolArrayArg) + Arrays.toString(boolArrayArg));
    }
  }

  @Override
  public void val(String desc, String className, String methodName, char charArg)
  {
    if (argTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + charArg);
    }
  }

  @Override
  public void val(String desc, String className, String methodName,
                  char[] charArrayArg)
  {
    if (argTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + getArrayLenStr(charArrayArg) + Arrays.toString(charArrayArg));
    }
  }

  private static final String ESCAPE_PATTERN_STR = "[\\x00-\\x1F\\x7F&&[^\\r\\n]]";
  private static final Pattern ESCAPE_PATTERN = Pattern.compile(ESCAPE_PATTERN_STR);
  private static final String ESCAPE_REPLACEMENT = "\u25A1";
  
  @Override
  public void val(String desc, String className, String methodName,
                  Object objArg)
  {
    if (argTrace)
    {
      String objStr;
      if (objArg.getClass().isArray())
      {
        // Array return values pass through this arm so we must do something
        // a bit special - use Arrays.deepToString and discard the surrounding
        // [] that we add.
        objStr = Arrays.deepToString(new Object[] { objArg });
        objStr = objStr.substring(1, objStr.length() - 1);
      }
      else
      {
        objStr = (objArg != null ? objArg.toString() : "null");
      }
      objStr = ESCAPE_PATTERN.matcher(objStr).replaceAll(ESCAPE_REPLACEMENT);
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + objStr);
    }
  }

  public void val(String desc, String className, String methodName,
                  Object[] objArrayArg)
  {
    if (argTrace)
    {
      String objStr = Arrays.deepToString(objArrayArg);
      objStr = ESCAPE_PATTERN.matcher(objStr).replaceAll(ESCAPE_REPLACEMENT);
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + getArrayLenStr(objArrayArg) + objStr);
    }
  }

  @Override
  public void branch(String className, String methodName, int lineNo)
  {
    if (branchTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": /:" + lineNo);
    }
  }

  public void val(String desc, String className, String methodName, int lineNo,
                  Throwable throwable)
  {
    if (branchTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ":"
                       + lineNo + ": " + throwableToString(throwable));
    }
  }

  private String throwableToString(Throwable throwable)
  {
    StringBuilder throwToStr = new StringBuilder();
    if (throwable == null)
    {
      throwToStr.append("null");
    }
    else
    {
      StringWriter strWriter = new StringWriter();
      PrintWriter writer = new PrintWriter(strWriter);
      throwable.printStackTrace(writer);
      throwToStr.append(strWriter.toString());
    }
    return throwToStr.toString();
  }

  @Override
  public void enter(String className, String methodName, int lineNo)
  {
    if (entryExitTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": {:" + lineNo);
    }
  }

  @Override
  public void exit(String className, String methodName, int lineNo)
  {
    if (entryExitTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": }:" + lineNo);
    }
  }

  /**
   * Write output to zero or more of the following.
   * <ul>
   * <li>StdOut
   * <li>FileOut
   * <li>NetworkOut
   * </ul>
   * 
   * @param xiOutput
   */
  public void writeTraceOutput(String xiOutput)
  {
    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    long threadID = Thread.currentThread().getId();
    String traceString = "[" + dateFormat.format(new Date()) + "]:[" + threadID
                         + "]:" + xiOutput;
    if (AgentHelper.getOutputSettings().isStdoutOutputEnabled())
    {
      System.out.println(traceString);
    }

    if (AgentHelper.getOutputSettings().isFileOutputEnabled())
    {
      AgentHelper.getOutputSettings().writeFileOutput(traceString);
    }

    if (AgentHelper.getOutputSettings().isNetOutputEnabled())
    {
      AgentHelper.writeDataOutput(traceString);
    }
  }
}
