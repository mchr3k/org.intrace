package intrace.ecl.ui.output;

import intrace.ecl.ui.launching.InTraceLaunch;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class EditorInput implements IEditorInput
{
  public final InTraceLaunch callback;
  public final InputType type;

  public enum InputType
  {
    NEWCONNECTION,
    RECONNECT
  }

  public EditorInput(InTraceLaunch xiCallback, InputType xiType)
  {
    this.callback = xiCallback;
    this.type = xiType;
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
    return "InTrace";
  }

  @Override
  public IPersistableElement getPersistable()
  {
    return null;
  }

  @Override
  public String getToolTipText()
  {
    return "InTrace";
  }
}
