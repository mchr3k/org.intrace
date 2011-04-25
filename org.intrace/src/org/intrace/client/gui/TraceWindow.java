package org.intrace.client.gui;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.intrace.client.gui.PatternInputWindow.PatternInputCallback;
import org.intrace.client.gui.helper.Connection;
import org.intrace.client.gui.helper.ControlConnectionThread;
import org.intrace.client.gui.helper.NetworkDataReceiverThread;
import org.intrace.client.gui.helper.ParsedSettingsData;
import org.intrace.client.gui.helper.StatusUpdater;
import org.intrace.client.gui.helper.TraceFilterThread;
import org.intrace.client.gui.helper.TraceFilterThread.TraceFilterProgressHandler;
import org.intrace.client.gui.helper.TraceFilterThread.TraceTextHandler;
import org.intrace.shared.AgentConfigConstants;

public class TraceWindow
{
  public static String NEWLINE = System.getProperty("line.separator");

  public void open()
  {
    placeDialogInCenter(sWindow.getDisplay().getPrimaryMonitor().getBounds(),
        sWindow);
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
    MigLayout windowLayout = new MigLayout("fill", "", "[][grow]");

    sWindow = new Shell();
    sWindow.setText("Trace Window");
    sWindow.setLayout(windowLayout);
    sWindow.setSize(new Point(800, 800));
    sWindow.setMinimumSize(new Point(800, 480));

    TabFolder buttonTabs = new TabFolder(sWindow, SWT.NONE);
    buttonTabs.setLayoutData("grow,wrap,wmin 0");

    outputTabs = new TabFolder(sWindow, SWT.NONE);
    outputTabs.setLayoutData("grow,wmin 0,hmin 0");

    fillButtonTabs(buttonTabs);
    fillOutputTabs(outputTabs);

    updateUIStateSameThread();
  }

  private ConnectionTab connTab;
  private InstruTab instruTab;
  private TraceTab traceTab;
  private ExtrasTab extraTab;

  private void fillButtonTabs(TabFolder tabFolder)
  {
    TabItem connTabItem = new TabItem(tabFolder, SWT.NONE);
    connTabItem.setText("Connection");
    connTab = new ConnectionTab(tabFolder, connTabItem);

    TabItem instrTabItem = new TabItem(tabFolder, SWT.NONE);
    instrTabItem.setText("Instrumentation");
    instruTab = new InstruTab(tabFolder, instrTabItem);

    TabItem traceTabItem = new TabItem(tabFolder, SWT.NONE);
    traceTabItem.setText("Trace");
    traceTab = new TraceTab(tabFolder, traceTabItem);

    TabItem extraTabItem = new TabItem(tabFolder, SWT.NONE);
    extraTabItem.setText("Advanced");
    extraTab = new ExtrasTab(tabFolder, extraTabItem);
  }

  private class ConnectionTab
  {
    final Button connectButton;
    final Text addressInput;
    final Text portInput;
    final StatusUpdater connectStatus;

    private ConnectionTab(TabFolder tabFolder, TabItem connTab)
    {
      MigLayout windowLayout = new MigLayout("fill", "[380][grow]");

      Composite composite = new Composite(tabFolder, SWT.NONE);
      composite.setLayout(windowLayout);
      connTab.setControl(composite);

      Group connectionGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      MigLayout connGroupLayout = new MigLayout("fill", "[40][200][grow]");
      connectionGroup.setLayout(connGroupLayout);
      connectionGroup.setText("Connection Details");
      connectionGroup.setLayoutData("spany,grow,wmin 300");

      Label addressLabel = new Label(connectionGroup, SWT.NONE);
      addressLabel.setText(ClientStrings.CONN_ADDRESS);
      addressLabel.setLayoutData("gapx 5px,right");
      addressInput = new Text(connectionGroup, SWT.BORDER);
      addressInput.setText("localhost");
      addressInput.setLayoutData("growx,gapy 8px");

      connectButton = new Button(connectionGroup, SWT.LEFT);
      connectButton.setText(ClientStrings.CONNECT);
      connectButton.setAlignment(SWT.CENTER);
      connectButton.setLayoutData("gapx 5px,spany,grow,wrap");

      Label portLabel = new Label(connectionGroup, SWT.NONE);
      portLabel.setText(ClientStrings.CONN_PORT);
      portLabel.setLayoutData("gapx 5px,right");
      portInput = new Text(connectionGroup, SWT.BORDER);
      portInput.setText("9123");
      portInput.setLayoutData("growx");

      Group statusGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      statusGroup.setLayoutData("spany,grow,wmin 0");
      MigLayout groupLayout = new MigLayout("fill", "[align center]");
      statusGroup.setLayout(groupLayout);
      statusGroup.setText("Connection Status");

      final Label statusLabel = new Label(statusGroup, SWT.WRAP);
      statusLabel.setAlignment(SWT.CENTER);
      statusLabel.setLayoutData("grow,wmin 0");
      connectStatus = new StatusUpdater(sWindow, statusLabel);

      SelectionListener connectListen = new org.eclipse.swt.events.SelectionAdapter()
      {
        @Override
        public void widgetSelected(SelectionEvent selectionevent)
        {
          handleSelection();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent arg0)
        {
          handleSelection();
        }

        private void handleSelection()
        {
          if ((connectionState == ConnectState.DISCONNECTED)
              || (connectionState == ConnectState.DISCONNECTED_ERR))
          {
            connectionState = ConnectState.CONNECTING;
            updateUIStateSameThread();
            Connection.connectToAgent(traceDialogRef, sWindow, addressInput
                .getText(), portInput.getText(), connectStatus);
          } else if (connectionState == ConnectState.CONNECTED)
          {
            disconnect();
          }
        }
      };

      addressInput.addSelectionListener(connectListen);
      portInput.addSelectionListener(connectListen);
      connectButton.addSelectionListener(connectListen);
    }
  }

  private class InstruTab
  {
    final Button togInstru;
    final Button classRegex;
    final Button listClasses;
    final Label instrStatusLabel;

    private InstruTab(TabFolder tabFolder, TabItem instrTab)
    {
      MigLayout windowLayout = new MigLayout("fill", "[][300][grow]", "[]");

      Composite composite = new Composite(tabFolder, SWT.NONE);
      composite.setLayout(windowLayout);
      instrTab.setControl(composite);
      composite.setLayoutData("hmin 0");

      Group mainControlGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      MigLayout mainControlGroupLayout = new MigLayout("filly", "[150][150]");
      mainControlGroup.setLayout(mainControlGroupLayout);
      mainControlGroup.setLayoutData("hmin 0, grow");
      mainControlGroup.setText("Instrumentation Settings");      

      classRegex = new Button(mainControlGroup, SWT.PUSH);
      classRegex.setText(ClientStrings.SET_CLASSREGEX);
      classRegex.setLayoutData("grow");

      togInstru = new Button(mainControlGroup, SWT.PUSH);
      togInstru.setText(ClientStrings.ENABLE_INSTR);
      togInstru.setAlignment(SWT.CENTER);
      togInstru.setLayoutData("grow");

      Group statusGroup = new Group(composite, SWT.SHADOW_IN);
      MigLayout statusGroupLayout = new MigLayout("fillx", "[][]");
      statusGroup.setLayout(statusGroupLayout);
      statusGroup.setText("Instrumentation Status");
      statusGroup.setLayoutData("grow");

      instrStatusLabel = new Label(statusGroup, SWT.WRAP | SWT.VERTICAL);
      instrStatusLabel.setAlignment(SWT.LEFT);
      instrStatusLabel.setLayoutData("hmin 0,growx,wrap");
      setStatus(0, 0);

      listClasses = new Button(statusGroup, SWT.PUSH);
      listClasses.setText(ClientStrings.LIST_MODIFIED_CLASSES);
      listClasses.setAlignment(SWT.CENTER);
      listClasses.setLayoutData("gapy 5px");

      togInstru
          .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
          {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
              if (!settingsData.instrEnabled)
              {
                togInstru.setText(ClientStrings.ENABLING_INSTR);
              }
              else
              {
                togInstru.setText(ClientStrings.DISABLE_INSTR);
              }
              togInstru.setEnabled(false);
              toggleSetting(settingsData.instrEnabled, "[instru-true",
                  "[instru-false");
              settingsData.instrEnabled = !settingsData.instrEnabled;
            }
          });

      final String helpText = "Enter pattern in the form "
          + "\"mypack.mysubpack.MyClass\" or using wildcards "
          + "\"mypack.*.MyClass\" or \"*MyClass\" etc";
      classRegex
          .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
          {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
              PatternInputWindow regexInput = new PatternInputWindow(
                  "Set Class Regex", helpText, new PatternInputCallback()
                  {
                    private String includePattern = null;
                    private String excludePattern = null;

                    @Override
                    public void setIncludePattern(String newIncludePattern)
                    {
                      includePattern = newIncludePattern;
                      savePatterns();
                    }

                    @Override
                    public void setExcludePattern(String newExcludePattern)
                    {
                      excludePattern = newExcludePattern;
                      savePatterns();
                    }

                    private void savePatterns()
                    {
                      if ((includePattern != null) && (excludePattern != null))
                      {
                        setRegex(includePattern, excludePattern);
                      }
                    }
                  }, settingsData.classRegex, settingsData.classExcludeRegex);
              placeDialogInCenter(sWindow.getBounds(), regexInput.sWindow);
            }
          });
      listClasses
          .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
          {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
              getModifiedClasses();
            }
          });

    }

    private void setStatus(int instruClasses, int totalClasses)
    {
      instrStatusLabel.setText("Instrumented/Total Loaded Classes: " + instruClasses + "/"
          + totalClasses);
    }

    private void setProgress(int progressClasses, int totalClasses, boolean done)
    {
      instrStatusLabel.setText("Progress: " + progressClasses + "/"
          + totalClasses);
      if (done)
      {
        if (settingsData.instrEnabled)
        {
          togInstru.setText(ClientStrings.DISABLE_INSTR);
        } else
        {
          togInstru.setText(ClientStrings.ENABLE_INSTR);
        }
        togInstru.setEnabled(true);
      }
    }
  }

  private class TraceTab
  {
    final Button entryExitTrace;
    final Button branchTrace;
    final Button argsTrace;

    final Button stdOutOutput;
    final Button fileOutput;

    private TraceTab(TabFolder tabFolder, TabItem traceTab)
    {
      MigLayout windowLayout = new MigLayout("fill", "[][][grow]");

      Composite composite = new Composite(tabFolder, SWT.NONE);
      composite.setLayout(windowLayout);
      traceTab.setControl(composite);

      Group traceTypesGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      MigLayout traceTypesGroupLayout = new MigLayout("fill", "[150]");
      traceTypesGroup.setLayout(traceTypesGroupLayout);
      traceTypesGroup.setText("Trace Settings");
      traceTypesGroup.setLayoutData("spany,grow");

      entryExitTrace = new Button(traceTypesGroup, SWT.CHECK);
      entryExitTrace.setText(ClientStrings.ENABLE_EE_TRACE);
      entryExitTrace.setLayoutData("wrap");

      branchTrace = new Button(traceTypesGroup, SWT.CHECK);
      branchTrace.setText(ClientStrings.ENABLE_BRANCH_TRACE);
      branchTrace.setAlignment(SWT.CENTER);
      branchTrace.setLayoutData("wrap");

      argsTrace = new Button(traceTypesGroup, SWT.CHECK);
      argsTrace.setText(ClientStrings.ENABLE_ARGS_TRACE);
      argsTrace.setAlignment(SWT.CENTER);

      Group outputTypesGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      MigLayout outputTypesGroupLayout = new MigLayout("fill", "[150]");
      outputTypesGroup.setLayout(outputTypesGroupLayout);
      outputTypesGroup.setText("Output Settings");
      outputTypesGroup.setLayoutData("spany,grow");

      stdOutOutput = new Button(outputTypesGroup, SWT.CHECK);
      stdOutOutput.setText(ClientStrings.ENABLE_STDOUT_OUTPUT);
      stdOutOutput.setLayoutData("wrap");

      fileOutput = new Button(outputTypesGroup, SWT.CHECK);
      fileOutput.setText(ClientStrings.ENABLE_FILE_OUTPUT);
      fileOutput.setAlignment(SWT.CENTER);
      fileOutput.setLayoutData("wrap");

      entryExitTrace
          .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
          {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
              toggleSetting(settingsData.entryExitEnabled, "[trace-ee-true",
                  "[trace-ee-false");
              settingsData.entryExitEnabled = !settingsData.entryExitEnabled;
            }
          });
      branchTrace
          .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
          {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
              toggleSetting(settingsData.branchEnabled, "[trace-branch-true",
                  "[trace-branch-false");
              settingsData.branchEnabled = !settingsData.branchEnabled;
            }
          });
      argsTrace
          .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
          {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
              toggleSetting(settingsData.argsEnabled, "[trace-args-true",
                  "[trace-args-false");
              settingsData.argsEnabled = !settingsData.argsEnabled;
            }
          });

      stdOutOutput
          .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
          {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
              toggleSetting(settingsData.stdOutEnabled, "[out-stdout-true",
                  "[out-stdout-false");
              settingsData.stdOutEnabled = !settingsData.stdOutEnabled;
            }
          });
      fileOutput
          .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
          {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
              toggleSetting(settingsData.fileOutEnabled, "[out-file-true",
                  "[out-file-false");
              settingsData.fileOutEnabled = !settingsData.fileOutEnabled;
            }
          });
    }
  }

  private class ExtrasTab
  {
    private Button togSaveClasses;
    private Button togVerbose;
    private Button printSettings;

    private ExtrasTab(TabFolder tabFolder, TabItem traceTab)
    {
      MigLayout windowLayout = new MigLayout("fill", "[200][grow]");

      Composite composite = new Composite(tabFolder, SWT.NONE);
      composite.setLayout(windowLayout);
      traceTab.setControl(composite);

      togSaveClasses = new Button(composite, SWT.CHECK);
      togSaveClasses.setText(ClientStrings.ENABLE_SAVECLASSES);
      togSaveClasses.setAlignment(SWT.LEFT);
      togSaveClasses.setLayoutData("growx,wrap");

      togVerbose = new Button(composite, SWT.CHECK);
      togVerbose.setText(ClientStrings.ENABLE_VERBOSEMODE);
      togVerbose.setAlignment(SWT.LEFT);
      togVerbose.setLayoutData("growx,wrap");

      printSettings = new Button(composite, SWT.PUSH);
      printSettings.setText(ClientStrings.DUMP_SETTINGS);
      printSettings.setAlignment(SWT.CENTER);
      printSettings.setLayoutData("growx");

      togSaveClasses
          .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
          {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
              toggleSetting(settingsData.saveTracedClassfiles,
                  "[saveinstru-true", "[saveinstru-false");
              settingsData.saveTracedClassfiles = !settingsData.saveTracedClassfiles;
            }
          });
      togVerbose
          .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
          {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
              toggleSetting(settingsData.verboseMode, "[verbose-true",
                  "[verbose-false");
              settingsData.verboseMode = !settingsData.verboseMode;
            }
          });
      printSettings
          .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
          {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
              textOutputTab.filterThread.addTraceLine("Settings:"
                  + settingsData.toString());
            }
          });
    }
  }

  TextOutputTab textOutputTab;

  private void fillOutputTabs(TabFolder tabFolder)
  {
    TabItem textOutputTabItem = new TabItem(tabFolder, SWT.NONE);
    textOutputTabItem.setText("Output");
    textOutputTab = new TextOutputTab(tabFolder, textOutputTabItem);
  }

  private class TextOutputTab
  {
    final StyledText textOutput;
    final TraceFilterThread filterThread;
    final Button textFilter;
    final ProgressBar pBar;
    final Button cancelButton;
    private Button networkOutput;

    private TextOutputTab(TabFolder tabFolder, TabItem textOutputTab)
    {
      MigLayout windowLayout = new MigLayout("fill,wmin 0,hmin 0",
          "[70][70][70][70][150][70][grow]", "[20][grow]");

      final Composite composite = new Composite(tabFolder, SWT.NONE);
      composite.setLayout(windowLayout);
      textOutputTab.setControl(composite);

      Button clearText = new Button(composite, SWT.PUSH);
      clearText.setText(ClientStrings.CLEAR_TEXT);
      clearText.setLayoutData("grow");

      Button autoScrollBtn = new Button(composite, SWT.CHECK);
      autoScrollBtn.setText(ClientStrings.AUTO_SCROLL);
      autoScrollBtn.setLayoutData("grow");
      autoScrollBtn.setSelection(autoScroll);

      networkOutput = new Button(composite, SWT.CHECK);
      networkOutput.setText(ClientStrings.ENABLE_NETWORK_OUTPUT);
      networkOutput.setAlignment(SWT.CENTER);

      textFilter = new Button(composite, SWT.PUSH);
      textFilter.setText(ClientStrings.FILTER_TEXT);
      textFilter.setLayoutData("grow");

      pBar = new ProgressBar(composite, SWT.NORMAL);
      pBar.setLayoutData("grow");
      pBar.setVisible(false);

      cancelButton = new Button(composite, SWT.PUSH);
      cancelButton.setText(ClientStrings.CANCEL_TEXT);
      cancelButton.setLayoutData("grow,wrap");
      cancelButton.setVisible(false);

      textOutput = new StyledText(composite, SWT.MULTI | SWT.V_SCROLL
          | SWT.H_SCROLL | SWT.BORDER);
      textOutput.setEditable(false);
      textOutput.setLayoutData("spanx,grow,wmin 0,hmin 0");
      textOutput.setBackground(Display.getCurrent().getSystemColor(
          SWT.COLOR_WHITE));

      clearText
          .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
          {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
              sWindow.getDisplay().asyncExec(new Runnable()
              {
                @Override
                public void run()
                {
                  filterThread.setClearTrace();
                }
              });
            }
          });

      autoScrollBtn
          .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
          {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
              autoScroll = !autoScroll;
            }
          });

      networkOutput
          .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
          {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
              toggleSetting(settingsData.netOutEnabled, "[out-network-true",
                  "[out-network-false");
              settingsData.netOutEnabled = !settingsData.netOutEnabled;
            }
          });

      final String helpText = "Enter pattern in the form "
          + "\"text\" or using wildcards " + "\"tex*\" or \"*ext\" etc";

      final PatternInputCallback patternCallback = new PatternInputCallback()
      {
        private String includePattern = null;
        private String excludePattern = null;

        @Override
        public void setIncludePattern(String newIncludePattern)
        {
          includePattern = newIncludePattern;
          savePatterns();
        }

        @Override
        public void setExcludePattern(String newExcludePattern)
        {
          excludePattern = newExcludePattern;
          savePatterns();
        }

        private void savePatterns()
        {
          if ((includePattern != null) && (excludePattern != null))
          {
            applyPatterns(includePattern, excludePattern);
          }
        }
      };

      textFilter
          .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
          {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
              PatternInputWindow regexInput;
              regexInput = new PatternInputWindow("Set Text Filter", helpText,
                  patternCallback, includeFilterPattern.pattern(),
                  excludeFilterPattern.pattern());
              placeDialogInCenter(sWindow.getBounds(), regexInput.sWindow);
            }
          });

      filterThread = new TraceFilterThread(new TraceTextHandler()
      {
        @Override
        public void setText(final String traceText)
        {
          if (sWindow.isDisposed())
            return;
          sWindow.getDisplay().syncExec(new Runnable()
          {
            @Override
            public void run()
            {
              textOutput.setRedraw(false);
              textOutput.setText(traceText);
              if (autoScroll)
              {
                textOutput.setTopIndex(Integer.MAX_VALUE);
              }
              textOutput.setRedraw(true);
            }
          });
        }

        @Override
        public void appendText(final String traceText)
        {
          if (sWindow.isDisposed())
            return;
          sWindow.getDisplay().syncExec(new Runnable()
          {
            @Override
            public void run()
            {
              textOutput.append(traceText);
              if (autoScroll)
              {
                textOutput.setTopIndex(Integer.MAX_VALUE);
              }
            }
          });
        }
      });
    }

    private Pattern getPattern(String patternString)
    {
      Pattern retPattern;
      if (patternString.equals(".*"))
      {
        retPattern = TraceFilterThread.MATCH_ALL;
      } else if (patternString.equals(""))
      {
        retPattern = TraceFilterThread.MATCH_NONE;
      } else
      {
        retPattern = Pattern.compile(patternString, Pattern.DOTALL);
      }
      return retPattern;
    }

    private void applyPatterns(String newIncludePattern,
        String newExcludePattern)
    {
      if (newIncludePattern.equals(includeFilterPattern.pattern())
          && newExcludePattern.equals(excludeFilterPattern.pattern()))
      {
        return;
      } else
      {
        oldIncludeFilterPattern = includeFilterPattern;
        oldExcludeFilterPattern = excludeFilterPattern;

        includeFilterPattern = getPattern(newIncludePattern);
        excludeFilterPattern = getPattern(newExcludePattern);

      }
      if (sWindow.isDisposed())
        return;
      sWindow.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          textFilter.setEnabled(false);
          pBar.setVisible(true);
          cancelButton.setVisible(true);
          final boolean[] filterCancelled = new boolean[]
          { false };

          final SelectionListener cancelListener = new org.eclipse.swt.events.SelectionAdapter()
          {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
              filterCancelled[0] = true;
            }
          };

          cancelButton.addSelectionListener(cancelListener);

          final Pattern newIncludePattern = includeFilterPattern;
          final Pattern newExcludePattern = excludeFilterPattern;

          final TraceFilterProgressHandler progressHandler = new TraceFilterProgressHandler()
          {
            @Override
            public boolean setProgress(final int percent)
            {
              if (sWindow.isDisposed())
                return false;
              sWindow.getDisplay().asyncExec(new Runnable()
              {
                @Override
                public void run()
                {
                  if (percent < 100)
                  {
                    pBar.setSelection(percent);
                  } else
                  {
                    pBar.setVisible(false);
                    cancelButton.setVisible(false);
                    cancelButton.removeSelectionListener(cancelListener);
                    textFilter.setEnabled(true);
                    if (filterCancelled[0])
                    {
                      includeFilterPattern = oldIncludeFilterPattern;
                      excludeFilterPattern = oldExcludeFilterPattern;
                    }
                  }
                }
              });
              return filterCancelled[0];
            }

            @Override
            public Pattern getIncludePattern()
            {
              return newIncludePattern;
            }

            @Override
            public Pattern getExcludePattern()
            {
              return newExcludePattern;
            }
          };

          filterThread.applyFilter(progressHandler);
        }
      });
    }
  }

  // Window ref
  private final TraceWindow traceDialogRef = this;

  // State
  private enum ConnectState
  {
    DISCONNECTED_ERR, DISCONNECTED, CONNECTING, CONNECTED
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
  private Pattern includeFilterPattern = TraceFilterThread.MATCH_ALL;
  private Pattern oldIncludeFilterPattern = TraceFilterThread.MATCH_ALL;
  private Pattern excludeFilterPattern = TraceFilterThread.MATCH_NONE;
  private Pattern oldExcludeFilterPattern = TraceFilterThread.MATCH_NONE;
  private boolean autoScroll = true;

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
            networkTracePort, traceDialogRef, textOutputTab.filterThread);
        networkTraceThread.start();
      } catch (IOException ex)
      {
        textOutputTab.filterThread
            .addSystemTraceLine("Failed to setup network trace: "
                + ex.toString());
      }
    } else
    {
      connectionState = ConnectState.DISCONNECTED_ERR;
    }
    updateUIState();
  }

  public ParsedSettingsData getSettings()
  {
    return settingsData;
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
          textOutputTab.filterThread.addSystemTraceLine("No instrumented classes");
        } else
        {
          modifiedClasses = modifiedClasses.substring(1, modifiedClasses
              .length() - 1);
          String[] classNames = modifiedClasses.split(",");
          for (String className : classNames)
          {
            textOutputTab.filterThread.addSystemTraceLine("Instrumented: "
                + (className != null ? className.trim() : "null"));
          }
        }
      }
    }).start();
  }

  public void setRegex(final String includePattern, final String excludePattern)
  {
    if (!sWindow.isDisposed())
    {
      sWindow.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          settingsData.classRegex = includePattern;
          settingsData.classExcludeRegex = excludePattern;
          controlThread.sendMessage("[regex-" + includePattern
              + "[excluderegex-" + excludePattern);
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
      final String enableCommand, final String disableCommand)
  {
    if (!sWindow.isDisposed())
    {
      sWindow.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          if (settingValue)
          {
            controlThread.sendMessage(disableCommand);
          } else
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
    } else
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
    } else
    {
      chooseText(connTab.connectButton,
          (connectionState == ConnectState.CONNECTED), ClientStrings.CONNECT,
          ClientStrings.DISCONNECT);
      chooseText(instruTab.togInstru,
          settingsData.instrEnabled, ClientStrings.ENABLE_INSTR,
          ClientStrings.DISABLE_INSTR);

      if (connectionState == ConnectState.CONNECTED)
      {
        // Enable all buttons
        instruTab.classRegex.setEnabled(true);
        instruTab.listClasses.setEnabled(true);
        instruTab.togInstru.setEnabled(true);

        traceTab.argsTrace.setEnabled(true);
        traceTab.branchTrace.setEnabled(true);
        traceTab.entryExitTrace.setEnabled(true);
        traceTab.fileOutput.setEnabled(true);
        traceTab.stdOutOutput.setEnabled(true);

        extraTab.togSaveClasses.setEnabled(true);
        extraTab.togVerbose.setEnabled(true);
        extraTab.printSettings.setEnabled(true);

        textOutputTab.networkOutput.setEnabled(true);

        // Update the button pressed/unpressed state
        instruTab.togInstru.setSelection(settingsData.instrEnabled);
        traceTab.argsTrace.setSelection(settingsData.argsEnabled);
        traceTab.branchTrace.setSelection(settingsData.branchEnabled);
        traceTab.entryExitTrace.setSelection(settingsData.entryExitEnabled);
        traceTab.fileOutput.setSelection(settingsData.fileOutEnabled);
        traceTab.stdOutOutput.setSelection(settingsData.stdOutEnabled);

        extraTab.togSaveClasses.setSelection(settingsData.saveTracedClassfiles);
        extraTab.togVerbose.setSelection(settingsData.verboseMode);

        textOutputTab.networkOutput.setSelection(settingsData.netOutEnabled);

        // Update number of classes
        instruTab.setStatus(settingsData.instruClasses,
            settingsData.totalClasses);
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

        instruTab.classRegex.setEnabled(false);
        instruTab.listClasses.setEnabled(false);
        instruTab.togInstru.setEnabled(false);

        traceTab.argsTrace.setEnabled(false);
        traceTab.branchTrace.setEnabled(false);
        traceTab.entryExitTrace.setEnabled(false);
        traceTab.fileOutput.setEnabled(false);
        traceTab.stdOutOutput.setEnabled(false);

        extraTab.togSaveClasses.setEnabled(false);
        extraTab.togVerbose.setEnabled(false);
        extraTab.printSettings.setEnabled(false);

        textOutputTab.networkOutput.setEnabled(false);
      }
    }
  }

  public void setProgress(final Map<String, String> progressMap)
  {
    if (!sWindow.isDisposed())
    {
      sWindow.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          int numDone = Integer.parseInt(progressMap
              .get(AgentConfigConstants.NUM_PROGRESS_COUNT));
          int numTotal = Integer.parseInt(progressMap
              .get(AgentConfigConstants.NUM_PROGRESS_TOTAL));
          boolean done = (progressMap
              .get(AgentConfigConstants.NUM_PROGRESS_DONE) != null);
          instruTab.setProgress(numDone, numTotal, done);
        }
      });
    }
  }
  
  public void setStatus(final Map<String, String> statusMap)
  {
    if (!sWindow.isDisposed())
    {
      sWindow.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          int numInstr = Integer.parseInt(statusMap
              .get(AgentConfigConstants.NUM_INSTR_CLASSES));
          int numTotal = Integer.parseInt(statusMap
              .get(AgentConfigConstants.NUM_TOTAL_CLASSES));
          instruTab.setStatus(numInstr, numTotal);
        }
      });
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
          textOutputTab.filterThread
              .addSystemTraceLine("Latest Settings Received");
          settingsData = new ParsedSettingsData(settingsMap);
          connectionState = ConnectState.CONNECTED;
          updateUIStateSameThread();
        }
      });
    }
  }

  private static void placeDialogInCenter(Rectangle parentSize, Shell shell)
  {
    Rectangle mySize = shell.getBounds();

    int locationX, locationY;
    locationX = (parentSize.width - mySize.width) / 2 + parentSize.x;
    locationY = (parentSize.height - mySize.height) / 2 + parentSize.y;

    shell.setLocation(new Point(locationX, locationY));
    shell.open();
  }
}
