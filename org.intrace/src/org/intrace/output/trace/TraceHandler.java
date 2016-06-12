package org.intrace.output.trace;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.intrace.output.AgentHelper;
import org.intrace.output.IInstrumentationHandler;

/**
 * Implements Standard Output Tracing
 */
public class TraceHandler implements IInstrumentationHandler
{
	/**
	 * Including the period at the end enables code in "org.intracetest" to be included in the trace
	 */
  private static final String INTRACE_PACKAGE = "org.intrace.";
  private static final String THREAD = "java.lang.Thread";
  private static final String GET_STACK_TRACE = "getStackTrace";
  public static final TraceHandler INSTANCE = new TraceHandler();
  private TraceHandler()
  {
    // Private constructor
  }

  private boolean entryExitTrace = true;
  private boolean branchTrace = false;
  private boolean argTrace = true;
  private boolean truncateArrays = true;
  private boolean exitStackTrace = false;

  private static final TraceSettings traceSettings = new TraceSettings("");

  @Override
  public String getResponse(String args)
  {
//    TraceSettings oldSettings = new TraceSettings(traceSettings);
    traceSettings.parseArgs(args);

//    if ((oldSettings.isEntryExitTraceEnabled() != traceSettings
//        .isEntryExitTraceEnabled())
//        || (oldSettings.isBranchTraceEnabled() != traceSettings
//            .isBranchTraceEnabled())
//        || (oldSettings.isArgTraceEnabled() != traceSettings
//            .isArgTraceEnabled())
//        || (oldSettings.isTruncateArraysEnabled() != traceSettings
//            .isTruncateArraysEnabled()))
//    {
////      System.out.println("## Trace Settings Changed");
//    }

    entryExitTrace = traceSettings.isEntryExitTraceEnabled();
    branchTrace = traceSettings.isBranchTraceEnabled();
    argTrace = traceSettings.isArgTraceEnabled();
    truncateArrays = traceSettings.isTruncateArraysEnabled();
    exitStackTrace = traceSettings.isExitStackTraceEnabled();

    return null;
  }

  @Override
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

  private String arrayStr(String xiArrStr)
  {
    String ret = xiArrStr;
    if (truncateArrays &&
        xiArrStr.length() > 100)
    {
      ret = xiArrStr.substring(0, 100) + "...";
    }
    return ret;
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

  @Override
  public void val(String desc, String className, String methodName,
                  byte[] byteArrayArg)
  {
    if (argTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + getArrayLenStr(byteArrayArg) + arrayStr(Arrays.toString(byteArrayArg)));
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

  @Override
  public void val(String desc, String className, String methodName,
                  short[] shortArrayArg)
  {
    if (argTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + getArrayLenStr(shortArrayArg) + arrayStr(Arrays.toString(shortArrayArg)));
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
                       + getArrayLenStr(intArrayArg) + arrayStr(Arrays.toString(intArrayArg)));
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
                       + getArrayLenStr(longArrayArg) + arrayStr(Arrays.toString(longArrayArg)));
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
                       + getArrayLenStr(floatArrayArg) + arrayStr(Arrays.toString(floatArrayArg)));
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
                       + getArrayLenStr(doubleArrayArg) + arrayStr(Arrays.toString(doubleArrayArg)));
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
                       + getArrayLenStr(boolArrayArg) + arrayStr(Arrays.toString(boolArrayArg)));
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
                       + getArrayLenStr(charArrayArg) + arrayStr(Arrays.toString(charArrayArg)));
    }
  }

  private static final char ESCAPE_REPLACEMENT = '\u25A1';
  /**
   * If user has requested to see a stack trace for each 'exit' event (with the parameter [exit-stack-trace-true) , then
   * this delimiter will follow the regular event text, which will be followed by the text of the stack trace.
   */
private static final String STACK_TRACE_DELIM = "~";
/**
 * Just like Arrays.toString(Object), place a comma between each element of the stack trace
 */
private static final Object STACK_ELE_DELIM = ",";

  private String replaceChars(String xiArg)
  {
    String ret = xiArg;
    StringBuilder str = null;
    for (int ii = 0; ii < xiArg.length(); ii++)
    {
      char c = xiArg.charAt(ii);

      // Detect special char
      if ((0x00 <= c) &&
          (c <= 0x20) &&
          (c != '\r') &&
          (c != '\n'))
      {
        // Replace char
        c = ESCAPE_REPLACEMENT;

        // Setup stringbuilder
        if (str == null)
        {
          str = new StringBuilder();

          // Append any previous non special chars
          if (ii > 0)
          {
            str.append(xiArg.substring(0, ii));
          }
        }
      }

      // If we are storing chars we better write
      // this one now
      if (str != null)
      {
        str.append(c);
      }
    }

    if (str != null)
    {
      ret = str.toString();
    }

    return ret;
  }

  @Override
  public void val(String desc, String className, String methodName,
                  Object objArg)
  {
    if (argTrace)
    {
      String objStr;
      if ((objArg != null) && objArg.getClass().isArray())
      {
        // Array return values pass through this arm so we must do something
        // a bit special - use Arrays.deepToString and discard the surrounding
        // [] that we add.
        objStr = Arrays.deepToString(new Object[] { objArg });
        objStr = objStr.substring(1, objStr.length() - 1);
        objStr = arrayStr(objStr);
        objStr = replaceChars(objStr);
      }
      else
      {
        objStr = (objArg != null ? objArg.toString() : "null");
      }
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + objStr);
    }
  }

  @Override
  public void val(String desc, String className, String methodName,
                  Object[] objArrayArg)
  {
    if (argTrace)
    {
      String objStr = Arrays.deepToString(objArrayArg);
      objStr = replaceChars(objStr);
      objStr = arrayStr(objStr);
      writeTraceOutput(className + ":" + methodName + ": " + desc + ": "
                       + getArrayLenStr(objArrayArg) + objStr);
    }
  }

  @Override
  public void branch(String className, String methodName, int lineNo)
  {
    if (branchTrace)
    {
      writeTraceOutput(className + ":" + methodName + ": /" +
                       (lineNo >= 0 ? ":" + lineNo : ""));
    }
  }

  @Override
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
      writeTraceOutput(className + ":" + methodName + ": {" +
                       (lineNo >= 0 ? ":" + lineNo : ""));
    }
  }

  /**
   * Remove all "org.intrace" elements from the current stack trace and return it as string.
   * @return
   */
  public String getStackTrace() {

	  StringBuilder sb = new StringBuilder();
	  int counter = 0;
	  for(StackTraceElement ste : Thread.currentThread().getStackTrace() ) {
		  boolean disqualify = false;
		  if (ste != null) {
			  
			  if ( (ste.getClassName() !=null) &&
					(  (ste.getClassName().indexOf(INTRACE_PACKAGE) >= 0) 
					  || (ste.getClassName().indexOf(THREAD) >= 0) ) 
				)
				  disqualify = true;
				  
			  if (ste.getMethodName()!=null 
					  && ste.getMethodName().indexOf(GET_STACK_TRACE) >=0)
					  disqualify = true;
		  }
				   
		  if ( !disqualify) {
			  if (counter++>0) sb.append(STACK_ELE_DELIM);  //Just like Arrays.toString(), place a comma between each stack trace ele.
			  sb.append(ste.toString());
		  }
	  }
	  return sb.toString();
  }
  @Override
  public void exit(String className, String methodName, int lineNo)
  {
    if (entryExitTrace)
    {

        if (exitStackTrace) {
        	writeTraceOutput(className + ":" + methodName + ": }" +
                    (lineNo >= 0 ? ":" + lineNo : "") +
                    STACK_TRACE_DELIM + getStackTrace() );
        } else {
        	writeTraceOutput(className + ":" + methodName + ": }" +
                    (lineNo >= 0 ? ":" + lineNo : "") );
        }
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
//    if (AgentHelper.getOutputSettings().isStdoutOutputEnabled())
//    {
//      System.out.println(traceString);
//    }

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
