package org.intracetest.agent;

import static org.easymock.EasyMock.isA;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;

import org.easymock.IAnswer;
import org.easymock.EasyMock;
import org.intrace.output.AgentHelper;
import org.intrace.output.IInstrumentationHandler;
import org.intrace.shared.AgentConfigConstants;
import org.intrace.shared.TraceConfigConstants;

/**
 * This test tests the Agent. To run this test you must run the test with this
 * JVM argument: "-javaagent:built/traceagent_test.jar="
 */
public class TracingSingleMethodTest extends TestCase
{
  private Receiver receiver;
  private Sender sender;
  private Socket socket;

	private String M1 = "org.intracetest.agent.ArgumentTypes#boolArrayArrayArg({{Z)V";
	private String M2 = "org.intracetest.agent.ArgumentTypes#objArrayArg({Ljava/lang/Object;)V";
	private String M3 = "org.intracetest.agent.ArgumentTypes#doubleArg(D)V";

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    deleteOldClassFiles();
    AgentHelper.setInstrumentationHandler(null);
    // Wait for agent to startup
    Thread.sleep(500);
    connectToAgent();
    testSetting(AgentConfigConstants.INSTRU_ENABLED, "false");
  }

  private void deleteOldClassFiles()
  {
    File genbin = new File("./genbin/");
    File[] files = genbin.listFiles();
    if (files != null)
    {
      for (int i = 0; i < files.length; i++)
      {
        if (files[i].isDirectory())
        {
          deleteDirectory(files[i]);
        }
        else
        {
          files[i].delete();
        }
      }
    }
  }

  public static boolean deleteDirectory(File path)
  {
    if (path.exists())
    {
      File[] files = path.listFiles();
      for (int i = 0; i < files.length; i++)
      {
        if (files[i].isDirectory())
        {
          deleteDirectory(files[i]);
        }
        else
        {
          files[i].delete();
        }
      }
    }
    return (path.delete());
  }


  @SuppressWarnings("unchecked")
  private void testSetting(String configConstant, String configValue)
      throws Exception
  {

    Map<String, String> settingsResponseMap = sendAndReceiveSettings(configConstant, configValue);
    //System.out.println("Compar1 act: " + settingsResponseMap.get(configConstant) );
    //System.out.println("Compar2 exp: " + configValue);
    assertEquals(configValue, settingsResponseMap.get(configConstant));
  }

  @SuppressWarnings("unchecked")
  private Map<String,String> sendAndReceiveSettings(String configConstant, String configValue)
      throws Exception
  {
    // Set setting
    sender.sendMessage(configConstant + configValue);
    Object okResponse = receiver.incomingMessages.take();
    assertNotNull(okResponse);
    assertTrue(okResponse instanceof String);
    assertEquals(okResponse, "OK");

    // Get settings
    sender.sendMessage("getsettings");
    Object settingsResponse = receiver.incomingMessages.take();
    assertNotNull(settingsResponse);
    assertTrue(settingsResponse instanceof Map<?, ?>);
    Map<String, String> settingsResponseMap = (Map<String, String>) settingsResponse;
    return settingsResponseMap;
  }



	public void testArgumentTypesForOneMethod()  throws Throwable {

    // Create and init the mock
    final BlockingQueue<String> capturedTrace = new LinkedBlockingQueue<String>();
    IInstrumentationHandler testHandler = new ArgCapture(capturedTrace);
    AgentHelper.setInstrumentationHandler(testHandler);

    // Setup agent
    testSetting(AgentConfigConstants.INSTRU_ENABLED, "false");
    testSetting(AgentConfigConstants.GZIP, "false");
    
    //The intArg() method takes a single int parameter...let's trace just this one method.
    testSetting(AgentConfigConstants.CLASS_REGEX, "org.intracetest.agent.ArgumentTypes#intArg(I)V");
    testSetting(AgentConfigConstants.VERBOSE_MODE, "false");
    testSetting(AgentConfigConstants.SAVE_TRACED_CLASSFILES, "true");
    testSetting(AgentConfigConstants.INSTRU_ENABLED, "true");

    // Run Patterns thread
    ArgumentTypes argTypes = new ArgumentTypes();
    Thread patternThread = new Thread(argTypes);
    patternThread.start();
    patternThread.join(5 * 1000);
    if (argTypes.th != null)
    {
      throw argTypes.th;
    }

    // Parse the trace
    Map<String, TraceData> parsedTraceData = new HashMap<String, TraceData>();
    String traceLine = capturedTrace.poll();
    while (traceLine != null)
    {
      parseLine(parsedTraceData, traceLine);
      traceLine = capturedTrace.poll();
    }

	/**
	Method-level tracing to the rescue!
	This map contains  trace lines (3 of them) for exactly 1 method invocation
	 because the new method-level-tracing feature specified only one method.
	*/
    assertEquals("Exactly 1 TraceData object for method intArg() should have been in this map",1,parsedTraceData.size() );

    assertNotNull(parsedTraceData.get("intArg"));
    {
      TraceData trData = parsedTraceData.get("intArg");
      assertEquals(1, trData.args.size());
      assertEquals("4", trData.args.get(0));
    }

	
}



  private void parseLine(Map<String, TraceData> parsedTraceData,
                         String traceLine)
  {
    //System.out.println("Parse: " + traceLine);
	if (traceLine.contains("DEBUG"))
		return;
    String[] traceParts = traceLine.split(":##:");
    String traceType = traceParts[0];
    String traceLineData = traceParts[1];
    traceLineData = traceLineData.substring(1, traceLineData.length() - 1);
    String[] dataParts = traceLineData.split(",");
    String methodSig;
    if (traceType.equals("Throwable"))
    {
      methodSig = dataParts[2].trim();
    }
    else
    {
      methodSig = dataParts[1].trim();
    }

    TraceData traceData = parsedTraceData.get(methodSig);
    if (traceData == null)
    {
      traceData = new TraceData();
      parsedTraceData.put(methodSig, traceData);
    }

    if (traceType.equals("Enter"))
    {
      traceData.seenEnter = true;
    }
    else if (traceType.equals("Exit"))
    {
      traceData.seenExit = true;
    }
    else if (traceType.equals("Branch"))
    {
      traceData.branchLines.add(Integer.parseInt(dataParts[2].trim()));
    }
    else if (traceType.equals("Throwable"))
    {
      if (traceLine.contains("Caught"))
      {
        traceData.caughtLines.add(Integer.parseInt(dataParts[3].trim()));
      }
    }
    else if (traceType.startsWith("Arg"))
    {
      traceData.args.add(dataParts[2].trim());
    }
  }


  @Override
  protected void tearDown() throws Exception
  {
    receiver.stop();
    sender.stop();
    socket.close();
    super.tearDown();
  }

  private void connectToAgent() throws Exception
  {
    String host = "localhost";
    int port = Integer.parseInt(System.getProperty("org.intrace.port"));

    socket = new Socket();
    socket.connect(new InetSocketAddress(host, port));

    // Start threads
    receiver = new Receiver(socket.getInputStream());
    receiver.start();
    sender = new Sender(socket.getOutputStream());
    sender.start();
  }

  private static class Sender implements Runnable
  {
    private final OutputStream outputStream;
    private final BlockingQueue<String> outgoingMessages = new LinkedBlockingQueue<String>();
    private Thread th;

    public Sender(OutputStream outputStream)
    {
      this.outputStream = outputStream;
    }

    public void stop()
    {
      try
      {
        outputStream.close();
      }
      catch (IOException e)
      {
        // Throw away
      }
      th.interrupt();
    }

    public void start()
    {
      th = new Thread(this);
      th.setDaemon(true);
      th.setName("Sender");
      th.start();
    }

    @Override
    public void run()
    {
      try
      {
        while (true)
        {
          String message = outgoingMessages.take();
          ObjectOutputStream objOut = new ObjectOutputStream(outputStream);
          objOut.writeObject(message);
          objOut.flush();
        }
      }
      catch (Exception e)
      {
        // Do something
      }
    }

    public void sendMessage(String message)
    {
      try
      {
        outgoingMessages.put(message);
      }
      catch (InterruptedException e)
      {
        // Do nothing
      }
    }
  }

  private static class Receiver implements Runnable
  {
    private final InputStream inputStream;
    private final BlockingQueue<Object> incomingMessages = new LinkedBlockingQueue<Object>();
    private Thread th;

    public Receiver(InputStream inputStream)
    {
      this.inputStream = inputStream;
    }

    public void stop()
    {
      try
      {
        inputStream.close();
      }
      catch (IOException e)
      {
        // Throw away
      }
      th.interrupt();
    }

    public void start()
    {
      th = new Thread(this);
      th.setDaemon(true);
      th.setName("Receiver");
      th.start();
    }

    @Override
    public void run()
    {
      try
      {
        while (true)
        {
          ObjectInputStream objIn = new ObjectInputStream(inputStream);
          Object receivedMessage = objIn.readObject();
          // System.out.println("Test received: " + receivedMessage);
          if (receivedMessage instanceof Map<?, ?>)
          {
            Map<?, ?> receivedMap = (Map<?, ?>) receivedMessage;
            if (receivedMap.containsKey(AgentConfigConstants.NUM_PROGRESS_ID)||
                receivedMap.containsKey(AgentConfigConstants.STID))
            {
              continue;
            }
          }          
          incomingMessages.put(receivedMessage);
        }
      }
      catch (Exception e)
      {
        // Do nothing
      }
    }
  }
}
