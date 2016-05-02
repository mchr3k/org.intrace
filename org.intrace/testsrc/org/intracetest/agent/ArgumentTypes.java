package org.intracetest.agent;

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

/**
  * InTrace Method specifier syntax:
        <pre>
org.intracetest.agent.ArgumentTypes#byteArg(B)V
        </pre> 
  */

  private void byteArg(byte arg)
  {
    setProperty(Byte.toString(arg));
  }

/**
  * InTrace Method specifier syntax:
        <pre>
org.intracetest.agent.ArgumentTypes#byteArrayArg({B)V
        </pre> 
  */

  private void byteArrayArg(byte[] arg)
  {
    setProperty(Arrays.toString(arg));
  }

/**
  * InTrace Method specifier syntax:
        <pre>
org.intracetest.agent.ArgumentTypes#shortArg(S)V
        </pre> 
  */

  private void shortArg(short arg)
  {
    setProperty(Short.toString(arg));
  }

/**
  * InTrace Method specifier syntax:
        <pre>
org.intracetest.agent.ArgumentTypes#shortArrayArg({S)V
        </pre> 
  */

  private void shortArrayArg(short[] arg)
  {
    setProperty(Arrays.toString(arg));
  }

/**
  * InTrace Method specifier syntax:
        <pre>
org.intracetest.agent.ArgumentTypes#intArg(I)V
        </pre> 
  */

  private void intArg(int arg)
  {
    setProperty(Integer.toString(arg));
  }

/**
  * InTrace Method specifier syntax:
        <pre>
org.intracetest.agent.ArgumentTypes#intArrayArg({I)V
        </pre> 
  */

  private void intArrayArg(int[] arg)
  {
    setProperty(Arrays.toString(arg));
  }

/**
  * InTrace Method specifier syntax:
        <pre>
org.intracetest.agent.ArgumentTypes#longArg(J)V
        </pre> 
  */

  private void longArg(long arg)
  {
    setProperty(Long.toString(arg));
  }

/**
  * InTrace Method specifier syntax:
        <pre>
org.intracetest.agent.ArgumentTypes#longArrayArg({J)V
        </pre> 
  */

  private void longArrayArg(long[] arg)
  {
    setProperty(Arrays.toString(arg));
  }

/**
  * InTrace Method specifier syntax:
        <pre>
org.intracetest.agent.ArgumentTypes#floatArg(F)V
        </pre> 
  */

  private void floatArg(float arg)
  {
    setProperty(Float.toString(arg));
  }

/**
  * InTrace Method specifier syntax:
        <pre>
org.intracetest.agent.ArgumentTypes#floatArrayArg({F)V
        </pre> 
  */

  private void floatArrayArg(float[] arg)
  {
    setProperty(Arrays.toString(arg));
  }

/**
  * InTrace Method specifier syntax:
        <pre>
org.intracetest.agent.ArgumentTypes#doubleArg(D)V
        </pre> 
  */

  private void doubleArg(double arg)
  {
    setProperty(Double.toString(arg));
  }

/**
  * InTrace Method specifier syntax:
        <pre>
org.intracetest.agent.ArgumentTypes#doubleArrayArg({D)V
        </pre> 
  */

  private void doubleArrayArg(double[] arg)
  {
    setProperty(Arrays.toString(arg));
  }

/**
  * InTrace Method specifier syntax:
        <pre>
org.intracetest.agent.ArgumentTypes#boolArg(Z)V
        </pre> 
  */

  private void boolArg(boolean arg)
  {
    setProperty(Boolean.toString(arg));
  }

/**
  * InTrace Method specifier syntax:
        <pre>
org.intracetest.agent.ArgumentTypes#boolArrayArg({Z)V
        </pre> 
  */

  private void boolArrayArg(boolean[] arg)
  {
    setProperty(Arrays.toString(arg));
  }

/**
  * InTrace Method specifier syntax:
	<pre>
org.intracetest.agent.ArgumentTypes#boolArrayArrayArg({{Z)V
	</pre>
  */
  private void boolArrayArrayArg(boolean[][] arg)
  {
    setProperty(Arrays.toString(arg));
  }

/**
  * InTrace Method specifier syntax:
        <pre>
org.intracetest.agent.ArgumentTypes#charArg(C)V
        </pre> 
  */

  private void charArg(char arg)
  {
    setProperty(Character.toString(arg));
  }

/**
  * InTrace Method specifier syntax:
        <pre>
org.intracetest.agent.ArgumentTypes#charArrayArg({C)V
        </pre> 
  */

  private void charArrayArg(char[] arg)
  {
    setProperty(Arrays.toString(arg));
  }

/**
  * InTrace Method specifier syntax:
        <pre>
org.intracetest.agent.ArgumentTypes#objArg(Ljava/lang/Object;)V
        </pre> 
  */

  private void objArg(Object arg)
  {
    setProperty(arg.toString());
  }

/**
  * InTrace Method specifier syntax:
        <pre>
org.intracetest.agent.ArgumentTypes#objArrayArg({Ljava/lang/Object;)V
        </pre> 
  */

  private void objArrayArg(Object[] arg)
  {
    setProperty(Arrays.toString(arg));
  }

  private void setProperty(String value)
  {
    System.setProperty("test-key", value);
  }

	public static void main(String args[]) throws Exception {
		System.out.println("Currently running sample code in background thread.");
		System.out.println("Make sure this program has -javaagent:./path/to/intrace-agent.jar on its command line.");
		System.out.println("Start InTrace GUI, download-able at https://mchr3k.github.io/org.intrace/");
		System.out.println("'Connect' & then configure GUI to trace any of these patterns:");
		System.out.println("org.intracetest.agent.ArgumentTypes#byteArrayArg({B)V -- to trace this single method");
		System.out.println("org.intracetest.agent.ArgumentTypes#shortArg(S)V      -- to trace this single method");
		System.out.println("org.intracetest.agent.ArgumentTypes                   -- to trace all methods in class ArgumentTypes");
		System.out.println("Press Ctrl+C to quit this program.");
		Runnable r = new ArgumentTypes();
		while(true) {
			new Thread(r).start();
			Thread.sleep(2000);
		}
	}
}
