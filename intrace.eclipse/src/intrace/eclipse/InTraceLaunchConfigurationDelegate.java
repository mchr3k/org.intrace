package intrace.eclipse;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

public class InTraceLaunchConfigurationDelegate implements
    ILaunchConfigurationDelegate
{
  @Override
  public void launch(ILaunchConfiguration configuration, 
                     String mode,
                     ILaunch launch, 
                     IProgressMonitor monitor) throws CoreException
  {    
    Set<String> modes = new HashSet<String>();
    modes.add(ILaunchManager.RUN_MODE);
    ILaunchDelegate delegate = configuration.getPreferredDelegate(modes);
    ILaunchConfigurationDelegate launchdelegate = delegate.getDelegate();
    launchdelegate.launch(configuration,
                          ILaunchManager.RUN_MODE, 
                          launch,
                          new SubProgressMonitor(monitor, 1));
  }

}
