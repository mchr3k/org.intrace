package org.intrace.agentbug.app;

public class TestApplication
{

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception
  {
    for (int ii = 0; ii < 10; ii++)
    {
      System.out.println(ii);
      Thread.sleep(1000);
    }
  }

}
