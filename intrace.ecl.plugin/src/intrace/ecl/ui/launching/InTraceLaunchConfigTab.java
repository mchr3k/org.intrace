package intrace.ecl.ui.launching;

import intrace.ecl.ui.output.InTraceEditor;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.miginfocom.swt.MigLayout;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.intrace.client.gui.helper.ClientStrings;
import org.intrace.client.gui.helper.InTraceUI;
import org.intrace.client.gui.helper.TraceFilterThread;
import org.intrace.client.gui.helper.InTraceUI.UIMode;
import org.intrace.client.gui.helper.InTraceUI.UIModeData;
import org.intrace.client.gui.helper.IncludeExcludeWindow;
import org.intrace.client.gui.helper.IncludeExcludeWindow.PatternInputCallback;

class InTraceLaunchConfigTab implements ILaunchConfigurationTab
{
  private Image icon16;
  private Display display;
  private Composite composite;
  private Button classesButton;
  
  private String classRegex = "";
  private String classExcludeRegex = "";
  
  private List<String> includePattern = TraceFilterThread.MATCH_ALL;
  private List<String> excludePattern = TraceFilterThread.MATCH_NONE;
  
  private Button textFilter;

  @Override
  public void createControl(Composite parent)
  {
    display = parent.getDisplay();
    MigLayout tabLayout = new MigLayout("fill", "[]", "[][][grow]");

    composite = new Composite(parent, SWT.NONE);
    composite.setLayout(tabLayout);
    
    classesButton = new Button(composite, SWT.PUSH);
    classesButton.setText("Classes To Trace...");
    classesButton.setLayoutData("growx,wrap");

    final UIModeData modeData = InTraceEditor.getUIModeData();
    
    classesButton
    .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent arg0)
      {
        IncludeExcludeWindow regexInput = new IncludeExcludeWindow(
            ClientStrings.CLASS_TITLE, ClientStrings.CLASS_HELP_TEXT, UIMode.ECLIPSE,
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
                  setRegex(InTraceUI.getStringFromList(includePattern), 
                           InTraceUI.getStringFromList(excludePattern));
                }
              }
            }, 
            InTraceUI.getListFromString(classRegex), 
            InTraceUI.getListFromString(classExcludeRegex),
            InTraceUI.ALLOW_CLASSES);
        InTraceUI.placeDialogInCenter(display.getBounds(), regexInput.sWindow);
      }
    });
    
    textFilter = new Button(composite, SWT.PUSH);
    textFilter.setText("Trace Filters...");
    textFilter.setLayoutData("growx");
    
    final PatternInputCallback patternCallback = new PatternInputCallback()
    {
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
        IncludeExcludeWindow regexInput;
        regexInput = new IncludeExcludeWindow("Output Filter", ClientStrings.FILTER_HELP_TEXT, UIMode.ECLIPSE,
            modeData,
            patternCallback, 
            includePattern,
            excludePattern, 
            InTraceUI.ALLOW_ALL);
        InTraceUI.placeDialogInCenter(display.getBounds(), regexInput.sWindow);
      }
    });
  }

  private void setRegex(String stringFromList,
                        String stringFromList2)
  {
    // TODO Auto-generated method stub    
  }

  private void applyPatterns(List<String> includePattern2,
      List<String> excludePattern2)
  {
    // TODO Auto-generated method stub    
  }

  @Override
  public Control getControl()
  {
    return composite;
  }

  @Override
  public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog)
  {
    // Do nothing
  }

  @Override
  public void initializeFrom(ILaunchConfiguration configuration)
  {
    // Ignore
  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
  {
    // Do nothing
  }

  @Override
  public void dispose()
  {
    icon16.dispose();
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy configuration)
  {
    // Ignore
  }

  @Override
  public String getErrorMessage()
  {
    return null;
  }

  @Override
  public String getMessage()
  {
    return null;
  }

  @Override
  public boolean isValid(ILaunchConfiguration launchConfig)
  {
    return true;
  }

  @Override
  public boolean canSave()
  {
    return false;
  }

  @Override
  public void launched(ILaunch launch)
  {
    // Not called anymore
  }

  @Override
  public String getName()
  {
    return "InTrace";
  }

  @Override
  public Image getImage()
  {
    ClassLoader loader = InTraceLaunchConfigTab.class.getClassLoader();
    InputStream is16 = loader.getResourceAsStream(
        "org/intrace/icons/intrace16.gif");    
    icon16 = new Image(display, is16);
    try
    {
      is16.close();
    }
    catch (IOException e)
    {
      // Ignore
    }
    return icon16;
  }

  @Override
  public void activated(ILaunchConfigurationWorkingCopy workingCopy)
  {
    // Ignore
  }

  @Override
  public void deactivated(ILaunchConfigurationWorkingCopy workingCopy)
  {
    // Ignore
  }    
}