package org.intrace.client.gui.helper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.intrace.client.gui.TraceWindow;

public class NetworkTraceReceiverThread implements Runnable
{
  private final Socket traceSocket;
  private final TraceWindow window;
  public NetworkTraceReceiverThread(InetAddress address, int networkTracePort, TraceWindow window) throws IOException
  {
    this.window = window; 
    traceSocket = new Socket();
    traceSocket.connect(new InetSocketAddress(address, networkTracePort));
  }

  public void start()
  {
    Thread t = new Thread(this);
    t.setDaemon(true);
    t.setName("Network Trace Receiver");
    t.start();
  }

  @Override
  public void run()
  {
    try
    {        
      ObjectInputStream objIn = new ObjectInputStream(traceSocket.getInputStream());
      while (true)
      {
        String traceLine = (String)objIn.readObject();
        window.addMessage(traceLine);
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
