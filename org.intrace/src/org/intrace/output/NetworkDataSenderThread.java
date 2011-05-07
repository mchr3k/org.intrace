package org.intrace.output;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.intrace.agent.server.AgentClientConnection;

public class NetworkDataSenderThread implements Runnable
{
  private boolean alive = true;
  private final ServerSocket networkSocket;
  private Socket traceSendingSocket = null;
  private final BlockingQueue<Object> outgoingData = new LinkedBlockingQueue<Object>(
                                                                                     30);
  private Set<NetworkDataSenderThread> set;
  private final AgentClientConnection connection;

  public NetworkDataSenderThread(AgentClientConnection connection, ServerSocket networkSocket)
  {
    this.connection = connection;
    this.networkSocket = networkSocket;
  }

  public void start(Set<NetworkDataSenderThread> set)
  {
    this.set = set;

    Thread networkThread = new Thread(this);
    networkThread.setDaemon(true);
    networkThread.setName(Thread.currentThread().getName()
                          + " - Network Data Sender");
    networkThread.start();
  }

  private void stop()
  {
    try
    {
      if (connection != null)
      {
        connection.setTraceConnEstablished(false);
      }
      alive = false;
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
      while (alive && !outgoingData.offer(data, 1, TimeUnit.SECONDS))
      {
        // Do nothing - work is done above
      }
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
      
      if (connection != null)
      {
        connection.setTraceConnEstablished(true);
      }
      
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
        traceWriter.flush();
        traceWriter.reset();
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
