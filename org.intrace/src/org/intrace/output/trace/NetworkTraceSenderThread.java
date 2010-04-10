package org.intrace.output.trace;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NetworkTraceSenderThread implements Runnable
{
  private final ServerSocket networkSocket;
  private final BlockingQueue<String> outgoingTrace = new LinkedBlockingQueue<String>(100);
  private Set<NetworkTraceSenderThread> set;
  
  public NetworkTraceSenderThread(ServerSocket networkSocket)
  {
    this.networkSocket = networkSocket; 
  }

  public void start(Set<NetworkTraceSenderThread> set)
  {
    this.set = set; 
    
    Thread networkThread = new Thread(this);
    networkThread.setDaemon(true);
    networkThread.setName("Network Trace Sender");
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
    System.out.println("## Network Trace Disconnected");
  }
  
  public void queueTrace(String traceLine)
  {
    try
    {
      outgoingTrace.put(traceLine);
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
          String traceLine = outgoingTrace.take();
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
