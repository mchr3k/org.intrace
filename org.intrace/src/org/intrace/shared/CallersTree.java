package org.intrace.shared;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CallersTree
{
  public CallersTree(String name)
  {
    this.name = name;
  }
  public final String name;
  public final Collection<CallersTree> callersChildren = new ConcurrentLinkedQueue<CallersTree>();
}
