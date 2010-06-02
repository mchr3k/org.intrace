package org.intrace.client.gui;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
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
import org.intrace.client.gui.PatternInputWindow.PatternInputCallback;
import org.intrace.client.gui.helper.Connection;
import org.intrace.client.gui.helper.ControlConnectionThread;
import org.intrace.client.gui.helper.NetworkDataReceiverThread;
import org.intrace.client.gui.helper.ParsedSettingsData;
import org.intrace.client.gui.helper.StatusUpdater;
import org.intrace.shared.CallersConfigConstants;

public class TraceWindow
{
  public void open()
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
  final TabFolder outputTabs;

  public TraceWindow()
  {
    MigLayout windowLayout = new MigLayout("fill", "", "[130][grow]");

    sWindow = new Shell();
    sWindow.setText("Trace Window");
    sWindow.setLayout(windowLayout);
    sWindow.setSize(new Point(700, 750));
    sWindow.setMinimumSize(new Point(700, 750));

    TabFolder buttonTabs = new TabFolder(sWindow, SWT.NONE);
    outputTabs = new TabFolder(sWindow, SWT.NONE);
    buttonTabs.setLayoutData("grow,wrap,wmin 0");
    outputTabs.setLayoutData("grow");

    fillButtonTabs(buttonTabs);
    fillOutputTabs(outputTabs);

    updateUIStateSameThread();
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
    final Text addressInput;
    final Text portInput;
    final StatusUpdater connectStatus;
    final Button printSettings;

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
      connectionGroup.setLayoutData("spany,grow,wmin 300");

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
      statusGroup.setLayoutData("spany,grow,wmin 0");
      MigLayout groupLayout = new MigLayout("fill", "[align center]");
      statusGroup.setLayout(groupLayout);
      statusGroup.setText("Status");

      final Label statusLabel = new Label(statusGroup, SWT.WRAP);
      statusLabel.setAlignment(SWT.CENTER);
      statusLabel.setLayoutData("grow,wmin 0");
      connectStatus = new StatusUpdater(sWindow, statusLabel);

      printSettings = new Button(composite, SWT.PUSH);
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

      printSettings.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
      {
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
        {
          addMessage("Settings:" + settingsData.toString());
        }
      });
    }
  }

  private class InstruTab
  {
    final Button togInstru;
    final Button classRegex;
    final Button listClasses;
    final Button togJars;
    final Button togSaveClasses;
    final Button togVerbose;

    private InstruTab(TabFolder tabFolder, TabItem instrTab)
    {
      MigLayout windowLayout = new MigLayout("fill", "[][grow][][]");

      Composite composite = new Composite(tabFolder, SWT.NONE);
      composite.setLayout(windowLayout);
      instrTab.setControl(composite);

      Group mainControlGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      MigLayout mainControlGroupLayout = new MigLayout("fill",
                                                       "[100][100][100]");
      mainControlGroup.setLayout(mainControlGroupLayout);
      mainControlGroup.setText("");
      mainControlGroup.setLayoutData("spany,grow");

      togInstru = new Button(mainControlGroup, SWT.TOGGLE);
      togInstru.setText(ClientStrings.ENABLE_INSTR);
      togInstru.setAlignment(SWT.CENTER);
      togInstru.setLayoutData("spany,grow");

      classRegex = new Button(mainControlGroup, SWT.PUSH);
      classRegex.setText(ClientStrings.SET_CLASSREGEX);
      classRegex.setLayoutData("gapx 10px,spany,grow");

      listClasses = new Button(mainControlGroup, SWT.PUSH);
      listClasses.setText(ClientStrings.LIST_MODIFIED_CLASSES);
      listClasses.setAlignment(SWT.CENTER);
      listClasses.setLayoutData("spany,grow");

      Group settingsGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      MigLayout settingsGroupLayout = new MigLayout("fill", "[100]");
      settingsGroup.setLayout(settingsGroupLayout);
      settingsGroup.setText("Settings");
      settingsGroup.setLayoutData("spany,grow,skip");

      togJars = new Button(settingsGroup, SWT.TOGGLE);
      togJars.setText(ClientStrings.ENABLE_ALLOWJARS);
      togJars.setAlignment(SWT.CENTER);
      togJars.setLayoutData("growx,wrap");

      Group debugGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      MigLayout debugGroupLayout = new MigLayout("fill", "[100]");
      debugGroup.setLayout(debugGroupLayout);
      debugGroup.setText("Debug");
      debugGroup.setLayoutData("spany,grow,skip");

      togSaveClasses = new Button(debugGroup, SWT.TOGGLE);
      togSaveClasses.setText(ClientStrings.ENABLE_SAVECLASSES);
      togSaveClasses.setAlignment(SWT.CENTER);
      togSaveClasses.setLayoutData("growx,wrap");

      togVerbose = new Button(debugGroup, SWT.TOGGLE);
      togVerbose.setText(ClientStrings.ENABLE_VERBOSEMODE);
      togVerbose.setAlignment(SWT.CENTER);
      togVerbose.setLayoutData("growx");

      togInstru.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
      {
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
        {
          toggleSetting(settingsData.instrEnabled, "[instru-true",
                        "[instru-false");
        }
      });
      
      final String helpText = "Enter pattern in the form " +
      "\"mypack.mysubpack.MyClass\" or using wildcards " +
      "\"mypack.*.MyClass\" or \"*MyClass\" etc";
      classRegex.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
      {
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
        {
          PatternInputWindow regexInput = new PatternInputWindow(
              "Set Class Regex",
              helpText,
              new PatternInputCallback()
              {               
                @Override
                public void setPattern(String newPattern)
                {
                  setRegex(newPattern);
                }
              },
              settingsData.classRegex);
          placeDialogInCenter(sWindow, regexInput.sWindow);
        }
      });
      listClasses.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
      {
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
        {
          getModifiedClasses();
        }
      });
      togJars.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
      {
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
        {
          toggleSetting(settingsData.allowJarsToBeTraced, "[instrujars-true",
                        "[instrujars-false");
        }
      });
      togSaveClasses.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
      {
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
        {
          toggleSetting(settingsData.saveTracedClassfiles, "[saveinstru-true",
                        "[saveinstru-false");
        }
      });
      togVerbose.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
      {
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
        {
          toggleSetting(settingsData.verboseMode, "[verbose-true",
                        "[verbose-false");
        }
      });

    }
  }

  private class OutputSettingsTab
  {
    final Button stdOutOutput;
    final Button fileOutput;
    final Button networkOutput;

    private OutputSettingsTab(TabFolder tabFolder, TabItem outputSettingsTab)
    {
      MigLayout windowLayout = new MigLayout("fill", "[][grow]");

      Composite composite = new Composite(tabFolder, SWT.NONE);
      composite.setLayout(windowLayout);
      outputSettingsTab.setControl(composite);

      Group outputTypesGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      MigLayout outputTypesGroupLayout = new MigLayout("fill",
                                                       "[100][100][100]");
      outputTypesGroup.setLayout(outputTypesGroupLayout);
      outputTypesGroup.setText("");
      outputTypesGroup.setLayoutData("spany,grow");

      stdOutOutput = new Button(outputTypesGroup, SWT.TOGGLE);
      stdOutOutput.setText(ClientStrings.ENABLE_STDOUT_OUTPUT);
      stdOutOutput.setLayoutData("spany,grow");

      fileOutput = new Button(outputTypesGroup, SWT.TOGGLE);
      fileOutput.setText(ClientStrings.ENABLE_FILE_OUTPUT);
      fileOutput.setAlignment(SWT.CENTER);
      fileOutput.setLayoutData("spany,grow");

      networkOutput = new Button(outputTypesGroup, SWT.TOGGLE);
      networkOutput.setText(ClientStrings.ENABLE_NETWORK_OUTPUT);
      networkOutput.setAlignment(SWT.CENTER);
      networkOutput.setLayoutData("spany,grow");

      stdOutOutput.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
      {
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
        {
          toggleSetting(settingsData.stdOutEnabled, "[out-stdout-true",
                        "[out-stdout-false");
        }
      });
      fileOutput.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
      {
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
        {
          toggleSetting(settingsData.fileOutEnabled, "[out-file-true",
                        "[out-file-false");
        }
      });
      networkOutput.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
      {
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
        {
          toggleSetting(settingsData.netOutEnabled, "[out-network-true",
                        "[out-network-false");
        }
      });
    }
  }

  private class TraceTab
  {
    final Button entryExitTrace;
    final Button branchTrace;
    final Button argsTrace;

    private TraceTab(TabFolder tabFolder, TabItem traceTab)
    {
      MigLayout windowLayout = new MigLayout("fill", "[][grow]");

      Composite composite = new Composite(tabFolder, SWT.NONE);
      composite.setLayout(windowLayout);
      traceTab.setControl(composite);

      Group traceTypesGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      MigLayout traceTypesGroupLayout = new MigLayout("fill", "[100][100][100]");
      traceTypesGroup.setLayout(traceTypesGroupLayout);
      traceTypesGroup.setText("");
      traceTypesGroup.setLayoutData("spany,grow");

      entryExitTrace = new Button(traceTypesGroup, SWT.TOGGLE);
      entryExitTrace.setText(ClientStrings.ENABLE_EE_TRACE);
      entryExitTrace.setLayoutData("spany,grow");

      branchTrace = new Button(traceTypesGroup, SWT.TOGGLE);
      branchTrace.setText(ClientStrings.ENABLE_BRANCH_TRACE);
      branchTrace.setAlignment(SWT.CENTER);
      branchTrace.setLayoutData("spany,grow");

      argsTrace = new Button(traceTypesGroup, SWT.TOGGLE);
      argsTrace.setText(ClientStrings.ENABLE_ARGS_TRACE);
      argsTrace.setAlignment(SWT.CENTER);
      argsTrace.setLayoutData("spany,grow");

      entryExitTrace.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
      {
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
        {
          toggleSetting(settingsData.entryExitEnabled, "[trace-ee-true",
                        "[trace-ee-false");
        }
      });
      branchTrace.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
      {
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
        {
          toggleSetting(settingsData.branchEnabled, "[trace-branch-true",
                        "[trace-branch-false");
        }
      });
      argsTrace.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
      {
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
        {
          toggleSetting(settingsData.argsEnabled, "[trace-args-true",
                        "[trace-args-false");
        }
      });
    }
  }

  private class CallersSettingsTab
  {
    final Button callersCapture;

    private CallersSettingsTab(TabFolder tabFolder, TabItem callersTab)
    {
      MigLayout windowLayout = new MigLayout("fill", "[][grow]");

      Composite composite = new Composite(tabFolder, SWT.NONE);
      composite.setLayout(windowLayout);
      callersTab.setControl(composite);

      Group callersTypesGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      MigLayout callersTypesGroupLayout = new MigLayout("fill", "[100]");
      callersTypesGroup.setLayout(callersTypesGroupLayout);
      callersTypesGroup.setText("");
      callersTypesGroup.setLayoutData("spany,grow");

      callersCapture = new Button(callersTypesGroup, SWT.PUSH);
      callersCapture.setText(ClientStrings.BEGIN_CAPTURE_CALLERS);
      callersCapture.setLayoutData("spany,grow");

      final String helpText = "Enter pattern in the form " +
      "\"functionName\" or using wildcards " +
      "\"functionN*\" or \"*tionName\" etc";
      
      callersCapture.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
      {
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
        {
          PatternInputWindow regexInput = new PatternInputWindow(
              "Enter Method Regex",
              helpText,
              new PatternInputCallback()
              {               
                @Override
                public void setPattern(String newPattern)
                {
                  setCallersRegex(newPattern);
                }
              },
              "");
          placeDialogInCenter(sWindow, regexInput.sWindow);
        }
      });
    }
  }

  TextOutputTab textOutputTab;
  Map<Long, CallersTab> callerTabs = new ConcurrentHashMap<Long, CallersTab>();

  private void fillOutputTabs(TabFolder tabFolder)
  {
    TabItem textOutputTabItem = new TabItem(tabFolder, SWT.NONE);
    textOutputTabItem.setText("Output");
    textOutputTab = new TextOutputTab(tabFolder, textOutputTabItem);
  }

  private class TextOutputTab
  {
    final Text textOutput;

    private TextOutputTab(TabFolder tabFolder, TabItem textOutputTab)
    {
      MigLayout windowLayout = new MigLayout("fill", "[70][grow]", "[20][grow]");

      Composite composite = new Composite(tabFolder, SWT.NONE);
      composite.setLayout(windowLayout);
      textOutputTab.setControl(composite);

      Button clearText = new Button(composite, SWT.PUSH);
      clearText.setText(ClientStrings.CLEAR_TEXT);
      clearText.setLayoutData("wrap,grow");

      textOutput = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL
                                       | SWT.BORDER);
      textOutput.setEditable(false);
      textOutput.setLayoutData("spanx,grow");
      textOutput.setBackground(Display.getCurrent()
                                      .getSystemColor(SWT.COLOR_WHITE));

      clearText.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
      {
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
        {
          sWindow.getDisplay().asyncExec(new Runnable()
          {
            @Override
            public void run()
            {
              textOutput.setText("");
            }
          });
        }
      });

    }
  }

  private class CallersTab
  {
    final TabItem callersTabItem;
    final TreeItem callersTreeRoot;
    final TabFolder tabFolder;
    private final Object tabCallersId;

    private CallersTab(TabFolder tabFolder, Object callersId)
    {
      this.tabFolder = tabFolder;
      this.tabCallersId = callersId;

      callersTabItem = new TabItem(tabFolder, SWT.NONE);
      callersTabItem.setText("Callers");

      MigLayout windowLayout = new MigLayout("fill", "[70][70][grow]",
                                             "[20][grow]");

      Composite composite = new Composite(tabFolder, SWT.NONE);
      composite.setLayout(windowLayout);
      callersTabItem.setControl(composite);

      final Button endCapture = new Button(composite, SWT.PUSH);
      endCapture.setText(ClientStrings.CAPTURE_END);
      endCapture.setLayoutData("grow");

      Button closeCapture = new Button(composite, SWT.PUSH);
      closeCapture.setText(ClientStrings.CAPTURE_CLOSE);
      closeCapture.setLayoutData("grow,wrap");

      Tree callersTree = new Tree(composite, SWT.BORDER);
      callersTree.setLayoutData("grow, spanx");
      callersTreeRoot = new TreeItem(callersTree, SWT.NULL);
      callersTreeRoot.setText("Callers");

      endCapture.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
      {
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
        {
          endCapture.setEnabled(false);
          controlThread.sendMessage("[callers-end-" + tabCallersId);
        }
      });

      closeCapture.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
      {
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
        {
          controlThread.sendMessage("[callers-end-" + tabCallersId);
          sWindow.getDisplay().asyncExec(new Runnable()
          {
            @Override
            public void run()
            {
              callersTabItem.dispose();
            }
          });
        }
      });
    }

    public void setData(final Map<String, Object> callersMap)
    {
      final Object finalFlag = callersMap.remove(CallersConfigConstants.FINAL);
      final Object callersRegex = callersMap
                                            .remove(CallersConfigConstants.PATTERN);

      sWindow.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          String newRootText = "Callers: " + callersRegex;
          String currentRootText = callersTreeRoot.getText();
          if (!currentRootText.equals(newRootText))
          {
            callersTreeRoot.removeAll();
            callersTreeRoot.setText(newRootText);
          }
          setCallersData(callersTreeRoot, callersMap);
          if ("true".equals(finalFlag))
          {
            tabFolder.setSelection(callersTabItem);
          }
        }
      });
    }

    @SuppressWarnings("unchecked")
    private void setCallersData(TreeItem parentItem,
                                Map<String, Object> callersMap)
    {
      for (Entry<String, Object> mapEntry : callersMap.entrySet())
      {
        String entryName = mapEntry.getKey();

        TreeItem item = null;
        // Look for existing item
        for (TreeItem iter_item : parentItem.getItems())
        {
          if (iter_item.getText().equals(entryName))
          {
            item = iter_item;
            break;
          }
        }

        // Create new item
        if (item == null)
        {
          item = new TreeItem(parentItem, SWT.NULL);
          item.setText(entryName);
        }

        // Process children
        Object entryValue = mapEntry.getValue();
        if (entryValue instanceof Map<?, ?>)
        {
          Map<String, Object> entryMap = (Map<String, Object>) entryValue;
          if (entryMap.size() > 0)
          {
            setCallersData(item, entryMap);
          }
        }
      }
    }
  }

  // Window ref
  private final TraceWindow traceDialogRef = this;

  // State
  private enum ConnectState
  {
    DISCONNECTED_ERR, DISCONNECTED, CONNECTING, CONNECTED, CONNECTED_UPDATING
  }

  private ConnectState connectionState = ConnectState.DISCONNECTED;

  // Network details
  private InetAddress remoteAddress;

  // Threads
  private NetworkDataReceiverThread networkTraceThread;
  private ControlConnectionThread controlThread;

  // Settings
  private ParsedSettingsData settingsData = new ParsedSettingsData(
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

      controlThread.sendMessage("[out-network");
      String networkTracePortStr = controlThread.getMessage();
      int networkTracePort = Integer.parseInt(networkTracePortStr);
      try
      {
        networkTraceThread = new NetworkDataReceiverThread(remoteAddress,
                                                           networkTracePort,
                                                           traceDialogRef);
        networkTraceThread.start();
      }
      catch (IOException ex)
      {
        addMessage("*** Failed to setup network trace: " + ex.toString());
      }
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

  private void getModifiedClasses()
  {
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        controlThread.sendMessage("[listmodifiedclasses");
        String modifiedClasses = controlThread.getMessage();
        if (modifiedClasses.length() <= 2)
        {
          addMessage("*** No modified classes");
        }
        else
        {
          modifiedClasses = modifiedClasses
                                           .substring(
                                                      1,
                                                      modifiedClasses.length() - 1);
          if (modifiedClasses.indexOf(",") == -1)
          {
            addMessage("*** Modified: " + modifiedClasses);
          }
          else
          {
            String[] classNames = modifiedClasses.split(",");
            for (String className : classNames)
            {
              addMessage("*** Modified: "
                         + (className != null ? className.trim() : "null"));
            }
          }
        }
      }
    }).start();
  }

  public void setRegex(final String regex)
  {
    if (!sWindow.isDisposed())
    {
      sWindow.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          connectionState = ConnectState.CONNECTED_UPDATING;
          updateUIStateSameThread();
          controlThread.sendMessage("[regex-" + regex);
          controlThread.sendMessage("getsettings");
        }
      });
    }
  }

  public void setCallersRegex(final String regex)
  {
    controlThread.sendMessage("[callers-start-" + regex);
  }

  private void toggleSetting(final boolean settingValue,
                             final String enableCommand,
                             final String disableCommand)
  {
    if (!sWindow.isDisposed())
    {
      sWindow.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          connectionState = ConnectState.CONNECTED_UPDATING;
          updateUIStateSameThread();
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
  }

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

      if (connectionState == ConnectState.CONNECTED)
      {
        // Enable all buttons
        connTab.printSettings.setEnabled(true);

        instruTab.classRegex.setEnabled(true);
        instruTab.listClasses.setEnabled(true);
        instruTab.togInstru.setEnabled(true);
        instruTab.togJars.setEnabled(true);
        instruTab.togSaveClasses.setEnabled(true);
        instruTab.togVerbose.setEnabled(true);

        outSettingsTab.fileOutput.setEnabled(true);
        outSettingsTab.networkOutput.setEnabled(true);
        outSettingsTab.stdOutOutput.setEnabled(true);

        traceTab.argsTrace.setEnabled(true);
        traceTab.branchTrace.setEnabled(true);
        traceTab.entryExitTrace.setEnabled(true);

        callSettingsTab.callersCapture.setEnabled(true);

        // Update the button pressed/unpressed state
        instruTab.togInstru.setSelection(settingsData.instrEnabled);
        instruTab.togJars.setSelection(settingsData.allowJarsToBeTraced);
        instruTab.togSaveClasses
                                .setSelection(settingsData.saveTracedClassfiles);
        instruTab.togVerbose.setSelection(settingsData.verboseMode);

        outSettingsTab.fileOutput.setSelection(settingsData.fileOutEnabled);
        outSettingsTab.networkOutput.setSelection(settingsData.netOutEnabled);
        outSettingsTab.stdOutOutput.setSelection(settingsData.stdOutEnabled);

        traceTab.argsTrace.setSelection(settingsData.argsEnabled);
        traceTab.branchTrace.setSelection(settingsData.branchEnabled);
        traceTab.entryExitTrace.setSelection(settingsData.entryExitEnabled);
      }

      if (connectionState == ConnectState.CONNECTED_UPDATING)
      {
        connTab.addressInput.setEnabled(true);
        connTab.portInput.setEnabled(true);
        connTab.printSettings.setEnabled(false);

        instruTab.classRegex.setEnabled(false);
        instruTab.listClasses.setEnabled(false);
        instruTab.togInstru.setEnabled(false);
        instruTab.togJars.setEnabled(false);
        instruTab.togSaveClasses.setEnabled(false);
        instruTab.togVerbose.setEnabled(false);

        outSettingsTab.fileOutput.setEnabled(false);
        outSettingsTab.networkOutput.setEnabled(false);
        outSettingsTab.stdOutOutput.setEnabled(false);
      }

      // Only reset status text if we didn't hit an error
      if (connectionState == ConnectState.DISCONNECTED)
      {
        connTab.connectStatus.setStatusText("Disconnected");
      }

      // Always reset button states
      if ((connectionState == ConnectState.DISCONNECTED)
          || (connectionState == ConnectState.DISCONNECTED_ERR))
      {
        connTab.addressInput.setEnabled(true);
        connTab.portInput.setEnabled(true);
        connTab.printSettings.setEnabled(false);

        instruTab.classRegex.setEnabled(false);
        instruTab.listClasses.setEnabled(false);
        instruTab.togInstru.setEnabled(false);
        instruTab.togJars.setEnabled(false);
        instruTab.togSaveClasses.setEnabled(false);
        instruTab.togVerbose.setEnabled(false);

        outSettingsTab.fileOutput.setEnabled(false);
        outSettingsTab.networkOutput.setEnabled(false);
        outSettingsTab.stdOutOutput.setEnabled(false);

        traceTab.argsTrace.setEnabled(false);
        traceTab.branchTrace.setEnabled(false);
        traceTab.entryExitTrace.setEnabled(false);

        callSettingsTab.callersCapture.setEnabled(false);
      }
    }
  }

  public void setConfig(final Map<String, String> settingsMap)
  {
    if (!sWindow.isDisposed())
    {
      sWindow.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          addMessageSameThread("*** Latest Settings Received");
          settingsData = new ParsedSettingsData(settingsMap);
          connectionState = ConnectState.CONNECTED;
          updateUIStateSameThread();
        }
      });
    }
  }

  public void addMessage(final String message)
  {
    if (!sWindow.isDisposed())
    {
      sWindow.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          addMessageSameThread(message);
        }
      });
    }
  }

  public void addMessageSameThread(final String message)
  {
    textOutputTab.textOutput.append(message + "\n");
  }

  public void setCallers(final Map<String, Object> callersMap)
  {
    final Object callersId = callersMap.remove(CallersConfigConstants.ID);

    CallersTab cTab = callerTabs.get(callersId);
    if (cTab == null)
    {
      sWindow.getDisplay().syncExec(new Runnable()
      {
        @Override
        public void run()
        {
          CallersTab cTab = new CallersTab(outputTabs, callersId);
          callerTabs.put((Long) callersId, cTab);
        }
      });
      cTab = callerTabs.get(callersId);
    }

    cTab.setData(callersMap);
  }

  private static void placeDialogInCenter(Shell parent, Shell shell)
  {
    Rectangle parentSize = parent.getBounds();
    Rectangle mySize = shell.getBounds();

    int locationX, locationY;
    locationX = (parentSize.width - mySize.width) / 2 + parentSize.x;
    locationY = (parentSize.height - mySize.height) / 2 + parentSize.y;

    shell.setLocation(new Point(locationX, locationY));
    shell.open();
  }
}
