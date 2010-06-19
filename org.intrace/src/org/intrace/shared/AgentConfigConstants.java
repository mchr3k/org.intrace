package org.intrace.shared;

import java.util.HashSet;
import java.util.Set;

public class AgentConfigConstants
{
  public static final String NUM_TOTAL_CLASSES = "NUM_TOTAL_CLASSES";
  public static final String NUM_INSTR_CLASSES = "NUM_INSTR_CLASSES";

  public static final String CLASS_REGEX = "[regex-";
  public static final String EXCLUDE_CLASS_REGEX = "[exclude-regex-";
  public static final String INSTRU_ENABLED = "[instru-";
  public static final String SAVE_TRACED_CLASSFILES = "[saveinstru-";
  public static final String VERBOSE_MODE = "[verbose-";
  public static final String ALLOW_JARS_TO_BE_TRACED = "[instrujars-";
  public static final Set<String> COMMANDS = new HashSet<String>();
  static
  {
    COMMANDS.add(CLASS_REGEX + "<regex>");
    COMMANDS.add(EXCLUDE_CLASS_REGEX + "<regex>");
    COMMANDS.add(INSTRU_ENABLED + "<true/false>");
    COMMANDS.add(SAVE_TRACED_CLASSFILES + "<true/false>");
    COMMANDS.add(VERBOSE_MODE + "<true/false>");
    COMMANDS.add(ALLOW_JARS_TO_BE_TRACED + "<true/false>");
  }
}
