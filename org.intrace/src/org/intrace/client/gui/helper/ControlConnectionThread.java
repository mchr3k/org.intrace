package org.intrace.client.gui.helper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.intrace.client.gui.TraceWindow;

public class ControlConnectionThread implements Runnable
{
  private final Socket socket;
  private final TraceWindow window;
  private final BlockingQueue<String> incomingMessages = new LinkedBlockingQueue<String>();
  private final BlockingQueue<String> outgoingMessages = new LinkedBlockingQueue<String>();
  private final ControlConnectionSenderThread senderThread = new ControlConnectionSenderThread();
  private Thread sendThread;

  public ControlConnectionThread(Socket socket, TraceWindow devTraceWindow)
  {
    this.window = devTraceWindow;
    this.socket = socket;
  }

  public void start()
  {
    Thread receiveThread = new Thread(this);
    receiveThread.setDaemon(true);
    receiveThread.setName("Control Receive Thread");
    receiveThread.start();

    sendThread = new Thread(senderThread);
    sendThread.setDaemon(true);
    sendThread.setName("Control Sender Thread");
    sendThread.start();
  }

  public String getMessage()
  {
    try
    {
      return incomingMessages.take();
    }
    catch (InterruptedException e)
    {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void run()
  {
    try
    {
      while (true)
      {
        ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream());
        Object receivedMessage = objIn.readObject();
        if (receivedMessage instanceof Map<?, ?>)
        {
          Map<String, String> settingsMap = (Map<String, String>) receivedMessage;
          window.setConfig(settingsMap);
        }
        else
        {
          String strMessage = (String) receivedMessage;
          if (!"OK".equals(strMessage))
          {
            incomingMessages.put(strMessage);
          }
        }
      }
    }
    catch (Exception e)
    {
      window.disconnect();
    }
  }

  public void disconnect()
  {
    if (sendThread != null)
    {
      sendThread.interrupt();
    }
    try
    {
      socket.close();
    }
    catch (IOException e)
    {
      // Throw away
    }
  }

  public void sendMessage(String xiString)
  {
    try
    {
      outgoingMessages.put(xiString);
    }
    catch (InterruptedException e1)
    {
      // Throw away
    }
  }

  private class ControlConnectionSenderThread implements Runnable
  {
    @Override
    public void run()
    {
      try
      {
        while (true)
        {
          String message = outgoingMessages.take();
          OutputStream out = socket.getOutputStream();
          ObjectOutputStream objOut = new ObjectOutputStream(out);
          objOut.writeObject(message);
          objOut.flush();
        }
      }
      catch (Exception e)
      {
        window.disconnect();
      }
    }
  }
}
