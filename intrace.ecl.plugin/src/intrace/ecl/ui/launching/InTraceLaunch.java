package intrace.ecl.ui.launching;

import intrace.ecl.Util;
import intrace.ecl.ui.output.InTraceEditor;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import org.eclipse.ui.statushandlers.StatusManager;
import org.intrace.shared.AgentConfigConstants;

/**
 * This class wraps the state associated with a single InTrace enabled launch.
 */
public class InTraceLaunch implements Runnable
{  
  /**
   * Server socket used for callback connection from InTrace agent
   */
  private final ServerSocket callbackserver;
  
  /**
   * Active connection established as part of InTrace callback
   */
  private Socket clientConnection = null;
  
  /**
   * Server port which the InTrace agent is listening on
   */
  public String agentServerPort = null;
  
  /**
   * Currently active InTrace editor
   */
  private InTraceEditor editor = null;

  /**
   * Construct an instance to listen on a provided ServerSocket
   * @param xiServer
   */
  public InTraceLaunch(ServerSocket xiServer)
  {
    this.callbackserver = xiServer;    
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void run()
  {
    try
    {
      // Listen for incoming callback connection
      clientConnection = callbackserver.accept();
      
      // Connected! Time to discover the server port.      
      ObjectOutputStream out = new ObjectOutputStream(clientConnection.getOutputStream());
      out.writeObject("getsettings");
      out.flush();
      
      // Read the returned settings
      ObjectInputStream in = new ObjectInputStream(clientConnection.getInputStream());
      Object obj = in.readObject();
      if (obj instanceof Map<?,?>)
      {
        Map<String,String> settingsMap = (Map<String,String>)obj;
        agentServerPort = settingsMap.get(AgentConfigConstants.SERVER_PORT);
      }
      
      // Notify the UI that we have got a connection
      notifyClientConnection();
    }
    catch (Throwable th)
    {
      Util.handleStatus(Util.createErrorStatus("Error during InTrace launch", th), 
                        StatusManager.SHOW | StatusManager.LOG);
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
   * Start the callback connection thread
   */
  public void start()
  {
    Thread callbackConnectionThread = new Thread(this);
    callbackConnectionThread.setDaemon(true);
    callbackConnectionThread.setName("InTrace-Launch-CallbackHandler");
    callbackConnectionThread.start();
  }

  /**
   * @return The active connection.
   * @throws InterruptedException
   */
  public synchronized Socket getClientConnection() throws InterruptedException
  {
    while (clientConnection == null)
    {
      this.wait();
    }
    return clientConnection;
  }
  
  public synchronized InTraceEditor getEditor()
  {
    return editor;
  }

  public synchronized void setEditor(InTraceEditor editor)
  {
    this.editor = editor;
  }
}
