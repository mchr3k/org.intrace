package example;

import java.util.Arrays;

public class InstrumentMe implements Runnable
{
	private byte myField = 1;

  @Override
  public void run()
  {
      byteArg((byte) 0);
  }

  private void byteArg(byte arg)
  {
   this.myField = arg; 
  }

}
