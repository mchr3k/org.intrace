package intrace.ecl.ui.launching;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import org.intrace.shared.AgentConfigConstants;

public class ConnectionHolder implements Runnable
{
  private final ServerSocket server;
  private Socket clientConnection = null;
  public String agentServerPort = null;

  public ConnectionHolder(ServerSocket xiServer)
  {
    this.server = xiServer;    
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void run()
  {
    try
    {
      clientConnection = server.accept();
      
      // Connected! Time to discover the server port.      
      ObjectOutputStream out = new ObjectOutputStream(clientConnection.getOutputStream());
      out.writeObject("getsettings");
      out.flush();
      
      ObjectInputStream in = new ObjectInputStream(clientConnection.getInputStream());
      Object obj = in.readObject();
      if (obj instanceof Map<?,?>)
      {
        Map<String,String> settingsMap = (Map<String,String>)obj;
        agentServerPort = settingsMap.get(AgentConfigConstants.SERVER_PORT);
      }
      
      // Notify the UI that we have the connection
      notifyClientConnection();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  /**
   * Mark that a connection is ready
   */
  private synchronized void notifyClientConnection()
  {
    this.notifyAll();
  }
  
  /**
   * @return The active connection.
   * @throws InterruptedException
   */
  public synchronized Socket getClientConnection() throws InterruptedException
  {
    if (clientConnection == null)
    {
      this.wait();
    }
    return clientConnection;
  }
  
  /**
   * Start the callback thread
   */
  public void start()
  {
    Thread callbackThread = new Thread(this);
    callbackThread.setDaemon(true);
    callbackThread.setName("InTrace-Callback");
    callbackThread.start();
  }
}
