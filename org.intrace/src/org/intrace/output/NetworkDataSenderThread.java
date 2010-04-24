package org.intrace.output;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class NetworkDataSenderThread implements Runnable
{
  private final ServerSocket networkSocket;
  private Socket traceSendingSocket = null;
  private final BlockingQueue<Object> outgoingData = new LinkedBlockingQueue<Object>(
                                                                                     100);
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
      if (traceSendingSocket != null)
      {
        traceSendingSocket.close();
      }
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
    try
    {
      traceSendingSocket = networkSocket.accept();
      traceSendingSocket.setKeepAlive(true);
      ObjectOutputStream traceWriter = new ObjectOutputStream(
                                                              traceSendingSocket
                                                                                .getOutputStream());
      while (true)
      {
        Object traceLine = outgoingData.poll(5, TimeUnit.SECONDS);
        if (traceLine != null)
        {
          traceWriter.writeObject(traceLine);
        }
        else
        {
          traceWriter.writeObject("NOOP");
        }
      }
    }
    catch (InterruptedException ex)
    {
      stop();
    }
    catch (IOException ex)
    {
      stop();
    }
  }
}
