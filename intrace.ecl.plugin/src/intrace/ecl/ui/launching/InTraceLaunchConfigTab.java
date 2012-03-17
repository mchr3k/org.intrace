package intrace.ecl.ui.launching;

import intrace.ecl.Activator;
import intrace.ecl.Util;
import intrace.ecl.ui.output.InTraceEditor;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.miginfocom.swt.MigLayout;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.intrace.client.gui.helper.ClientStrings;
import org.intrace.client.gui.helper.InTraceUI;
import org.intrace.client.gui.helper.InTraceUI.UIMode;
import org.intrace.client.gui.helper.InTraceUI.UIModeData;
import org.intrace.client.gui.helper.IncludeExcludeWindow;
import org.intrace.client.gui.helper.IncludeExcludeWindow.PatternInputCallback;
import org.intrace.client.gui.helper.TraceFilterThread;

public class InTraceLaunchConfigTab implements ILaunchConfigurationTab
{
  private Image icon16;
  private Display display;
  private Composite composite;
  private Button classesButton;

  private String classIncludePattern = "";
  private String classExcludePattern = "";

  private List<String> outputIncludePattern = TraceFilterThread.MATCH_ALL;
  private List<String> outputExcludePattern = TraceFilterThread.MATCH_NONE;

  private Button textFilter;

  public static final String CLASS_INCL_ATTR = "INTRACE_CLASS_INCL_ATTR";
  public static final String CLASS_EXCL_ATTR = "INTRACE_CLASS_EXCL_ATTR";
  public static final String OUTPUT_INCL_ATTR = "OUTPUT_CLASS_INCL_ATTR";
  public static final String OUTPUT_EXCL_ATTR = "OUTPUT_CLASS_EXCL_ATTR";

  private ILaunchConfigurationDialog dialog;
  private ILaunchConfigurationWorkingCopy wc;

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
              private List<String> localIncludePattern = null;
              private List<String> localExcludePattern = null;

              @Override
              public void setIncludePattern(List<String> newIncludePattern)
              {
                localIncludePattern = newIncludePattern;
                savePatterns();
              }

              @Override
              public void setExcludePattern(List<String> newExcludePattern)
              {
                localExcludePattern = newExcludePattern;
                savePatterns();
              }

              private void savePatterns()
              {
                if ((localIncludePattern != null) && (localExcludePattern != null))
                {
                  setRegex(InTraceUI.getStringFromList(localIncludePattern),
                           InTraceUI.getStringFromList(localExcludePattern));
                }
              }
            },
            InTraceUI.getListFromString(classIncludePattern),
            InTraceUI.getListFromString(classExcludePattern),
            InTraceUI.ALLOW_CLASSES);
        InTraceUI.placeDialogInCenter(display.getBounds(), regexInput.sWindow);
      }
    });

    textFilter = new Button(composite, SWT.PUSH);
    textFilter.setText("Trace Filters...");
    textFilter.setLayoutData("growx");

    final PatternInputCallback patternCallback = new PatternInputCallback()
    {
      private List<String> localIncludePattern = TraceFilterThread.MATCH_ALL;
      private List<String> localExcludePattern = TraceFilterThread.MATCH_NONE;

      @Override
      public void setIncludePattern(List<String> newIncludePattern)
      {
        localIncludePattern = newIncludePattern;
        savePatterns();
      }

      @Override
      public void setExcludePattern(List<String> newExcludePattern)
      {
        localExcludePattern = newExcludePattern;
        savePatterns();
      }

      private void savePatterns()
      {
        if ((localIncludePattern != null) && (localExcludePattern != null))
        {
          if (localIncludePattern.equals(TraceFilterThread.MATCH_NONE) &&
              localExcludePattern.equals(TraceFilterThread.MATCH_NONE))
          {
            localIncludePattern = TraceFilterThread.MATCH_ALL;
          }
          applyPatterns(localIncludePattern, localExcludePattern);
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
            outputIncludePattern,
            outputExcludePattern,
            InTraceUI.ALLOW_ALL);
        InTraceUI.placeDialogInCenter(display.getBounds(), regexInput.sWindow);
      }
    });
  }

  private void setRegex(String classIncludePattern,
                        String classExcludePattern)
  {
    this.classIncludePattern = classIncludePattern;
    this.classExcludePattern = classExcludePattern;
    wc.setAttribute(CLASS_INCL_ATTR, classIncludePattern);
    wc.setAttribute(CLASS_EXCL_ATTR, classExcludePattern);
    dialog.updateButtons();
  }

  private void applyPatterns(List<String> outputIncludePattern,
                             List<String> outputExcludePattern)
  {
    this.outputIncludePattern = outputIncludePattern;
    this.outputExcludePattern = outputExcludePattern;
    wc.setAttribute(OUTPUT_INCL_ATTR, outputIncludePattern);
    wc.setAttribute(OUTPUT_EXCL_ATTR, outputExcludePattern);
    dialog.updateButtons();
  }

  @Override
  public Control getControl()
  {
    return composite;
  }

  @Override
  public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog)
  {
    this.dialog = dialog;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void initializeFrom(ILaunchConfiguration configuration)
  {
    try
    {
      classIncludePattern = configuration.getAttribute(CLASS_INCL_ATTR, "");
      classExcludePattern = configuration.getAttribute(CLASS_EXCL_ATTR, "");
      outputIncludePattern = configuration.getAttribute(OUTPUT_INCL_ATTR, TraceFilterThread.MATCH_ALL);
      outputExcludePattern = configuration.getAttribute(OUTPUT_EXCL_ATTR, TraceFilterThread.MATCH_NONE);
    }
    catch (CoreException e)
    {
      Activator.getDefault().getLog().log(Util.createErrorStatus("Error", e));
    }
  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
  {
    try
    {
      String mainClass = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "");
      configuration.setAttribute(CLASS_INCL_ATTR, mainClass);
    }
    catch (CoreException e)
    {
      Activator.getDefault().getLog().log(Util.createErrorStatus("Error", e));
    }
  }

  @Override
  public void dispose()
  {
    if (icon16 != null)
    {
      icon16.dispose();
    }
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy configuration)
  {
    // ??
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
    return true;
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
    this.wc = workingCopy;
  }

  @Override
  public void deactivated(ILaunchConfigurationWorkingCopy workingCopy)
  {
    // Ignore
  }
}