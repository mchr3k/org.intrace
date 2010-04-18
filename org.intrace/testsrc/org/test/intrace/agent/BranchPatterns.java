package org.test.intrace.agent;

public class BranchPatterns implements Runnable
{
  public Throwable th = null;

  @Override
  public void run()
  {
    try
    {
      dowhile();
      forloop();
      whileloop();
      switchblock(1);
      switchblock(2);
      ternary(true);
      ternary(false);
      trycatchfinally();
      singlelineif(true);
      ifthenelse(true);
      ifthenelse(false);
      ifthenelseifelse(true, false);
      ifthenelseifelse(false, true);
      ifthenelseifelse(false, false);
    }
    catch (Throwable thr)
    {
      th = thr;
    }
  }

  private void dowhile()
  {
    int loopCount = 0;
    do
    {
      setProperty();
      loopCount++;
    }
    while (loopCount < 2);
  }

  private void whileloop()
  {
    int loopCount = 0;
    while (loopCount < 2)
    {
      setProperty();
      loopCount++;
    }
  }

  private void forloop()
  {
    for (int ii = 0; ii < 2; ii++)
    {
      setProperty();
    }
  }

  private void switchblock(int arg)
  {
    switch (arg)
    {
    case 1:
    {
      setProperty("1");
      break;
    }

    case 2:
    {
      setProperty("2");
      break;
    }
    }
  }

  private void ternary(boolean arg)
  {
    String value = (arg ? "a" : "b");
    setProperty(value);
  }

  private void trycatchfinally()
  {
    try
    {
      setProperty();
      throw new Exception("test");
    }
    catch (Exception ex)
    {
      setProperty();
    }
    finally
    {
      setProperty();
    }
  }

  private void singlelineif(boolean arg)
  {
    if (arg)
      setProperty();
  }

  private void ifthenelse(boolean arg)
  {
    if (arg)
    {
      setProperty();
    }
    else
    {
      setProperty();
    }
  }

  private void ifthenelseifelse(boolean arg1, boolean arg2)
  {
    if (arg1)
    {
      setProperty();
    }
    else if (arg2)
    {
      setProperty();
    }
    else
    {
      setProperty();
    }
  }

  private void setProperty()
  {
    setProperty(Long.toString(System.currentTimeMillis()));
  }

  private void setProperty(String value)
  {
    System.setProperty("test-key", value);
  }
}
