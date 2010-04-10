package org.intrace.output;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NetworkDataSenderThread implements Runnable
{
  private final ServerSocket networkSocket;
  private final BlockingQueue<Object> outgoingData = new LinkedBlockingQueue<Object>(100);
  private Set<NetworkDataSenderThread> set;

  public NetworkDataSenderThread(ServerSocket networkSocket)
  {
    this.networkSocket = networkSocket;
  }

  public void start(Set<NetworkDataSenderThread> set)
  {
    this.set = set;

    Thread networkThread = new Thread(this);
    networkThread.setDaemon(true);
    networkThread.setName("Network Data Sender");
    networkThread.start();
  }

  private void stop()
  {
    try
    {
      networkSocket.close();
    }
    catch (IOException e)
    {
      // Throw away
    }
    set.remove(this);
    System.out.println("## Network Data Connection Disconnected");
  }

  public void queueData(Object data)
  {
    try
    {
      outgoingData.put(data);
    }
    catch (InterruptedException e)
    {
      // Throw away
    }
  }

  @Override
  public void run()
  {
    Socket traceSendingSocket;
    try
    {
      traceSendingSocket = networkSocket.accept();
      try
      {
        ObjectOutputStream traceWriter = new ObjectOutputStream(traceSendingSocket.getOutputStream());
        while (true)
        {
          Object traceLine = outgoingData.take();
          traceWriter.writeObject(traceLine);
        }
      }
      catch (Exception e)
      {
        traceSendingSocket.close();
        throw e;
      }
    }
    catch (Exception e1)
    {
      stop();
    }
  }
}
