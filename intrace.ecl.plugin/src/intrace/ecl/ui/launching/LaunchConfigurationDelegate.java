package intrace.ecl.ui.launching;

import intrace.ecl.Activator;
import intrace.ecl.Util;
import intrace.ecl.ui.output.EditorInput;
import intrace.ecl.ui.output.EditorInput.InputType;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Class which handles adding the InTrace JVM argument and launching the InTrace editor
 */
public class LaunchConfigurationDelegate extends
    AbstractJavaLaunchConfigurationDelegate implements IExecutableExtension
{
  /**
   * Config key used to save the launch ID into an ILaunch object
   */
  public static final String INTRACE_LAUNCHKEY = "INTRACE_LAUNCHKEY";
  
  /**
   * Map of active launches
   */
  public static final Map<Long, InTraceLaunch> intraceLaunches = new ConcurrentHashMap<Long, InTraceLaunch>();
  
  /**
   * AtommicLong used to generate launch IDs
   */
  private static final AtomicLong intraceLaunchId = new AtomicLong();

  /**
   * The type of launch being handled.
   */
  protected String launchtype;

  /**
   * The delegate which will do most of the work.
   */
  protected ILaunchConfigurationDelegate launchdelegate;

  // IExecutableExtension interface:

  public void setInitializationData(IConfigurationElement config,
      String propertyName, Object data) throws CoreException
  {
    launchtype = config.getAttribute("type"); //$NON-NLS-1$
    launchdelegate = getLaunchDelegate(launchtype);
  }

  @SuppressWarnings("deprecation")
  private ILaunchConfigurationDelegate getLaunchDelegate(String launchtype)
      throws CoreException
  {
    ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager()
        .getLaunchConfigurationType(launchtype);
    if (type == null)
    {
      throw new CoreException(Util.createErrorStatus("Unknown launch type",
          new Throwable()));
    }
    return type.getDelegate(ILaunchManager.RUN_MODE);
  }

  // ILaunchConfigurationDelegate interface:

  @Override
  public void launch(ILaunchConfiguration configuration, String mode,
      ILaunch launch, IProgressMonitor monitor) throws CoreException
  {
    try
    {
      // Create working copy launch config
      ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
      
      // Identify the main class
      String mainClass = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "");
      
      // Prepare InTraceLaunch object to handle callback connection
      ServerSocket callbackServer = new ServerSocket(0);
      final InTraceLaunch intraceLaunch = new InTraceLaunch(mainClass, callbackServer);
      intraceLaunch.start();
      
      // Save off launch object for later access
      Long connId = intraceLaunchId.getAndIncrement();
      intraceLaunches.put(connId, intraceLaunch);
      launch.setAttribute(INTRACE_LAUNCHKEY, Long.toString(connId));
      
      // Setup a launch listener to cleanup the intrace launch object
      ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
      manager.addLaunchListener(new LaunchListener(launch, connId));      
      
      // Add VM arguments
      String vmArgs = wc.getAttribute(
          IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
      if (Activator.getDefault().baseAgentArg.length() > 0)
      {
        vmArgs += Activator.getDefault().baseAgentArg;
        vmArgs += "=[callbackport-";
        vmArgs += Integer.toString(callbackServer.getLocalPort());
        vmArgs += "[startwait";
      }
      wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
          vmArgs);

      // Launch editor
      final IWorkbench workbench = PlatformUI.getWorkbench();
      Display display = workbench.getDisplay();
      display.asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          try
          {
            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            IDE.openEditor(page, new EditorInput(intraceLaunch,
                InputType.NEWCONNECTION),
                "intrace.ecl.plugin.ui.output.inTraceEditor");
          }
          catch (PartInitException ex)
          {
            Util.handleStatus(Util.createErrorStatus("Failed to open InTrace Output", ex), 
                              StatusManager.SHOW);
          }
        }
      });

      // Start launch
      launchdelegate.launch(wc, ILaunchManager.RUN_MODE, launch,
          new SubProgressMonitor(monitor, 1));
    }
    catch (IOException e1)
    {
      Util.handleStatus(Util.createErrorStatus("InTrace launch failed", e1), 
          StatusManager.SHOW);
    }
  }
  
  /**
   * Launch listener which handles the cleanup of entries in the intraceLaunches map
   */
  private static class LaunchListener implements ILaunchesListener2
  {
    public LaunchListener(ILaunch targetlaunch,
                          Long intraceLaunchId)
    {
      this.targetlaunch = targetlaunch;
      this.intraceLaunchId = intraceLaunchId;
    }

    private final ILaunch targetlaunch;
    private final Long intraceLaunchId;
    
    @Override
    public void launchesRemoved(ILaunch[] launches)
    {
      // Do nothing
    }

    @Override
    public void launchesAdded(ILaunch[] launches)
    {
      // Do nothing
    }

    @Override
    public void launchesChanged(ILaunch[] launches)
    {
      // Do nothing
    }

    @Override
    public void launchesTerminated(ILaunch[] launches)
    {
      for (ILaunch launch : launches)
      {
        if (launch == targetlaunch)
        {
          InTraceLaunch intraceLaunch = intraceLaunches.remove(intraceLaunchId);
          
          if (intraceLaunch != null)
          {
            intraceLaunch.destroy();
          }
          
          ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
          manager.removeLaunchListener(this);
        }
      }
    }
    
  }
}
