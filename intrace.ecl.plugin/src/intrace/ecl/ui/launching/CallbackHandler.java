package intrace.ecl.ui.launching;

import java.net.ServerSocket;
import java.net.Socket;

public class CallbackHandler implements Runnable
{
  private final ServerSocket server;
  private Socket clientConnection = null;

  public CallbackHandler(ServerSocket xiServer)
  {
    this.server = xiServer;    
  }
  
  @Override
  public void run()
  {
    try
    {
      clientConnection = server.accept();
      notifyClientConnection();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  private synchronized void notifyClientConnection()
  {
    this.notifyAll();
  }
  
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
