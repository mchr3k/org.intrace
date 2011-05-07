package intrace.ecl.plugin.ui.output;

import java.net.Socket;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.intrace.client.gui.helper.InTraceUI;

public class InTraceEditor extends EditorPart
{      
  private InTraceUI inTraceUI;

  public InTraceEditor()
  {
    // Do nothing
  }

  @Override
  public void createPartControl(Composite parent)
  {
    IWorkbench workbench = PlatformUI.getWorkbench();
    Display display = workbench.getDisplay();
    Shell window = display.getActiveShell();
    inTraceUI = new InTraceUI(window, parent);
  }

  @Override
  public void init(IEditorSite site, 
                   IEditorInput input)
      throws PartInitException
  {
    setSite(site);
    setInput(input);
    final EditorInput intraceInput = (EditorInput)input;
    new Thread(new Runnable()
    {      
      @Override
      public void run()
      {
        try
        {
          Socket connection = intraceInput.callback.getClientConnection();
          inTraceUI.setFixedLocalConnection(connection);
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
        }       
      }
    }).start();       
  }

  @Override
  public void dispose()
  {
    inTraceUI.dispose();
    super.dispose();
  }
  
  @Override
  public void setFocus()
  {
    // Do nothing
  }
  
  @Override
  public void doSave(IProgressMonitor monitor)
  {
    // Do nothing
  }

  @Override
  public void doSaveAs()
  {
    // Do nothing
  }

  @Override
  public boolean isDirty()
  {
    return false;
  }

  @Override
  public boolean isSaveAsAllowed()
  {
    return false;
  }
}
