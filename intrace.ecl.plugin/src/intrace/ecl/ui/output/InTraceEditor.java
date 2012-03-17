package intrace.ecl.ui.output;

import intrace.ecl.Activator;
import intrace.ecl.Util;
import intrace.ecl.ui.launching.InTraceLaunchConfigTab;
import intrace.ecl.ui.output.EditorInput.InputType;

import java.net.Socket;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.themes.ITheme;
import org.intrace.client.gui.helper.Connection.ConnectState;
import org.intrace.client.gui.helper.InTraceUI;
import org.intrace.client.gui.helper.InTraceUI.ConfigDataInterface;
import org.intrace.client.gui.helper.InTraceUI.ConfigDataInterface.Callback;
import org.intrace.client.gui.helper.InTraceUI.IConnectionStateCallback;
import org.intrace.client.gui.helper.InTraceUI.UIMode;
import org.intrace.client.gui.helper.InTraceUI.UIModeData;
import org.intrace.client.gui.helper.TraceFilterThread;

@SuppressWarnings("restriction")
public class InTraceEditor extends EditorPart
{
  private InTraceUI inTraceUI;
  private EditorInput intraceInput;

  public InTraceEditor()
  {
    // Do nothing
  }

  public static UIModeData getUIModeData()
  {
    IWorkbench workBench = PlatformUI.getWorkbench();
    ITheme theme = workBench.getThemeManager().getCurrentTheme();
    ColorRegistry colreg = theme.getColorRegistry();

    Color c1 = colreg.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START);
    Color c2 = colreg.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END);
    UIModeData data = new UIModeData(c1, c2);
    return data;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void createPartControl(final Composite parent)
  {
    Composite ui = new Composite(parent, SWT.NONE);

    UIModeData data = getUIModeData();

    intraceInput = (EditorInput)getEditorInput();

    final ILaunchConfiguration config = intraceInput.callback.configuration;
    String classIncludePattern = null;
    String classExcludePattern = null;
    List<String> outputIncludePattern = null;
    List<String> outputExcludePattern = null;

    if (intraceInput.type == InputType.NEWCONNECTION)
    {
      try
      {
        // Only apply class patterns for a new connection as the agent will remember them
        // for an existing connection.
        classIncludePattern = config.getAttribute(InTraceLaunchConfigTab.CLASS_INCL_ATTR, "");
        classExcludePattern = config.getAttribute(InTraceLaunchConfigTab.CLASS_EXCL_ATTR, "");
      }
      catch (CoreException e)
      {
        Activator.getDefault().getLog().log(Util.createErrorStatus("Error", e));
      }
    }

    try
    {
      outputIncludePattern = config.getAttribute(InTraceLaunchConfigTab.OUTPUT_INCL_ATTR, TraceFilterThread.MATCH_ALL);
      outputExcludePattern = config.getAttribute(InTraceLaunchConfigTab.OUTPUT_EXCL_ATTR, TraceFilterThread.MATCH_NONE);
    }
    catch (CoreException e)
    {
      Activator.getDefault().getLog().log(Util.createErrorStatus("Error", e));
    }

    Callback cb = new Callback()
    {
      @Override
      public void setClassConfig(String classIncludePattern,
                                 String classExcludePattern)
      {
        try
        {
          ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();

          wc.setAttribute(InTraceLaunchConfigTab.CLASS_INCL_ATTR, classIncludePattern);
          wc.setAttribute(InTraceLaunchConfigTab.CLASS_EXCL_ATTR, classExcludePattern);

          wc.doSave();
        }
        catch (CoreException e)
        {
          Activator.getDefault().getLog().log(Util.createErrorStatus("Error", e));
        }
      }

      @Override
      public void setOutputConfig(List<String> outputIncludePattern,
                                  List<String> outputExcludePattern)
      {
        try
        {
          ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();

          wc.setAttribute(InTraceLaunchConfigTab.OUTPUT_INCL_ATTR, outputIncludePattern);
          wc.setAttribute(InTraceLaunchConfigTab.OUTPUT_EXCL_ATTR, outputExcludePattern);

          wc.doSave();
        }
        catch (CoreException e)
        {
          Activator.getDefault().getLog().log(Util.createErrorStatus("Error", e));
        }
      }
    };

    ConfigDataInterface configInterface = new ConfigDataInterface(classIncludePattern,
                                                                  classExcludePattern,
                                                                  outputIncludePattern,
                                                                  outputExcludePattern,
                                                                  cb);

    IWorkbench workbench = PlatformUI.getWorkbench();
    final Display display = workbench.getDisplay();
    Shell window = display.getActiveShell();
    inTraceUI = new InTraceUI(window, ui, UIMode.ECLIPSE, data, configInterface);
    inTraceUI.setConnCallback(new IConnectionStateCallback()
    {
      @Override
      public void setConnectionState(final ConnectState state)
      {
        if (!parent.isDisposed())
        {
          display.syncExec(new Runnable()
          {
            @Override
            public void run()
            {
              setPartName("InTrace: " + state.str);
            }
          });
        }
      }
    });
    inTraceUI.setConnectionState(ConnectState.CONNECTING);

    if (intraceInput.type == InputType.NEWCONNECTION)
    {
      new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          try
          {
            Socket connection = intraceInput.callback.getClientConnection();
            inTraceUI.setFixedLocalConnection(connection);
          }
          catch (InterruptedException e)
          {
            e.printStackTrace();
          }
        }
      }).start();
    }
    else
    {
      inTraceUI.setFixedLocalConnection(intraceInput.callback.agentServerPort);
    }
    intraceInput.callback.setEditor(this);
  }

  @Override
  public void init(IEditorSite site,
                   IEditorInput input)
      throws PartInitException
  {
    setSite(site);
    setInput(input);
  }

  @Override
  public void dispose()
  {
    if (intraceInput != null)
    {
      intraceInput.callback.setEditor(null);
    }
    if (inTraceUI != null)
    {
      inTraceUI.dispose();
    }
    super.dispose();
  }

  @Override
  public void setFocus()
  {
    // Do nothing
  }

  @Override
  public void doSave(IProgressMonitor monitor)
  {
    // Do nothing
  }

  @Override
  public void doSaveAs()
  {
    // Do nothing
  }

  @Override
  public boolean isDirty()
  {
    return false;
  }

  @Override
  public boolean isSaveAsAllowed()
  {
    return false;
  }
}
