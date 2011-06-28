package intrace.ecl.ui.launching;

import intrace.ecl.Util;
import intrace.ecl.ui.output.EditorInput;
import intrace.ecl.ui.output.InTraceEditor;
import intrace.ecl.ui.output.EditorInput.InputType;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.statushandlers.StatusManager;

public class ShowInTraceOutputAction implements IViewActionDelegate,
    IActionDelegate2
{
  /**
   * The underlying action for this delegate
   */
  private IAction fAction;
  /**
   * This action's view part, or <code>null</code> if not installed in a view.
   */
  private IViewPart fViewPart;

  /**
   * Cache of the most recent selection
   */
  private IStructuredSelection fSelection = StructuredSelection.EMPTY;

  /**
   * Whether this delegate has been initialized
   */
  private boolean fInitialized = false;

  @Override
  public void run(IAction action)
  {
    if (action.isEnabled())
    {
      IStructuredSelection selection = getSelection();
      runInForeground(selection);
    }
  }

  /**
   * Runs this action in the UI thread.
   */
  private void runInForeground(final IStructuredSelection selection)
  {
    BusyIndicator.showWhile(Display.getCurrent(), new Runnable()
    {
      public void run()
      {
        Iterator<?> selectionIter = selection.iterator();
        while (selectionIter.hasNext())
        {
          Object element = selectionIter.next();
          try
          {
            // Action's enablement could have been changed since
            // it was last enabled. Check that the action is still
            // enabled before running the action.
            if (isEnabledFor(element))
            {
              doAction(element);
            }
          }
          catch (Exception ex)
          {
            Util.handleStatus(Util.createErrorStatus("Failed to Show InTrace Output", ex), 
                              StatusManager.SHOW);
          }
        }
      }
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
   * .IAction, org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection s)
  {
    boolean wasInitialized = initialize(action, s);
    if (!wasInitialized)
    {
      if (getView() != null)
      {
        update(action, s);
      }
    }
  }

  /**
   * Updates the specified selection based on the selection, as well as setting
   * the selection for this action
   * 
   * @param action
   *          the action to update
   * @param s
   *          the selection
   */
  protected void update(IAction action, ISelection s)
  {
    if (s instanceof IStructuredSelection)
    {
      IStructuredSelection ss = getTargetSelection((IStructuredSelection) s);
      boolean enabled = getEnableStateForSelection(ss);
      action.setEnabled(enabled);
      setSelection(ss);
    }
    else
    {
      action.setEnabled(false);
      setSelection(StructuredSelection.EMPTY);
    }
  }

  protected void doAction(Object object)
  {
    ILaunch launch = getLaunch(object);
    if (launch != null)
    {
      // Get connection object
      String connIDStr = launch.getAttribute(LaunchConfigurationDelegate.INTRACE_LAUNCHKEY);
      Long connID = Long.parseLong(connIDStr);
      final InTraceLaunch conn = LaunchConfigurationDelegate.intraceLaunches.get(connID);
      if (conn != null)
      {      
        // Launch editor
        final IWorkbench workbench = PlatformUI.getWorkbench();
        Display display = workbench.getDisplay();
        display.asyncExec(new Runnable()
        {      
          @Override
          public void run()
          {
            InTraceEditor editor = conn.getEditor();
            if (editor != null)
            {
              // Show an existing editor
              editor.getSite().getPage().activate(editor);
              editor.setFocus();
            }
            else
            {
              // Launch a new editor
              try
              {          
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                IWorkbenchPage page = window.getActivePage();
                IDE.openEditor(page, new EditorInput(conn, InputType.RECONNECT), 
                               "intrace.ecl.plugin.ui.output.inTraceEditor");
              }
              catch (PartInitException ex)
              {
                Util.handleStatus(Util.createErrorStatus("Failed to open InTrace Output", ex), 
                                  StatusManager.SHOW);
              }
            }
          }
        });   
      }
    }
  }

  @Override
  public void dispose()
  {
    fSelection = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
   */
  public void init(IViewPart view)
  {
    fViewPart = view;
  }

  /**
   * Returns this action's view part, or <code>null</code> if not installed in a
   * view.
   * 
   * @return view part or <code>null</code>
   */
  protected IViewPart getView()
  {
    return fViewPart;
  }

  /**
   * Initialize this delegate, updating this delegate's presentation. As well,
   * all of the flavors of AbstractDebugActionDelegates need to have the initial
   * enabled state set with a call to update(IAction, ISelection).
   * 
   * @param action
   *          the presentation for this action
   * @return whether the action was initialized
   */
  protected boolean initialize(IAction action, ISelection selection)
  {
    if (!isInitialized())
    {
      setAction(action);
      update(action, selection);
      setInitialized(true);
      return true;
    }
    return false;
  }

  /**
   * Returns the most recent selection
   * 
   * @return structured selection
   */
  protected IStructuredSelection getSelection()
  {
    return fSelection;
  }

  /**
   * Sets the most recent selection
   * 
   * @parm selection structured selection
   */
  private void setSelection(IStructuredSelection selection)
  {
    fSelection = selection;
  }

  /**
   * Allows the underlying <code>IAction</code> to be set to the specified
   * <code>IAction</code>
   * 
   * @param action
   *          the action to set
   */
  protected void setAction(IAction action)
  {
    fAction = action;
  }

  /**
   * Allows access to the underlying <code>IAction</code>
   * 
   * @return the underlying <code>IAction</code>
   */
  protected IAction getAction()
  {
    return fAction;
  }

  /**
   * Returns if this action has been initialized or not
   * 
   * @return if this action has been initialized or not
   */
  protected boolean isInitialized()
  {
    return fInitialized;
  }

  /**
   * Sets the initialized state of this action to the specified boolean value
   * 
   * @param initialized
   *          the value to set the initialized state to
   */
  protected void setInitialized(boolean initialized)
  {
    fInitialized = initialized;
  }

  /**
   * Return whether the action should be enabled or not based on the given
   * selection.
   */
  protected boolean getEnableStateForSelection(IStructuredSelection selection)
  {
    if (selection.size() == 0)
    {
      return false;
    }
    Iterator<?> itr = selection.iterator();
    while (itr.hasNext())
    {
      Object element = itr.next();
      if (!isEnabledFor(element))
      {
        return false;
      }
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction
   * , org.eclipse.swt.widgets.Event)
   */
  public void runWithEvent(IAction action, Event event)
  {
    run(action);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
   */
  public void init(IAction action)
  {
    fAction = action;
  }

  /**
   * Return the ILaunch associated with a model element, or null if there is no
   * such association.
   * 
   * @param element
   *          the model element
   * @return the ILaunch associated with the element, or null.
   * @since 3.6
   */
  private static ILaunch getLaunch(Object element)
  {
    // support for custom models
    ILaunch launch = (ILaunch) DebugPlugin.getAdapter(element, ILaunch.class);
    if (launch == null)
    {
      // support for standard debug model
      if (element instanceof IDebugElement)
      {
        launch = ((IDebugElement) element).getLaunch();
      }
      else if (element instanceof ILaunch)
      {
        launch = ((ILaunch) element);
      }
      else if (element instanceof IProcess)
      {
        launch = ((IProcess) element).getLaunch();
      }
    }
    return launch;
  }

  /**
   * Returns whether the given launch configuration should be visible in the
   * debug ui. If the config is marked as private, or belongs to a different
   * category (i.e. non-null), then this configuration should not be displayed
   * in the debug ui.
   * 
   * @param launchConfiguration
   * @return boolean
   */
  private static boolean isVisible(ILaunchConfiguration launchConfiguration)
  {
    try
    {
      return !(launchConfiguration.getAttribute(IDebugUIConstants.ATTR_PRIVATE,
          false));
    }
    catch (CoreException e)
    {
    }
    return false;
  }

  protected boolean isEnabledFor(Object element)
  {
    ILaunch launch = getLaunch(element);

    return (launch != null) && 
           (launch.getLaunchConfiguration() != null) && 
           isVisible(launch.getLaunchConfiguration()) && 
           isInTraceLaunchAvailable(launch);
  }

  /**
   * @param launch
   * @return True if the provided launch has a corresponding InTraceLaunch
   */
  private boolean isInTraceLaunchAvailable(ILaunch launch)
  {
    String launchIDStr = launch.getAttribute(LaunchConfigurationDelegate.INTRACE_LAUNCHKEY);
    if ((launchIDStr != null) &&
        (launchIDStr.length() > 0))
    {
      try
      {
        Long connID = Long.parseLong(launchIDStr);
        InTraceLaunch intraceLaunch = LaunchConfigurationDelegate.intraceLaunches.get(connID);
        if (intraceLaunch != null)
        {
          intraceLaunch.setOpeneditoraction(fAction);
          return true;
        }
        else
        {
          return false;
        }
      }
      catch (NumberFormatException ex)
      {
        return false;
      }
    }    
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#
   * getTargetSelection(org.eclipse.jface.viewers.IStructuredSelection)
   */
  protected IStructuredSelection getTargetSelection(IStructuredSelection s)
  {
    if (s.isEmpty())
    {
      return s;
    }
    Set<ILaunch> dups = new LinkedHashSet<ILaunch>();
    Iterator<?> iterator = s.iterator();
    while (iterator.hasNext())
    {
      Object object = iterator.next();
      ILaunch launch = getLaunch(object);
      if (launch == null)
      {
        return s;
      }
      dups.add(launch);
    }
    return new StructuredSelection(dups.toArray());
  }

}
