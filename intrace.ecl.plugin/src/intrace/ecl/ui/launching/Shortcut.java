package intrace.ecl.ui.launching;

import intrace.ecl.Activator;
import intrace.ecl.Util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Abstract class which handles relaunching an existing InTrace launch
 * configuration.
 * <p>
 * Subclasses specify the ID of the correct delegate type.
 */
public abstract class Shortcut implements ILaunchShortcut
{
  private final String delegateId;

  public Shortcut(String xiDelegateId)
  {
    delegateId = xiDelegateId;
  }

  private ILaunchShortcut delegate;

  private ILaunchShortcut getDelegate()
  {
    if (delegate == null)
    {
      IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
          .getExtensionPoint(IDebugUIConstants.PLUGIN_ID,
              IDebugUIConstants.EXTENSION_POINT_LAUNCH_SHORTCUTS);
      IConfigurationElement[] configs = extensionPoint
          .getConfigurationElements();
      for (IConfigurationElement config : configs)
      {
        String configID = config.getAttribute("id"); //$NON-NLS-1$
        if (delegateId.equals(configID))
        {
          try
          {
            delegate = (ILaunchShortcut) config
                .createExecutableExtension("class"); //$NON-NLS-1$
          }
          catch (CoreException e)
          {
            Activator.getDefault().getLog()
                .log(Util.createErrorStatus("Error", e));
          }
          break;
        }
      }
      if (delegate == null)
      {
        String msg = "ILaunchShortcut declaration not found: " + delegateId; //$NON-NLS-1$
        Util.handleStatus(Util.createErrorStatus(msg, null), StatusManager.SHOW);
      }
    }
    return delegate;
  }

  // ILaunchShortcut interface:

  public void launch(ISelection selection, String mode)
  {
    ILaunchShortcut delegate = getDelegate();
    if (delegate != null)
    {
      delegate.launch(selection, "intrace");
    }
  }

  public void launch(IEditorPart editor, String mode)
  {
    ILaunchShortcut delegate = getDelegate();
    if (delegate != null)
    {
      delegate.launch(editor, "intrace");
    }
  }
}
