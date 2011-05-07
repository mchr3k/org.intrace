package intrace.ecl.ui.launching;

import intrace.ecl.Activator;
import intrace.ecl.Util;
import intrace.ecl.plugin.ui.output.EditorInput;

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

public class ConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate implements IExecutableExtension
{
  protected String launchtype;

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
      throw new CoreException(Util.errorStatus("Unknown launch type", new Throwable()));
    }
    return type.getDelegate(ILaunchManager.RUN_MODE);
  }

  // ILaunchConfigurationDelegate interface:

  @Override
  public void launch(ILaunchConfiguration configuration, String mode,
      ILaunch launch, IProgressMonitor monitor) throws CoreException
  {
    // Add VM arguments
    ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();    
    String vmArgs = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
    vmArgs += Activator.getDefault().agentArg;
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArgs);    
    
    // Add listener for termination
    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    launchManager.addLaunchListener(new TerminationListener(launch));
    
    // Launch editor input
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
          IDE.openEditor(page, new EditorInput(), "intrace.ecl.plugin.ui.output.inTraceEditor");
        }
        catch (PartInitException e)
        {
          e.printStackTrace();
        } 
      }
    });    
    
    // Start launch
    launchdelegate.launch(wc, ILaunchManager.RUN_MODE, launch,
        new SubProgressMonitor(monitor, 1));
  }
  
  public static class TerminationListener implements ILaunchesListener2
  {
    private final ILaunch target;
    
    public TerminationListener(ILaunch target)
    {
      this.target = target;
    }
    
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
        if (launch == target)
        {
          System.out.println("Removed");
          ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
          launchManager.removeLaunchListener(this);
        }
      }
    }    
  }
}
