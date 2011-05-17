package intrace.ecl.ui.output;

import intrace.ecl.ui.launching.ConnectionHolder;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class EditorInput implements IEditorInput
{
  public final ConnectionHolder callback;

  public EditorInput(ConnectionHolder xiCallback)
  {
    this.callback = xiCallback;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Object getAdapter(Class adapter)
  {
    return null;
  }

  @Override
  public boolean exists()
  {
    return false;
  }

  @Override
  public ImageDescriptor getImageDescriptor()
  {
    return ImageDescriptor.getMissingImageDescriptor();
  }

  @Override
  public String getName()
  {
    return "InTraceInput";
  }

  @Override
  public IPersistableElement getPersistable()
  {
    return null;
  }

  @Override
  public String getToolTipText()
  {
    return "InTraceInput";
  }
}
