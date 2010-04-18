package org.test.intrace.agent;

import java.util.Arrays;

public class ArgumentTypes implements Runnable
{
  public Throwable th = null;

  @Override
  public void run()
  {
    try
    {
      byteArg((byte) 0);
      byteArrayArg(new byte[]
      { 1 });

      shortArg((short) 2);
      shortArrayArg(new short[]
      { 3 });

      intArg(4);
      intArrayArg(new int[]
      { 5 });

      longArg(6l);
      longArrayArg(new long[]
      { 7l });

      floatArg(8f);
      floatArrayArg(new float[]
      { 9f });

      doubleArg(10d);
      doubleArrayArg(new double[]
      { 11d });

      boolArg(true);
      boolArg(false);
      boolArrayArg(new boolean[]
      { true });
      boolArrayArg(new boolean[]
      { false });
      boolArrayArrayArg(new boolean[][]
      { new boolean[]
      { true } });
      boolArrayArrayArg(new boolean[][]
      { new boolean[]
      { false } });

      charArg('2');
      charArrayArg(new char[]
      { '3' });

      Object objToStr = new Object()
      {
        @Override
        public String toString()
        {
          return "obj";
        }
      };
      objArg(objToStr);
      objArrayArg(new Object[]
      { objToStr });
    }
    catch (Throwable thr)
    {
      th = thr;
    }
  }

  private void byteArg(byte arg)
  {
    setProperty(Byte.toString(arg));
  }

  private void byteArrayArg(byte[] arg)
  {
    setProperty(Arrays.toString(arg));
  }

  private void shortArg(short arg)
  {
    setProperty(Short.toString(arg));
  }

  private void shortArrayArg(short[] arg)
  {
    setProperty(Arrays.toString(arg));
  }

  private void intArg(int arg)
  {
    setProperty(Integer.toString(arg));
  }

  private void intArrayArg(int[] arg)
  {
    setProperty(Arrays.toString(arg));
  }

  private void longArg(long arg)
  {
    setProperty(Long.toString(arg));
  }

  private void longArrayArg(long[] arg)
  {
    setProperty(Arrays.toString(arg));
  }

  private void floatArg(float arg)
  {
    setProperty(Float.toString(arg));
  }

  private void floatArrayArg(float[] arg)
  {
    setProperty(Arrays.toString(arg));
  }

  private void doubleArg(double arg)
  {
    setProperty(Double.toString(arg));
  }

  private void doubleArrayArg(double[] arg)
  {
    setProperty(Arrays.toString(arg));
  }

  private void boolArg(boolean arg)
  {
    setProperty(Boolean.toString(arg));
  }

  private void boolArrayArg(boolean[] arg)
  {
    setProperty(Arrays.toString(arg));
  }

  private void boolArrayArrayArg(boolean[][] arg)
  {
    setProperty(Arrays.toString(arg));
  }

  private void charArg(char arg)
  {
    setProperty(Character.toString(arg));
  }

  private void charArrayArg(char[] arg)
  {
    setProperty(Arrays.toString(arg));
  }

  private void objArg(Object arg)
  {
    setProperty(arg.toString());
  }

  private void objArrayArg(Object[] arg)
  {
    setProperty(Arrays.toString(arg));
  }

  private void setProperty(String value)
  {
    System.setProperty("test-key", value);
  }
}
