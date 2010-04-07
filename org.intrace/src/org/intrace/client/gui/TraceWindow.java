package org.intrace.client.gui;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TraceWindow
{
  private final String ENABLE_INSTR = "Enable Instrumentation";
  private final String DISABLE_INSTR = "Disable Instrumentation";
  
  private final String SET_CLASSREGEX = "Set Class Regex...";
  
  private final String ENABLE_ALLOWJARS = "Enable JAR Instrumentation";
  private final String DISABLE_ALLOWJARS = "Disable JAR Instrumentation";
  
  private final String ENABLE_SAVECLASSES = "Save Instrumented Classes";
  private final String DISABLE_SAVECLASSES = "Don't Save Classes";
  
  private final String ENABLE_VERBOSEMODE = "Enable Verbose Mode";
  private final String DISABLE_VERBOSEMODE = "Disable Verbose Mode";
  
  private final String ENABLE_EE_TRACE = "Enable Entry/Exit Trace";
  private final String DISABLE_EE_TRACE = "Disable Entry/Exit Trace";
  
  private final String ENABLE_BRANCH_TRACE = "Enable Branch Trace";
  private final String DISABLE_BRANCH_TRACE = "Disable Branch Trace";
  
  private final String ENABLE_ARGS_TRACE = "Enable Args Trace";
  private final String DISABLE_ARGS_TRACE = "Disable Args Trace";
  
  private final String ENABLE_STDOUT_TRACE = "Enable StdOut Trace";
  private final String DISABLE_STDOUT_TRACE = "Disable StdOut Trace";
  
  private final String ENABLE_FILE_TRACE = "Enable File Trace";
  private final String DISABLE_FILE_TRACE = "Disable File Trace";
  
  private final String ENABLE_NETWORK_TRACE = "Enable Network Trace";
  private final String DISABLE_NETWORK_TRACE = "Disable Network Trace";
  
  private ParsedSettingsData settingsData = new ParsedSettingsData(new HashMap<String, String>());  //  @jve:decl-index=0:
  private final ExecutorService asyncExecutor = Executors.newSingleThreadExecutor();  //  @jve:decl-index=0:
  private TraceWindow instanceWindowRef = this;
  
  private boolean networkTraceEnabled = false;
  private NetworkTraceThread networkThread;
  private final ReceiveThread receiveThread;
  
  private Shell sShell = null;
  private Button toggleEntryExitButton = null;
  private Button Disconnect = null;
  private Text statusTextArea = null;
  
  private ConnectionDetails instanceRef;
  private Socket socket;
  private Button toggleStdOutButton = null;
  private Button toggleBranchButton = null;
  private Button toggleArgsButton = null;
  private Label traceSettingsLabel = null;
  private Label outputSettingsLabel = null;
  private Label spacerLabel = null;
  private Composite composite = null;
  private Label instrumentSettingsLabel = null;
  private Button toggleInstrumentEnabled = null;
  private Button setClassRegexButton = null;
  private Button toggleAllowJarInstru = null;
  private Button toggleSaveClassFiles = null;
  private Button toggleVerboseMode = null;
  private Button toggleFileOutputButton = null;
  private Button toggleNetworkTraceButton = null;
  public TraceWindow(ConnectionDetails instanceRef, Socket socket)
  {
    this.instanceRef = instanceRef;
    this.socket = socket;
    receiveThread = new ReceiveThread(socket);
    receiveThread.start();       
  }

  /**
   * This method initializes sShell
   */
  private void createSShell()
  {
    GridData gridData22 = new GridData();
    gridData22.widthHint = 150;
    GridData gridData11 = new GridData();
    gridData11.widthHint = 150;
    GridData gridData7 = new GridData();
    gridData7.widthHint = 150;
    GridData gridData6 = new GridData();
    gridData6.widthHint = 150;
    GridData gridData5 = new GridData();
    gridData5.widthHint = 150;
    GridData gridData41 = new GridData();
    gridData41.widthHint = 150;
    GridData gridData31 = new GridData();
    gridData31.widthHint = 150;
    GridData gridData21 = new GridData();
    gridData21.widthHint = 150;
    GridData gridData1 = new GridData();
    gridData1.widthHint = 150;
    GridData gridData4 = new GridData();
    gridData4.widthHint = 150;
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    GridData gridData2 = new GridData();
    gridData2.widthHint = 150;
    GridData gridData = new GridData();
    gridData.widthHint = 150;
    gridData.verticalAlignment = org.eclipse.swt.layout.GridData.BEGINNING;
    sShell = new Shell();
    sShell.setText("Trace Window");
    sShell.setLayout(gridLayout);
    sShell.setSize(new Point(500, 500));
    instrumentSettingsLabel = new Label(sShell, SWT.NONE);
    instrumentSettingsLabel.setText("Instrumentation Settings:");
    createComposite();
    toggleInstrumentEnabled = new Button(sShell, SWT.LEFT);
    toggleInstrumentEnabled.setText(ENABLE_INSTR);
    toggleInstrumentEnabled.setLayoutData(gridData31);
    toggleInstrumentEnabled
        .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
        {
          public void mouseUp(org.eclipse.swt.events.MouseEvent e)
          {
            toggleSetting(settingsData.instrEnabled, "[instru-true", "[instru-false");
          }
        });
    setClassRegexButton = new Button(sShell, SWT.LEFT);
    setClassRegexButton.setText(SET_CLASSREGEX);
    setClassRegexButton.setLayoutData(gridData41);
    setClassRegexButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        RegexInput regexInput = new RegexInput();
        regexInput.open(instanceWindowRef, settingsData.classRegex);
      }
    });
    toggleAllowJarInstru = new Button(sShell, SWT.LEFT);
    toggleAllowJarInstru.setText(ENABLE_ALLOWJARS);
    toggleAllowJarInstru.setLayoutData(gridData5);
    toggleAllowJarInstru.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        toggleSetting(settingsData.allowJarsToBeTraced, "[instrujars-true", "[instrujars-false");
      }
    });
    toggleSaveClassFiles = new Button(sShell, SWT.LEFT);
    toggleSaveClassFiles.setText(ENABLE_SAVECLASSES);
    toggleSaveClassFiles.setLayoutData(gridData6);
    toggleSaveClassFiles.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        toggleSetting(settingsData.saveTracedClassfiles, "[saveinstru-true", "[saveinstru-false");
      }
    });
    toggleVerboseMode = new Button(sShell, SWT.LEFT);
    toggleVerboseMode.setText(ENABLE_VERBOSEMODE);
    toggleVerboseMode.setLayoutData(gridData7);
    toggleVerboseMode.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        toggleSetting(settingsData.verboseMode, "[verbose-true", "[verbose-false");
      }
    });
    sShell.setMinimumSize(new Point(500, 500));
    traceSettingsLabel = new Label(sShell, SWT.NONE);
    traceSettingsLabel.setText("Trace Settings:");
    sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter()
    {
      public void shellClosed(org.eclipse.swt.events.ShellEvent e)
      {
        disconnect();
        sShell.dispose();
        asyncExecutor.shutdownNow();
      }
    });
    toggleEntryExitButton = new Button(sShell, SWT.LEFT);
    toggleEntryExitButton.setText(ENABLE_EE_TRACE);
    toggleEntryExitButton.setLayoutData(gridData2);
    toggleEntryExitButton
        .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
        {
          public void mouseUp(org.eclipse.swt.events.MouseEvent e)
          {
            toggleSetting(settingsData.entryExitEnabled, "[trace-ee-true", "[trace-ee-false");
          }
        });
    toggleBranchButton = new Button(sShell, SWT.LEFT);
    toggleBranchButton.setText(ENABLE_BRANCH_TRACE);
    toggleBranchButton.setLayoutData(gridData1);
    toggleBranchButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        toggleSetting(settingsData.branchEnabled, "[trace-branch-true", "[trace-branch-false");
      }
    });
    toggleArgsButton = new Button(sShell, SWT.LEFT);
    toggleArgsButton.setText(ENABLE_ARGS_TRACE);
    toggleArgsButton.setLayoutData(gridData21);
    outputSettingsLabel = new Label(sShell, SWT.NONE);
    outputSettingsLabel.setText("Output Modes:");
    toggleArgsButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        toggleSetting(settingsData.argsEnabled, "[trace-args-true", "[trace-args-false");
      }
    });
    toggleStdOutButton = new Button(sShell, SWT.LEFT);
    toggleStdOutButton.setText(ENABLE_STDOUT_TRACE);
    toggleStdOutButton.setLayoutData(gridData4);
    toggleFileOutputButton = new Button(sShell, SWT.LEFT);
    toggleFileOutputButton.setText(ENABLE_FILE_TRACE);
    toggleFileOutputButton.setLayoutData(gridData11);
    toggleNetworkTraceButton = new Button(sShell, SWT.LEFT);
    toggleNetworkTraceButton.setText(ENABLE_NETWORK_TRACE);
    toggleNetworkTraceButton.setLayoutData(gridData22);
    toggleNetworkTraceButton
        .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
        {
          public void mouseUp(org.eclipse.swt.events.MouseEvent e)
          {
            toggleNetworkTrace();            
          }
        });
    toggleFileOutputButton
        .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
        {
          public void mouseUp(org.eclipse.swt.events.MouseEvent e)
          {
            toggleSetting(settingsData.fileOutEnabled, "[trace-file-true", "[trace-file-false");
          }
        });
    spacerLabel = new Label(sShell, SWT.NONE);
    spacerLabel.setText("");
    toggleStdOutButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        toggleSetting(settingsData.stdOutEnabled, "[trace-stdout-true", "[trace-stdout-false");
      }
    });
    Disconnect = new Button(sShell, SWT.LEFT);
    Disconnect.setText("Disconnect");
    Disconnect.setLayoutData(gridData);
    Disconnect.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        sShell.close();
      }
    });
  }

  /**
   * This method initializes composite	
   *
   */
  private void createComposite()
  {
    FillLayout fillLayout = new FillLayout();
    fillLayout.type = org.eclipse.swt.SWT.HORIZONTAL;
    GridData gridData3 = new GridData();
    gridData3.grabExcessVerticalSpace = true;
    gridData3.heightHint = 99999;
    gridData3.widthHint = 99999;
    gridData3.verticalSpan = 17;
    gridData3.grabExcessHorizontalSpace = true;
    composite = new Composite(sShell, SWT.NONE);
    composite.setLayoutData(gridData3);
    composite.setLayout(fillLayout);
    statusTextArea = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
    statusTextArea.setEditable(false);
    statusTextArea.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
  }

  public void open()
  {
    createSShell();
    sShell.open();
    statusTextArea.append("Connected!\n");
    try
    {
      sendMessage("getsettings");
    }
    catch (IOException e)
    {
      sShell.close();
    }
  }
 
  private void toggleNetworkTrace()
  {
    sShell.getDisplay().asyncExec(new Runnable()
    {            
      @Override
      public void run()
      {
        disableButtons();
        asyncExecutor.execute(new Runnable()
        {      
          @Override
          public void run()
          {
            try
            {
              if (!networkTraceEnabled)
              {
                sendMessage("[trace-network");
                String networkTracePortStr = receiveThread.getMessage();
                int networkTracePort = Integer.parseInt(networkTracePortStr);
                networkThread = new NetworkTraceThread(networkTracePort);
                networkThread.start();
                networkTraceEnabled = true;
              }
              else
              {
                networkThread.stop();
                networkTraceEnabled = false;
              }
              sShell.getDisplay().asyncExec(new Runnable()
              {              
                @Override
                public void run()
                {
                  updateButtonText();
                }
              });                        
            }
            catch (IOException e)
            {
              sShell.close();
            }        
          }
        });
      }
    });
  }
  
  public void setRegex(final String regex)
  {
    sShell.getDisplay().asyncExec(new Runnable()
    {            
      @Override
      public void run()
      {
        disableButtons();
        asyncExecutor.execute(new Runnable()
        {      
          @Override
          public void run()
          {
            try
            {
              sendMessage("[regex-" + regex);                                   
              sendMessage("getsettings");
            }
            catch (IOException e)
            {
              sShell.close();
            }        
          }
        });
      }
    });
  }  
  
  private void toggleSetting(final boolean settingValue, final String enableCommand, final String disableCommand)
  {
    sShell.getDisplay().asyncExec(new Runnable()
    {            
      @Override
      public void run()
      {
        disableButtons();
        asyncExecutor.execute(new Runnable()
        {      
          @Override
          public void run()
          {
            try
            {
              if (settingValue)
              {
                sendMessage(disableCommand);
              }
              else
              {
                sendMessage(enableCommand);
              }
              sendMessage("getsettings");
            }
            catch (IOException e)
            {
              sShell.close();
            }        
          }
        });
      }
    }); 
  }
  
  private void disableButtons()
  {
    toggleInstrumentEnabled.setEnabled(false);
    setClassRegexButton.setEnabled(false);
    toggleAllowJarInstru.setEnabled(false);
    toggleSaveClassFiles.setEnabled(false);
    toggleVerboseMode.setEnabled(false);
    toggleEntryExitButton.setEnabled(false);
    toggleBranchButton.setEnabled(false);
    toggleArgsButton.setEnabled(false);
    toggleStdOutButton.setEnabled(false);
    toggleFileOutputButton.setEnabled(false);
    toggleNetworkTraceButton.setEnabled(false);
  }
  
  private void chooseText(Button control, boolean option, String enabledText, String disabledText)
  {
    if (option)
    {
      control.setText(disabledText);
    }
    else
    {
      control.setText(enabledText);
    }
    control.setEnabled(true);
  }
  
  private void updateButtonText()
  {
    statusTextArea.append("Got Settings...\n");
    
    chooseText(toggleInstrumentEnabled, settingsData.instrEnabled, ENABLE_INSTR, DISABLE_INSTR);
    chooseText(toggleAllowJarInstru, settingsData.allowJarsToBeTraced, ENABLE_ALLOWJARS, DISABLE_ALLOWJARS);
    chooseText(toggleSaveClassFiles, settingsData.saveTracedClassfiles, ENABLE_SAVECLASSES, DISABLE_SAVECLASSES);
    chooseText(toggleVerboseMode, settingsData.verboseMode, ENABLE_VERBOSEMODE, DISABLE_VERBOSEMODE);
    chooseText(toggleEntryExitButton, settingsData.entryExitEnabled, ENABLE_EE_TRACE, DISABLE_EE_TRACE);
    chooseText(toggleBranchButton, settingsData.branchEnabled, ENABLE_BRANCH_TRACE, DISABLE_BRANCH_TRACE);
    chooseText(toggleArgsButton, settingsData.argsEnabled, ENABLE_ARGS_TRACE, DISABLE_ARGS_TRACE);
    chooseText(toggleStdOutButton, settingsData.stdOutEnabled, ENABLE_STDOUT_TRACE, DISABLE_STDOUT_TRACE);
    chooseText(toggleFileOutputButton, settingsData.fileOutEnabled, ENABLE_FILE_TRACE, DISABLE_FILE_TRACE);
    chooseText(toggleNetworkTraceButton, networkTraceEnabled, ENABLE_NETWORK_TRACE, DISABLE_NETWORK_TRACE);
    setClassRegexButton.setEnabled(true);
  }
  
  private void disconnect()
  {
    try
    {
      socket.close();
    }
    catch (IOException e)
    {
      // Throw away
    }
    instanceRef.show();
  }

  private void sendMessage(String xiString) throws IOException
  {
    OutputStream out = socket.getOutputStream();
    ObjectOutputStream objOut = new ObjectOutputStream(out);
    objOut.writeObject(xiString);
    objOut.flush();
  }
  
  private class ReceiveThread implements Runnable
  {
    private final Socket receiveSocket;
    private final BlockingQueue<String> queuedMessages = new LinkedBlockingQueue<String>();
    public ReceiveThread(Socket socket)
    {
      receiveSocket = socket;
    }

    public void start()
    {
      Thread t = new Thread(this);
      t.setDaemon(true);
      t.setName("Network Receive Thread");
      t.start();
    }
    
    public String getMessage()
    {
      try
      {
        return queuedMessages.take();
      }
      catch (InterruptedException e)
      {
        return null;
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run()
    {
      try
      {                
        while (true)
        {
          ObjectInputStream objIn = new ObjectInputStream(receiveSocket.getInputStream());
          Object receivedMessage = (Object)objIn.readObject();
          if (receivedMessage instanceof Map<?,?>)
          {
            Map<String,String> settingsMap = (Map<String,String>)receivedMessage;
            handleConfig(settingsMap);
          }
          else
          {
            String strMessage = (String)receivedMessage;
            if (!"OK".equals(strMessage))
            {
              queuedMessages.put(strMessage);
            }
          }
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
        sShell.getDisplay().asyncExec(new Runnable()
        {          
          @Override
          public void run()
          {
            if (!sShell.isDisposed())
            {
              sShell.close();
            }
          }
        });        
      }
    }
    
    private void handleConfig(final Map<String, String> settingsMap)
    {
      sShell.getDisplay().asyncExec(new Runnable()
      {      
        @Override
        public void run()
        {
          statusTextArea.append("Fetching Settings...\n");
          settingsData = new ParsedSettingsData(settingsMap);
          updateButtonText();           
        }
      });    
    }    
  }
  
  private class NetworkTraceThread implements Runnable
  {
    private final Socket traceSocket;
    public NetworkTraceThread(int networkTracePort) throws IOException
    {
      traceSocket = new Socket();
      traceSocket.connect(new InetSocketAddress(socket.getInetAddress(), networkTracePort));
    }

    public void start()
    {
      Thread t = new Thread(this);
      t.setDaemon(true);
      t.setName("Network Trace");
      t.start();
    }
    
    public void stop()
    {
      try
      {
        traceSocket.close();
      }
      catch (IOException e)
      {
        // Throw away
      }
    }

    @Override
    public void run()
    {
      try
      {        
        ObjectInputStream objIn = new ObjectInputStream(traceSocket.getInputStream());
        while (true)
        {
          final String traceLine = (String)objIn.readObject();
          sShell.getDisplay().asyncExec(new Runnable()
          {           
            @Override
            public void run()
            {
              statusTextArea.append(traceLine + "\n");              
            }
          });
        }
      }
      catch (Exception e)
      {
        sShell.close();
      }
    }
    
  }
}
