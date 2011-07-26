package org.intrace.shared;

import java.util.HashSet;
import java.util.Set;

public class TraceConfigConstants
{
  public static final String ENTRY_EXIT = "[trace-ee-";
  public static final String BRANCH = "[trace-branch-";
  public static final String ARRAYS = "[trace-truncarrays-";
  public static final String ARG = "[trace-args-";
  public static final String STD_OUT = "[out-stdout-";
  public static final String FILE_OUT = "[out-file-";
  public static final String NET_OUT = "[out-network-";
  public static final Set<String> COMMANDS = new HashSet<String>();
  static
  {
    COMMANDS.add(ENTRY_EXIT + "<true/false>");
    COMMANDS.add(BRANCH + "<true/false>");
    COMMANDS.add(ARRAYS + "<true/false>");
    COMMANDS.add(ARG + "<true/false>");
    COMMANDS.add(STD_OUT + "<true/false>");
    COMMANDS.add(FILE_OUT + "<true/false>");
    COMMANDS.add(NET_OUT + "<true/false>");
  }
}
