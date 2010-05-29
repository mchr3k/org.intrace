package org.intrace.client.gui;

import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
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

public class DevTraceWindow
{
  private Shell sWindow = null;

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    DevTraceWindow window = new DevTraceWindow();
    window.open();
  }

  private void open()
  {
    createWindow();
    sWindow.open();
    Display display = Display.getDefault();
    while (!sWindow.isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }

  private void createWindow()
  {
    MigLayout windowLayout = new MigLayout("fill", "", "[100][grow]");

    sWindow = new Shell();
    sWindow.setText("Trace Window");
    sWindow.setLayout(windowLayout);
    sWindow.setSize(new Point(700, 750));
    sWindow.setMinimumSize(new Point(700, 750));

    TabFolder buttonTabs = new TabFolder(sWindow, SWT.NONE);
    TabFolder outputTabs = new TabFolder(sWindow, SWT.NONE);
    buttonTabs.setLayoutData("grow,wrap");
    outputTabs.setLayoutData("grow");

    fillButtonTabs(buttonTabs);
    fillOutputTabs(outputTabs);
  }

  private void fillButtonTabs(TabFolder tabFolder)
  {
    TabItem connTab = new TabItem(tabFolder, SWT.NONE);
    connTab.setText("Connection");
    fillConnectionTab(tabFolder, connTab);

    TabItem instrTab = new TabItem(tabFolder, SWT.NONE);
    instrTab.setText("Instrumentation");
    fillInstruTab(tabFolder, instrTab);

    TabItem outputSettingsTab = new TabItem(tabFolder, SWT.NONE);
    outputSettingsTab.setText("Output");
    fillOutputSettingsTab(tabFolder, outputSettingsTab);

    TabItem traceTab = new TabItem(tabFolder, SWT.NONE);
    traceTab.setText("Trace");
    fillTraceTab(tabFolder, traceTab);

    TabItem callersTab = new TabItem(tabFolder, SWT.NONE);
    callersTab.setText("Callers");
    fillCallersSettingsTab(tabFolder, callersTab);
  }

  Button connectButton;
  StatusUpdater connectStatus;

  private void fillConnectionTab(TabFolder tabFolder, TabItem connTab)
  {
    MigLayout windowLayout = new MigLayout("fill",
                                           "[100][50][100][300][grow][100]");

    Composite composite = new Composite(tabFolder, SWT.NONE);
    composite.setLayout(windowLayout);
    connTab.setControl(composite);

    connectButton = new Button(composite, SWT.LEFT);
    connectButton.setText(ClientStrings.CONNECT);
    connectButton.setAlignment(SWT.CENTER);
    connectButton.setLayoutData("spany,grow");

    Label addressLabel = new Label(composite, SWT.NONE);
    addressLabel.setText(ClientStrings.CONN_ADDRESS);
    addressLabel.setLayoutData("right");
    final Text addressInput = new Text(composite, SWT.BORDER);
    addressInput.setText("localhost");
    addressInput.setLayoutData("grow,gapy 8px");

    Group statusGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
    MigLayout groupLayout = new MigLayout("fill");
    statusGroup.setLayout(groupLayout);
    statusGroup.setText("Connection Status");
    statusGroup.setLayoutData("spany,grow");

    final Label statusLabel = new Label(statusGroup, SWT.NONE);
    statusLabel.setText("Disconnected");
    statusLabel.setAlignment(SWT.CENTER);
    statusLabel.setLayoutData("grow");
    connectStatus = new StatusUpdater(sWindow, statusLabel);

    Button printSettings = new Button(composite, SWT.TOGGLE);
    printSettings.setText(ClientStrings.DUMP_SETTINGS);
    printSettings.setAlignment(SWT.CENTER);
    printSettings.setLayoutData("skip,spany,grow,wrap");

    Label portLabel = new Label(composite, SWT.NONE);
    portLabel.setText(ClientStrings.CONN_PORT);
    portLabel.setLayoutData("right");
    final Text portInput = new Text(composite, SWT.BORDER);
    portInput.setText("9123");
    portInput.setLayoutData("grow,wrap");

    connectButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        if ((connected == ConnectState.DISCONNECTED)
            || (connected == ConnectState.DISCONNECTED_ERR))
        {
          connected = ConnectState.CONNECTING;
          updateUIStateSameThread();
          Connection.connectToAgent(traceDialogRef, sWindow,
                                    addressInput.getText(),
                                    portInput.getText(), connectStatus);
        }
        else if (connected == ConnectState.CONNECTED)
        {
          disconnect();
        }
      }
    });
  }

  private void fillInstruTab(TabFolder tabFolder, TabItem instrTab)
  {
    MigLayout windowLayout = new MigLayout("fill",
                                           "[100][100][100][grow][100][100]");

    Composite composite = new Composite(tabFolder, SWT.NONE);
    composite.setLayout(windowLayout);
    instrTab.setControl(composite);

    Button togInstru = new Button(composite, SWT.TOGGLE);
    togInstru.setText(ClientStrings.ENABLE_INSTR);
    togInstru.setAlignment(SWT.CENTER);
    togInstru.setLayoutData("spany,grow");

    Button classRegex = new Button(composite, SWT.PUSH);
    classRegex.setText(ClientStrings.SET_CLASSREGEX);
    classRegex.setLayoutData("gapx 10px,spany,grow");

    Button listClasses = new Button(composite, SWT.PUSH);
    listClasses.setText(ClientStrings.LIST_MODIFIED_CLASSES);
    listClasses.setAlignment(SWT.CENTER);
    listClasses.setLayoutData("spany,grow");

    Button togJars = new Button(composite, SWT.TOGGLE);
    togJars.setText(ClientStrings.ENABLE_ALLOWJARS);
    togJars.setAlignment(SWT.CENTER);
    togJars.setLayoutData("skip,growx");

    Button togSaveClasses = new Button(composite, SWT.TOGGLE);
    togSaveClasses.setText(ClientStrings.ENABLE_SAVECLASSES);
    togSaveClasses.setAlignment(SWT.CENTER);
    togSaveClasses.setLayoutData("growx,wrap");

    Button togVerbose = new Button(composite, SWT.TOGGLE);
    togVerbose.setText(ClientStrings.ENABLE_VERBOSEMODE);
    togVerbose.setAlignment(SWT.CENTER);
    togVerbose.setLayoutData("skip,growx");

  }

  private void fillOutputSettingsTab(TabFolder tabFolder,
                                     TabItem outputSettingsTab)
  {
    MigLayout windowLayout = new MigLayout("fill", "[100][100][100][grow]");

    Composite composite = new Composite(tabFolder, SWT.NONE);
    composite.setLayout(windowLayout);
    outputSettingsTab.setControl(composite);

    Button stdOutOutput = new Button(composite, SWT.TOGGLE);
    stdOutOutput.setText(ClientStrings.ENABLE_STDOUT_OUTPUT);
    stdOutOutput.setLayoutData("spany,grow");

    Button fileOutput = new Button(composite, SWT.TOGGLE);
    fileOutput.setText(ClientStrings.ENABLE_FILE_OUTPUT);
    fileOutput.setAlignment(SWT.CENTER);
    fileOutput.setLayoutData("spany,grow");

    Button networkOutput = new Button(composite, SWT.TOGGLE);
    networkOutput.setText(ClientStrings.ENABLE_NETWORK_OUTPUT);
    networkOutput.setAlignment(SWT.CENTER);
    networkOutput.setLayoutData("spany,grow");
  }

  private void fillTraceTab(TabFolder tabFolder, TabItem traceTab)
  {
    MigLayout windowLayout = new MigLayout("fill", "[100][100][100][grow]");

    Composite composite = new Composite(tabFolder, SWT.NONE);
    composite.setLayout(windowLayout);
    traceTab.setControl(composite);

    Button entryExitTrace = new Button(composite, SWT.TOGGLE);
    entryExitTrace.setText(ClientStrings.ENABLE_EE_TRACE);
    entryExitTrace.setLayoutData("spany,grow");

    Button branchTrace = new Button(composite, SWT.TOGGLE);
    branchTrace.setText(ClientStrings.ENABLE_BRANCH_TRACE);
    branchTrace.setAlignment(SWT.CENTER);
    branchTrace.setLayoutData("spany,grow");

    Button argsTrace = new Button(composite, SWT.TOGGLE);
    argsTrace.setText(ClientStrings.ENABLE_ARGS_TRACE);
    argsTrace.setAlignment(SWT.CENTER);
    argsTrace.setLayoutData("spany,grow");
  }

  private void fillCallersSettingsTab(TabFolder tabFolder, TabItem callersTab)
  {
    MigLayout windowLayout = new MigLayout("fill", "[100][100][100][grow]");

    Composite composite = new Composite(tabFolder, SWT.NONE);
    composite.setLayout(windowLayout);
    callersTab.setControl(composite);

    Button callersCapture = new Button(composite, SWT.PUSH);
    callersCapture.setText(ClientStrings.BEGIN_CAPTURE_CALLERS);
    callersCapture.setLayoutData("spany,grow");
  }

  private void fillOutputTabs(TabFolder tabFolder)
  {
    TabItem textOutputTab = new TabItem(tabFolder, SWT.NONE);
    textOutputTab.setText("Output");
    fillTextOutputTab(tabFolder, textOutputTab);

    TabItem callersTab = new TabItem(tabFolder, SWT.NONE);
    callersTab.setText("Callers");
    fillCallersTab(tabFolder, callersTab);
  }

  private void fillTextOutputTab(TabFolder tabFolder, TabItem textOutputTab)
  {
    MigLayout windowLayout = new MigLayout("fill", "[70][grow]", "[20][grow]");

    Composite composite = new Composite(tabFolder, SWT.NONE);
    composite.setLayout(windowLayout);
    textOutputTab.setControl(composite);

    Button callersCapture = new Button(composite, SWT.PUSH);
    callersCapture.setText(ClientStrings.CLEAR_TEXT);
    callersCapture.setLayoutData("wrap,grow");

    Text statusTextArea = new Text(composite, SWT.MULTI | SWT.WRAP
                                              | SWT.V_SCROLL | SWT.BORDER);
    statusTextArea.setEditable(false);
    statusTextArea.setLayoutData("spanx,grow");
    statusTextArea.setBackground(Display.getCurrent()
                                        .getSystemColor(SWT.COLOR_WHITE));

  }

  private void fillCallersTab(TabFolder tabFolder, TabItem callersTab)
  {
    MigLayout windowLayout = new MigLayout("fill");

    Composite composite = new Composite(tabFolder, SWT.NONE);
    composite.setLayout(windowLayout);
    callersTab.setControl(composite);

    Tree callersTree = new Tree(composite, SWT.BORDER);
    callersTree.setLayoutData("grow");
    TreeItem callersTreeRoot = new TreeItem(callersTree, SWT.NULL);
    callersTreeRoot.setText("Callers");
  }

  // Window ref
  private final DevTraceWindow traceDialogRef = this;

  // State
  private enum ConnectState
  {
    DISCONNECTED_ERR, DISCONNECTED, CONNECTING, CONNECTED
  }

  private ConnectState connected = ConnectState.DISCONNECTED;

  // Network details
  private InetAddress remoteAddress;
  private final boolean networkDataEnabled = false;

  // Threads
  private NetworkDataReceiverThread networkTraceThread;
  private ControlConnectionThread controlThread;

  // Settings
  private final ParsedSettingsData settingsData = new ParsedSettingsData(
                                                                         new HashMap<String, String>());

  public void setConnectionState(Socket socket)
  {
    if (socket != null)
    {
      this.remoteAddress = socket.getInetAddress();
      controlThread = new ControlConnectionThread(socket, this);
      controlThread.start();
      controlThread.sendMessage("getsettings");
      connected = ConnectState.CONNECTED;
      // updateButtonText();
    }
    else
    {
      connected = ConnectState.DISCONNECTED_ERR;
    }
    updateUIState();
  }

  public void disconnect()
  {
    if (controlThread != null)
    {
      controlThread.disconnect();
    }
    if (networkTraceThread != null)
    {
      networkTraceThread.disconnect();
    }
    connected = ConnectState.DISCONNECTED;
    updateUIState();
  }

  // private void toggleNetworkTrace()
  // {
  // sShell.getDisplay().asyncExec(new Runnable()
  // {
  // @Override
  // public void run()
  // {
  // disableButtons();
  // if (!networkDataEnabled)
  // {
  // controlThread.sendMessage("[out-network");
  // String networkTracePortStr = controlThread.getMessage();
  // int networkTracePort = Integer.parseInt(networkTracePortStr);
  // try
  // {
  // networkTraceThread = new NetworkDataReceiverThread(
  // remoteAddress,
  // networkTracePort,
  // traceDialogRef);
  // networkTraceThread.start();
  // networkDataEnabled = true;
  // }
  // catch (IOException ex)
  // {
  // addMessage("*** Failed to setup network trace: " + ex.toString());
  // }
  // }
  // else
  // {
  // networkTraceThread.disconnect();
  // networkDataEnabled = false;
  // }
  // updateButtonText();
  // }
  // });
  // }

  // private void getModifiedClasses()
  // {
  // new Thread(new Runnable()
  // {
  // @Override
  // public void run()
  // {
  // controlThread.sendMessage("[listmodifiedclasses");
  // String modifiedClasses = controlThread.getMessage();
  // if (modifiedClasses.length() <= 2)
  // {
  // addMessage("*** No modified classes");
  // }
  // else
  // {
  // modifiedClasses = modifiedClasses
  // .substring(
  // 1,
  // modifiedClasses.length() - 1);
  // if (modifiedClasses.indexOf(",") == -1)
  // {
  // addMessage("*** Modified: " + modifiedClasses);
  // }
  // else
  // {
  // String[] classNames = modifiedClasses.split(",");
  // for (String className : classNames)
  // {
  // addMessage("*** Modified: " + className);
  // }
  // }
  // }
  // }
  // }).start();
  // }
  //
  // public void setRegex(final String regex)
  // {
  // sShell.getDisplay().asyncExec(new Runnable()
  // {
  // @Override
  // public void run()
  // {
  // disableButtons();
  // controlThread.sendMessage("[regex-" + regex);
  // controlThread.sendMessage("getsettings");
  // }
  // });
  // }
  //
  // public void setCallersRegex(final String regex)
  // {
  // if (!settingsData.callersCaptureInProgress)
  // {
  // controlThread.sendMessage("[callers-regex-" + regex);
  // toggleSetting(settingsData.callersCaptureInProgress,
  // "[callers-enabled-true", "[callers-enabled-false");
  // sShell.getDisplay().asyncExec(new Runnable()
  // {
  // @Override
  // public void run()
  // {
  // callersTreeRoot.removeAll();
  // }
  // });
  // }
  // }
  //
  // private void toggleSetting(final boolean settingValue,
  // final String enableCommand,
  // final String disableCommand)
  // {
  // sShell.getDisplay().asyncExec(new Runnable()
  // {
  // @Override
  // public void run()
  // {
  // disableButtons();
  // if (settingValue)
  // {
  // controlThread.sendMessage(disableCommand);
  // }
  // else
  // {
  // controlThread.sendMessage(enableCommand);
  // }
  // controlThread.sendMessage("getsettings");
  // }
  // });
  // }

  private void chooseText(Button control, boolean option, String enabledText,
                          String disabledText)
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

  private void updateUIState()
  {
    if (!sWindow.isDisposed())
    {
      sWindow.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          updateUIStateSameThread();
        }
      });
    }
  }

  private void updateUIStateSameThread()
  {
    // if (connected)
    // {
    // chooseText(toggleInstrumentEnabled, settingsData.instrEnabled,
    // ClientStrings.ENABLE_INSTR, ClientStrings.DISABLE_INSTR);
    // chooseText(toggleAllowJarInstru, settingsData.allowJarsToBeTraced,
    // ClientStrings.ENABLE_ALLOWJARS,
    // ClientStrings.DISABLE_ALLOWJARS);
    // chooseText(toggleSaveClassFiles, settingsData.saveTracedClassfiles,
    // ClientStrings.ENABLE_SAVECLASSES,
    // ClientStrings.DISABLE_SAVECLASSES);
    // chooseText(toggleVerboseMode, settingsData.verboseMode,
    // ClientStrings.ENABLE_VERBOSEMODE,
    // ClientStrings.DISABLE_VERBOSEMODE);
    // listModifiedClasses.setEnabled(true);
    //
    // chooseText(toggleEntryExitButton, settingsData.entryExitEnabled,
    // ClientStrings.ENABLE_EE_TRACE, ClientStrings.DISABLE_EE_TRACE);
    // chooseText(toggleBranchButton, settingsData.branchEnabled,
    // ClientStrings.ENABLE_BRANCH_TRACE,
    // ClientStrings.DISABLE_BRANCH_TRACE);
    // chooseText(toggleArgsButton, settingsData.argsEnabled,
    // ClientStrings.ENABLE_ARGS_TRACE,
    // ClientStrings.DISABLE_ARGS_TRACE);
    //
    // chooseText(callersStateButton, settingsData.callersCaptureInProgress,
    // ClientStrings.BEGIN_CAPTURE_CALLERS,
    // ClientStrings.END_CAPTURE_CALLERS);
    // callersStateButton.setEnabled(networkDataEnabled);
    //
    // chooseText(toggleStdOutButton, settingsData.stdOutEnabled,
    // ClientStrings.ENABLE_STDOUT_OUTPUT,
    // ClientStrings.DISABLE_STDOUT_OUTPUT);
    // chooseText(toggleFileOutputButton, settingsData.fileOutEnabled,
    // ClientStrings.ENABLE_FILE_OUTPUT,
    // ClientStrings.DISABLE_FILE_OUTPUT);
    // chooseText(toggleNetworkTraceButton, networkDataEnabled,
    // ClientStrings.ENABLE_NETWORK_OUTPUT,
    // ClientStrings.DISABLE_NETWORK_OUTPUT);
    // setClassRegexButton.setEnabled(true);
    //
    // dumpSettingsButton.setEnabled(true);
    // }
    // else
    // {
    // disableButtons();
    // }

    if (connected == ConnectState.CONNECTING)
    {
      connectButton.setText(ClientStrings.CONNECTING);
      connectButton.setEnabled(false);
    }
    else
    {
      chooseText(connectButton, (connected == ConnectState.CONNECTED),
                 ClientStrings.CONNECT, ClientStrings.DISCONNECT);
      if (connected == ConnectState.DISCONNECTED)
      {
        connectStatus.setStatusText("Disconnected");
      }
    }
  }

  // private void disableButtons()
  // {
  // toggleInstrumentEnabled.setEnabled(false);
  // setClassRegexButton.setEnabled(false);
  // toggleAllowJarInstru.setEnabled(false);
  // toggleSaveClassFiles.setEnabled(false);
  // toggleVerboseMode.setEnabled(false);
  // listModifiedClasses.setEnabled(false);
  //
  // toggleEntryExitButton.setEnabled(false);
  // toggleBranchButton.setEnabled(false);
  // toggleArgsButton.setEnabled(false);
  //
  // callersStateButton.setEnabled(false);
  //
  // toggleStdOutButton.setEnabled(false);
  // toggleFileOutputButton.setEnabled(false);
  // toggleNetworkTraceButton.setEnabled(false);
  //
  // dumpSettingsButton.setEnabled(false);
  // }

  // public void setConfig(final Map<String, String> settingsMap)
  // {
  // sShell.getDisplay().asyncExec(new Runnable()
  // {
  // @Override
  // public void run()
  // {
  // addMessage("*** Latest Settings Received");
  // settingsData = new ParsedSettingsData(settingsMap);
  // updateButtonText();
  // }
  // });
  // }
  //
  // public void addMessage(final String message)
  // {
  // if (!sShell.isDisposed())
  // {
  // sShell.getDisplay().asyncExec(new Runnable()
  // {
  // @Override
  // public void run()
  // {
  // statusTextArea.append(message + "\n");
  // }
  // });
  // }
  // }
  //
  // public void setCallers(final Map<String, Object> callersMap)
  // {
  // final Object finalFlag = callersMap.remove(CallersConfigConstants.FINAL);
  // final Object callersRegex = callersMap
  // .remove(CallersConfigConstants.PATTERN);
  // sShell.getDisplay().asyncExec(new Runnable()
  // {
  // @Override
  // public void run()
  // {
  // String newRootText = "Callers: " + callersRegex;
  // String currentRootText = callersTreeRoot.getText();
  // if (!currentRootText.equals(newRootText))
  // {
  // callersTreeRoot.removeAll();
  // callersTreeRoot.setText(newRootText);
  // }
  // setCallersData(callersTreeRoot, callersMap);
  // if ("true".equals(finalFlag))
  // {
  // outputTabFolder.setSelection(callersOutputTabItem);
  // }
  // }
  // });
  // }
  //
  // @SuppressWarnings("unchecked")
  // private void setCallersData(TreeItem parentItem,
  // Map<String, Object> callersMap)
  // {
  // for (Entry<String, Object> mapEntry : callersMap.entrySet())
  // {
  // String entryName = mapEntry.getKey();
  //
  // TreeItem item = null;
  // // Look for existing item
  // for (TreeItem iter_item : parentItem.getItems())
  // {
  // if (iter_item.getText().equals(entryName))
  // {
  // item = iter_item;
  // break;
  // }
  // }
  //
  // // Create new item
  // if (item == null)
  // {
  // item = new TreeItem(parentItem, SWT.NULL);
  // item.setText(entryName);
  // }
  //
  // // Process children
  // Object entryValue = mapEntry.getValue();
  // if (entryValue instanceof Map<?, ?>)
  // {
  // Map<String, Object> entryMap = (Map<String, Object>) entryValue;
  // if (entryMap.size() > 0)
  // {
  // setCallersData(item, entryMap);
  // }
  // }
  // }
  // }
}
