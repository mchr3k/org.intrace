package org.intrace.client.gui.helper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.intrace.shared.Base64;


public class NetworkDataReceiverThread implements Runnable
{
  public static interface INetworkOutputConfig
  {
    public boolean isNetOutputEnabled();
    public boolean isGzipEnabled();
  }
  
  private final Socket traceSocket;
  private final INetworkOutputConfig outputConfig;
  private final TraceFilterThread traceThread;

  public NetworkDataReceiverThread(InetAddress address, int networkTracePort,
      INetworkOutputConfig outputConfig, TraceFilterThread traceThread)
      throws IOException
  {
    this.outputConfig = outputConfig;
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
            if (outputConfig.isNetOutputEnabled())
            {
              if (outputConfig.isGzipEnabled()) {
            	  byte[] tmp = Base64.decode(traceLine);
            	  traceLine = new String(tmp);
              }
              traceThread.addTraceLine(traceLine);
            }
          }
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
