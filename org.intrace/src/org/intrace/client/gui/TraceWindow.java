package org.intrace.client.gui;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
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
import org.intrace.shared.CallersConfigConstants;

public class TraceWindow
{
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
    buttonTabs.setLayoutData("growx,wrap,wmin 0,hmin 0");
    
    outputTabs = new TabFolder(sWindow, SWT.NONE);    
    outputTabs.setLayoutData("grow");

    fillButtonTabs(buttonTabs);
    fillOutputTabs(outputTabs);

    updateUIStateSameThread();
  }

  private ConnectionTab connTab;
  private InstruTab instruTab;
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
            Connection.connectToAgent(traceDialogRef, sWindow,
                                      addressInput.getText(),
                                      portInput.getText(), connectStatus);
          }
          else if (connectionState == ConnectState.CONNECTED)
          {
            disconnect();
          }
        }
      };

      addressInput.addSelectionListener(connectListen);
      portInput.addSelectionListener(connectListen);
      connectButton.addSelectionListener(connectListen);

      printSettings
                   .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
                   {
                     @Override
                     public void widgetSelected(SelectionEvent arg0)
                     {
                       textOutputTab.filterThread
                                                 .addTraceLine("Settings:"
                                                               + settingsData
                                                                             .toString());
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
    final Label instrStatusLabel;

    private InstruTab(TabFolder tabFolder, TabItem instrTab)
    {
      MigLayout windowLayout = new MigLayout("fill", "[][grow][][]", "[][]");

      Composite composite = new Composite(tabFolder, SWT.NONE);
      composite.setLayout(windowLayout);
      instrTab.setControl(composite);
      composite.setLayoutData("hmin 0");

      Group mainControlGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      MigLayout mainControlGroupLayout = new MigLayout("",
                                                       "[100][100][100]");
      mainControlGroup.setLayout(mainControlGroupLayout);
      mainControlGroup.setText("Control");
      mainControlGroup.setLayoutData("hmin 0");

      togInstru = new Button(mainControlGroup, SWT.TOGGLE);
      togInstru.setText(ClientStrings.ENABLE_INSTR);
      togInstru.setAlignment(SWT.CENTER);
      togInstru.setLayoutData("");

      classRegex = new Button(mainControlGroup, SWT.PUSH);
      classRegex.setText(ClientStrings.SET_CLASSREGEX);
      classRegex.setLayoutData("gapx 10px");

      listClasses = new Button(mainControlGroup, SWT.PUSH);
      listClasses.setText(ClientStrings.LIST_MODIFIED_CLASSES);
      listClasses.setAlignment(SWT.CENTER);
      listClasses.setLayoutData("");

      Group settingsGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      MigLayout settingsGroupLayout = new MigLayout("fill", "[100]");
      settingsGroup.setLayout(settingsGroupLayout);
      settingsGroup.setText("Settings");
      settingsGroup.setLayoutData("grow,skip,spany");

      togJars = new Button(settingsGroup, SWT.TOGGLE);
      togJars.setText(ClientStrings.ENABLE_ALLOWJARS);
      togJars.setAlignment(SWT.CENTER);
      togJars.setLayoutData("growx,wrap,aligny top");

      Group debugGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      MigLayout debugGroupLayout = new MigLayout("fill", "[100]");
      debugGroup.setLayout(debugGroupLayout);
      debugGroup.setText("Debug");
      debugGroup.setLayoutData("grow,skip,spany,wrap");

      togSaveClasses = new Button(debugGroup, SWT.TOGGLE);
      togSaveClasses.setText(ClientStrings.ENABLE_SAVECLASSES);
      togSaveClasses.setAlignment(SWT.CENTER);
      togSaveClasses.setLayoutData("growx,wrap,aligny top");

      togVerbose = new Button(debugGroup, SWT.TOGGLE);
      togVerbose.setText(ClientStrings.ENABLE_VERBOSEMODE);
      togVerbose.setAlignment(SWT.CENTER);
      togVerbose.setLayoutData("growx,aligny top");

      Group statusGroup = new Group(composite, SWT.SHADOW_IN);
      MigLayout statusGroupLayout = new MigLayout("fillx");
      statusGroup.setLayout(statusGroupLayout);
      statusGroup.setText("Status");
      statusGroup.setLayoutData("growx");

      instrStatusLabel = new Label(statusGroup, SWT.WRAP | SWT.SHADOW_IN
                                                | SWT.VERTICAL);
      instrStatusLabel.setAlignment(SWT.LEFT);
      instrStatusLabel.setLayoutData("wmin 0,hmin 0");
      setStatus(0, 0);

      togInstru
               .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
               {
                 @Override
                 public void widgetSelected(SelectionEvent arg0)
                 {
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
                                                                           "Set Class Regex",
                                                                           helpText,
                                                                           new PatternInputCallback()
                                                                           {
                                                                             private String includePattern = null;
                                                                             private String excludePattern = null;

                                                                             @Override
                                                                             public void setIncludePattern(
                                                                                                           String newIncludePattern)
                                                                             {
                                                                               includePattern = newIncludePattern;
                                                                               savePatterns();
                                                                             }

                                                                             @Override
                                                                             public void setExcludePattern(
                                                                                                           String newExcludePattern)
                                                                             {
                                                                               excludePattern = newExcludePattern;
                                                                               savePatterns();
                                                                             }

                                                                             private void savePatterns()
                                                                             {
                                                                               if ((includePattern != null)
                                                                                   && (excludePattern != null))
                                                                               {
                                                                                 setRegex(
                                                                                          includePattern,
                                                                                          excludePattern);
                                                                               }
                                                                             }
                                                                           },
                                                                           settingsData.classRegex,
                                                                           settingsData.classExcludeRegex);
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
      togJars
             .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
             {
               @Override
               public void widgetSelected(SelectionEvent arg0)
               {
                 toggleSetting(settingsData.allowJarsToBeTraced,
                               "[instrujars-true", "[instrujars-false");
                 settingsData.allowJarsToBeTraced = !settingsData.allowJarsToBeTraced;
               }
             });
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
    }

    private void setStatus(int instruClasses, int totalClasses)
    {
      instrStatusLabel.setText("Instru'd: " + instruClasses + "/"
                               + totalClasses);
    }

    private void setProgress(int progressClasses, int totalClasses)
    {
      instrStatusLabel.setText("Progress: " + progressClasses + "/"
                               + totalClasses);
    }
  }

  private class TraceTab
  {
    final Button entryExitTrace;
    final Button branchTrace;
    final Button argsTrace;

    final Button stdOutOutput;
    final Button fileOutput;
    final Button networkOutput;

    private TraceTab(TabFolder tabFolder, TabItem traceTab)
    {
      MigLayout windowLayout = new MigLayout("fill", "[][][grow]");

      Composite composite = new Composite(tabFolder, SWT.NONE);
      composite.setLayout(windowLayout);
      traceTab.setControl(composite);

      Group traceTypesGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      MigLayout traceTypesGroupLayout = new MigLayout("fill", "[80][80][80]");
      traceTypesGroup.setLayout(traceTypesGroupLayout);
      traceTypesGroup.setText("Trace Settings");
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

      Group outputTypesGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      MigLayout outputTypesGroupLayout = new MigLayout("fill", "[80][80][80]");
      outputTypesGroup.setLayout(outputTypesGroupLayout);
      outputTypesGroup.setText("Output Settings");
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

      entryExitTrace
                    .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
                    {
                      @Override
                      public void widgetSelected(SelectionEvent arg0)
                      {
                        toggleSetting(settingsData.entryExitEnabled,
                                      "[trace-ee-true", "[trace-ee-false");
                        settingsData.entryExitEnabled = !settingsData.entryExitEnabled;
                      }
                    });
      branchTrace
                 .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
                 {
                   @Override
                   public void widgetSelected(SelectionEvent arg0)
                   {
                     toggleSetting(settingsData.branchEnabled,
                                   "[trace-branch-true", "[trace-branch-false");
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
                      toggleSetting(settingsData.stdOutEnabled,
                                    "[out-stdout-true", "[out-stdout-false");
                      settingsData.stdOutEnabled = !settingsData.stdOutEnabled;
                    }
                  });
      fileOutput
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
                {
                  @Override
                  public void widgetSelected(SelectionEvent arg0)
                  {
                    toggleSetting(settingsData.fileOutEnabled,
                                  "[out-file-true", "[out-file-false");
                    settingsData.fileOutEnabled = !settingsData.fileOutEnabled;
                  }
                });
      networkOutput
                   .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
                   {
                     @Override
                     public void widgetSelected(SelectionEvent arg0)
                     {
                       toggleSetting(settingsData.netOutEnabled,
                                     "[out-network-true", "[out-network-false");
                       settingsData.netOutEnabled = !settingsData.netOutEnabled;
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
      callersTypesGroup.setText("Start Capture");
      callersTypesGroup.setLayoutData("spany,grow");

      callersCapture = new Button(callersTypesGroup, SWT.PUSH);
      callersCapture.setText(ClientStrings.BEGIN_CAPTURE_CALLERS);
      callersCapture.setLayoutData("spany,grow");

      final String helpText = "Enter pattern in the form "
                              + "\"functionName\" or using wildcards "
                              + "\"functionN*\" or \"*tionName\" etc";

      callersCapture
                    .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
                    {
                      @Override
                      public void widgetSelected(SelectionEvent arg0)
                      {
                        PatternInputWindow regexInput = new PatternInputWindow(
                                                                               "Enter Method Regex",
                                                                               helpText,
                                                                               new PatternInputCallback()
                                                                               {
                                                                                 @Override
                                                                                 public void setIncludePattern(
                                                                                                               String newPattern)
                                                                                 {
                                                                                   setCallersRegex(newPattern);
                                                                                 }

                                                                                 @Override
                                                                                 public void setExcludePattern(
                                                                                                               String newExcludePattern)
                                                                                 {
                                                                                   // Do
                                                                                   // nothing
                                                                                 }
                                                                               },
                                                                               "",
                                                                               null);
                        placeDialogInCenter(sWindow.getBounds(),
                                            regexInput.sWindow);
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
    final StyledText textOutput;
    final TraceFilterThread filterThread;
    final Button textFilter;
    final ProgressBar pBar;
    final Button cancelButton;

    private TextOutputTab(TabFolder tabFolder, TabItem textOutputTab)
    {
      MigLayout windowLayout = new MigLayout("fill",
                                             "[70][70][70][150][70][grow]",
                                             "[20][grow]");

      final Composite composite = new Composite(tabFolder, SWT.NONE);
      composite.setLayout(windowLayout);
      textOutputTab.setControl(composite);

      Button clearText = new Button(composite, SWT.PUSH);
      clearText.setText(ClientStrings.CLEAR_TEXT);
      clearText.setLayoutData("grow");

      Button autoScrollBtn = new Button(composite, SWT.TOGGLE);
      autoScrollBtn.setText(ClientStrings.AUTO_SCROLL);
      autoScrollBtn.setLayoutData("grow");
      autoScrollBtn.setSelection(autoScroll);

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

      textOutput = new StyledText(composite, SWT.MULTI | SWT.WRAP
                                             | SWT.V_SCROLL | SWT.BORDER);
      textOutput.setEditable(false);
      textOutput.setLayoutData("spanx,grow");
      textOutput.setBackground(Display.getCurrent()
                                      .getSystemColor(SWT.COLOR_WHITE));

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

      final String helpText = "Enter pattern in the form "
                              + "\"text\" or using wildcards "
                              + "\"tex*\" or \"*ext\" etc";

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
                    regexInput = new PatternInputWindow(
                                                        "Set Text Filter",
                                                        helpText,
                                                        patternCallback,
                                                        includeFilterPattern
                                                                            .pattern(),
                                                        excludeFilterPattern
                                                                            .pattern());
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
      }
      else if (patternString.equals(""))
      {
        retPattern = TraceFilterThread.MATCH_NONE;
      }
      else
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
      }
      else
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
                  }
                  else
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

      endCapture
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
                {
                  @Override
                  public void widgetSelected(SelectionEvent arg0)
                  {
                    endCapture.setEnabled(false);
                    controlThread.sendMessage("[callers-end-" + tabCallersId);
                  }
                });

      closeCapture
                  .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
                  {
                    @Override
                    public void widgetSelected(SelectionEvent arg0)
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
          String newRootText = "Callers: "
                               + callersRegex.toString().replace(".*", "*");
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
        networkTraceThread = new NetworkDataReceiverThread(
                                                           remoteAddress,
                                                           networkTracePort,
                                                           traceDialogRef,
                                                           textOutputTab.filterThread);
        networkTraceThread.start();
      }
      catch (IOException ex)
      {
        textOutputTab.filterThread
                                  .addSystemTraceLine("Failed to setup network trace: "
                                                      + ex.toString());
      }
    }
    else
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
          textOutputTab.filterThread.addSystemTraceLine("No modified classes");
        }
        else
        {
          modifiedClasses = modifiedClasses
                                           .substring(
                                                      1,
                                                      modifiedClasses.length() - 1);
          if (modifiedClasses.indexOf(",") == -1)
          {
            textOutputTab.filterThread.addSystemTraceLine("Modified: "
                                                          + modifiedClasses);
          }
          else
          {
            String[] classNames = modifiedClasses.split(",");
            for (String className : classNames)
            {
              textOutputTab.filterThread
                                        .addSystemTraceLine("Modified: "
                                                            + (className != null ? className
                                                                                            .trim()
                                                                                : "null"));
            }
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

        traceTab.argsTrace.setEnabled(true);
        traceTab.branchTrace.setEnabled(true);
        traceTab.entryExitTrace.setEnabled(true);
        traceTab.fileOutput.setEnabled(true);
        traceTab.networkOutput.setEnabled(true);
        traceTab.stdOutOutput.setEnabled(true);

        callSettingsTab.callersCapture.setEnabled(true);

        // Update the button pressed/unpressed state
        instruTab.togInstru.setSelection(settingsData.instrEnabled);
        instruTab.togJars.setSelection(settingsData.allowJarsToBeTraced);
        instruTab.togSaveClasses
                                .setSelection(settingsData.saveTracedClassfiles);
        instruTab.togVerbose.setSelection(settingsData.verboseMode);

        traceTab.argsTrace.setSelection(settingsData.argsEnabled);
        traceTab.branchTrace.setSelection(settingsData.branchEnabled);
        traceTab.entryExitTrace.setSelection(settingsData.entryExitEnabled);
        traceTab.fileOutput.setSelection(settingsData.fileOutEnabled);
        traceTab.networkOutput.setSelection(settingsData.netOutEnabled);
        traceTab.stdOutOutput.setSelection(settingsData.stdOutEnabled);

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
        connTab.printSettings.setEnabled(false);

        instruTab.classRegex.setEnabled(false);
        instruTab.listClasses.setEnabled(false);
        instruTab.togInstru.setEnabled(false);
        instruTab.togJars.setEnabled(false);
        instruTab.togSaveClasses.setEnabled(false);
        instruTab.togVerbose.setEnabled(false);

        traceTab.argsTrace.setEnabled(false);
        traceTab.branchTrace.setEnabled(false);
        traceTab.entryExitTrace.setEnabled(false);
        traceTab.fileOutput.setEnabled(false);
        traceTab.networkOutput.setEnabled(false);
        traceTab.stdOutOutput.setEnabled(false);

        callSettingsTab.callersCapture.setEnabled(false);
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
          instruTab
                   .setProgress(
                                Integer
                                       .parseInt(progressMap
                                                            .get(AgentConfigConstants.NUM_PROGRESS_COUNT)),
                                Integer
                                       .parseInt(progressMap
                                                            .get(AgentConfigConstants.NUM_PROGRESS_TOTAL)));
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
