package example;

import java.util.Arrays;

public class TraceExample
{

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception
  {
    otherMain("123");
  }

  public static void otherMain(String arg) throws Exception
  {
    while (true)
    {
      Thread.sleep(1000 * 5);
      InnerTestClass.foo();
      workMethod("foobar");
    }
  }

  private static void workMethod(String foo)
  {
    long currentTime = System.currentTimeMillis();
    System.setProperty("a", foo);
    System.out.println(exceptionMethod());
    System.out.println(intArrayMethod(new int[]
    { 1, 2, 3 }));
    if ((currentTime % 2) == 0)
    {
      System.setProperty("a", "Even time");
    }
    else
    {
      System.setProperty("a", "Odd time");
    }
  }

  private static String exceptionMethod()
  {
    try
    {
      long currentTime = System.currentTimeMillis();
      if ((currentTime % 2) == 0)
      {
        throw new Exception("Exception text");
      }
    }
    catch (Exception ex)
    {
      return "seen exception";      
    }
    return "no exception";
  }

  private static int intArrayMethod(int[] intArg)
  {
    System.setProperty("a", Arrays.toString(intArg));
    return 123;
  }

  private static class InnerTestClass
  {
    private static void foo()
    {
      System.setProperty("a", "bar");
    }
  }

}
