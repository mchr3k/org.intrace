package org.intrace.client.gui;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.intrace.client.gui.helper.ControlConnectionThread;
import org.intrace.client.gui.helper.NetworkDataReceiverThread;
import org.intrace.client.gui.helper.ParsedSettingsData;
import org.intrace.shared.CallersConfigConstants;

public class TraceWindow
{
  // Window refs
  private final TraceWindow traceDialogRef = this;
  private final NewConnectionWindow newConnectionDialogRef;

  // Network details
  private final InetAddress remoteAddress;
  private boolean networkTraceEnabled = false;

  // Threads
  private NetworkDataReceiverThread networkTraceThread;
  private final ControlConnectionThread controlThread;

  // Settings
  private ParsedSettingsData settingsData = new ParsedSettingsData(new HashMap<String, String>());  //  @jve:decl-index=0:

  // UI Elements
  private Shell sShell = null;
  private Button toggleEntryExitButton = null;
  private Button disconnectButton = null;
  private Text statusTextArea = null;
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
  private TabFolder outputTabFolder = null;
  private TabItem textOutputTabItem = null;
  private TabItem callersOutputTabItem = null;
  private Tree callersTree = null;
  private Label callersLabel = null;
  private Button callersStateButton = null;

  public TraceWindow(NewConnectionWindow instanceRef, Socket socket)
  {
    this.newConnectionDialogRef = instanceRef;
    this.remoteAddress = socket.getInetAddress();
    controlThread = new ControlConnectionThread(socket, this);
    controlThread.start();
  }

  /**
   * This method initializes sShell
   */
  private void createSShell()
  {
    GridData gridData12 = new GridData();
    gridData12.widthHint = 150;
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
    sShell.setSize(new Point(700, 500));
    instrumentSettingsLabel = new Label(sShell, SWT.NONE);
    instrumentSettingsLabel.setText("Instrumentation Settings:");
    createComposite();
    toggleInstrumentEnabled = new Button(sShell, SWT.LEFT);
    toggleInstrumentEnabled.setText(ClientStrings.ENABLE_INSTR);
    toggleInstrumentEnabled.setLayoutData(gridData31);
    toggleInstrumentEnabled
    .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        toggleSetting(settingsData.instrEnabled, "[instru-true", "[instru-false");
      }
    });
    setClassRegexButton = new Button(sShell, SWT.LEFT);
    setClassRegexButton.setText(ClientStrings.SET_CLASSREGEX);
    setClassRegexButton.setLayoutData(gridData41);
    setClassRegexButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        InstruRegexInputWindow regexInput = new InstruRegexInputWindow();
        regexInput.open(traceDialogRef, settingsData.classRegex);
      }
    });
    toggleAllowJarInstru = new Button(sShell, SWT.LEFT);
    toggleAllowJarInstru.setText(ClientStrings.ENABLE_ALLOWJARS);
    toggleAllowJarInstru.setLayoutData(gridData5);
    toggleAllowJarInstru.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        toggleSetting(settingsData.allowJarsToBeTraced, "[instrujars-true", "[instrujars-false");
      }
    });
    toggleSaveClassFiles = new Button(sShell, SWT.LEFT);
    toggleSaveClassFiles.setText(ClientStrings.ENABLE_SAVECLASSES);
    toggleSaveClassFiles.setLayoutData(gridData6);
    toggleSaveClassFiles.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        toggleSetting(settingsData.saveTracedClassfiles, "[saveinstru-true", "[saveinstru-false");
      }
    });
    toggleVerboseMode = new Button(sShell, SWT.LEFT);
    toggleVerboseMode.setText(ClientStrings.ENABLE_VERBOSEMODE);
    toggleVerboseMode.setLayoutData(gridData7);
    toggleVerboseMode.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        toggleSetting(settingsData.verboseMode, "[verbose-true", "[verbose-false");
      }
    });
    sShell.setMinimumSize(new Point(700, 500));
    traceSettingsLabel = new Label(sShell, SWT.NONE);
    traceSettingsLabel.setText("Trace Settings:");
    sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter()
    {
      @Override
      public void shellClosed(org.eclipse.swt.events.ShellEvent e)
      {
        disconnect();
        newConnectionDialogRef.show();
        sShell.dispose();
      }
    });
    toggleEntryExitButton = new Button(sShell, SWT.LEFT);
    toggleEntryExitButton.setText(ClientStrings.ENABLE_EE_TRACE);
    toggleEntryExitButton.setLayoutData(gridData2);
    toggleEntryExitButton
    .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        toggleSetting(settingsData.entryExitEnabled, "[trace-ee-true", "[trace-ee-false");
      }
    });
    toggleBranchButton = new Button(sShell, SWT.LEFT);
    toggleBranchButton.setText(ClientStrings.ENABLE_BRANCH_TRACE);
    toggleBranchButton.setLayoutData(gridData1);
    toggleBranchButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        toggleSetting(settingsData.branchEnabled, "[trace-branch-true", "[trace-branch-false");
      }
    });
    toggleArgsButton = new Button(sShell, SWT.LEFT);
    toggleArgsButton.setText(ClientStrings.ENABLE_ARGS_TRACE);
    toggleArgsButton.setLayoutData(gridData21);
    callersLabel = new Label(sShell, SWT.NONE);
    callersLabel.setText("Callers Settings:");
    callersStateButton = new Button(sShell, SWT.NONE);
    callersStateButton.setLayoutData(gridData12);
    callersStateButton.setText(ClientStrings.BEGIN_CAPTURE_CALLERS);
    callersStateButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        if (!settingsData.callersCaptureInProgress)
        {
          CallersRegexInputWindow regexInput = new CallersRegexInputWindow();
          regexInput.open(traceDialogRef, settingsData.callersRegex);
        }
        else
        {
          toggleSetting(settingsData.callersCaptureInProgress, "[callers-enabled-true", "[callers-enabled-false");
        }
      }
    });
    outputSettingsLabel = new Label(sShell, SWT.NONE);
    outputSettingsLabel.setText("Output Modes:");
    toggleArgsButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        toggleSetting(settingsData.argsEnabled, "[trace-args-true", "[trace-args-false");
      }
    });
    toggleStdOutButton = new Button(sShell, SWT.LEFT);
    toggleStdOutButton.setText(ClientStrings.ENABLE_STDOUT_OUTPUT);
    toggleStdOutButton.setLayoutData(gridData4);
    toggleFileOutputButton = new Button(sShell, SWT.LEFT);
    toggleFileOutputButton.setText(ClientStrings.ENABLE_FILE_OUTPUT);
    toggleFileOutputButton.setLayoutData(gridData11);
    toggleNetworkTraceButton = new Button(sShell, SWT.LEFT);
    toggleNetworkTraceButton.setText(ClientStrings.ENABLE_NETWORK_OUTPUT);
    toggleNetworkTraceButton.setLayoutData(gridData22);
    toggleNetworkTraceButton
    .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        toggleNetworkTrace();
      }
    });
    toggleFileOutputButton
    .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        toggleSetting(settingsData.fileOutEnabled, "[out-file-true", "[out-file-false");
      }
    });
    spacerLabel = new Label(sShell, SWT.NONE);
    spacerLabel.setText("");
    toggleStdOutButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        toggleSetting(settingsData.stdOutEnabled, "[out-stdout-true", "[out-stdout-false");
      }
    });
    disconnectButton = new Button(sShell, SWT.LEFT);
    disconnectButton.setText("Disconnect");
    disconnectButton.setLayoutData(gridData);
    disconnectButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
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
    gridData3.verticalSpan = 19;
    gridData3.grabExcessHorizontalSpace = true;
    composite = new Composite(sShell, SWT.NONE);
    composite.setLayoutData(gridData3);
    composite.setLayout(fillLayout);
    createOutputTabFolder();
  }

  public void open()
  {
    createSShell();
    sShell.open();
    addMessage("*** Connected!");
    controlThread.sendMessage("getsettings");
  }

  public void disconnect()
  {
    controlThread.disconnect();
    if (networkTraceThread != null)
    {
      networkTraceThread.disconnect();
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
        if (!networkTraceEnabled)
        {
          controlThread.sendMessage("[out-network");
          String networkTracePortStr = controlThread.getMessage();
          int networkTracePort = Integer.parseInt(networkTracePortStr);
          try
          {
            networkTraceThread = new NetworkDataReceiverThread(remoteAddress, networkTracePort, traceDialogRef);
            networkTraceThread.start();
            networkTraceEnabled = true;
          }
          catch (IOException ex)
          {
            addMessage("*** Failed to setup network trace: " + ex.toString());
          }
        }
        else
        {
          networkTraceThread.disconnect();
          networkTraceEnabled = false;
        }
        updateButtonText();
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
        controlThread.sendMessage("[regex-" + regex);
        controlThread.sendMessage("getsettings");
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
        if (settingValue)
        {
          controlThread.sendMessage(disableCommand);
        }
        else
        {
          controlThread.sendMessage(enableCommand);
        }
        controlThread.sendMessage("getsettings");
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

    callersStateButton.setEnabled(false);

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
    addMessage("*** Latest Settings Received");

    chooseText(toggleInstrumentEnabled, settingsData.instrEnabled, ClientStrings.ENABLE_INSTR, ClientStrings.DISABLE_INSTR);
    chooseText(toggleAllowJarInstru, settingsData.allowJarsToBeTraced, ClientStrings.ENABLE_ALLOWJARS, ClientStrings.DISABLE_ALLOWJARS);
    chooseText(toggleSaveClassFiles, settingsData.saveTracedClassfiles, ClientStrings.ENABLE_SAVECLASSES, ClientStrings.DISABLE_SAVECLASSES);
    chooseText(toggleVerboseMode, settingsData.verboseMode, ClientStrings.ENABLE_VERBOSEMODE, ClientStrings.DISABLE_VERBOSEMODE);

    chooseText(toggleEntryExitButton, settingsData.entryExitEnabled, ClientStrings.ENABLE_EE_TRACE, ClientStrings.DISABLE_EE_TRACE);
    chooseText(toggleBranchButton, settingsData.branchEnabled, ClientStrings.ENABLE_BRANCH_TRACE, ClientStrings.DISABLE_BRANCH_TRACE);
    chooseText(toggleArgsButton, settingsData.argsEnabled, ClientStrings.ENABLE_ARGS_TRACE, ClientStrings.DISABLE_ARGS_TRACE);

    chooseText(callersStateButton, settingsData.callersCaptureInProgress, ClientStrings.BEGIN_CAPTURE_CALLERS, ClientStrings.END_CAPTURE_CALLERS);

    chooseText(toggleStdOutButton, settingsData.stdOutEnabled, ClientStrings.ENABLE_STDOUT_OUTPUT, ClientStrings.DISABLE_STDOUT_OUTPUT);
    chooseText(toggleFileOutputButton, settingsData.fileOutEnabled, ClientStrings.ENABLE_FILE_OUTPUT, ClientStrings.DISABLE_FILE_OUTPUT);
    chooseText(toggleNetworkTraceButton, networkTraceEnabled, ClientStrings.ENABLE_NETWORK_OUTPUT, ClientStrings.DISABLE_NETWORK_OUTPUT);
    setClassRegexButton.setEnabled(true);
  }

  public void setConfig(final Map<String, String> settingsMap)
  {
    sShell.getDisplay().asyncExec(new Runnable()
    {
      @Override
      public void run()
      {
        addMessage("*** Fetch Settings");
        settingsData = new ParsedSettingsData(settingsMap);
        updateButtonText();
      }
    });
  }

  public void addMessage(final String message)
  {
    sShell.getDisplay().asyncExec(new Runnable()
    {
      @Override
      public void run()
      {
        statusTextArea.append(message + "\n");
      }
    });
  }

  /**
   * This method initializes outputTabFolder
   *
   */
  private void createOutputTabFolder()
  {
    outputTabFolder = new TabFolder(composite, SWT.NONE);
    statusTextArea = new Text(outputTabFolder, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
    statusTextArea.setEditable(false);
    statusTextArea.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
    callersTree = new Tree(outputTabFolder, SWT.BORDER);

    textOutputTabItem = new TabItem(outputTabFolder, SWT.NONE);
    textOutputTabItem.setControl(statusTextArea);
    textOutputTabItem.setText("Text Output");
    callersOutputTabItem = new TabItem(outputTabFolder, SWT.NONE);
    callersOutputTabItem.setControl(callersTree);
    callersOutputTabItem.setText("Callers");
  }

  public void setCallers(final Map<String, Object> callersMap)
  {
    callersMap.remove(CallersConfigConstants.MAP_ID);
    sShell.getDisplay().asyncExec(new Runnable()
    {
      @Override
      public void run()
      {
        callersTree.removeAll();
        addCallersData(callersTree, callersMap);
      }
    });
  }

  @SuppressWarnings("unchecked")
  private void addCallersData(Object parentItem, Map<String, Object> callersMap)
  {
    for (Entry<String, Object> mapEntry : callersMap.entrySet())
    {
      String entryName = mapEntry.getKey();
      TreeItem item;
      if (parentItem instanceof Tree)
      {
        item = new TreeItem((Tree)parentItem, SWT.NULL);
      }
      else
      {
        item = new TreeItem((TreeItem)parentItem, SWT.NULL);
      }
      item.setText(entryName);
      Object entryValue = mapEntry.getValue();
      if (entryValue instanceof Map<?,?>)
      {
        Map<String, Object> entryMap = (Map<String, Object>)entryValue;
        if (entryMap.size() > 0)
        {
          addCallersData(item, entryMap);
        }
      }
    }
  }

  public void setCallersRegex(String regex)
  {
    if (!settingsData.callersCaptureInProgress)
    {
      controlThread.sendMessage("[callers-regex-" + regex);
      toggleSetting(settingsData.callersCaptureInProgress, "[callers-enabled-true", "[callers-enabled-false");
    }
  }
}
