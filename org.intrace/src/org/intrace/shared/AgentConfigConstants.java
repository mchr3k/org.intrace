package org.intrace.shared;

import java.util.HashSet;
import java.util.Set;

public class AgentConfigConstants
{
  public static final String STID = "STID";
  public static final String STCLS = "STCLS";
  public static final String STINST = "STINST";

  public static final String NUM_PROGRESS_ID = "NUM_PROGRESS_ID";
  public static final String NUM_PROGRESS_COUNT = "NUM_PROGRESS_COUNT";
  public static final String NUM_PROGRESS_TOTAL = "NUM_PROGRESS_TOTAL";
  public static final String NUM_PROGRESS_DONE = "NUM_PROGRESS_DONE";
  
  public static final String SERVER_PORT = "SERVER_PORT";

  public static final String CLASS_REGEX = "[regex-";
  public static final String EXCLUDE_CLASS_REGEX = "[exclude-regex-";
  public static final String INSTRU_ENABLED = "[instru-";
  public static final String SAVE_TRACED_CLASSFILES = "[saveinstru-";
  public static final String VERBOSE_MODE = "[verbose-";
  public static final String OPT_SERVER_PORT = "[serverport-";
  public static final String CALLBACK_PORT = "[callbackport-";
  
  public static final String START_WAIT = "[startwait";
  public static final String START_ACTIVATE = "[startactivate";
  
  public static final Set<String> COMMANDS = new HashSet<String>();
  static
  {
    COMMANDS.add(CLASS_REGEX + "<regex>");
    COMMANDS.add(EXCLUDE_CLASS_REGEX + "<regex>");
    COMMANDS.add(INSTRU_ENABLED + "<true/false>");
    COMMANDS.add(SAVE_TRACED_CLASSFILES + "<true/false>");
    COMMANDS.add(VERBOSE_MODE + "<true/false>");
    COMMANDS.add(OPT_SERVER_PORT + "<int>");
    COMMANDS.add(CALLBACK_PORT + "<int>");
  }
}
