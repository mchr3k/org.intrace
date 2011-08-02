package org.intrace.client.gui.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
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
import org.intrace.client.gui.helper.IncludeExcludeWindow.PatternInputCallback;
import org.intrace.client.gui.helper.NetworkDataReceiverThread.INetworkOutputConfig;
import org.intrace.client.gui.helper.TraceFilterThread.TraceFilterProgressHandler;
import org.intrace.client.gui.helper.TraceFilterThread.TraceTextHandler;
import org.intrace.shared.AgentConfigConstants;
import org.intrace.shared.TraceConfigConstants;

public class InTraceUI implements ISocketCallback, IControlConnectionListener
{
  public static interface IConnectionStateCallback
  {
    public void setConnectionState(ConnectState state);
  }
  
  public static String NEWLINE = System.getProperty("line.separator");
  
  private static final Pattern TRACE_LINE = Pattern.compile("^\\[[^\\]]+]:(\\[[^\\]]+\\]:([^:]+:[^:]+)):.*");
  
  private static final Pattern ALLOW_ALL = Pattern.compile(".*");
  private static final Pattern ALLOW_CLASSES = Pattern.compile("^[0-9a-zA-Z\\.\\$]*|\\*$");

  public void open()
  {
//    new Thread()
//    {
//      public void run() 
//      {
//        try
//        {
//          Thread.sleep(500);
//        }
//        catch (InterruptedException e)
//        {
//          // Ignore
//        }
//        sWindow.getDisplay().syncExec(new Runnable()
//        {
//          @Override
//          public void run()
//          {
//            startProgramBar.show();
//          }
//        });
//      };
//    }.start();
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
  private final UIMode mode;  
  private final UIModeData modeData;

  private IConnectionStateCallback connCallback = null;
  private MigLayout rootLayout;
  
  // UI elements
  final private MainBar mainBar;
  final private ConnectionBar connBar;
  final private SettingsTabs settingsTabs;
  final private StartProgramBar startProgramBar;
  final private OutputTabs outputTabs;

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
  
  public static class UIModeData
  {
    public UIModeData(Color colorOne, Color colorTwo)
    {
      this.colorOne = colorOne;
      this.colorTwo = colorTwo;
    }
    public final Color colorOne;
    public final Color colorTwo;
  }
  
  public InTraceUI(Shell xiWindow, Composite xiRoot, UIMode xiMode, UIModeData xiModeData)
  {
    sWindow = xiWindow;
    sRoot = xiRoot;
    mode = xiMode;
    modeData = xiModeData;
    
    rootLayout = new MigLayout("fill,hidemode 2", "[]", "0[]0[]0[]0[]0[grow]");
    xiRoot.setLayout(rootLayout);
    
    mainBar = new MainBar(xiRoot);
    mainBar.composite.setLayoutData("grow,wrap,pad 0");
    
    connBar = new ConnectionBar(xiRoot);
    connBar.composite.setLayoutData("grow,wrap,pad 0");
    
    settingsTabs = new SettingsTabs(xiRoot);
    settingsTabs.composite.setLayoutData("grow,wrap,pad 0");
    settingsTabs.hide();
    
    if (mode == UIMode.STANDALONE)
    {
      connBar.show();
    }
    else
    {
      connBar.hide();
    }
    
    startProgramBar = new StartProgramBar(xiRoot);
    startProgramBar.composite.setLayoutData("grow,wrap,pad 0");
    startProgramBar.hide();
     
    outputTabs = new OutputTabs(xiRoot);
    outputTabs.composite.setLayoutData("grow,wmin 0,hmin 0");
    
    updateUIStateSameThread();
        
    sWindow.getDisplay().addFilter(SWT.KeyDown, new Listener()
    {
      @Override
      public void handleEvent(Event e)
      {
        if (!sRoot.isDisposed() &&
            (sWindow.getDisplay().getActiveShell() == sWindow))
        {
          // This is the active window
          
          if ((outputTabs.textOutputTab != null) &&
              (outputTabs.textOutputTab.textOutput != null) &&
              (!outputTabs.textOutputTab.textOutput.isDisposed()) &&
              outputTabs.textOutputTab.textOutput.isFocusControl())
          {
            // Textoutput is focused
            
            if (((e.stateMask & SWT.CTRL) != 0) &&
                (e.keyCode == 'f'))
            {
              outputTabs.textOutputTab.findInput.setFocus();
            }
            else if (((e.stateMask & SWT.CTRL) != 0) &&
                      (e.keyCode == SWT.ARROW_UP))
            {
              outputTabs.textOutputTab.findEntry();
            }
            else if (((e.stateMask & SWT.CTRL) != 0) &&
                      (e.keyCode == SWT.ARROW_DOWN))
            {
              outputTabs.textOutputTab.findExit();
            }
          }
        }
        else if (sRoot.isDisposed())
        {
          // Remove this listener
          sWindow.getDisplay().removeFilter(SWT.KeyDown, this);
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
          connBar.addressInput.setText("localhost");
          connBar.portInput.setText(xiPort);
          outputTabs.textOutputTab.filterThread.addSystemTraceLine("Instructions");
          outputTabs.textOutputTab.filterThread.addSystemTraceLine(" - Select Classes you want to Trace");
          outputTabs.textOutputTab.filterThread.addSystemTraceLine("Full help available on the Help tab");
          outputTabs.textOutputTab.filterThread.addSystemTraceLine("");
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
          connBar.addressInput.setText("localhost");
          connBar.portInput.setText("detecting...");
          outputTabs.textOutputTab.filterThread.addSystemTraceLine("Instructions");
          outputTabs.textOutputTab.filterThread.addSystemTraceLine(" - Select Classes you want to Trace");
          outputTabs.textOutputTab.filterThread.addSystemTraceLine("Full help available on the Help tab");
          outputTabs.textOutputTab.filterThread.addSystemTraceLine("");
        }
      });
    }
    setSocket(xiSocket);
  }

  private class MainBar
  {
    private final Label mainStatusLabel;
    private final Button classesButton;
    private Composite composite;

    private static final String UP = "\u25B2";
    private static final String DOWN = "\u25BC";
    private static final String RIGHT = "\u25BA";
    
    private static final String TAHOMA_UP = "\u06F8";
    private static final String TAHOMA_DOWN = "\u06F7";
    private static final String TAHOMA_RIGHT = "\u003E";
    
    private final boolean tahomaUIFont;
    private final Button connectButton;
    private final Button settingsButton;
    
    private MainBar(Composite parent)
    {
      composite = new Composite(parent, SWT.NONE);          
      MigLayout barLayout;
      if (mode == UIMode.STANDALONE)
      {
        barLayout = new MigLayout("fill", "0[100][50,center][100][15,center][grow,left][15,center][100]0", "4[]2");
      }
      else
      {
        barLayout = new MigLayout("fill", "0[100][30,center][grow,left][30,center][100]0", "4[]0");
      }
      composite.setLayout(barLayout);
      
      tahomaUIFont = composite.getFont().getFontData()[0].getName().equalsIgnoreCase("tahoma");
      
      if (mode == UIMode.STANDALONE)
      {
        connectButton = new Button(composite, SWT.PUSH);
        if (tahomaUIFont)
        {
          connectButton.setText(TAHOMA_DOWN + " Connection");
        }
        else
        {
          connectButton.setText(DOWN + " Connection");
        }
        connectButton.setLayoutData("growx");
        
        Label arrowLabel = new Label(composite, SWT.NONE);
        
        if (tahomaUIFont)
        {
          arrowLabel.setText(TAHOMA_RIGHT);
        }
        else
        {
          arrowLabel.setText(RIGHT);
        }
        
        FontData[] defFontData = arrowLabel.getFont().getFontData();
        Font newFont = new Font(Display.getCurrent(), 
            defFontData[0].getName(), 
            14,
            SWT.NORMAL);
        arrowLabel.setFont(newFont);
        
        connectButton.addSelectionListener(new SelectionAdapter()
        {        
          @Override
          public void widgetSelected(SelectionEvent arg0)
          {
            if (connBar.composite.isVisible())
            {
              connBar.hide();              
            }
            else
            {
              connBar.show();
            }
          }
        });
      }
      else
      {
        connectButton = null;
      }
      
      classesButton = new Button(composite, SWT.PUSH);
      classesButton.setText("Classes...");
      classesButton.setLayoutData("growx");
      
      Label barLabel1 = new Label(composite, SWT.NONE);
      barLabel1.setText("|");
      
      mainStatusLabel = new Label(composite, SWT.NONE);
      mainStatusLabel.setText("Status: None");
      mainStatusLabel.setLayoutData("growx");
      
      Label barLabel2 = new Label(composite, SWT.NONE);
      barLabel2.setText("|");
      
      settingsButton = new Button(composite, SWT.PUSH);
      if (tahomaUIFont)
      {
        settingsButton.setText(TAHOMA_DOWN + " Settings");
      }
      else
      {
        settingsButton.setText(DOWN + " Settings");
      }
      settingsButton.setLayoutData("growx");
      
      settingsButton.addSelectionListener(new SelectionAdapter()
      {        
        @Override
        public void widgetSelected(SelectionEvent arg0)
        {
          if (settingsTabs.composite.isVisible())
          {
            settingsTabs.hide();
          }
          else
          {
            settingsTabs.show();            
          }
        }
      });
      
      final String helpText = "Enter complete or partial class names.\n\n "
        + "e.g.\n"
        + "\"mypack.mysubpack.MyClass\"\n"
        + "\"MyClass\"";
      classesButton
      .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
      {
        @Override
        public void widgetSelected(SelectionEvent arg0)
        {
          IncludeExcludeWindow regexInput = new IncludeExcludeWindow(
              "Classes to Instrument", helpText, mode,
              modeData,
              new PatternInputCallback()
              {
                private List<String> includePattern = null;
                private List<String> excludePattern = null;

                @Override
                public void setIncludePattern(List<String> newIncludePattern)
                {
                  includePattern = newIncludePattern;
                  savePatterns();
                }

                @Override
                public void setExcludePattern(List<String> newExcludePattern)
                {
                  excludePattern = newExcludePattern;
                  savePatterns();
                }

                private void savePatterns()
                {
                  if ((includePattern != null) && (excludePattern != null))
                  {
                    setRegex(getStringFromList(includePattern), 
                             getStringFromList(excludePattern));
                  }
                }
              }, 
              getListFromString(settingsData.classRegex), 
              getListFromString(settingsData.classExcludeRegex),
              ALLOW_CLASSES);
          placeDialogInCenter(sWindow.getBounds(), regexInput.sWindow);
        }
      });
    }
        
    private List<String> getListFromString(String pattern)
    {
      List<String> items = new ArrayList<String>();
      String[] patternParts = pattern.split("\\|");
      for (String part : patternParts)
      {
        items.add(part);
      }
      return items;
    }
    
    private String getStringFromList(List<String> list)
    {
      StringBuilder str = new StringBuilder();
      
      for (int ii = 0; ii < list.size(); ii++)
      {
        String item = list.get(ii);
        str.append(item);
        if (ii < (list.size() - 1))
        {
          str.append("|");
        }
      }
      
      return str.toString();
    }

    private void setStatus(int instruClasses, int totalClasses)
    {
      if (!sRoot.isDisposed())
      {
        mainStatusLabel.setText("Instrumented/Total Classes: " + instruClasses + "/"
            + totalClasses);
      }
    }

    private void setProgress(int progressClasses, int totalClasses, boolean done)
    {
      if (!sRoot.isDisposed())
      {
        mainStatusLabel.setText("Progress: " + progressClasses + "/"
            + totalClasses);
      }
    }
  }
  
  private class ConnectionBar
  {
    final Button connectButton;
    final Text addressInput;
    final Text portInput;
    final Composite composite;

    private ConnectionBar(Composite parent)
    {
      MigLayout windowLayout = new MigLayout("fill", "0[40][200][100][grow]0", "4[]0");

      composite = new Composite(parent, SWT.NONE);
      composite.setLayout(windowLayout);

      Label addressLabel = new Label(composite, SWT.NONE);
      addressLabel.setText(ClientStrings.CONN_ADDRESS);
      addressLabel.setLayoutData("gapx 5px,right");
      addressInput = new Text(composite, SWT.BORDER);
      addressInput.setText("localhost");
      addressInput.setLayoutData("growx");

      connectButton = new Button(composite, SWT.LEFT);
      connectButton.setText(ClientStrings.CONNECT);
      connectButton.setAlignment(SWT.CENTER);
      connectButton.setLayoutData("gapx 5px,spany,grow,wrap");

      Label portLabel = new Label(composite, SWT.NONE);
      portLabel.setText(ClientStrings.CONN_PORT);
      portLabel.setLayoutData("gapx 5px,right");
      portInput = new Text(composite, SWT.BORDER);
      portInput.setText("9123");
      portInput.setLayoutData("growx");

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
    
    private void show()
    {
      if (mainBar.connectButton != null)
      {
        if (mainBar.tahomaUIFont)
        {
          mainBar.connectButton.setText(MainBar.TAHOMA_UP + " Connection");
        }
        else
        {
          mainBar.connectButton.setText(MainBar.UP + " Connection");
        }
      }
      settingsTabs.hide();
      composite.setVisible(true);   
      sRoot.layout(true, true);
    }
    
    private void hide()
    {
      if (mainBar.connectButton != null)
      {
        if (mainBar.tahomaUIFont)
        {
          mainBar.connectButton.setText(MainBar.TAHOMA_DOWN + " Connection");
        }
        else
        {
          mainBar.connectButton.setText(MainBar.DOWN + " Connection");
        }
      }
      composite.setVisible(false);
      sRoot.layout(true, true);
    }
  }
  
  private class StartProgramBar
  {
    private final Composite composite;
    private final Button startButton;
    
    private StartProgramBar(Composite parent)
    {
      composite = new Composite(parent, SWT.NONE);
      MigLayout startLayout = new MigLayout("fillx", "[align center]", "4[]0");
      composite.setLayout(startLayout);
      
      Label startLabel = new Label(composite, SWT.NONE);
      startLabel.setText("Program has been paused.");
      startLabel.setLayoutData("split");
      
      startButton = new Button(composite, SWT.PUSH);
      startButton.setText("Start Program");
      
      startButton.addSelectionListener(new SelectionAdapter()
      {
        @Override
        public void widgetSelected(SelectionEvent arg0)
        {
          if (controlThread != null)
          {
            controlThread.sendMessage(AgentConfigConstants.START_ACTIVATE);
            controlThread.sendMessage("getsettings");
            startButton.setEnabled(false);
          }
          else
          {
            startProgramBar.hide();
          }
        }
      });
    }
    
    private void show()
    {
      startButton.setEnabled(true);
      composite.setVisible(true);   
      sRoot.layout(true, true);
    }
    
    private void hide()
    {
      composite.setVisible(false);
      sRoot.layout(true, true);
    }
  }

  private class SettingsTabs
  {    
    final private TabFolder mSettingsTabs;
    final private CTabFolder mSettingsCTabs;
    final private Composite composite;
    final private TraceTab traceTab;
    final private ExtrasTab extraTab;
    final private AgentOutputSettingsTab agentOutputSettingsTab;
    final private LocalOutputSettingsTab localOutputSettingsTab;
    
    private SettingsTabs(Composite parent)
    {
      MigLayout windowLayout = new MigLayout("fill", "0[grow]0", "4[]0");

      composite = new Composite(parent, SWT.NONE);
      composite.setLayout(windowLayout);

      if (mode == UIMode.STANDALONE)
      {
        mSettingsCTabs = null;           
        mSettingsTabs = new TabFolder(composite, SWT.NONE);
        mSettingsTabs.setLayoutData("grow,wrap,wmin 0");
        
        TabItem traceTabItem = new TabItem(mSettingsTabs, SWT.NONE);
        traceTabItem.setText("Trace");
        traceTab = new TraceTab(mSettingsTabs);
        traceTabItem.setControl(traceTab.composite);

        TabItem agentOutputSettingsTabItem = new TabItem(mSettingsTabs, SWT.NONE);
        agentOutputSettingsTabItem.setText("Agent Output");
        agentOutputSettingsTab = new AgentOutputSettingsTab(mSettingsTabs);
        agentOutputSettingsTabItem.setControl(agentOutputSettingsTab.composite);
        
        TabItem localOutputSettingsTabItem = new TabItem(mSettingsTabs, SWT.NONE);
        localOutputSettingsTabItem.setText("Local Output");
        localOutputSettingsTab = new LocalOutputSettingsTab(mSettingsTabs);
        localOutputSettingsTabItem.setControl(localOutputSettingsTab.composite);
        
        TabItem extraTabItem = new TabItem(mSettingsTabs, SWT.NONE);
        extraTabItem.setText("Advanced");
        extraTab = new ExtrasTab(mSettingsTabs);
        extraTabItem.setControl(extraTab.composite);
      }
      else
      {
        mSettingsTabs = null;      
        mSettingsCTabs = new CTabFolder(composite, SWT.TOP | SWT.BORDER);
        mSettingsCTabs.setSimple(false);
        mSettingsCTabs.setLayoutData("grow,wrap,wmin 0");
        if (modeData != null)
        {
          mSettingsCTabs.setSelectionBackground(
              new Color[]{modeData.colorOne, 
                          modeData.colorTwo}, 
                          new int[]{100}, true);
        }
        
        CTabItem traceTabItem = new CTabItem(mSettingsCTabs, SWT.NONE);
        traceTabItem.setText("Trace");
        traceTab = new TraceTab(mSettingsCTabs);
        traceTabItem.setControl(traceTab.composite);
        
        CTabItem agentOutputSettingsTabItem = new CTabItem(mSettingsCTabs, SWT.NONE);
        agentOutputSettingsTabItem.setText("Agent Output");
        agentOutputSettingsTab = new AgentOutputSettingsTab(mSettingsCTabs);
        agentOutputSettingsTabItem.setControl(agentOutputSettingsTab.composite);

        CTabItem localOutputSettingsTabItem = new CTabItem(mSettingsCTabs, SWT.NONE);
        localOutputSettingsTabItem.setText("Local Output");
        localOutputSettingsTab = new LocalOutputSettingsTab(mSettingsCTabs);
        localOutputSettingsTabItem.setControl(localOutputSettingsTab.composite);
        
        CTabItem extraTabItem = new CTabItem(mSettingsCTabs, SWT.NONE);
        extraTabItem.setText("Advanced");
        extraTab = new ExtrasTab(mSettingsCTabs);
        extraTabItem.setControl(extraTab.composite);
        
        mSettingsCTabs.setSelection(0);
      }
    }
    
    private void show()
    {
      if (mainBar.tahomaUIFont)
      {
        mainBar.settingsButton.setText(MainBar.TAHOMA_UP + " Settings");
      }
      else
      {
        mainBar.settingsButton.setText(MainBar.UP + " Settings");
      }
      connBar.hide();
      composite.setVisible(true);   
      sRoot.layout(true, true);
    }
    
    private void hide()
    {
      if (mainBar.tahomaUIFont)
      {
        mainBar.settingsButton.setText(MainBar.TAHOMA_DOWN + " Settings");
      }
      else
      {
        mainBar.settingsButton.setText(MainBar.DOWN + " Settings");
      }
      composite.setVisible(false);
      sRoot.layout(true, true);
    }

    private class TraceTab
    {
      final Button entryExitTrace;
      final Button branchTrace;
      final Button argsTrace;
      final Button arrayTrace;
    
      final Composite composite;      
    
      private TraceTab(Composite parent)
      {
        MigLayout windowLayout = new MigLayout("fill", "[80][80][250][100][grow]");
    
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(windowLayout);
    
        entryExitTrace = new Button(composite, SWT.CHECK);
        entryExitTrace.setText(ClientStrings.ENABLE_EE_TRACE);
        entryExitTrace.setAlignment(SWT.CENTER);
    
        branchTrace = new Button(composite, SWT.CHECK);
        branchTrace.setText(ClientStrings.ENABLE_BRANCH_TRACE);
        branchTrace.setAlignment(SWT.CENTER);
    
        argsTrace = new Button(composite, SWT.CHECK);
        argsTrace.setText(ClientStrings.ENABLE_ARGS_TRACE);
        argsTrace.setAlignment(SWT.CENTER);
        
        arrayTrace = new Button(composite, SWT.CHECK);
        arrayTrace.setText(ClientStrings.ENABLE_ARRAY_TRACE);
        arrayTrace.setAlignment(SWT.CENTER);
    
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
        arrayTrace
        .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        {
          @Override
          public void widgetSelected(SelectionEvent arg0)
          {
            toggleSetting(settingsData.truncArraysEnabled, TraceConfigConstants.ARRAYS + "true",
                TraceConfigConstants.ARRAYS + "false");
            settingsData.truncArraysEnabled = !settingsData.truncArraysEnabled;
          }
        });
      }
    }
    
    private class AgentOutputSettingsTab
    {
      final Button stdOutOutput;
      final Button fileOutput;
      final Composite composite;
    
      private AgentOutputSettingsTab(Composite parent)
      {
        MigLayout windowLayout = new MigLayout("fill", "[120][120][grow]");
    
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(windowLayout);
    
        stdOutOutput = new Button(composite, SWT.CHECK);
        stdOutOutput.setText(ClientStrings.ENABLE_STDOUT_OUTPUT);
        stdOutOutput.setAlignment(SWT.CENTER);
    
        fileOutput = new Button(composite, SWT.CHECK);
        fileOutput.setText(ClientStrings.ENABLE_FILE_OUTPUT);
        fileOutput.setAlignment(SWT.CENTER);
        
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
    
    private class LocalOutputSettingsTab
    {
      final Button discardFiltered;
      final Button discardExcess;
      final Composite composite;
    
      private LocalOutputSettingsTab(Composite parent)
      {
        MigLayout windowLayout = new MigLayout("fill", "[150][150][grow]");
    
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(windowLayout);
    
        discardFiltered = new Button(composite, SWT.CHECK);
        discardFiltered.setText(ClientStrings.DISCARD_FILTERED);
        discardFiltered.setAlignment(SWT.CENTER);
        discardFiltered.setSelection(true);
    
        discardExcess = new Button(composite, SWT.CHECK);
        discardExcess.setText(ClientStrings.DISCARD_EXCESS);
        discardExcess.setAlignment(SWT.CENTER);
        discardExcess.setSelection(true);
        
        discardFiltered
            .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
            {
              @Override
              public void widgetSelected(SelectionEvent arg0)
              {
                outputTabs.textOutputTab.applyPatterns(activeIncludeFilterPattern, activeExcludeFilterPattern, false, discardFiltered.getSelection());
              }
            });
        discardExcess
            .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
            {
              @Override
              public void widgetSelected(SelectionEvent arg0)
              {
                outputTabs.textOutputTab.filterThread.setDiscardExcess(
                                                discardExcess.getSelection());
              }
            });
      }
    }

    private class ExtrasTab
    {
      final Button togSaveClasses;
      final Button togVerbose;
      final Button printSettings;
      final Button listClasses;
      final Composite composite;
    
      private ExtrasTab(Composite parent)
      {
        MigLayout windowLayout = new MigLayout("fill", "[120][120][120][160][grow]");
    
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(windowLayout);
    
        togSaveClasses = new Button(composite, SWT.CHECK);
        togSaveClasses.setText(ClientStrings.ENABLE_SAVECLASSES);
        togSaveClasses.setAlignment(SWT.LEFT);
        togSaveClasses.setLayoutData("growx");
        
        togVerbose = new Button(composite, SWT.CHECK);
        togVerbose.setText(ClientStrings.ENABLE_VERBOSEMODE);
        togVerbose.setAlignment(SWT.LEFT);
        togVerbose.setLayoutData("growx");
                
        printSettings = new Button(composite, SWT.PUSH);
        printSettings.setText(ClientStrings.DUMP_SETTINGS);
        printSettings.setAlignment(SWT.CENTER);
        printSettings.setLayoutData("growx");
        
        listClasses = new Button(composite, SWT.PUSH);
        listClasses.setText(ClientStrings.LIST_MODIFIED_CLASSES);
        listClasses.setAlignment(SWT.CENTER);
        listClasses.setLayoutData("growx");
        listClasses
            .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
            {
              @Override
              public void widgetSelected(SelectionEvent arg0)
              {
                getModifiedClasses();
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
        printSettings
            .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
            {
              @Override
              public void widgetSelected(SelectionEvent arg0)
              {
                outputTabs.textOutputTab.filterThread.addSystemTraceLine("Settings:"
                    + settingsData.toString());
              }
            });
      }
    }
  }
  
  private class OutputTabs
  {    
    final private TabFolder mOutputTabs;
    final private CTabFolder mOutputCTabs;
    final private Composite composite;
    final private TextOutputTab textOutputTab;
    final private HelpOutputTab helpOutputTab;
    
    private OutputTabs(Composite parent)
    {
      MigLayout windowLayout = new MigLayout("fill", "0[grow]0", "4[]0");
  
      composite = new Composite(parent, SWT.NONE);
      composite.setLayout(windowLayout);
  
      if (mode == UIMode.STANDALONE)
      {
        mOutputCTabs = null;
        mOutputTabs = new TabFolder(composite, SWT.NONE);
        mOutputTabs.setLayoutData("grow,wmin 0,hmin 0");
        
        TabItem textOutputTabItem = new TabItem(mOutputTabs, SWT.NONE);
        textOutputTabItem.setText("Output");
        textOutputTab = new TextOutputTab(mOutputTabs);
        textOutputTabItem.setControl(textOutputTab.composite);
        
        TabItem helpOutputTabItem = new TabItem(mOutputTabs, SWT.NONE);
        helpOutputTabItem.setText("Help");
        helpOutputTab = new HelpOutputTab(mOutputTabs);
        helpOutputTabItem.setControl(helpOutputTab.composite);
      }
      else
      {
        mOutputTabs = null;
        mOutputCTabs = new CTabFolder(composite, SWT.TOP | SWT.BORDER);
        mOutputCTabs.setSimple(false);
        mOutputCTabs.setLayoutData("grow,wmin 0,hmin 0");
        if (modeData != null)
        {
          mOutputCTabs.setSelectionBackground(
                       new Color[]{modeData.colorOne, 
                                   modeData.colorTwo}, 
                                   new int[]{100}, true);
        }
        
        CTabItem textOutputTabItem = new CTabItem(mOutputCTabs, SWT.NONE);
        textOutputTabItem.setText("Output");
        textOutputTab = new TextOutputTab(mOutputCTabs);
        textOutputTabItem.setControl(textOutputTab.composite);
        
        CTabItem helpOutputTabItem = new CTabItem(mOutputCTabs, SWT.NONE);
        helpOutputTabItem.setText("Help");
        helpOutputTab = new HelpOutputTab(mOutputCTabs);
        helpOutputTabItem.setControl(helpOutputTab.composite);
        
        mOutputCTabs.setSelection(0);
      }      
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
      final Color findDefaultBack;
      final Color findDefaultFore;
      final Font downButtonDefFont;
      final Font downButtonBoldFont;
      final Font upButtonDefFont;
      final Font upButtonBoldFont;
    
      private TextOutputTab(Composite parent)
      {
        MigLayout windowLayout = new MigLayout("fill,wmin 0,hmin 0",
            "[90][90][15,center][90][grow][90][align right,100]", "[][][grow][]");
    
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(windowLayout);
    
        Button saveText = new Button(composite, SWT.PUSH);
        saveText.setText(ClientStrings.SAVE_TEXT);
        saveText.setLayoutData("grow");
        
        Button clearText = new Button(composite, SWT.PUSH);
        clearText.setText(ClientStrings.CLEAR_TEXT);
        clearText.setLayoutData("grow");
        
        Label barLabel1 = new Label(composite, SWT.NONE);
        barLabel1.setText("|");        
        
        textFilter = new Button(composite, SWT.PUSH);
        textFilter.setText(ClientStrings.FILTER_TEXT);
        textFilter.setLayoutData("grow");
    
        pBar = new ProgressBar(composite, SWT.NORMAL);
        pBar.setLayoutData("grow");
        pBar.setVisible(false);
    
        cancelButton = new Button(composite, SWT.PUSH);
        cancelButton.setText(ClientStrings.CANCEL_TEXT);
        cancelButton.setLayoutData("grow");
        cancelButton.setVisible(false);
                
        Button autoScrollBtn = new Button(composite, SWT.CHECK);
        autoScrollBtn.setText(ClientStrings.AUTO_SCROLL);
        autoScrollBtn.setLayoutData("wrap");
        autoScrollBtn.setSelection(autoScroll);
        
        networkOutput = new Button(composite, SWT.CHECK);
        networkOutput.setText(ClientStrings.ENABLE_NETWORK_OUTPUT);
        networkOutput.setLayoutData("grow");
        networkOutput.setSelection(true);
        
        enableFilter = new Button(composite, SWT.CHECK);
        enableFilter.setText(ClientStrings.ENABLE_FILTER);
        enableFilter.setLayoutData("grow,wrap,skip 2");
        enableFilter.setSelection(true);
    
        textOutput = new StyledText(composite, SWT.MULTI | SWT.V_SCROLL
            | SWT.H_SCROLL | SWT.BORDER);
        textOutput.setEditable(false);
        textOutput.setLayoutData("spanx,grow,wmin 0,hmin 0,wrap");
        textOutput.setBackground(Display.getCurrent().getSystemColor(
            SWT.COLOR_WHITE)); 
        textOutput.setKeyBinding(SWT.PAGE_UP, ST.PAGE_UP);
        textOutput.setKeyBinding(SWT.PAGE_DOWN, ST.PAGE_DOWN);
        
        textOutput.addMenuDetectListener(new MenuDetectListener()
        {        
          @Override
          public void menuDetected(MenuDetectEvent e)
          {
            if (e.widget == textOutput)
            {
              Menu textMenu = new Menu(sWindow, SWT.POP_UP);
              
              // Extract current line of text
              String line = getCurrentLine();
              
              // Check if it is a trace line
              Matcher traceLineMatcher = TRACE_LINE.matcher(line);
              if (traceLineMatcher.matches())
              {
                final String matchStr = traceLineMatcher.group(1);
                final String methodStr = traceLineMatcher.group(2);
                
                MenuItem entryItem = new MenuItem(textMenu, SWT.PUSH);
                entryItem.setText("Find method entry: " + methodStr + ": {");
                entryItem.addSelectionListener(new SelectionAdapter()
                {
                  @Override
                  public void widgetSelected(SelectionEvent e)
                  {
                    findInput.setText(matchStr + ": {");
                    doFind(false);
                  }
                });
                
                MenuItem excludeItem = new MenuItem(textMenu, SWT.PUSH);
                excludeItem.setText("Find method exit: " + methodStr + ": }");
                excludeItem.addSelectionListener(new SelectionAdapter()
                {
                  @Override
                  public void widgetSelected(SelectionEvent e)
                  {
                    findInput.setText(matchStr + ": }");
                    doFind(true);
                  }
                });
              }
              
              final String selectedText = textOutput.getSelectionText();
              if ((selectedText.length() > 0) && !selectedText.contains("\n"))
              {
                MenuItem includeItem = new MenuItem(textMenu, SWT.PUSH);
                includeItem.setText("Include: " + selectedText);
                includeItem.addSelectionListener(new SelectionAdapter()
                {
                  @Override
                  public void widgetSelected(SelectionEvent e)
                  {
                    List<String> newIncludePattern = new ArrayList<String>(lastEnteredIncludeFilterPattern);
                    if (lastEnteredIncludeFilterPattern.equals(TraceFilterThread.MATCH_ALL))
                    {
                      newIncludePattern.clear();
                    }
                    String newPattern = selectedText;
                    if (!newIncludePattern.contains(newPattern))
                    {
                      newIncludePattern.add(newPattern);
                      lastEnteredIncludeFilterPattern = newIncludePattern;
                      if (enableFilter.getSelection())
                      {
                        applyPatterns(lastEnteredIncludeFilterPattern, 
                                      lastEnteredExcludeFilterPattern, false,
                                      settingsTabs.localOutputSettingsTab.discardFiltered.getSelection());
                      }
                    }
                  }
                });
                
                MenuItem excludeItem = new MenuItem(textMenu, SWT.PUSH);
                excludeItem.setText("Exclude: " + selectedText);
                excludeItem.addSelectionListener(new SelectionAdapter()
                {
                  @Override
                  public void widgetSelected(SelectionEvent e)
                  {
                    List<String> newExcludePattern = new ArrayList<String>(lastEnteredExcludeFilterPattern);
                    
                    String newPattern = selectedText;
                    if (!newExcludePattern.contains(newPattern))
                    {
                      newExcludePattern.add(newPattern);
                      lastEnteredExcludeFilterPattern = newExcludePattern;
                      if (enableFilter.getSelection())
                      {
                        applyPatterns(lastEnteredIncludeFilterPattern, 
                                      lastEnteredExcludeFilterPattern, false,
                                      settingsTabs.localOutputSettingsTab.discardFiltered.getSelection());
                      }
                    }
                  }
                });
              }
              
              if (textMenu.getItemCount() > 0)
              {
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
            "0[][grow][][200][][]0", "0[]0");
    
        final Composite compositeBar = new Composite(composite, SWT.NONE);
        compositeBar.setLayout(barLayout);
        compositeBar.setLayoutData("spanx,growx,pad 0");
        
        statusLabel = new Label(compositeBar, SWT.NONE);
        setOutputStatus(0, 0);
        statusLabel.setLayoutData("growx,skip");
        
        Label findLabel = new Label(compositeBar, SWT.NONE);
        findLabel.setText("Find:");
        
        findInput = new Text(compositeBar, SWT.BORDER);
        findInput.setLayoutData("growx");
        findDefaultBack = findInput.getBackground();
        findDefaultFore = findInput.getForeground();
        
        downButton = new Button(compositeBar, SWT.PUSH);
        downButton.setText("Down");
        downButton.setAlignment(SWT.CENTER);
        downButtonDefFont = downButton.getFont();
        FontData[] downDefData = downButtonDefFont.getFontData();
        downButtonBoldFont = new Font(Display.getCurrent(), 
                                      downDefData[0].getName(), 
                                      downDefData[0].getHeight(),
                                      SWT.BOLD);
        
        upButton = new Button(compositeBar, SWT.PUSH);
        upButton.setText("Up");
        upButton.setAlignment(SWT.CENTER);
        upButtonDefFont = downButton.getFont();
        FontData[] upDefData = upButtonDefFont.getFontData();
        upButtonBoldFont = new Font(Display.getCurrent(), 
                                    upDefData[0].getName(), 
                                    upDefData[0].getHeight(),
                                    SWT.BOLD);
        
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
        findInput.addModifyListener(new ModifyListener()
        {        
          @Override
          public void modifyText(ModifyEvent arg0)
          {
            if (!composite.isDisposed())
            {
              findInput.setForeground(findDefaultFore);
              findInput.setBackground(findDefaultBack);
              downButton.setFont(downButtonDefFont);
              upButton.setFont(upButtonDefFont);
            }
          }
        });
        
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
                MessageBox messageBox = new MessageBox(sWindow, SWT.ICON_WARNING |SWT.YES | SWT.NO);
                messageBox.setMessage("Clear all output?");
                messageBox.setText("Output");
                int rc = messageBox.open();
                
                if (rc == SWT.YES)
                {                
                  filterThread.setClearTrace();
                }
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
    
        final String helpText = "Enter text to match against trace lines. " +
        		"You can match any part of the line. " +
        		"\n\nYou can also select some text and right click the " +
        		"selection to quickly add an include or exclude filter.\n";
        
        final PatternInputCallback patternCallback = new PatternInputCallback()
        {
          private List<String> includePattern = null;
          private List<String> excludePattern = null;
    
          @Override
          public void setIncludePattern(List<String> newIncludePattern)
          {
            includePattern = newIncludePattern;
            savePatterns();
          }
    
          @Override
          public void setExcludePattern(List<String> newExcludePattern)
          {
            excludePattern = newExcludePattern;
            savePatterns();
          }
    
          private void savePatterns()
          {
            if ((includePattern != null) && (excludePattern != null))
            {
              if (includePattern.equals(TraceFilterThread.MATCH_NONE) &&
                  excludePattern.equals(TraceFilterThread.MATCH_NONE))
              {
                includePattern = TraceFilterThread.MATCH_ALL;
              }
              lastEnteredIncludeFilterPattern = includePattern;
              lastEnteredExcludeFilterPattern = excludePattern;
              if (enableFilter.getSelection())
              {
                applyPatterns(includePattern, excludePattern, false,
                    settingsTabs.localOutputSettingsTab.discardFiltered.getSelection());
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
                IncludeExcludeWindow regexInput;
                regexInput = new IncludeExcludeWindow("Output Filter", helpText, mode,
                    modeData,
                    patternCallback, lastEnteredIncludeFilterPattern,
                    lastEnteredExcludeFilterPattern, ALLOW_ALL);
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
              applyPatterns(lastEnteredIncludeFilterPattern, 
                            lastEnteredExcludeFilterPattern, true,
                            settingsTabs.localOutputSettingsTab.discardFiltered.getSelection());
            }
            else
            {
              applyPatterns(TraceFilterThread.MATCH_ALL, 
                            TraceFilterThread.MATCH_NONE, true,
                            settingsTabs.localOutputSettingsTab.discardFiltered.getSelection());
            }
          }
        });
        
        filterThread = new TraceFilterThread(new TraceTextHandler()
        {
          // Re-usable class to avoid object allocations
          class SetTextRunnable implements Runnable
          {
            String traceText = null;
            
            @Override
            public void run()
            {
              if (!sRoot.isDisposed())
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
              if (!sRoot.isDisposed())
              {
                textOutput.append(traceText);
                if (autoScroll)
                {
                  textOutput.setTopIndex(Integer.MAX_VALUE);
                }
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
      
      public void findEntry()
      {
        // Extract current line of text
        String line = getCurrentLine();
        
        // Check if it is a trace line
        Matcher traceLineMatcher = TRACE_LINE.matcher(line);
        if (traceLineMatcher.matches())
        {
          String matchStr = traceLineMatcher.group(1);
          findInput.setText(matchStr + ": {");
          doFind(false);
        }
      }
      
      public void findExit()
      {
        // Extract current line of text
        String line = getCurrentLine();
        
        // Check if it is a trace line
        Matcher traceLineMatcher = TRACE_LINE.matcher(line);
        if (traceLineMatcher.matches())
        {
          String matchStr = traceLineMatcher.group(1);
          findInput.setText(matchStr + ": }");
          doFind(true);
        }
      }
      
      private String getCurrentLine()
      {
        Point linePoint = textOutput.getSelection();
        int lineOffset = linePoint.x;
        String line = "";
        
        if (lineOffset != -1)
        {
          String text = textOutput.getText();
          String earlierText = text.substring(0, lineOffset);
          int startIndex = earlierText.lastIndexOf("\n");
          int endIndex = text.indexOf("\n", lineOffset);
          if ((startIndex != -1) && (endIndex != -1))
          {
            line = text.substring(startIndex + 1, endIndex - 1);
          }
        }
        return line;
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
                if (down)
                {                
                  downButton.setFont(downButtonBoldFont);
                  upButton.setFont(downButtonDefFont);
                }
                else
                {
                  downButton.setFont(downButtonDefFont);
                  upButton.setFont(upButtonBoldFont);
                }
                
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
                  findInput.setForeground(findDefaultFore);
                  findInput.setBackground(findDefaultBack);
                }
                else
                {
                  Display disp = sWindow.getDisplay();
                  findInput.setBackground(new Color(Display.getCurrent(), 255, 200, 200));
                  findInput.setForeground(disp.getSystemColor(SWT.COLOR_BLACK));
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
          if (!sRoot.isDisposed())
          {
            if (activeDiscardFiltered)
            {
              statusLabel.setText("Displayed lines: " + displayed);
            }
            else
            {
              statusLabel.setText("Displayed lines: " + displayed + ", Total lines: " + total);
            }
          }
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
    
      private void applyPatterns(List<String> newIncludePattern,
                                 List<String> newExcludePattern,
                                 final boolean isToggle,
                                 final boolean isDiscardFiltered)
      {
        if (newIncludePattern.equals(activeIncludeFilterPattern)
            && newExcludePattern.equals(activeExcludeFilterPattern) &&
            isDiscardFiltered == activeDiscardFiltered)
        {
          return;
        } 
        else
        {
          oldIncludeFilterPattern = activeIncludeFilterPattern;
          oldExcludeFilterPattern = activeExcludeFilterPattern;
    
          activeIncludeFilterPattern = newIncludePattern;
          activeExcludeFilterPattern = newExcludePattern;
          
          activeDiscardFiltered = isDiscardFiltered;
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
    
            final List<String> newIncludePattern = activeIncludeFilterPattern;
            final List<String> newExcludePattern = activeExcludeFilterPattern;
    
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
              public List<String> getIncludePattern()
              {
                return newIncludePattern;
              }
    
              @Override
              public List<String> getExcludePattern()
              {
                return newExcludePattern;
              }

              @Override
              public boolean discardFiltered()
              {
                return isDiscardFiltered;
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
            "[grow]", "[][grow]");
    
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(windowLayout);
        
        helpLink = new Link(composite, SWT.NONE);
        helpLink.setLayoutData("growx,wrap");
        helpLink.setText("<A HREF=\"http://mchr3k.github.com/org.intrace/\">Online Help</A>");
        
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
  }

  // Window ref
  private final InTraceUI thisWindow = this;

  private ConnectState connectionState = ConnectState.DISCONNECTED;

  public void setConnectionState(ConnectState connectionState)
  {
    this.connectionState = connectionState;
    if (connectionState == ConnectState.CONNECTED)
    {
      if (!sWindow.isDisposed())
      {
        sWindow.getDisplay().syncExec(new Runnable()
        {
          @Override
          public void run()
          {
            connBar.hide(); 
          }
        });        
      }
    }
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
  
  private List<String> lastEnteredIncludeFilterPattern = TraceFilterThread.MATCH_ALL;  
  private List<String> activeIncludeFilterPattern = TraceFilterThread.MATCH_ALL;
  private List<String> oldIncludeFilterPattern = TraceFilterThread.MATCH_ALL;
  
  private List<String> lastEnteredExcludeFilterPattern = TraceFilterThread.MATCH_NONE;
  private List<String> activeExcludeFilterPattern = TraceFilterThread.MATCH_NONE;
  private List<String> oldExcludeFilterPattern = TraceFilterThread.MATCH_NONE;
  private boolean activeDiscardFiltered = true; 
  
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
            networkTracePort, config, outputTabs.textOutputTab.filterThread);
        networkTraceThread.start();
      } catch (IOException ex)
      {
        outputTabs.textOutputTab.filterThread
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
          outputTabs.textOutputTab.filterThread.addSystemTraceLine("No instrumented classes");
        } 
        else
        {
          modifiedClasses = modifiedClasses.substring(1, modifiedClasses
              .length() - 1);
          String[] classNames = modifiedClasses.split(",");
          for (String className : classNames)
          {
            outputTabs.textOutputTab.filterThread.addSystemTraceLine("Instrumented: "
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
          controlThread.sendMessage(AgentConfigConstants.CLASS_REGEX + includePattern
              + AgentConfigConstants.EXCLUDE_CLASS_REGEX + excludePattern);
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
      connBar.portInput.setText(Integer.toString(settingsData.actualServerPort));
    }
    
    if (settingsData.waitStart)
    {
      startProgramBar.show();
    }
    else
    {
      startProgramBar.hide();
    }
    
    if (connectionState == ConnectState.CONNECTING)
    {
      connBar.connectButton.setText(ClientStrings.CONNECTING);
      connBar.connectButton.setEnabled(false);
      connBar.addressInput.setEnabled(false);
      connBar.portInput.setEnabled(false);
    } 
    else
    {
      chooseText(connBar.connectButton,
          (connectionState == ConnectState.CONNECTED), ClientStrings.CONNECT,
          ClientStrings.DISCONNECT);

      if (connectionState == ConnectState.CONNECTED)
      {
        // Disable connection details
        connBar.addressInput.setEnabled(false);
        connBar.portInput.setEnabled(false);
        
        // Enable all buttons
        mainBar.classesButton.setEnabled(true);
        settingsTabs.extraTab.listClasses.setEnabled(true);

        settingsTabs.traceTab.argsTrace.setEnabled(true);
        settingsTabs.traceTab.branchTrace.setEnabled(true);
        settingsTabs.traceTab.entryExitTrace.setEnabled(true);
        settingsTabs.traceTab.arrayTrace.setEnabled(true);
        settingsTabs.agentOutputSettingsTab.fileOutput.setEnabled(true);
        settingsTabs.agentOutputSettingsTab.stdOutOutput.setEnabled(true);

        settingsTabs.extraTab.togSaveClasses.setEnabled(true);
        settingsTabs.extraTab.togVerbose.setEnabled(true);
        settingsTabs.extraTab.printSettings.setEnabled(true);

        outputTabs.textOutputTab.networkOutput.setEnabled(true);

        // Update the button pressed/unpressed state
        settingsTabs.traceTab.argsTrace.setSelection(settingsData.argsEnabled);
        settingsTabs.traceTab.branchTrace.setSelection(settingsData.branchEnabled);
        settingsTabs.traceTab.entryExitTrace.setSelection(settingsData.entryExitEnabled);
        settingsTabs.traceTab.arrayTrace.setSelection(settingsData.truncArraysEnabled);
        settingsTabs.agentOutputSettingsTab.fileOutput.setSelection(settingsData.fileOutEnabled);
        settingsTabs.agentOutputSettingsTab.stdOutOutput.setSelection(settingsData.stdOutEnabled);

        settingsTabs.extraTab.togSaveClasses.setSelection(settingsData.saveTracedClassfiles);
        settingsTabs.extraTab.togVerbose.setSelection(settingsData.verboseMode);

        outputTabs.textOutputTab.networkOutput.setSelection(settingsData.netOutEnabled);

        // Update number of classes
        mainBar.setStatus(settingsData.instruClasses,
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
          connBar.addressInput.setEnabled(true);
          connBar.portInput.setEnabled(true);
        }
        
        startProgramBar.hide();

        mainBar.classesButton.setEnabled(false);
        settingsTabs.extraTab.listClasses.setEnabled(false);

        settingsTabs.traceTab.argsTrace.setEnabled(false);
        settingsTabs.traceTab.branchTrace.setEnabled(false);
        settingsTabs.traceTab.entryExitTrace.setEnabled(false);
        settingsTabs.traceTab.arrayTrace.setEnabled(false);
        settingsTabs.agentOutputSettingsTab.fileOutput.setEnabled(false);
        settingsTabs.agentOutputSettingsTab.stdOutOutput.setEnabled(false);

        settingsTabs.extraTab.togSaveClasses.setEnabled(false);
        settingsTabs.extraTab.togVerbose.setEnabled(false);
        settingsTabs.extraTab.printSettings.setEnabled(false);

        outputTabs.textOutputTab.networkOutput.setEnabled(false);
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
          mainBar.mainStatusLabel.setText(statusText);
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
          mainBar.setProgress(numDone, numTotal, done);
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
          mainBar.setStatus(numInstr, numTotal);
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
          outputTabs.textOutputTab.filterThread
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
    outputTabs.textOutputTab.filterThread.interrupt();
  }
  
  public static Image[] getIcons(Display display) throws IOException
  {
    // Load icons
    ClassLoader loader = InTraceUI.class.getClassLoader();
    
    InputStream is16 = loader.getResourceAsStream(
                       "org/intrace/icons/intrace16.gif");    
    Image icon16 = new Image(display, is16);
    is16.close();
    
    InputStream is32 = loader.getResourceAsStream(
                       "org/intrace/icons/intrace32.gif");    
    Image icon32 = new Image(display, is32);
    is32.close();
    
    InputStream is48 = loader.getResourceAsStream(
                       "org/intrace/icons/intrace48.gif");    
    Image icon48 = new Image(display, is48);
    is48.close();

    InputStream is128 = loader.getResourceAsStream(
                        "org/intrace/icons/intrace128.png");    
    Image icon128 = new Image(display, is128);
    is128.close();
    
    return new Image[] {icon16, icon32, icon48, icon128};
  }
}
