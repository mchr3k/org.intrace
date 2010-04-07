package org.intrace.agent.server;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.intrace.agent.AgentHelper;
import org.intrace.agent.ClassTransformer;

/**
 * Server thread handling a single connected client.
 */
public class AgentClientConnection implements Runnable
{
  private final Socket connectedClient;
  private final ClassTransformer transformer;
  private final AgentServer serverRef;

  /**
   * cTor
   * @param agentServer 
   * @param xiConnectedClient
   * @param xiTransformer
   */
  public AgentClientConnection(AgentServer agentServer, 
                               Socket xiConnectedClient,
                               ClassTransformer xiTransformer)
  {
    super();
    serverRef = agentServer;
    connectedClient = xiConnectedClient;
    transformer = xiTransformer;
    System.out.println("Connected to: " + xiConnectedClient.getPort());
  }

  @Override
  public void run()
  {
      try
      {
        boolean quit = false;
        while (!quit)
        {
          try
          {
            String message = receiveMessage();
            if (message.equals("getsettings"))
            {
              Map<String,String> settingsMap = new HashMap<String, String>();
              settingsMap.putAll(transformer.getSettings());
              settingsMap.putAll(AgentHelper.getActiveOutputHandler().getSettingsMap());
              serverRef.broadcastMessage(this, settingsMap);
            }
            else
            {
              String response = transformer.getResponse(message);
              if (response != null)
              {
                sendMessage(response);
              }
              else
              {
                sendMessage("OK");
              }
            }
          }
          catch (IOException e)
          {
            quit = true;
          }
        }
        System.out.println("Disconnected from: " + connectedClient.getPort());
        connectedClient.close();
      }
      catch (IOException e1)
      {
        e1.printStackTrace();
      }
      finally
      {
        serverRef.removeClientConnection(this);
      }
  }

  private String receiveMessage() throws IOException
  {
    InputStream in = connectedClient.getInputStream();
    ObjectInputStream objIn = new ObjectInputStream(in);
    try
    {
      return (String)objIn.readObject();
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
    return null;
  }

  public void sendMessage(Object xiObject) throws IOException
  {
    OutputStream out = connectedClient.getOutputStream();
    ObjectOutputStream objOut = new ObjectOutputStream(out);
    objOut.writeObject(xiObject);
    objOut.flush();
  }
}
