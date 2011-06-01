package example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TraceExample
{
  
  private static Map<String,String> map = new HashMap<String, String>();
  static
  {
    map.put("foo", "bar");
  }
  
  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args)
  {
    try
    {
      otherMain(args[0]);
    } 
    catch (Throwable ex)
    {
      ex.printStackTrace();
    }
  }

  public static void otherMain(String arg) throws Exception
  {
    while (true)
    {
      Thread.sleep(Long.parseLong(arg));
      InnerTestClass.foo();
      workMethod("foobar");
      new InnerTestClass().boolMethod(true);
    }
  }

  private static void workMethod(String foo)
  {
    long currentTime = System.currentTimeMillis();
    System.setProperty("a", foo);
    System.setProperty("foo", exceptionMethod());
    System.setProperty("foo", ": " + intArrayMethod(new int[]
    { 1, 2, 3 }));
    if ((currentTime % 2) == 0)
    {
      System.setProperty("a", "Even time");
    }
    else
    {
      System.setProperty("a", "Odd time");
    }
    map.get("foo");
    new InnerTestClass().instanceFoo();
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
    private void instanceFoo()
    {
      System.setProperty("a", "foobar");
    }
    
    private void boolMethod(boolean xiArg)
    {
      System.out.println(Boolean.toString(xiArg));
    }
    
    private static void foo()
    {
      System.setProperty("a", "bar");
    }
  }
}
