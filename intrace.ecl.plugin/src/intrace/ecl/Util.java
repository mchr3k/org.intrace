package intrace.ecl;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class Util
{
  public static IStatus errorStatus(String message, Throwable t) {
    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, message, t);
  }
}
