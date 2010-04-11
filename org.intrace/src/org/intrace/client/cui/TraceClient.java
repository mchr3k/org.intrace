package org.intrace.client.cui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Command line agent client.
 */
public class TraceClient
{
  private static Sender sender;
  private static Receiver receiver;

  /**
   * Cmd line tool.
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception
  {
    BufferedReader readIn = new BufferedReader(new InputStreamReader(System.in));
    String inLine = "";
    while (!"quit".equals(inLine))
    {
      System.out.print("Enter command [connect/quit]: ");
      inLine = readIn.readLine();

      if ("connect".equals(inLine))
      {
        connectToAgent(readIn);
      }
    }
  }

  private static void connectToAgent(BufferedReader xiReadIn) throws Exception
  {
    System.out.print("Enter host address (localhost): ");
    String host = xiReadIn.readLine();
    if (host.length() == 0)
    {
      host = "localhost";
    }
    System.out.print("Enter port (9123): ");
    String port = xiReadIn.readLine();
    if (port.length() == 0)
    {
      port = "9123";
    }

    Socket socket = new Socket();
    socket.connect(new InetSocketAddress(host, Integer.valueOf(port)));
    System.out.println("Connected!");

    // Start threads
    receiver = new Receiver(socket.getInputStream());
    receiver.start();
    sender = new Sender(socket.getOutputStream());
    sender.start();

    // Command loop
    String inLine = "";
    while (!"disconnect".equals(inLine))
    {
      promptUser("Enter command [getsettings/help/disconnect]: ");
      inLine = xiReadIn.readLine();
      promptActive = false;
      if (!"".equals(inLine))
      {
        sender.sendMessage(inLine);
      }
    }
    receiver.stop();
    sender.stop();
    socket.close();
    System.out.println("Disconnected!");
  }

  private static boolean promptActive = false;
  private static String promptString = "";
  private static synchronized void promptUser(String prompt)
  {
    promptActive = true;
    promptString = prompt;
    System.out.print(promptString);
  }

  private static synchronized void printMsg(String msg)
  {
    if (promptActive)
    {
      System.out.print("<Received Message>");
      System.out.println();
    }
    System.out.println(" => " + msg);
    if (promptActive)
    {
      System.out.print(promptString);
    }
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
        while(true)
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

    @SuppressWarnings("unchecked")
    @Override
    public void run()
    {
      try
      {
        while (true)
        {
          ObjectInputStream objIn = new ObjectInputStream(inputStream);
          Object receivedMessage = objIn.readObject();
          if (receivedMessage instanceof Map<?,?>)
          {
            Map<String,String> settingsMap = (Map<String,String>)receivedMessage;
            dumpSettings(settingsMap);
          }
          else if (receivedMessage instanceof Set<?>)
          {
            StringBuffer commandBuffer = new StringBuffer("Available Commands:\n");
            Set<String> commandSet = (Set<String>)receivedMessage;
            for (String command : commandSet)
            {
              commandBuffer.append("  " + command + "\n");
            }
            printMsg(commandBuffer.toString());
          }
          else
          {
            String strMessage = (String)receivedMessage;
            printMsg(strMessage);
          }
        }
      }
      catch (Exception e)
      {
        // Do something
      }
    }

    private void dumpSettings(Map<String,String> settingsMap)
    {
      StringBuffer settingsString = new StringBuffer("Current Settings:\n");
      StringBuffer singleLineSettingsString = new StringBuffer();
      for (Entry<String,String> entry : settingsMap.entrySet())
      {
        if (entry.getKey().startsWith("["))
        {
          settingsString.append("  ");
          settingsString.append(entry.getKey());
          settingsString.append(entry.getValue());
          settingsString.append("\n");

          singleLineSettingsString.append(entry.getKey());
          singleLineSettingsString.append(entry.getValue());
        }
      }
      settingsString.append("\n => Single Line Config: " + singleLineSettingsString.toString());
      settingsString.append("\n");
      printMsg(settingsString.toString());
    }
  }
}
