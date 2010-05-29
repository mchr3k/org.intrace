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
  /**
   * @param args
   */
  public static void main(String[] args)
  {
    new DevTraceWindow().open();
  }

  private void open()
  {
    sWindow.open();
    Display display = Display.getDefault();
    while (!sWindow.isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }

  private Shell sWindow = null;

  private DevTraceWindow()
  {
    MigLayout windowLayout = new MigLayout("fill", "", "[130][grow]");

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

  private ConnectionTab connTab;
  private InstruTab instruTab;
  private OutputSettingsTab outSettingsTab;
  private TraceTab traceTab;
  private CallersSettingsTab callSettingsTab;

  private void fillButtonTabs(TabFolder tabFolder)
  {
    TabItem connTabItem = new TabItem(tabFolder, SWT.NONE);
    connTabItem.setText("Connection");
    connTab = new ConnectionTab(tabFolder, connTabItem);

    TabItem instrTabItem = new TabItem(tabFolder, SWT.NONE);
    instrTabItem.setText("Instrumentation");
    instruTab = new InstruTab(tabFolder, instrTabItem);

    TabItem outputSettingsTabItem = new TabItem(tabFolder, SWT.NONE);
    outputSettingsTabItem.setText("Output");
    outSettingsTab = new OutputSettingsTab(tabFolder, outputSettingsTabItem);

    TabItem traceTabItem = new TabItem(tabFolder, SWT.NONE);
    traceTabItem.setText("Trace");
    traceTab = new TraceTab(tabFolder, traceTabItem);

    TabItem callersTabItem = new TabItem(tabFolder, SWT.NONE);
    callersTabItem.setText("Callers");
    callSettingsTab = new CallersSettingsTab(tabFolder, callersTabItem);
  }

  private class ConnectionTab
  {
    final Button connectButton;
    final StatusUpdater connectStatus;
    final Text addressInput;
    final Text portInput;

    private ConnectionTab(TabFolder tabFolder, TabItem connTab)
    {
      MigLayout windowLayout = new MigLayout("fill", "[300][grow][100]");

      Composite composite = new Composite(tabFolder, SWT.NONE);
      composite.setLayout(windowLayout);
      connTab.setControl(composite);

      Group connectionGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      MigLayout connGroupLayout = new MigLayout("fill", "[80][40][grow]");
      connectionGroup.setLayout(connGroupLayout);
      connectionGroup.setText("Details");
      connectionGroup.setLayoutData("spany,grow");

      connectButton = new Button(connectionGroup, SWT.LEFT);
      connectButton.setText(ClientStrings.CONNECT);
      connectButton.setAlignment(SWT.CENTER);
      connectButton.setLayoutData("spany,grow");

      Label addressLabel = new Label(connectionGroup, SWT.NONE);
      addressLabel.setText(ClientStrings.CONN_ADDRESS);
      addressLabel.setLayoutData("right");
      addressInput = new Text(connectionGroup, SWT.BORDER);
      addressInput.setText("localhost");
      addressInput.setLayoutData("grow,gapy 8px,wrap");

      Label portLabel = new Label(connectionGroup, SWT.NONE);
      portLabel.setText(ClientStrings.CONN_PORT);
      portLabel.setLayoutData("right");
      portInput = new Text(connectionGroup, SWT.BORDER);
      portInput.setText("9123");
      portInput.setLayoutData("grow,wrap");

      Group statusGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      statusGroup.setLayoutData("spany,grow");
      MigLayout groupLayout = new MigLayout("fill", "[align center]");
      statusGroup.setLayout(groupLayout);
      statusGroup.setText("Status");

      final Label statusLabel = new Label(statusGroup, SWT.NONE);
      statusLabel.setText("Disconnected");
      statusLabel.setAlignment(SWT.CENTER);
      statusLabel.setLayoutData("grow,wmax 230");
      connectStatus = new StatusUpdater(sWindow, statusLabel);

      Button printSettings = new Button(composite, SWT.TOGGLE);
      printSettings.setText(ClientStrings.DUMP_SETTINGS);
      printSettings.setAlignment(SWT.CENTER);
      printSettings.setLayoutData("gap 10px 0px 5px,spany,grow,wrap");

      connectButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
      {
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
        {
          if ((connectionState == ConnectState.DISCONNECTED)
              || (connectionState == ConnectState.DISCONNECTED_ERR))
          {
            connectionState = ConnectState.CONNECTING;
            updateUIStateSameThread();
            Connection.connectToAgent(traceDialogRef, sWindow,
                                      addressInput.getText(),
                                      portInput.getText(), connectStatus);
          }
          else if (connectionState == ConnectState.CONNECTED)
          {
            disconnect();
          }
        }
      });
    }
  }

  private class InstruTab
  {
    private InstruTab(TabFolder tabFolder, TabItem instrTab)
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
  }

  private class OutputSettingsTab
  {
    private OutputSettingsTab(TabFolder tabFolder, TabItem outputSettingsTab)
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
  }

  private class TraceTab
  {
    private TraceTab(TabFolder tabFolder, TabItem traceTab)
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
  }

  private class CallersSettingsTab
  {
    private CallersSettingsTab(TabFolder tabFolder, TabItem callersTab)
    {
      MigLayout windowLayout = new MigLayout("fill", "[100][100][100][grow]");

      Composite composite = new Composite(tabFolder, SWT.NONE);
      composite.setLayout(windowLayout);
      callersTab.setControl(composite);

      Button callersCapture = new Button(composite, SWT.PUSH);
      callersCapture.setText(ClientStrings.BEGIN_CAPTURE_CALLERS);
      callersCapture.setLayoutData("spany,grow");
    }
  }

  TextOutputTab textOutputTab;
  CallersTab callersTab;

  private void fillOutputTabs(TabFolder tabFolder)
  {
    TabItem textOutputTabItem = new TabItem(tabFolder, SWT.NONE);
    textOutputTabItem.setText("Output");
    textOutputTab = new TextOutputTab(tabFolder, textOutputTabItem);

    TabItem callersTabItem = new TabItem(tabFolder, SWT.NONE);
    callersTabItem.setText("Callers");
    callersTab = new CallersTab(tabFolder, callersTabItem);
  }

  private class TextOutputTab
  {
    private TextOutputTab(TabFolder tabFolder, TabItem textOutputTab)
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
  }

  private class CallersTab
  {
    private CallersTab(TabFolder tabFolder, TabItem callersTab)
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
  }

  // Window ref
  private final DevTraceWindow traceDialogRef = this;

  // State
  private enum ConnectState
  {
    DISCONNECTED_ERR, DISCONNECTED, CONNECTING, CONNECTED
  }

  private ConnectState connectionState = ConnectState.DISCONNECTED;

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
      connectionState = ConnectState.CONNECTED;
      // updateButtonText();
    }
    else
    {
      connectionState = ConnectState.DISCONNECTED_ERR;
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
    connectionState = ConnectState.DISCONNECTED;
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

    if (connectionState == ConnectState.CONNECTING)
    {
      connTab.connectButton.setText(ClientStrings.CONNECTING);
      connTab.connectButton.setEnabled(false);
      connTab.addressInput.setEnabled(false);
      connTab.portInput.setEnabled(false);
    }
    else
    {
      chooseText(connTab.connectButton,
                 (connectionState == ConnectState.CONNECTED),
                 ClientStrings.CONNECT, ClientStrings.DISCONNECT);
      if (connectionState == ConnectState.DISCONNECTED)
      {
        connTab.connectStatus.setStatusText("Disconnected");
      }
      if ((connectionState == ConnectState.DISCONNECTED)
          || (connectionState == ConnectState.DISCONNECTED_ERR))
      {
        connTab.addressInput.setEnabled(true);
        connTab.portInput.setEnabled(true);
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
