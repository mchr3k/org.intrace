package mchr;

public class LoveExample
{

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception
  {
    while (true)
    {
      iLoveD();
      Thread.sleep(1000);
    }
  }
  
  private static void iLoveD()  
  {
    String[] loveMessage = new String[]
    {
      "      _  _        ",
      "     / \\/ \\       ",
      "     \\ D /       ",
      "      \\  /         ",
      "       \\/         "
    };
    for (int ii = 0; ii < loveMessage.length; ii++)
    {
      loveDLine(loveMessage[ii]);
    }    
  }
  private static void loveDLine(String loveD)
  {
    System.setProperty("key", loveD);
  }

}
