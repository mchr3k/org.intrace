package org.intrace.client.gui.helper;

public class ClientStrings
{
  public static final String SET_CLASSREGEX = "Select Classes ...";
  public static final String ENABLE_ALLOWJARS = "Instru JARs";
  public static final String ENABLE_SAVECLASSES = "Save Classes";
  public static final String ENABLE_VERBOSEMODE = "Verbose Mode";
  public static final String LIST_MODIFIED_CLASSES = "List Instrumented Classes";

  public static final String ENABLE_EE_TRACE = "Entry/Exit";
  public static final String ENABLE_BRANCH_TRACE = "Branch";
  public static final String ENABLE_ARGS_TRACE = "Method Args, Return Values, Exceptions";
  public static final String ENABLE_ARRAY_TRACE = "Truncate Array Args";

  public static final String BEGIN_CAPTURE_CALLERS = "New Callers Capture...";
  public static final String END_CAPTURE_CALLERS = "End Callers Capture";

  public static final String ENABLE_STDOUT_OUTPUT = "StdOut";
  public static final String ENABLE_FILE_OUTPUT = "File";
  public static final String ENABLE_NETWORK_OUTPUT = "Collect Trace";
  public static final String ENABLE_FILTER = "Filter";

  public static final String DISCARD_FILTERED = "Discard Filtered Trace";
  public static final String DISCARD_EXCESS = "Discard Excess Trace";
  
  public static final String CONNECT = "Connect";
  public static final String CONNECTING = "Connect...";
  public static final String DISCONNECT = "Disconnect";
  public static final String CONN_ADDRESS = "Address:";
  public static final String CONN_PORT = "Port:";
  public static final String CONN_STATUS = "Status: ";

  public static final String DUMP_SETTINGS = "Dump Settings";

  public static final String CAPTURE_END = "End Capture";
  public static final String CAPTURE_CLOSE = "Close";

  public static final String CLEAR_TEXT = "Clear";
  public static final String AUTO_SCROLL = "Auto Scroll";
  public static final String FILTER_TEXT = "Filter...";
  public static final String CANCEL_TEXT = "Cancel";
  public static final String SAVE_TEXT = "Save...";
  
  public static final String CLASS_TITLE = "Classes to Instrument";
  public static final String CLASS_HELP_TEXT = "Enter complete or partial class names.\n\n "
  + "e.g.\n"
  + "\"mypack.mysubpack.MyClass\"\n"
  + "\"MyClass\"";
  
  public static final String FILTER_HELP_TEXT = "Enter text to match against trace lines. " +
      "You can match any part of the line. " +
      "\n\nYou can also select some text and right click the " +
      "selection to quickly add an include or exclude filter.\n";
}
