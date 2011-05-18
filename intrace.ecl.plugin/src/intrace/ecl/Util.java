package intrace.ecl;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;

public class Util
{
  public static IStatus createInfoStatus(String message)
  {
    return new Status(IStatus.INFO, Activator.PLUGIN_ID, message);
  }

  public static IStatus createErrorStatus(String message, Throwable t)
  {
    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, t);
  }
  
  public static void handleStatus(IStatus status, int statusMode)
  {
    StatusManager.getManager().handle(status, statusMode);
  }
}
