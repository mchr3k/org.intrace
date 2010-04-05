package org.intrace.agent.server;


import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import org.intrace.agent.ClassTransformer;

/**
 * TCP Server used for communication with the Trace client.
 */
public class AgentServer implements Runnable
{
  private final ClassTransformer transformer;

  /**
   * cTor
   * @param xiT
   */
  public AgentServer(ClassTransformer xiT)
  {
    transformer= xiT;
  }

  @Override
  public void run()
  {
    int clientNum = 1;
    int numAllowedExcept = 10;
    int tracePort = 9123;
    while (numAllowedExcept > 0)
    {
      try
      {
        ServerSocket serversock = new ServerSocket(tracePort);
        System.out.println("Listening on port " + serversock.getLocalPort());
        while (true)
        {
          Socket connectedClient = serversock.accept();
          Thread clientThread = new Thread(new AgentServerConnection(connectedClient, transformer));
          clientThread.setDaemon(true);
          clientThread.setName("AgentServer-Client" + clientNum);
          clientThread.start();
          clientNum++;
        }
      }
      catch (BindException e)
      {
        numAllowedExcept--;
        System.out.println("Unable to listen on port: " + tracePort);
        tracePort++;
      }
      catch (Throwable t)
      {
        numAllowedExcept--;
        t.printStackTrace();
        try
        {
          Thread.sleep(5 * 1000);
        }
        catch (InterruptedException e)
        {
          // Throw away
        }
      }
    }
    System.out.println("Too many exceptions - server thread quitting.");
  }
}
