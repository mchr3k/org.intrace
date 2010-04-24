package org.test.intrace.agent;

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
import org.easymock.classextension.EasyMock;
import org.intrace.output.AgentHelper;
import org.intrace.output.IInstrumentationHandler;
import org.intrace.shared.AgentConfigConstants;
import org.intrace.shared.OutputConfigConstants;
import org.intrace.shared.TraceConfigConstants;

/**
 * This test tests the Agent. To run this test you must run the test with this
 * JVM argument: "-javaagent:built/traceagent_test.jar="
 */
public class AgentTest extends TestCase
{
  private Receiver receiver;
  private Sender sender;
  private Socket socket;

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    deleteOldClassFiles();
    AgentHelper.instrumentationHandlers.clear();
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

  public void testGetSettings() throws Exception
  {
    sender.sendMessage("getsettings");
    Object settingsResponse = receiver.incomingMessages.take();
    assertNotNull(settingsResponse);
    assertTrue(settingsResponse instanceof Map<?, ?>);
  }

  public void testHelp() throws Exception
  {
    sender.sendMessage("help");
    Object helpResponse = receiver.incomingMessages.take();
    assertNotNull(helpResponse);
    assertTrue(helpResponse instanceof Set<?>);
    Set<String> expectedHelpResponse = new HashSet<String>();
    expectedHelpResponse.addAll(AgentConfigConstants.COMMANDS);
    expectedHelpResponse.addAll(OutputConfigConstants.COMMANDS);
    expectedHelpResponse.addAll(TraceConfigConstants.COMMANDS);
    assertEquals(helpResponse, expectedHelpResponse);
  }

  public void testSettings() throws Exception
  {
    // Boolean settings
    testSetting(AgentConfigConstants.ALLOW_JARS_TO_BE_TRACED, "true");
    testSetting(AgentConfigConstants.ALLOW_JARS_TO_BE_TRACED, "false");
    testSetting(AgentConfigConstants.INSTRU_ENABLED, "true");
    testSetting(AgentConfigConstants.INSTRU_ENABLED, "false");
    testSetting(AgentConfigConstants.SAVE_TRACED_CLASSFILES, "true");
    testSetting(AgentConfigConstants.SAVE_TRACED_CLASSFILES, "false");
    testSetting(AgentConfigConstants.VERBOSE_MODE, "true");
    testSetting(AgentConfigConstants.VERBOSE_MODE, "false");

    // Regex (Test in verbose mode to exercise the verbose code
    testSetting(AgentConfigConstants.CLASS_REGEX, "foo.*");
    testSetting(AgentConfigConstants.ALLOW_JARS_TO_BE_TRACED, "true");
    testSetting(AgentConfigConstants.INSTRU_ENABLED, "true");
    testSetting(AgentConfigConstants.VERBOSE_MODE, "true");
    testSetting(AgentConfigConstants.CLASS_REGEX, ".*");
  }

  public void testBranchPatterns() throws Throwable
  {
    // Create and init the mock
    IInstrumentationHandler testHandler = EasyMock
                                                  .createMock(IInstrumentationHandler.class);
    EasyMock.expect(testHandler.getResponse(isA(String.class))).andReturn(null)
            .anyTimes();
    EasyMock.expect(testHandler.getSettingsMap())
            .andReturn(new HashMap<String, String>()).anyTimes();

    // Capture objects
    final BlockingQueue<String> capturedTrace = new LinkedBlockingQueue<String>();
    IAnswer<Object> entryTraceWriter = new IAnswer<Object>()
    {
      @Override
      public Object answer() throws Throwable
      {
        Object[] args = EasyMock.getCurrentArguments();
        capturedTrace.put("Enter:##:" + Arrays.toString(args));
        return null;
      }
    };
    IAnswer<Object> branchTraceWriter = new IAnswer<Object>()
    {
      @Override
      public Object answer() throws Throwable
      {
        Object[] args = EasyMock.getCurrentArguments();
        capturedTrace.put("Branch:##:" + Arrays.toString(args));
        return null;
      }
    };
    IAnswer<Object> exitTraceWriter = new IAnswer<Object>()
    {
      @Override
      public Object answer() throws Throwable
      {
        Object[] args = EasyMock.getCurrentArguments();
        capturedTrace.put("Exit:##:" + Arrays.toString(args));
        return null;
      }
    };

    // Expect trace calls
    testHandler.enter(isA(String.class), isA(String.class), EasyMock.anyInt());
    EasyMock.expectLastCall().andAnswer(entryTraceWriter).anyTimes();
    testHandler.branch(isA(String.class), isA(String.class), EasyMock.anyInt());
    EasyMock.expectLastCall().andAnswer(branchTraceWriter).anyTimes();
    testHandler.exit(isA(String.class), isA(String.class), EasyMock.anyInt());
    EasyMock.expectLastCall().andAnswer(exitTraceWriter).anyTimes();

    testHandler
               .arg(isA(String.class), isA(String.class), EasyMock.anyBoolean());
    EasyMock.expectLastCall().anyTimes();
    testHandler.arg(isA(String.class), isA(String.class), EasyMock.anyObject());
    EasyMock.expectLastCall().anyTimes();
    testHandler.arg(isA(String.class), isA(String.class), EasyMock.anyInt());
    EasyMock.expectLastCall().anyTimes();

    EasyMock.replay(testHandler);
    AgentHelper.instrumentationHandlers.put(testHandler, new Object());

    // Setup agent
    testSetting(AgentConfigConstants.INSTRU_ENABLED, "false");
    testSetting(AgentConfigConstants.CLASS_REGEX, ".*BranchPatterns.*");
    testSetting(AgentConfigConstants.VERBOSE_MODE, "false");
    testSetting(AgentConfigConstants.ALLOW_JARS_TO_BE_TRACED, "false");
    testSetting(AgentConfigConstants.SAVE_TRACED_CLASSFILES, "true");
    testSetting(AgentConfigConstants.INSTRU_ENABLED, "true");

    // Run Patterns thread
    BranchPatterns branchPatterns = new BranchPatterns();
    Thread patternThread = new Thread(branchPatterns);
    patternThread.start();
    patternThread.join();
    if (branchPatterns.th != null)
    {
      throw branchPatterns.th;
    }

    // Verify that we got all of the expected calls
    EasyMock.verify(testHandler);

    // Parse the trace
    Map<String, TraceData> parsedTraceData = new HashMap<String, TraceData>();
    String traceLine = capturedTrace.poll();
    while (traceLine != null)
    {
      parseLine(parsedTraceData, traceLine);
      traceLine = capturedTrace.poll();
    }

    // Verify the trace
    assertNotNull(parsedTraceData.get("switchblock"));
    {
      TraceData trData = parsedTraceData.get("switchblock");
      assertTrue(trData.seenEnter);
      assertTrue(trData.seenExit);
      assertEquals(4, trData.branchLines.size());
    }

    assertNotNull(parsedTraceData.get("ternary"));
    {
      TraceData trData = parsedTraceData.get("ternary");
      assertTrue(trData.seenEnter);
      assertTrue(trData.seenExit);
      assertEquals(0, trData.branchLines.size());
    }

    assertNotNull(parsedTraceData.get("trycatchfinally"));
    {
      TraceData trData = parsedTraceData.get("trycatchfinally");
      assertTrue(trData.seenEnter);
      assertTrue(trData.seenExit);
      assertEquals(1, trData.branchLines.size());
    }

    assertNotNull(parsedTraceData.get("whileloop"));
    {
      TraceData trData = parsedTraceData.get("whileloop");
      assertTrue(trData.seenEnter);
      assertTrue(trData.seenExit);
      // Number depends on code generated by JVM
      assertTrue((2 == trData.branchLines.size())
                 || (5 == trData.branchLines.size()));
    }

    assertNotNull(parsedTraceData.get("forloop"));
    {
      TraceData trData = parsedTraceData.get("forloop");
      assertTrue(trData.seenEnter);
      assertTrue(trData.seenExit);
      // Number depends on code generated by JVM
      assertTrue((2 == trData.branchLines.size())
                 || (3 == trData.branchLines.size()));
    }

    assertNotNull(parsedTraceData.get("singlelineif"));
    {
      TraceData trData = parsedTraceData.get("singlelineif");
      assertTrue(trData.seenEnter);
      assertTrue(trData.seenExit);
      assertEquals(1, trData.branchLines.size());
    }

    assertNotNull(parsedTraceData.get("ifthenelse"));
    {
      TraceData trData = parsedTraceData.get("ifthenelse");
      assertTrue(trData.seenEnter);
      assertTrue(trData.seenExit);
      assertEquals(2, trData.branchLines.size());
    }

    assertNotNull(parsedTraceData.get("ifthenelseifelse"));
    {
      TraceData trData = parsedTraceData.get("ifthenelseifelse");
      assertTrue(trData.seenEnter);
      assertTrue(trData.seenExit);
      assertEquals(5, trData.branchLines.size());
    }

    assertNotNull(parsedTraceData.get("dowhile"));
    {
      TraceData trData = parsedTraceData.get("dowhile");
      assertTrue(trData.seenEnter);
      assertTrue(trData.seenExit);
      assertEquals(2, trData.branchLines.size());
    }
  }

  public void testArgumentTypes() throws Throwable
  {
    // Create and init the mock
    final BlockingQueue<String> capturedTrace = new LinkedBlockingQueue<String>();
    IInstrumentationHandler testHandler = new ArgCapture(capturedTrace);
    AgentHelper.instrumentationHandlers.put(testHandler, new Object());

    // Setup agent
    testSetting(AgentConfigConstants.INSTRU_ENABLED, "false");
    testSetting(AgentConfigConstants.CLASS_REGEX, ".*ArgumentTypes.*");
    testSetting(AgentConfigConstants.VERBOSE_MODE, "false");
    testSetting(AgentConfigConstants.ALLOW_JARS_TO_BE_TRACED, "false");
    testSetting(AgentConfigConstants.SAVE_TRACED_CLASSFILES, "true");
    testSetting(AgentConfigConstants.INSTRU_ENABLED, "true");

    // Run Patterns thread
    ArgumentTypes argTypes = new ArgumentTypes();
    Thread patternThread = new Thread(argTypes);
    patternThread.start();
    patternThread.join();
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

    // Verify the trace
    assertNotNull(parsedTraceData.get("byteArg"));
    {
      TraceData trData = parsedTraceData.get("byteArg");
      assertEquals(1, trData.args.size());
      assertEquals("0", trData.args.get(0));
    }

    assertNotNull(parsedTraceData.get("byteArrayArg"));
    {
      TraceData trData = parsedTraceData.get("byteArrayArg");
      assertEquals(1, trData.args.size());
      assertEquals("[1]", trData.args.get(0));
    }

    assertNotNull(parsedTraceData.get("shortArg"));
    {
      TraceData trData = parsedTraceData.get("shortArg");
      assertEquals(1, trData.args.size());
      assertEquals("2", trData.args.get(0));
    }

    assertNotNull(parsedTraceData.get("shortArrayArg"));
    {
      TraceData trData = parsedTraceData.get("shortArrayArg");
      assertEquals(1, trData.args.size());
      assertEquals("[3]", trData.args.get(0));
    }

    assertNotNull(parsedTraceData.get("intArg"));
    {
      TraceData trData = parsedTraceData.get("intArg");
      assertEquals(1, trData.args.size());
      assertEquals("4", trData.args.get(0));
    }

    assertNotNull(parsedTraceData.get("intArrayArg"));
    {
      TraceData trData = parsedTraceData.get("intArrayArg");
      assertEquals(1, trData.args.size());
      assertEquals("[5]", trData.args.get(0));
    }

    assertNotNull(parsedTraceData.get("longArg"));
    {
      TraceData trData = parsedTraceData.get("longArg");
      assertEquals(1, trData.args.size());
      assertEquals("6", trData.args.get(0));
    }

    assertNotNull(parsedTraceData.get("longArrayArg"));
    {
      TraceData trData = parsedTraceData.get("longArrayArg");
      assertEquals(1, trData.args.size());
      assertEquals("[7]", trData.args.get(0));
    }

    assertNotNull(parsedTraceData.get("floatArg"));
    {
      TraceData trData = parsedTraceData.get("floatArg");
      assertEquals(1, trData.args.size());
      assertEquals("8.0", trData.args.get(0));
    }

    assertNotNull(parsedTraceData.get("floatArrayArg"));
    {
      TraceData trData = parsedTraceData.get("floatArrayArg");
      assertEquals(1, trData.args.size());
      assertEquals("[9.0]", trData.args.get(0));
    }

    assertNotNull(parsedTraceData.get("doubleArg"));
    {
      TraceData trData = parsedTraceData.get("doubleArg");
      assertEquals(1, trData.args.size());
      assertEquals("10.0", trData.args.get(0));
    }

    assertNotNull(parsedTraceData.get("doubleArrayArg"));
    {
      TraceData trData = parsedTraceData.get("doubleArrayArg");
      assertEquals(1, trData.args.size());
      assertEquals("[11.0]", trData.args.get(0));
    }

    assertNotNull(parsedTraceData.get("boolArg"));
    {
      TraceData trData = parsedTraceData.get("boolArg");
      assertEquals(2, trData.args.size());
      assertEquals("true", trData.args.get(0));
      assertEquals("false", trData.args.get(1));
    }

    assertNotNull(parsedTraceData.get("boolArrayArg"));
    {
      TraceData trData = parsedTraceData.get("boolArrayArg");
      assertEquals(2, trData.args.size());
      assertEquals("[true]", trData.args.get(0));
      assertEquals("[false]", trData.args.get(1));
    }

    assertNotNull(parsedTraceData.get("boolArrayArrayArg"));
    {
      TraceData trData = parsedTraceData.get("boolArrayArrayArg");
      assertEquals(2, trData.args.size());
      assertEquals("[[true]]", trData.args.get(0));
      assertEquals("[[false]]", trData.args.get(1));
    }

    assertNotNull(parsedTraceData.get("charArg"));
    {
      TraceData trData = parsedTraceData.get("charArg");
      assertEquals(1, trData.args.size());
      assertEquals("2", trData.args.get(0));
    }

    assertNotNull(parsedTraceData.get("charArrayArg"));
    {
      TraceData trData = parsedTraceData.get("charArrayArg");
      assertEquals(1, trData.args.size());
      assertEquals("[3]", trData.args.get(0));
    }

    assertNotNull(parsedTraceData.get("objArg"));
    {
      TraceData trData = parsedTraceData.get("objArg");
      assertEquals(1, trData.args.size());
      assertEquals("obj", trData.args.get(0));
    }

    assertNotNull(parsedTraceData.get("objArrayArg"));
    {
      TraceData trData = parsedTraceData.get("objArrayArg");
      assertEquals(1, trData.args.size());
      assertEquals("[obj]", trData.args.get(0));
    }
  }

  private void parseLine(Map<String, TraceData> parsedTraceData,
                         String traceLine)
  {
    String[] traceParts = traceLine.split(":##:");
    String traceType = traceParts[0];
    String traceLineData = traceParts[1];
    traceLineData = traceLineData.substring(1, traceLineData.length() - 1);
    String[] dataParts = traceLineData.split(",");
    String methodSig = dataParts[1].trim();

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
    else if (traceType.equals("Arg"))
    {
      traceData.args.add(dataParts[2].trim());
    }
  }

  @SuppressWarnings("unchecked")
  private void testSetting(String configConstant, String configValue)
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
    assertEquals(configValue, settingsResponseMap.get(configConstant));
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
          incomingMessages.put(receivedMessage);
        }
      }
      catch (Exception e)
      {
        // Do something
      }
    }
  }
}
