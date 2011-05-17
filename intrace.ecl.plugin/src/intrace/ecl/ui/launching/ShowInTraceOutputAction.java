package intrace.ecl.ui.launching;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class ShowInTraceOutputAction implements IViewActionDelegate, IActionDelegate2
{
  
  @Override
  public void run(IAction action)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection)
  {
    action.setEnabled(true);
  }

  @Override
  public void init(IAction action)
  {
    action.setEnabled(true);
  }

  @Override
  public void dispose()
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void runWithEvent(IAction action, Event event)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void init(IViewPart view)
  {
    // TODO Auto-generated method stub
    
  }
}
