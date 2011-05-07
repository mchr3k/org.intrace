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
    DISCONNECTED_ERR, DISCONNECTED, CONNECTING, CONNECTED
  }
  
  public static interface ISocketCallback
  {
    public void setSocket(Socket socket);
  }
  
  public static void connectToAgent(final ISocketCallback owningWindow,
                                    final Shell sShell, final String host,
                                    final String port,
                                    final StatusUpdater statusUpdater)
  {
    if (host.length() == 0)
    {
      displayError(sShell, "Please enter an address");
      statusUpdater.setStatusText("Error: Please enter an address");
      owningWindow.setSocket(null);
    }
    else if (port.length() == 0)
    {
      displayError(sShell, "Please enter a port");
      statusUpdater.setStatusText("Error: Please enter a port");
      owningWindow.setSocket(null);
    }
    else
    {
      new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          statusUpdater.setStatusText("Connecting...");
          final Socket socket = new Socket();
          try
          {
            socket.connect(new InetSocketAddress(host, Integer.valueOf(port)));
            statusUpdater.setStatusText("Connected");
            owningWindow.setSocket(socket);
          }
          catch (Exception e)
          {
            statusUpdater.setStatusText("Error: " + e.toString());
            owningWindow.setSocket(null);
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
