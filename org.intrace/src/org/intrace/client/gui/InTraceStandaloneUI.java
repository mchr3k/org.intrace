package org.intrace.client.gui;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.intrace.client.gui.helper.InTraceUI;
import org.intrace.client.gui.helper.Connection.ConnectState;
import org.intrace.client.gui.helper.InTraceUI.IConnectionStateCallback;

public class InTraceStandaloneUI
{
  /**
   * @param args
   */
  public static void main(String[] args)
  {
    // Prepare window
    final Shell window = new Shell();
    window.setSize(new Point(800, 800));
    window.setMinimumSize(new Point(800, 480));
    
    // Fill in UI
    InTraceUI ui = new InTraceUI(window, window, true);
    
    // Register title callback
    ui.setConnCallback(new IConnectionStateCallback()
    {      
      @Override
      public void setConnectionState(final ConnectState state)
      {
        if (!window.isDisposed())
        {
          window.getDisplay().syncExec(new Runnable()
          {            
            @Override
            public void run()
            {
              window.setText("InTrace UI: " + state.str);
            }
          });
        }
      }
    });
    
    // Open UI
    ui.open();
  }

}
