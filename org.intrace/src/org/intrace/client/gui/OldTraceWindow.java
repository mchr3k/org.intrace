package org.intrace.client.gui;

import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
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

public class OldTraceWindow
{
  // Window ref
  private final OldTraceWindow traceDialogRef = this;

  // State
  private boolean connected = false;

  // Network details
  private InetAddress remoteAddress;
  private boolean networkDataEnabled = false;

  // Threads
  private NetworkDataReceiverThread networkTraceThread;
  private ControlConnectionThread controlThread; // @jve:decl-index=0:

  // Settings
  private ParsedSettingsData settingsData = new ParsedSettingsData(
                                                                   new HashMap<String, String>()); // @jve:decl-index=0:

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
  private Button listModifiedClasses = null;
  private Button toggleFileOutputButton = null;
  private Button toggleNetworkTraceButton = null;
  private TabFolder outputTabFolder = null;
  private TabItem textOutputTabItem = null;
  private TabItem callersOutputTabItem = null;
  private Tree callersTree = null;
  private TreeItem callersTreeRoot = null;
  private Label callersLabel = null;
  private Button callersStateButton = null;
  private Label generalLabel = null;
  private Button dumpSettingsButton = null;
  private Button clearTextButton = null;

  /**
   * This method initializes sShell
   */
  private void createSShell()
  {
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    GridData gridData = new GridData();
    gridData.widthHint = 150;
    sShell = new Shell();
    sShell.setText("Trace Window");
    sShell.setLayout(gridLayout);
    sShell.setSize(new Point(700, 750));
    sShell.setMinimumSize(new Point(700, 750));
    instrumentSettingsLabel = new Label(sShell, SWT.NONE);
    instrumentSettingsLabel.setText("Instrumentation Settings:");
    createComposite();
    toggleInstrumentEnabled = new Button(sShell, SWT.LEFT);
    toggleInstrumentEnabled.setText(ClientStrings.ENABLE_INSTR);
    toggleInstrumentEnabled.setLayoutData(gridData);
    toggleInstrumentEnabled
                           .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
                           {
                             @Override
                             public void mouseUp(
                                                 org.eclipse.swt.events.MouseEvent e)
                             {
                               toggleSetting(settingsData.instrEnabled,
                                             "[instru-true", "[instru-false");
                             }
                           });
    setClassRegexButton = new Button(sShell, SWT.LEFT);
    setClassRegexButton.setText(ClientStrings.SET_CLASSREGEX);
    setClassRegexButton.setLayoutData(gridData);
    // setClassRegexButton
    // .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    // {
    // @Override
    // public void mouseUp(org.eclipse.swt.events.MouseEvent e)
    // {
    // InstruRegexInputWindow regexInput = new InstruRegexInputWindow();
    // regexInput.open(traceDialogRef,
    // settingsData.classRegex);
    // placeDialogInCenter(sShell, regexInput.sShell);
    // }
    // });
    toggleAllowJarInstru = new Button(sShell, SWT.LEFT);
    toggleAllowJarInstru.setText(ClientStrings.ENABLE_ALLOWJARS);
    toggleAllowJarInstru.setLayoutData(gridData);
    toggleAllowJarInstru
                        .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
                        {
                          @Override
                          public void mouseUp(
                                              org.eclipse.swt.events.MouseEvent e)
                          {
                            toggleSetting(settingsData.allowJarsToBeTraced,
                                          "[instrujars-true",
                                          "[instrujars-false");
                          }
                        });
    toggleSaveClassFiles = new Button(sShell, SWT.LEFT);
    toggleSaveClassFiles.setText(ClientStrings.ENABLE_SAVECLASSES);
    toggleSaveClassFiles.setLayoutData(gridData);
    toggleSaveClassFiles
                        .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
                        {
                          @Override
                          public void mouseUp(
                                              org.eclipse.swt.events.MouseEvent e)
                          {
                            toggleSetting(settingsData.saveTracedClassfiles,
                                          "[saveinstru-true",
                                          "[saveinstru-false");
                          }
                        });
    toggleVerboseMode = new Button(sShell, SWT.LEFT);
    toggleVerboseMode.setText(ClientStrings.ENABLE_VERBOSEMODE);
    toggleVerboseMode.setLayoutData(gridData);
    toggleVerboseMode
                     .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
                     {
                       @Override
                       public void mouseUp(org.eclipse.swt.events.MouseEvent e)
                       {
                         toggleSetting(settingsData.verboseMode,
                                       "[verbose-true", "[verbose-false");
                       }
                     });
    listModifiedClasses = new Button(sShell, SWT.LEFT);
    listModifiedClasses.setText(ClientStrings.LIST_MODIFIED_CLASSES);
    listModifiedClasses.setLayoutData(gridData);
    listModifiedClasses
                       .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
                       {
                         @Override
                         public void mouseUp(org.eclipse.swt.events.MouseEvent e)
                         {
                           getModifiedClasses();
                         }
                       });
    traceSettingsLabel = new Label(sShell, SWT.NONE);
    traceSettingsLabel.setText("Trace Settings:");
    sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter()
    {
      @Override
      public void shellClosed(org.eclipse.swt.events.ShellEvent e)
      {
        disconnect();
        sShell.dispose();
      }
    });
    toggleEntryExitButton = new Button(sShell, SWT.LEFT);
    toggleEntryExitButton.setText(ClientStrings.ENABLE_EE_TRACE);
    toggleEntryExitButton.setLayoutData(gridData);
    toggleEntryExitButton
                         .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
                         {
                           @Override
                           public void mouseUp(
                                               org.eclipse.swt.events.MouseEvent e)
                           {
                             toggleSetting(settingsData.entryExitEnabled,
                                           "[trace-ee-true", "[trace-ee-false");
                           }
                         });
    toggleBranchButton = new Button(sShell, SWT.LEFT);
    toggleBranchButton.setText(ClientStrings.ENABLE_BRANCH_TRACE);
    toggleBranchButton.setLayoutData(gridData);
    toggleBranchButton
                      .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
                      {
                        @Override
                        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
                        {
                          toggleSetting(settingsData.branchEnabled,
                                        "[trace-branch-true",
                                        "[trace-branch-false");
                        }
                      });
    toggleArgsButton = new Button(sShell, SWT.LEFT);
    toggleArgsButton.setText(ClientStrings.ENABLE_ARGS_TRACE);
    toggleArgsButton.setLayoutData(gridData);
    callersLabel = new Label(sShell, SWT.NONE);
    callersLabel.setText("Callers Settings:");
    callersStateButton = new Button(sShell, SWT.NONE);
    callersStateButton.setLayoutData(gridData);
    callersStateButton.setText(ClientStrings.BEGIN_CAPTURE_CALLERS);
    // callersStateButton
    // .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    // {
    // @Override
    // public void mouseUp(org.eclipse.swt.events.MouseEvent e)
    // {
    // if (!settingsData.callersCaptureInProgress)
    // {
    // CallersRegexInputWindow regexInput = new CallersRegexInputWindow();
    // regexInput.open(traceDialogRef,
    // settingsData.callersRegex);
    // placeDialogInCenter(sShell, regexInput.sShell);
    // }
    // else
    // {
    // toggleSetting(
    // settingsData.callersCaptureInProgress,
    // "[callers-enabled-true",
    // "[callers-enabled-false");
    // }
    // }
    // });
    outputSettingsLabel = new Label(sShell, SWT.NONE);
    outputSettingsLabel.setText("Output Modes:");
    toggleArgsButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        toggleSetting(settingsData.argsEnabled, "[trace-args-true",
                      "[trace-args-false");
      }
    });
    toggleStdOutButton = new Button(sShell, SWT.LEFT);
    toggleStdOutButton.setText(ClientStrings.ENABLE_STDOUT_OUTPUT);
    toggleStdOutButton.setLayoutData(gridData);
    toggleFileOutputButton = new Button(sShell, SWT.LEFT);
    toggleFileOutputButton.setText(ClientStrings.ENABLE_FILE_OUTPUT);
    toggleFileOutputButton.setLayoutData(gridData);
    toggleNetworkTraceButton = new Button(sShell, SWT.LEFT);
    toggleNetworkTraceButton.setText(ClientStrings.ENABLE_NETWORK_OUTPUT);
    toggleNetworkTraceButton.setLayoutData(gridData);
    generalLabel = new Label(sShell, SWT.NONE);
    generalLabel.setText("General:");
    dumpSettingsButton = new Button(sShell, SWT.LEFT);
    dumpSettingsButton.setText("Dump Settings");
    dumpSettingsButton.setLayoutData(gridData);
    dumpSettingsButton
                      .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
                      {
                        @Override
                        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
                        {
                          addMessage("Settings:" + settingsData.toString());
                        }
                      });
    clearTextButton = new Button(sShell, SWT.LEFT);
    clearTextButton.setText("Clear Text");
    clearTextButton.setLayoutData(gridData);
    clearTextButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        sShell.getDisplay().asyncExec(new Runnable()
        {
          @Override
          public void run()
          {
            statusTextArea.setText("");
          }
        });
      }
    });
    toggleNetworkTraceButton
                            .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
                            {
                              @Override
                              public void mouseUp(
                                                  org.eclipse.swt.events.MouseEvent e)
                              {
                                toggleNetworkTrace();
                              }
                            });
    toggleFileOutputButton
                          .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
                          {
                            @Override
                            public void mouseUp(
                                                org.eclipse.swt.events.MouseEvent e)
                            {
                              toggleSetting(settingsData.fileOutEnabled,
                                            "[out-file-true", "[out-file-false");
                            }
                          });
    spacerLabel = new Label(sShell, SWT.NONE);
    spacerLabel.setText("");
    toggleStdOutButton
                      .addMouseListener(new org.eclipse.swt.events.MouseAdapter()
                      {
                        @Override
                        public void mouseUp(org.eclipse.swt.events.MouseEvent e)
                        {
                          toggleSetting(settingsData.stdOutEnabled,
                                        "[out-stdout-true", "[out-stdout-false");
                        }
                      });
    disconnectButton = new Button(sShell, SWT.LEFT);
    disconnectButton.setText(ClientStrings.CONNECT);
    disconnectButton.setLayoutData(gridData);
    // disconnectButton.addMouseListener(new
    // org.eclipse.swt.events.MouseAdapter()
    // {
    // @Override
    // public void mouseUp(org.eclipse.swt.events.MouseEvent e)
    // {
    // if (!connected)
    // {
    // NewConnectionWindow connWindow = new NewConnectionWindow();
    // connWindow.open(traceDialogRef);
    // placeDialogInCenter(sShell, connWindow.sShell);
    // }
    // else
    // {
    // disconnect();
    // }
    // }
    // });
    updateButtonText();
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
    gridData3.verticalSpan = 22;
    gridData3.grabExcessHorizontalSpace = true;
    composite = new Composite(sShell, SWT.NONE);
    composite.setLayoutData(gridData3);
    composite.setLayout(fillLayout);
    createOutputTabFolder();
  }

  /**
   * This method initializes outputTabFolder
   * 
   */
  private void createOutputTabFolder()
  {
    outputTabFolder = new TabFolder(composite, SWT.NONE);
    statusTextArea = new Text(outputTabFolder, SWT.MULTI | SWT.WRAP
                                               | SWT.V_SCROLL | SWT.BORDER);
    statusTextArea.setEditable(false);
    statusTextArea.setBackground(Display.getCurrent()
                                        .getSystemColor(SWT.COLOR_WHITE));
    callersTree = new Tree(outputTabFolder, SWT.BORDER);
    callersTreeRoot = new TreeItem(callersTree, SWT.NULL);
    callersTreeRoot.setText("Callers");

    textOutputTabItem = new TabItem(outputTabFolder, SWT.NONE);
    textOutputTabItem.setControl(statusTextArea);
    textOutputTabItem.setText("Text Output");
    callersOutputTabItem = new TabItem(outputTabFolder, SWT.NONE);
    callersOutputTabItem.setControl(callersTree);
    callersOutputTabItem.setText("Callers");
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

  public void open()
  {
    createSShell();
    sShell.open();
    Display display = Display.getDefault();
    while (!sShell.isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }

  public void setConnection(Socket socket)
  {
    this.remoteAddress = socket.getInetAddress();
    // controlThread = new ControlConnectionThread(socket, this);
    // controlThread.start();
    // controlThread.sendMessage("getsettings");
    connected = true;
    sShell.getDisplay().asyncExec(new Runnable()
    {
      @Override
      public void run()
      {
        updateButtonText();
      }
    });
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
    connected = false;
    if (!sShell.isDisposed())
    {
      sShell.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          updateButtonText();
        }
      });
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
        if (!networkDataEnabled)
        {
          controlThread.sendMessage("[out-network");
          String networkTracePortStr = controlThread.getMessage();
          int networkTracePort = Integer.parseInt(networkTracePortStr);
          // try
          {
            // networkTraceThread = new NetworkDataReceiverThread(
            // remoteAddress,
            // networkTracePort,
            // traceDialogRef);
            // networkTraceThread.start();
            // networkDataEnabled = true;
          }
          // catch (IOException ex)
          {
            // addMessage("*** Failed to setup network trace: " +
            // ex.toString());
          }
        }
        else
        {
          networkTraceThread.disconnect();
          networkDataEnabled = false;
        }
        updateButtonText();
      }
    });
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
              addMessage("*** Modified: " + className);
            }
          }
        }
      }
    }).start();
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

  private void toggleSetting(final boolean settingValue,
                             final String enableCommand,
                             final String disableCommand)
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

  private void updateButtonText()
  {
    if (!sShell.isDisposed())
    {
      if (connected)
      {
        chooseText(toggleInstrumentEnabled, settingsData.instrEnabled,
                   ClientStrings.ENABLE_INSTR, ClientStrings.DISABLE_INSTR);
        chooseText(toggleAllowJarInstru, settingsData.allowJarsToBeTraced,
                   ClientStrings.ENABLE_ALLOWJARS,
                   ClientStrings.DISABLE_ALLOWJARS);
        chooseText(toggleSaveClassFiles, settingsData.saveTracedClassfiles,
                   ClientStrings.ENABLE_SAVECLASSES,
                   ClientStrings.DISABLE_SAVECLASSES);
        chooseText(toggleVerboseMode, settingsData.verboseMode,
                   ClientStrings.ENABLE_VERBOSEMODE,
                   ClientStrings.DISABLE_VERBOSEMODE);
        listModifiedClasses.setEnabled(true);

        chooseText(toggleEntryExitButton, settingsData.entryExitEnabled,
                   ClientStrings.ENABLE_EE_TRACE,
                   ClientStrings.DISABLE_EE_TRACE);
        chooseText(toggleBranchButton, settingsData.branchEnabled,
                   ClientStrings.ENABLE_BRANCH_TRACE,
                   ClientStrings.DISABLE_BRANCH_TRACE);
        chooseText(toggleArgsButton, settingsData.argsEnabled,
                   ClientStrings.ENABLE_ARGS_TRACE,
                   ClientStrings.DISABLE_ARGS_TRACE);

        // chooseText(callersStateButton, settingsData.callersCaptureInProgress,
        // ClientStrings.BEGIN_CAPTURE_CALLERS,
        // ClientStrings.END_CAPTURE_CALLERS);
        callersStateButton.setEnabled(networkDataEnabled);

        chooseText(toggleStdOutButton, settingsData.stdOutEnabled,
                   ClientStrings.ENABLE_STDOUT_OUTPUT,
                   ClientStrings.DISABLE_STDOUT_OUTPUT);
        chooseText(toggleFileOutputButton, settingsData.fileOutEnabled,
                   ClientStrings.ENABLE_FILE_OUTPUT,
                   ClientStrings.DISABLE_FILE_OUTPUT);
        chooseText(toggleNetworkTraceButton, networkDataEnabled,
                   ClientStrings.ENABLE_NETWORK_OUTPUT,
                   ClientStrings.DISABLE_NETWORK_OUTPUT);
        setClassRegexButton.setEnabled(true);

        dumpSettingsButton.setEnabled(true);
      }
      else
      {
        disableButtons();
      }
      chooseText(disconnectButton, connected, ClientStrings.CONNECT,
                 ClientStrings.DISCONNECT);
    }
  }

  private void disableButtons()
  {
    toggleInstrumentEnabled.setEnabled(false);
    setClassRegexButton.setEnabled(false);
    toggleAllowJarInstru.setEnabled(false);
    toggleSaveClassFiles.setEnabled(false);
    toggleVerboseMode.setEnabled(false);
    listModifiedClasses.setEnabled(false);

    toggleEntryExitButton.setEnabled(false);
    toggleBranchButton.setEnabled(false);
    toggleArgsButton.setEnabled(false);

    callersStateButton.setEnabled(false);

    toggleStdOutButton.setEnabled(false);
    toggleFileOutputButton.setEnabled(false);
    toggleNetworkTraceButton.setEnabled(false);

    dumpSettingsButton.setEnabled(false);
  }

  public void setConfig(final Map<String, String> settingsMap)
  {
    sShell.getDisplay().asyncExec(new Runnable()
    {
      @Override
      public void run()
      {
        addMessage("*** Latest Settings Received");
        settingsData = new ParsedSettingsData(settingsMap);
        updateButtonText();
      }
    });
  }

  public void addMessage(final String message)
  {
    if (!sShell.isDisposed())
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
  }

  public void setCallers(final Map<String, Object> callersMap)
  {
    final Object finalFlag = callersMap.remove(CallersConfigConstants.FINAL);
    final Object callersRegex = callersMap
                                          .remove(CallersConfigConstants.PATTERN);
    sShell.getDisplay().asyncExec(new Runnable()
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
          outputTabFolder.setSelection(callersOutputTabItem);
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
