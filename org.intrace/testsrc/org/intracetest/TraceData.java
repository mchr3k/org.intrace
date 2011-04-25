package org.intracetest;

import java.util.ArrayList;
import java.util.List;

public class TraceData
{
  public boolean seenEnter;
  public boolean seenExit;
  public final List<Integer> branchLines = new ArrayList<Integer>();
  public final List<Integer> caughtLines = new ArrayList<Integer>();
  public final List<String> args = new ArrayList<String>();
}
