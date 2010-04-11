package org.intrace.shared;

import java.util.HashSet;
import java.util.Set;

public class OutputConfigConstants
{
  public static final String STD_OUT    = "[out-stdout-";
  public static final String FILE_OUT   = "[out-file-";
  public static final Set<String> COMMANDS = new HashSet<String>();
  static
  {
    COMMANDS.add(STD_OUT + "<true/false>");
    COMMANDS.add(FILE_OUT + "<true/false>");
  }
}
