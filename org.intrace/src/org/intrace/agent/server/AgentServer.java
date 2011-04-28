package org.intrace.agent.server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.intrace.agent.ClassTransformer;

/**
 * TCP Server used for communication with Trace clients.
 */
public class AgentServer implements Runnable
{
  // CTor field
  private final ClassTransformer transformer;

  // Map of client connections
  private static final Map<AgentClientConnection, Object> clientConnections = new ConcurrentHashMap<AgentClientConnection, Object>();

  // Target server port
  private final int serverPort;

  /**
   * cTor s
   * 
   * @param xiT
   * @param serverPort 
   */
  public AgentServer(ClassTransformer xiT, int xiServerPort)
  {
    transformer = xiT;
    serverPort = xiServerPort;
  }

  /**
   * @param connection
   *          Remove this connection.
   */
  public static void addClientConnection(AgentClientConnection connection)
  {
    clientConnections.put(connection, new Object());
  }

  /**
   * @param connection
   *          Remove this connection.
   */
  public static void removeClientConnection(AgentClientConnection connection)
  {
    clientConnections.remove(connection);
  }

  /**
   * Broadcast a message to all currently connected clients.
   * 
   * @param requestingConn
   * @param message
   * @throws IOException
   */
  public static void broadcastMessage(AgentClientConnection requestingConn,
                                      Object message) throws IOException
  {
    IOException ex = null;
    for (AgentClientConnection clientConn : clientConnections.keySet())
    {
      try
      {
        clientConn.sendMessage(message);
      }
      catch (IOException ioex)
      {
        // Only remember exceptions for the connection sending the message
        if (requestingConn == clientConn)
        {
          ex = ioex;
        }
      }
    }
    if (ex != null)
    {
      throw ex;
    }
  }

  /**
   * Main server loop
   */
  @Override
  public void run()
  {
    // Server constants

    // Number used for naming client threads
    int clientNum = 1;

    // Number of allowed exceptions before we give up
    int numAllowedExceptions = 10;

    // Default listen port - we increment the port if we cannot listen on this
    // port
    int tracePort = serverPort;

    while (numAllowedExceptions > 0)
    {
      try
      {
        ServerSocket serversock = new ServerSocket(tracePort);
        System.out.println("## Listening on port " + serversock.getLocalPort());
        System.setProperty("org.intrace.port",
                           Integer.toString(serversock.getLocalPort()));
        while (true)
        {
          Socket connectedClient = serversock.accept();
          AgentClientConnection clientConnection = new AgentClientConnection(
                                                                             connectedClient,
                                                                             transformer);
          addClientConnection(clientConnection);
          clientConnection.start(clientNum);
          clientNum++;
        }
      }
      catch (BindException e)
      {
        numAllowedExceptions--;
        System.out.println("## Unable to listen on port: " + tracePort);
        tracePort++;
      }
      catch (Throwable t)
      {
        numAllowedExceptions--;
        t.printStackTrace();
        try
        {
          Thread.sleep(5 * 1000);
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
        }
      }
    }
    System.out.println("## Too many exceptions - server thread quitting.");
  }

  /**
   * Start the Server - create a new, named, daemon thread.
   */
  public void start()
  {
    Thread traceServer = new Thread(this);
    traceServer.setName("TraceServer");
    traceServer.setDaemon(true);
    traceServer.start();
  }
}
