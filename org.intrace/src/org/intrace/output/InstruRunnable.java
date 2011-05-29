package org.intrace.output;

import java.lang.Thread.UncaughtExceptionHandler;

public abstract class InstruRunnable implements Runnable
{
  /**
   * Wrapper work method which sets up the uncaughtexceptionhandler
   */
  @Override
  public void run()
  {
    Thread currentTh = Thread.currentThread();
    UncaughtExceptionHandler handler = currentTh.getUncaughtExceptionHandler();
    try
    {
      currentTh.setUncaughtExceptionHandler(AgentHelper.INSTRU_CRITICAL_BLOCK);
      runMethod();
    }
    finally
    {
      currentTh.setUncaughtExceptionHandler(handler);
    }
  }

  /**
   * Actual work method
   */
  public abstract void runMethod();
}
