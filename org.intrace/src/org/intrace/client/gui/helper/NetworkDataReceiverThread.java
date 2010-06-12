package org.intrace.client.gui.helper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

import org.intrace.client.gui.TraceWindow;

public class NetworkDataReceiverThread implements Runnable
{
  private final Socket traceSocket;
  private final TraceWindow window;
  private final TraceFilterThread traceThread;

  public NetworkDataReceiverThread(InetAddress address, int networkTracePort,
      TraceWindow traceDialogRef, TraceFilterThread traceThread)
      throws IOException
  {
    this.window = traceDialogRef;
    this.traceThread = traceThread;
    traceSocket = new Socket();
    traceSocket.connect(new InetSocketAddress(address, networkTracePort));
  }

  public void start()
  {
    Thread t = new Thread(this);
    t.setDaemon(true);
    t.setName("Network Data Receiver");
    t.start();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void run()
  {
    try
    {
      ObjectInputStream objIn = new ObjectInputStream(
                                                      traceSocket
                                                                 .getInputStream());
      while (true)
      {
        Object data = objIn.readObject();
        if (data instanceof String)
        {
          String traceLine = (String) data;
          if (!"NOOP".equals(traceLine))
          {
            if (window.getSettings().netOutEnabled)
            {
              traceThread.addTraceLine(traceLine);
            }
          }
        }
        else if (data instanceof Map<?, ?>)
        {
          Map<String, Object> callersMap = (Map<String, Object>) data;
          window.setCallers(callersMap);
        }
      }
    }
    catch (Exception e)
    {
      disconnect();
    }
  }

  public void disconnect()
  {
    try
    {
      traceSocket.close();
    }
    catch (IOException e)
    {
      // Do nothing
    }
  }

}
