package intrace.ecl.ui.launching;

import intrace.ecl.Util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;

public class TabGroup extends AbstractLaunchConfigurationTabGroup implements IExecutableExtension
{
  private static final String DELEGATE_LAUNCHMODE = ILaunchManager.RUN_MODE;
  private static final String EXPOINT_TABGROUP = "org.eclipse.debug.ui.launchConfigurationTabGroups"; //$NON-NLS-1$
  private static final String CONFIGATTR_TYPE = "type"; //$NON-NLS-1$

  private ILaunchConfigurationTabGroup tabGroupDelegate;
  private ILaunchConfigurationTab[] alltabs;

  public void setInitializationData(IConfigurationElement config,
      String propertyName, Object data) throws CoreException
  {
    tabGroupDelegate = createDelegate(config.getAttribute(CONFIGATTR_TYPE));
  }

  protected ILaunchConfigurationTabGroup createDelegate(String type)
      throws CoreException
  {
    IExtensionPoint extensionpoint = Platform.getExtensionRegistry()
        .getExtensionPoint(EXPOINT_TABGROUP);
    IConfigurationElement[] tabGroupConfigs = extensionpoint
        .getConfigurationElements();
    IConfigurationElement element = null;
    findloop: for (IConfigurationElement tabGroupConfig : tabGroupConfigs)
    {
      if (type.equals(tabGroupConfig.getAttribute(CONFIGATTR_TYPE)))
      {
        IConfigurationElement[] modeConfigs = tabGroupConfig
            .getChildren("launchMode"); //$NON-NLS-1$
        for (IConfigurationElement modeConfig : modeConfigs)
        {
          if (DELEGATE_LAUNCHMODE.equals(modeConfig.getAttribute("mode"))) { //$NON-NLS-1$
            element = tabGroupConfig;
            break findloop;
          }
        }
      }
    }

    if (element == null)
    {
      String msg = "No tab group registered to run " + type; //$NON-NLS-1$;
      throw new CoreException(Util.createErrorStatus(msg, null));
    }
    else
    {
      return (ILaunchConfigurationTabGroup) element
          .createExecutableExtension("class"); //$NON-NLS-1$
    }
  }

  @Override
  public void createTabs(ILaunchConfigurationDialog dialog, String mode)
  {
    tabGroupDelegate.createTabs(dialog, mode);
    ILaunchConfigurationTab[] tabs = tabGroupDelegate.getTabs();
    alltabs = new ILaunchConfigurationTab[tabs.length + 1];
    alltabs[0] = tabs[0];    
    alltabs[1] = new InTraceLaunchConfigTab();
    for (int ii = 2; ii < alltabs.length; ii++)
    {
      alltabs[ii] = tabs[ii - 1];
    }
    setTabs(alltabs);
  }
  
  @Override
  public ILaunchConfigurationTab[] getTabs()
  {
    return alltabs;
  }
}
