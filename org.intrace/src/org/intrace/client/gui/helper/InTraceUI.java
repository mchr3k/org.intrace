package org.intrace.client.gui.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.intrace.client.gui.helper.Connection.ConnectState;
import org.intrace.client.gui.helper.Connection.ISocketCallback;
import org.intrace.client.gui.helper.ControlConnectionThread.IControlConnectionListener;
import org.intrace.client.gui.helper.NetworkDataReceiverThread.INetworkOutputConfig;
import org.intrace.client.gui.helper.PatternInputWindow.PatternInputCallback;
import org.intrace.client.gui.helper.TraceFilterThread.TraceFilterProgressHandler;
import org.intrace.client.gui.helper.TraceFilterThread.TraceTextHandler;
import org.intrace.shared.AgentConfigConstants;

public class InTraceUI implements ISocketCallback, IControlConnectionListener
{
  public static interface IConnectionStateCallback
  {
    public void setConnectionState(ConnectState state);
  }
  
  public static String NEWLINE = System.getProperty("line.separator");

  public void open()
  {
    placeDialogInCenter(sWindow.getDisplay().getPrimaryMonitor().getBounds(),
        sWindow);
    sWindow.open();
    Display display = Display.getDefault();
    while (!sRoot.isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }

  private final Shell sWindow;
  private final Composite sRoot;
  
  private final TabFolder outputTabs;
  private final TabFolder buttonTabs;  
  private final CTabFolder buttonCTabs;
  private final CTabFolder outputCTabs;
  private final UIMode mode;
  
  private IConnectionStateCallback connCallback = null;
  private MigLayout rootLayout;

  public void setConnCallback(IConnectionStateCallback connCallback)
  {
    this.connCallback = connCallback;
    if (connCallback != null)
    {
      connCallback.setConnectionState(connectionState);
    }
  }

  public static enum UIMode
  {
    STANDALONE,
    ECLIPSE
  }
  
  public InTraceUI(Shell xiWindow, Composite xiRoot, UIMode xiMode)
  {
    sWindow = xiWindow;
    sRoot = xiRoot;
    mode = xiMode;
    
    rootLayout = new MigLayout("fill", "[]", "[][][grow]");
    xiRoot.setLayout(rootLayout);
    
    if (mode == UIMode.STANDALONE)
    {
      buttonCTabs = null;           
      buttonTabs = new TabFolder(xiRoot, SWT.NONE);
      buttonTabs.setLayoutData("grow,wrap,wmin 0");      
      fillButtonTabs(buttonTabs);
    }
    else
    {
      buttonTabs = null;      
      buttonCTabs = new CTabFolder(xiRoot, SWT.TOP | SWT.BORDER);
      buttonCTabs.setSimple(false);
      buttonCTabs.setLayoutData("grow,wrap,wmin 0");
      fillButtonCTabs(buttonCTabs);
      buttonCTabs.setSelection(0);
    }
     
    if (mode == UIMode.STANDALONE)
    {
      outputCTabs = null;
      outputTabs = new TabFolder(xiRoot, SWT.NONE);
      outputTabs.setLayoutData("grow,wmin 0,hmin 0,cell 0 2");
      fillOutputTabs(outputTabs);
    }
    else
    {       
      outputTabs = null;
      outputCTabs = new CTabFolder(xiRoot, SWT.TOP | SWT.BORDER);
      outputCTabs.setSimple(false);
      outputCTabs.setLayoutData("grow,wmin 0,hmin 0,cell 0 2");      
      fillOutputCTabs(outputCTabs);           
      outputCTabs.setSelection(0);      
    }
    
    updateUIStateSameThread();
    
    sWindow.getDisplay().addFilter(SWT.KeyDown, new Listener()
    {
      @Override
      public void handleEvent(Event e)
      {
        if (((e.stateMask & SWT.CTRL) != 0) &&
            (e.keyCode == 'f') &&
            (sWindow.getDisplay().getActiveShell() == sWindow) &&
            (textOutputTab.textOutput.isFocusControl()))
        {
          textOutputTab.findInput.setFocus();
        }
      }
    });   
  }
  
  public void setFixedLocalConnection(final String xiPort)
  {    
    fixedConnection = true;
    if (!sRoot.isDisposed())
    {
      sWindow.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          connTab.addressInput.setText("localhost");
          connTab.portInput.setText(xiPort);
          textOutputTab.filterThread.addSystemTraceLine("Instructions");
          textOutputTab.filterThread.addSystemTraceLine(" - Select Classes you want to Trace");
          textOutputTab.filterThread.addSystemTraceLine("Full help available on the Help tab");
          textOutputTab.filterThread.addSystemTraceLine("");
          setConnectionState(ConnectState.CONNECTING);
          updateUIStateSameThread();
          Connection.connectToAgent(thisWindow, sWindow, "localhost", xiPort);
        }
      });
    }
  }
  
  public void setFixedLocalConnection(final Socket xiSocket)
  {    
    fixedConnection = true;
    if (!sRoot.isDisposed())
    {
      sWindow.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          connTab.addressInput.setText("localhost");
          connTab.portInput.setText("detecting...");
          textOutputTab.filterThread.addSystemTraceLine("Instructions");
          textOutputTab.filterThread.addSystemTraceLine(" - Select Classes you want to Trace");
          textOutputTab.filterThread.addSystemTraceLine("Full help available on the Help tab");
          textOutputTab.filterThread.addSystemTraceLine("");
        }
      });
    }
    setSocket(xiSocket);
  }

  private ConnectionTab connTab;
  private InstruTab instruTab;
  private TraceTab traceTab;
  private ExtrasTab extraTab;
  private Composite startPanel;

  private void fillButtonTabs(TabFolder tabFolder)
  {
    TabItem connTabItem = new TabItem(tabFolder, SWT.NONE);
    connTabItem.setText("Connection");
    connTab = new ConnectionTab(tabFolder);
    connTabItem.setControl(connTab.composite);

    TabItem instrTabItem = new TabItem(tabFolder, SWT.NONE);
    instrTabItem.setText("Instrumentation");
    instruTab = new InstruTab(tabFolder);
    instrTabItem.setControl(instruTab.composite);

    TabItem traceTabItem = new TabItem(tabFolder, SWT.NONE);
    traceTabItem.setText("Trace");
    traceTab = new TraceTab(tabFolder);
    traceTabItem.setControl(traceTab.composite);

    TabItem extraTabItem = new TabItem(tabFolder, SWT.NONE);
    extraTabItem.setText("Advanced");
    extraTab = new ExtrasTab(tabFolder);
    extraTabItem.setControl(extraTab.composite);
  }
  
  private void fillButtonCTabs(CTabFolder tabFolder)
  {
    CTabItem connTabItem = new CTabItem(tabFolder, SWT.NONE);
    connTabItem.setText("Connection");
    connTab = new ConnectionTab(tabFolder);
    connTabItem.setControl(connTab.composite);
    
    // Hide the connection controls - in Eclipse mode the
    // connection is handled implicitly
    connTabItem.dispose();

    CTabItem instrTabItem = new CTabItem(tabFolder, SWT.NONE);
    instrTabItem.setText("Instrumentation");
    instruTab = new InstruTab(tabFolder);
    instrTabItem.setControl(instruTab.composite);

    CTabItem traceTabItem = new CTabItem(tabFolder, SWT.NONE);
    traceTabItem.setText("Trace");
    traceTab = new TraceTab(tabFolder);
    traceTabItem.setControl(traceTab.composite);

    CTabItem extraTabItem = new CTabItem(tabFolder, SWT.NONE);
    extraTabItem.setText("Advanced");
    extraTab = new ExtrasTab(tabFolder);
    extraTabItem.setControl(extraTab.composite);
  }
  
  private boolean waitStartUIShown = false;
  
  private void addWaitStartUI()
  {    
    if (!waitStartUIShown)
    {
      startPanel = new Composite(sRoot, SWT.NONE);
      startPanel.setLayoutData("grow,wrap,cell 0 1");
      MigLayout startLayout = new MigLayout("fillx", "[align center]", "[]");
      startPanel.setLayout(startLayout);
      
      Label startLabel = new Label(startPanel, SWT.NONE);
      startLabel.setText("Program has been paused.");
      startLabel.setLayoutData("split");
      
      final Button startButton = new Button(startPanel, SWT.PUSH);
      startButton.setText("Start Program");
      
      startButton.addSelectionListener(new SelectionAdapter()
      {
        @Override
        public void widgetSelected(SelectionEvent arg0)
        {
          controlThread.sendMessage(AgentConfigConstants.START_ACTIVATE);
          controlThread.sendMessage("getsettings");
          startButton.setEnabled(false);
        }
      });
      
      sRoot.layout();
      
      waitStartUIShown = true;
    }
  }
  
  private void removeWaitStartUI()
  {
    if (waitStartUIShown)
    {
      startPanel.dispose();
      
      sRoot.layout();

      waitStartUIShown = false;      
    }
  }

  private class ConnectionTab
  {
    final Button connectButton;
    final Text addressInput;
    final Text portInput;
    final Label statusLabel;
    final Composite composite;

    private ConnectionTab(Composite parent)
    {
      MigLayout windowLayout = new MigLayout("fill", "[380][grow]");

      composite = new Composite(parent, SWT.NONE);
      composite.setLayout(windowLayout);

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

      statusLabel = new Label(statusGroup, SWT.WRAP);
      statusLabel.setAlignment(SWT.CENTER);
      statusLabel.setLayoutData("grow,wmin 0");

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
            setConnectionState(ConnectState.CONNECTING);
            updateUIStateSameThread();
            Connection.connectToAgent(thisWindow, sWindow, addressInput
                .getText(), portInput.getText());
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
    final Button classRegex;
    final Button listClasses;
    final Label instrStatusLabel;
    final Composite composite;

    private InstruTab(Composite parent)
    {
      MigLayout windowLayout = new MigLayout("fill", "[][300][grow]", "[]");

      composite = new Composite(parent, SWT.NONE);
      composite.setLayout(windowLayout);
      composite.setLayoutData("hmin 0");

      Group mainControlGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
      MigLayout mainControlGroupLayout = new MigLayout("filly", "[150]");
      mainControlGroup.setLayout(mainControlGroupLayout);
      mainControlGroup.setLayoutData("hmin 0, grow");
      mainControlGroup.setText("Instrumentation Settings");      

      classRegex = new Button(mainControlGroup, SWT.PUSH);
      classRegex.setText(ClientStrings.SET_CLASSREGEX);
      classRegex.setLayoutData("grow");

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
    }
  }

  private class TraceTab
  {
    final Button entryExitTrace;
    final Button branchTrace;
    final Button argsTrace;

    final Button stdOutOutput;
    final Button fileOutput;
    final Composite composite;

    private TraceTab(Composite parent)
    {
      MigLayout windowLayout = new MigLayout("fill", "[][][grow]");

      composite = new Composite(parent, SWT.NONE);
      composite.setLayout(windowLayout);

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
    final Button togSaveClasses;
    final Button togVerbose;
    final Button printSettings;
    final Composite composite;

    private ExtrasTab(Composite parent)
    {
      MigLayout windowLayout = new MigLayout("fill", "[200][grow]");

      composite = new Composite(parent, SWT.NONE);
      composite.setLayout(windowLayout);

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
              textOutputTab.filterThread.addSystemTraceLine("Settings:"
                  + settingsData.toString());
            }
          });
    }
  }

  TextOutputTab textOutputTab;
  HelpOutputTab helpOutputTab;

  private void fillOutputTabs(TabFolder tabFolder)
  {
    TabItem textOutputTabItem = new TabItem(tabFolder, SWT.NONE);
    textOutputTabItem.setText("Output");
    textOutputTab = new TextOutputTab(tabFolder);
    textOutputTabItem.setControl(textOutputTab.composite);
    
    TabItem helpOutputTabItem = new TabItem(tabFolder, SWT.NONE);
    helpOutputTabItem.setText("Help");
    helpOutputTab = new HelpOutputTab(tabFolder);
    helpOutputTabItem.setControl(helpOutputTab.composite);
  }
  
  private void fillOutputCTabs(CTabFolder tabFolder)
  {
    CTabItem textOutputTabItem = new CTabItem(tabFolder, SWT.NONE);
    textOutputTabItem.setText("Output");
    textOutputTab = new TextOutputTab(tabFolder);
    textOutputTabItem.setControl(textOutputTab.composite);
    
    CTabItem helpOutputTabItem = new CTabItem(tabFolder, SWT.NONE);
    helpOutputTabItem.setText("Help");
    helpOutputTab = new HelpOutputTab(tabFolder);
    helpOutputTabItem.setControl(helpOutputTab.composite);
  }

  private class TextOutputTab
  {
    final StyledText textOutput;
    final TraceFilterThread filterThread;
    final Button textFilter;
    final ProgressBar pBar;
    final Button cancelButton;
    final Button networkOutput;
    final Button enableFilter;
    final Label statusLabel;
    final Button downButton;
    final Button upButton;
    final Text findInput;
    final Composite composite;

    private TextOutputTab(Composite parent)
    {
      MigLayout windowLayout = new MigLayout("fill,wmin 0,hmin 0",
          "[100][100][100][150][100][grow]", "[20][20][grow][]");

      composite = new Composite(parent, SWT.NONE);
      composite.setLayout(windowLayout);

      Button saveText = new Button(composite, SWT.PUSH);
      saveText.setText(ClientStrings.SAVE_TEXT);
      saveText.setLayoutData("grow");
      
      Button clearText = new Button(composite, SWT.PUSH);
      clearText.setText(ClientStrings.CLEAR_TEXT);
      clearText.setLayoutData("grow");
      
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
      
      networkOutput = new Button(composite, SWT.CHECK);
      networkOutput.setText(ClientStrings.ENABLE_NETWORK_OUTPUT);
      networkOutput.setLayoutData("grow");
      networkOutput.setSelection(true);
      
      Button autoScrollBtn = new Button(composite, SWT.CHECK);
      autoScrollBtn.setText(ClientStrings.AUTO_SCROLL);
      autoScrollBtn.setLayoutData("grow");
      autoScrollBtn.setSelection(autoScroll);
      
      enableFilter = new Button(composite, SWT.CHECK);
      enableFilter.setText(ClientStrings.ENABLE_FILTER);
      enableFilter.setLayoutData("grow,wrap");
      enableFilter.setSelection(true);

      textOutput = new StyledText(composite, SWT.MULTI | SWT.V_SCROLL
          | SWT.H_SCROLL | SWT.BORDER);
      textOutput.setEditable(false);
      textOutput.setLayoutData("spanx,grow,wmin 0,hmin 0,wrap");
      textOutput.setBackground(Display.getCurrent().getSystemColor(
          SWT.COLOR_WHITE));          
      
      textOutput.addMenuDetectListener(new MenuDetectListener()
      {        
        @Override
        public void menuDetected(MenuDetectEvent e)
        {
          if (e.widget == textOutput)
          {
            final String selectedText = textOutput.getSelectionText();
            if (selectedText.length() > 0)
            {
              Menu textMenu = new Menu(sWindow, SWT.POP_UP);
              MenuItem includeItem = new MenuItem(textMenu, SWT.PUSH);
              includeItem.setText("Include: " + getDisplayPatternFromText(selectedText));
              includeItem.addSelectionListener(new SelectionAdapter()
              {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                  String existingIncludePattern = lastEnteredIncludeFilterPattern.pattern() + "|";
                  if (lastEnteredIncludeFilterPattern == TraceFilterThread.MATCH_ALL)
                  {
                    existingIncludePattern = "";
                  }
                  String newPattern = getPatternFromText(selectedText);
                  if (!existingIncludePattern.contains(newPattern))
                  {
                    String newIncludePatternStr = existingIncludePattern + newPattern;
                    lastEnteredIncludeFilterPattern = Pattern.compile(newIncludePatternStr);
                    if (enableFilter.getSelection())
                    {
                      applyPatterns(lastEnteredIncludeFilterPattern.pattern(), 
                                    lastEnteredExcludeFilterPattern.pattern(), false);
                    }
                  }
                }
              });
              
              MenuItem excludeItem = new MenuItem(textMenu, SWT.PUSH);
              excludeItem.setText("Exclude: " + getDisplayPatternFromText(selectedText));
              excludeItem.addSelectionListener(new SelectionAdapter()
              {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                  String existingExcludePattern = lastEnteredExcludeFilterPattern.pattern() + "|";
                  if (lastEnteredExcludeFilterPattern == TraceFilterThread.MATCH_NONE)
                  {
                    existingExcludePattern = "";
                  }
                  String newPattern = getPatternFromText(selectedText);
                  if (!existingExcludePattern.contains(newPattern))
                  {
                    String newExcludePatternStr = existingExcludePattern + newPattern;
                    lastEnteredExcludeFilterPattern = Pattern.compile(newExcludePatternStr);
                    if (enableFilter.getSelection())
                    {
                      applyPatterns(lastEnteredIncludeFilterPattern.pattern(), 
                                    lastEnteredExcludeFilterPattern.pattern(), false);
                    }
                  }
                }
              });
              
              textOutput.setMenu(textMenu);
            }
            else
            {
              textOutput.setMenu(null);
            }
          }
        }
      });
      
      MigLayout barLayout = new MigLayout("fill,wmin 0,hmin 0",
          "[][grow][][200][][]", "[]");

      final Composite compositeBar = new Composite(composite, SWT.NONE);
      compositeBar.setLayout(barLayout);
      compositeBar.setLayoutData("spanx,growx");
      
      statusLabel = new Label(compositeBar, SWT.NONE);
      setOutputStatus(0, 0);
      statusLabel.setLayoutData("growx,skip");
      
      Label findLabel = new Label(compositeBar, SWT.NONE);
      findLabel.setText("Find:");
      
      findInput = new Text(compositeBar, SWT.BORDER);
      findInput.setLayoutData("growx");
      
      downButton = new Button(compositeBar, SWT.PUSH);
      downButton.setText("Down");
      downButton.setAlignment(SWT.CENTER);
      
      upButton = new Button(compositeBar, SWT.PUSH);
      upButton.setText("Up");
      upButton.setAlignment(SWT.CENTER);
      
      SelectionListener findListen = new org.eclipse.swt.events.SelectionAdapter()
      {
        @Override
        public void widgetSelected(SelectionEvent selectionevent)
        {
          doFind(true);
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent arg0)
        {
          doFind(true);
        }
      };
      
      downButton.addSelectionListener(findListen);
      findInput.addSelectionListener(findListen);
      
      upButton.addSelectionListener(new SelectionAdapter()
      {
        @Override
        public void widgetSelected(SelectionEvent e)
        {
          doFind(false);
        }
      });
      
      clearText
          .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
          {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
              filterThread.setClearTrace();
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
      
      saveText.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
      {
        @Override
        public void widgetSelected(SelectionEvent arg0)
        {
          FileDialog dialog = new FileDialog(sWindow, SWT.SAVE);
          dialog.setFilterNames(new String[] { "Text Files", "All Files (*.*)" });
          dialog.setFilterExtensions(new String[] { "*.txt", "*.*" });
          dialog.setFileName("output.txt");
          String fileName = dialog.open();
          if (fileName != null)
          {
            try
            {
              Writer out = new OutputStreamWriter(new FileOutputStream(new File(fileName)));
              out.append(textOutput.getText());
              out.flush();
              out.close();
              
              MessageBox messageBox = new MessageBox(sWindow, SWT.ICON_INFORMATION | SWT.OK);              
              messageBox.setText("Save Complete");
              messageBox.setMessage("Output Saved");
              messageBox.open();
            }
            catch (IOException e)
            {
              MessageBox messageBox = new MessageBox(sWindow, SWT.ICON_INFORMATION | SWT.OK);              
              messageBox.setText("Save Error");
              messageBox.setMessage("Error: " + e.toString());
              messageBox.open();
            }
          }
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
            lastEnteredIncludeFilterPattern = getPattern(includePattern);
            lastEnteredExcludeFilterPattern = getPattern(excludePattern);
            if (enableFilter.getSelection())
            {
              applyPatterns(includePattern, excludePattern, false);
            }
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
                  patternCallback, lastEnteredIncludeFilterPattern.pattern(),
                  lastEnteredExcludeFilterPattern.pattern());
              placeDialogInCenter(sWindow.getBounds(), regexInput.sWindow);
            }
          });
      
      enableFilter
      .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
      {
        @Override
        public void widgetSelected(SelectionEvent arg0)
        {
          if (enableFilter.getSelection())
          {
            applyPatterns(lastEnteredIncludeFilterPattern.pattern(), 
                          lastEnteredExcludeFilterPattern.pattern(), true);
          }
          else
          {
            applyPatterns(TraceFilterThread.MATCH_ALL.pattern(), 
                          TraceFilterThread.MATCH_NONE.pattern(), true);
          }
        }
      });
      
      filterThread = new TraceFilterThread(mode, 
                                           new TraceTextHandler()
      {
        // Re-usable class to avoid object allocations
        class SetTextRunnable implements Runnable
        {
          String traceText = null;
          
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
        }        
        SetTextRunnable mSetTextRunnable = new SetTextRunnable();
        
        @Override
        public void setText(String traceText)
        {
          if (sRoot.isDisposed())
            return;
          
          mSetTextRunnable.traceText = traceText;
          sWindow.getDisplay().syncExec(mSetTextRunnable);
          mSetTextRunnable.traceText = null;
        }

        // Re-usable class to avoid object allocations
        class AppendTextRunnable implements Runnable
        {
          String traceText = null;
          
          @Override
          public void run()
          {
            textOutput.append(traceText);
            if (autoScroll)
            {
              textOutput.setTopIndex(Integer.MAX_VALUE);
            }           
          }          
        }        
        AppendTextRunnable mAppendTextRunnable = new AppendTextRunnable();
        
        @Override
        public void appendText(final String traceText)
        {
          if (sRoot.isDisposed())
            return;

          mAppendTextRunnable.traceText = traceText;
          sWindow.getDisplay().syncExec(mAppendTextRunnable);
          mAppendTextRunnable.traceText = null;
        }

        @Override
        public void setStatus(int displayed, int total)
        {          
          setOutputStatus(displayed, total);
        }
      });
      
      if (mode == UIMode.STANDALONE)
      {
        filterThread.addSystemTraceLine("Instructions");
        filterThread.addSystemTraceLine("(1) Connect to Agent");
        filterThread.addSystemTraceLine("(2) Select Classes you want to Trace");
        filterThread.addSystemTraceLine("Full help available on the Help tab");
        filterThread.addSystemTraceLine("");
      }      
    }
    
    private String getDisplayPatternFromText(String text)
    {
      String pattern = "*" + text + "*";;
      return pattern;
    }
    
    private String getPatternFromText(String text)
    {
      String pattern = ".*" + text + ".*";;
      pattern = pattern.replace("[", "\\[");
      pattern = pattern.replace("]", "\\]");
      return pattern;
    }
    
    private void doFind(final boolean down)
    {
      if (!sRoot.isDisposed())
      {
        sWindow.getDisplay().asyncExec(new Runnable()
        {
          @Override
          public void run()
          {
            String searchText = findInput.getText();
            if ((searchText != null) &&
                (searchText.length() > 0) &&
                (textOutput.getCharCount() > 0))
            {
              String fullText = textOutput.getText();
              int startIndex = textOutput.getCaretOffset();
              int nextIndex;
              if (down)
              {
                nextIndex = fullText.indexOf(searchText, startIndex);
              }
              else
              {
                nextIndex = fullText.lastIndexOf(searchText, startIndex - searchText.length());
                Point selection = textOutput.getSelection();
                if ((nextIndex == selection.x) || (nextIndex == selection.y))
                {
                  nextIndex = fullText.lastIndexOf(searchText, startIndex - searchText.length() - 1);  
                }                
              }
              if (nextIndex > -1)
              {
                textOutput.setSelectionRange(nextIndex, searchText.length());
                int lineIndex = textOutput.getLineAtOffset(nextIndex);
                textOutput.setTopIndex(lineIndex);
              }
            }
          }
        });
      }
    }
    
    // Re-usable class to avoid object allocations
    class SetOutputStatusRunnable implements Runnable
    {
      int displayed;
      int total;
      
      @Override
      public void run()
      {
        statusLabel.setText("Displayed lines: " + displayed + ", Total lines: " + total);            
      }          
    }       
    SetOutputStatusRunnable mSetOutputStatusRunnable = new SetOutputStatusRunnable();
    
    public void setOutputStatus(final int displayed, final int total)
    {
      if (!sRoot.isDisposed())
      {
        mSetOutputStatusRunnable.displayed = displayed;
        mSetOutputStatusRunnable.total = total;
        sWindow.getDisplay().syncExec(mSetOutputStatusRunnable);
      }    
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
                               String newExcludePattern,
                               final boolean isToggle)
    {
      if (newIncludePattern.equals(activeIncludeFilterPattern.pattern())
          && newExcludePattern.equals(activeExcludeFilterPattern.pattern()))
      {
        return;
      } else
      {
        oldIncludeFilterPattern = activeIncludeFilterPattern;
        oldExcludeFilterPattern = activeExcludeFilterPattern;

        activeIncludeFilterPattern = getPattern(newIncludePattern);
        activeExcludeFilterPattern = getPattern(newExcludePattern);
      }
      if (sRoot.isDisposed())
        return;
      sWindow.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          enableFilter.setEnabled(false);
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

          final Pattern newIncludePattern = activeIncludeFilterPattern;
          final Pattern newExcludePattern = activeExcludeFilterPattern;

          final TraceFilterProgressHandler progressHandler = new TraceFilterProgressHandler()
          {
            @Override
            public boolean setProgress(final int percent)
            {
              if (sRoot.isDisposed())
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
                    enableFilter.setEnabled(true);
                    if (filterCancelled[0])
                    {
                      activeIncludeFilterPattern = oldIncludeFilterPattern;
                      activeExcludeFilterPattern = oldExcludeFilterPattern;
                      if (isToggle)
                      {
                        enableFilter.setSelection(!enableFilter.getSelection());
                      }
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
  
  private class HelpOutputTab
  {
    final StyledText textOutput;
    final Composite composite;
    final Link helpLink;

    private HelpOutputTab(Composite parent)
    {
      MigLayout windowLayout = new MigLayout("fill,wmin 0,hmin 0",
          "[grow]", "[20][grow]");

      composite = new Composite(parent, SWT.NONE);
      composite.setLayout(windowLayout);
      
      helpLink = new Link(composite, SWT.NONE);
      helpLink.setLayoutData("growx,wrap");
      helpLink.setText("<A HREF=\"https://github.com/mchr3k/org.intrace/wiki\">Online Help</A>");
      
      textOutput = new StyledText(composite, SWT.MULTI | SWT.V_SCROLL
          | SWT.H_SCROLL | SWT.BORDER);
      textOutput.setEditable(false);
      textOutput.setLayoutData("spanx,grow,wmin 0,hmin 0");
      textOutput.setBackground(Display.getCurrent().getSystemColor(
          SWT.COLOR_WHITE));
      
      helpLink.addSelectionListener(new SelectionAdapter()
      {
        @Override
        public void widgetSelected(SelectionEvent event)
        {
          Program.launch(event.text);
        }
      });
      
      StringBuilder helpStr = new StringBuilder();
      helpStr.append("Here is some example trace:\n");
      helpStr.append("[14:14:30]:[1]:example.ExampleClass:multiplyMethod: {:100\n");      
      helpStr.append("[14:14:30]:[1]:example.ExampleClass:multiplyMethod: Arg: 2\n");
      helpStr.append("[14:14:30]:[1]:example.ExampleClass:multiplyMethod: Arg: 4\n");
      helpStr.append("[14:14:30]:[1]:example.ExampleClass:multiplyMethod: /:101\n");
      helpStr.append("[14:14:30]:[1]:example.ExampleClass:multiplyMethod: Return: 8\n");
      helpStr.append("[14:14:30]:[1]:example.ExampleClass:multiplyMethod: }:105\n");
      helpStr.append("\n");
      helpStr.append("This means the following:\n");
      helpStr.append("[14:14:30] is a timestamp");
      helpStr.append("[1] - the thread ID\n");
      helpStr.append("example.ExampleClass:multiplyMethod - the Class and Method being traced\n");
      helpStr.append("{:100 - The method was entered on source line 100\n");
      helpStr.append("Arg: 2 - The first argument had value 2\n");
      helpStr.append("Arg: 2 - The second argument had value 4\n");
      helpStr.append("/:101 - An optional block of code was executed starting at source line 101 (e.g. an if statement)\n");
      helpStr.append("Return: 8 - The method returned value 8\n");
      helpStr.append("}:105 - The method returned on source line 105\n");
      textOutput.setText(helpStr.toString());
    }
  }

  // Window ref
  private final InTraceUI thisWindow = this;

  private ConnectState connectionState = ConnectState.DISCONNECTED;

  public void setConnectionState(ConnectState connectionState)
  {
    this.connectionState = connectionState;
    IConnectionStateCallback callback = connCallback;
    if (callback != null)
    {
      callback.setConnectionState(connectionState);
    }
  }

  // Network details
  private InetAddress remoteAddress;

  // Threads
  private NetworkDataReceiverThread networkTraceThread;
  private ControlConnectionThread controlThread;

  // Settings
  private ParsedSettingsData settingsData = new ParsedSettingsData(
      new HashMap<String, String>());
  
  private Pattern lastEnteredIncludeFilterPattern = TraceFilterThread.MATCH_NONE;  
  private Pattern activeIncludeFilterPattern = TraceFilterThread.MATCH_NONE;
  private Pattern oldIncludeFilterPattern = TraceFilterThread.MATCH_NONE;
  
  private Pattern lastEnteredExcludeFilterPattern = TraceFilterThread.MATCH_NONE;
  private Pattern activeExcludeFilterPattern = TraceFilterThread.MATCH_NONE;
  private Pattern oldExcludeFilterPattern = TraceFilterThread.MATCH_NONE;
  
  private boolean autoScroll = true;
  private boolean fixedConnection = false;

  public void setSocket(Socket socket)
  {
    if (socket != null)
    {
      setConnectionStatus("Connected");
      remoteAddress = socket.getInetAddress();
      controlThread = new ControlConnectionThread(socket, this);
      controlThread.start();
      controlThread.sendMessage("getsettings");
      setConnectionState(ConnectState.CONNECTED);

      controlThread.sendMessage("[out-network");
      String networkTracePortStr = controlThread.getMessage();
      int networkTracePort = Integer.parseInt(networkTracePortStr);
      try
      {
        INetworkOutputConfig config = new INetworkOutputConfig()
        {          
          @Override
          public boolean isNetOutputEnabled()
          {
            return settingsData.netOutEnabled;
          }
        };
        networkTraceThread = new NetworkDataReceiverThread(remoteAddress,
            networkTracePort, config, textOutputTab.filterThread);
        networkTraceThread.start();
      } catch (IOException ex)
      {
        textOutputTab.filterThread
            .addSystemTraceLine("Failed to setup network trace: "
                + ex.toString());
      }
    } else
    {
      setConnectionState(ConnectState.DISCONNECTED_ERR);
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
    setConnectionState(ConnectState.DISCONNECTED);
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
    if (!sRoot.isDisposed())
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
    if (!sRoot.isDisposed())
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
    if (!sRoot.isDisposed())
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
    if (fixedConnection)
    {
      connTab.portInput.setText(Integer.toString(settingsData.actualServerPort));
    }
    
    if (settingsData.waitStart)
    {
      addWaitStartUI();
    }
    else
    {
      removeWaitStartUI();
    }
    
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

      if (connectionState == ConnectState.CONNECTED)
      {
        // Disable connection details
        connTab.addressInput.setEnabled(false);
        connTab.portInput.setEnabled(false);
        
        // Enable all buttons
        instruTab.classRegex.setEnabled(true);
        instruTab.listClasses.setEnabled(true);

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
        setConnectionStatus("Disconnected");
      }

      // Always reset button states
      if ((connectionState == ConnectState.DISCONNECTED)
          || (connectionState == ConnectState.DISCONNECTED_ERR))
      {
        if (!fixedConnection)
        {
          connTab.addressInput.setEnabled(true);
          connTab.portInput.setEnabled(true);
        }

        instruTab.classRegex.setEnabled(false);
        instruTab.listClasses.setEnabled(false);

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

  public void setConnectionStatus(final String statusText)
  {
    if (!sRoot.isDisposed())
    {
      sWindow.getDisplay().syncExec(new Runnable()
      {
        @Override
        public void run()
        {
          connTab.statusLabel.setText(statusText);
        }
      });
    }
  }
  
  public void setProgress(final Map<String, String> progressMap)
  {
    if (!sRoot.isDisposed())
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
    if (!sRoot.isDisposed())
    {
      sWindow.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          int numInstr = Integer.parseInt(statusMap
              .get(AgentConfigConstants.STINST));
          int numTotal = Integer.parseInt(statusMap
              .get(AgentConfigConstants.STCLS));
          instruTab.setStatus(numInstr, numTotal);
        }
      });
    }
  }

  public void setConfig(final Map<String, String> settingsMap)
  {
    if (!sRoot.isDisposed())
    {
      sWindow.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          textOutputTab.filterThread
              .addSystemTraceLine("Latest Settings Received");
          settingsData = new ParsedSettingsData(settingsMap);
          setConnectionState(ConnectState.CONNECTED);
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
  
  public void dispose()
  {
    disconnect();
    textOutputTab.filterThread.interrupt();
  }
}
