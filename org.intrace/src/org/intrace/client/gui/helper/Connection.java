package org.intrace.client.gui.helper;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class Connection
{
  // State
  public enum ConnectState
  {
    DISCONNECTED_ERR("Disconnected"), 
    DISCONNECTED("Disconnected"), 
    CONNECTING("Connecting"), 
    CONNECTED("Connected");
    public final String str;
    private ConnectState(String xiStr)
    {
      str = xiStr;
    }
  }
  
  public static interface ISocketCallback
  {
    public void setSocket(Socket socket);
    public void setConnectionStatus(final String statusText);
  }
  
  public static void connectToAgent(final ISocketCallback socketCallback,
                                    final Shell sShell, final String host,
                                    final String port)
  {
    if (host.length() == 0)
    {
      displayError(sShell, "Please enter an address");
      socketCallback.setConnectionStatus("Error: Please enter an address");
      socketCallback.setSocket(null);
    }
    else if (port.length() == 0)
    {
      displayError(sShell, "Please enter a port");
      socketCallback.setConnectionStatus("Error: Please enter a port");
      socketCallback.setSocket(null);
    }
    else
    {
      new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          socketCallback.setConnectionStatus("Connecting...");
          final Socket socket = new Socket();
          try
          {
            socket.connect(new InetSocketAddress(host, Integer.valueOf(port)));            
            socketCallback.setSocket(socket);
          }
          catch (Exception e)
          {
            socketCallback.setConnectionStatus("Error: " + e.toString());
            socketCallback.setSocket(null);
          }
        }
      }).start();
    }
  }

  private static void displayError(Shell sShell, String errorMessage)
  {
    MessageBox errorMB = new MessageBox(sShell, SWT.OK | SWT.ICON_WARNING);
    errorMB.setMessage(errorMessage);
    errorMB.setText("Connection Error");
    errorMB.open();
  }
}
