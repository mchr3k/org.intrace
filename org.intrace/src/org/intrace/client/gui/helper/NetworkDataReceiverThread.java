package org.intrace.client.gui.helper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.intrace.shared.SerializationHelper;


public class NetworkDataReceiverThread implements Runnable
{
  public static interface INetworkOutputConfig
  {
    public boolean isNetOutputEnabled();
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
              traceThread.addTraceLine(traceLine);
            }
          }
        } else if ( data instanceof byte[]) {
        	String[] myObj1 = SerializationHelper.fromWire( (byte[])data);
        	for(String s : myObj1)
        		traceThread.addTraceLine(s);
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
