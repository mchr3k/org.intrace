package org.intrace.client.gui;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NewConnectionWindow
{
  Shell sShell = null; // @jve:decl-index=0:visual-constraint="10,10"
  private Text addressText = null;
  private Text portText = null;
  private Label addressLabel = null;
  private Label portLabel = null;
  private Button connectButton = null;
  private Label statusLabel = null;

  private TraceWindow mainWindowRef;

  public void open(TraceWindow mainWindowRef)
  {
    this.mainWindowRef = mainWindowRef;
    createSShell();
  }

  /**
   * This method initializes sShell
   */
  private void createSShell()
  {
    GridData gridData11 = new GridData();
    gridData11.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
    gridData11.widthHint = 400;
    GridData gridData2 = new GridData();
    gridData2.widthHint = 400;
    GridData gridData1 = new GridData();
    gridData1.widthHint = 400;
    GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.widthHint = 200;
    gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    sShell = new Shell(SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.APPLICATION_MODAL);
    sShell.setText("Trace Client");
    sShell.setLayout(gridLayout);
    sShell.setSize(new Point(475, 126));
    sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter()
    {
      @Override
      public void shellClosed(org.eclipse.swt.events.ShellEvent e)
      {
        sShell.dispose();
      }
    });
    addressLabel = new Label(sShell, SWT.NONE);
    addressLabel.setText("Address:");
    addressText = new Text(sShell, SWT.BORDER);
    addressText.setText("localhost");
    addressText.setLayoutData(gridData1);
    portLabel = new Label(sShell, SWT.NONE);
    portLabel.setText("Port:");
    portText = new Text(sShell, SWT.BORDER);
    portText.setText("9123");
    portText.setLayoutData(gridData2);
    @SuppressWarnings("unused")
    Label filler = new Label(sShell, SWT.NONE);
    connectButton = new Button(sShell, SWT.NONE);
    connectButton.setText("Connect");
    connectButton.setLayoutData(gridData);
    Label filler1 = new Label(sShell, SWT.NONE);
    filler1.setText("Status:");
    statusLabel = new Label(sShell, SWT.WRAP | SWT.LEFT);
    statusLabel.setText("Enter Details Above");
    statusLabel.setLayoutData(gridData11);
    connectButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        String host = addressText.getText();
        String port = portText.getText();
        connectToAgent(host, port);
      }
    });
  }

  private void connectToAgent(final String host, final String port)
  {
    if (host.length() == 0)
    {
      sShell.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          statusLabel.setText("Please enter an address");
        }
      });
    }
    else if (port.length() == 0)
    {
      sShell.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          statusLabel.setText("Please enter a port");
        }
      });
    }
    else
    {
      sShell.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          statusLabel.setText("Connecting...");
          new Thread(new Runnable()
          {
            @Override
            public void run()
            {
              final Socket socket = new Socket();
              String statusMessage;
              try
              {
                socket.connect(new InetSocketAddress(host, Integer.valueOf(port)));
                statusMessage = "Connected";
                sShell.getDisplay().asyncExec(new Runnable()
                {
                  @Override
                  public void run()
                  {
                    mainWindowRef.setConnection(socket);
                    sShell.close();
                  }
                });
              }
              catch (Exception e)
              {
                statusMessage = e.toString();
              }
              final String statusMessageRef = statusMessage;
              sShell.getDisplay().asyncExec(new Runnable()
              {
                @Override
                public void run()
                {
                  if (!sShell.isDisposed())
                  {
                    statusLabel.setText(statusMessageRef);
                  }
                }
              });
            }
          }).start();
        }
      });
    }
  }
}
