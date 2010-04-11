package org.intrace.shared;

import java.util.HashSet;
import java.util.Set;

public class TraceConfigConstants
{
  public static final String ENTRY_EXIT = "[trace-ee-";
  public static final String BRANCH     = "[trace-branch-";
  public static final String ARG        = "[trace-args-";
  public static final Set<String> COMMANDS = new HashSet<String>();
  static
  {
    COMMANDS.add(ENTRY_EXIT + "<true/false>");
    COMMANDS.add(BRANCH + "<true/false>");
    COMMANDS.add(ARG + "<true/false>");
  }
}
