package org.intrace.client.gui;

import java.io.IOException;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.intrace.client.gui.helper.Connection.ConnectState;
import org.intrace.client.gui.helper.InTraceUI;
import org.intrace.client.gui.helper.InTraceUI.IConnectionStateCallback;
import org.intrace.client.gui.helper.InTraceUI.UIMode;

public class InTraceStandaloneUI
{
  /**
   * @param args
   */
  public static void main(String[] args) throws IOException
  {
    // Prepare window
    Display.setAppName("InTrace");
    final Shell window = new Shell();
    window.setSize(new Point(800, 800));
    window.setMinimumSize(new Point(800, 480));

    // Load icons
    Display display = window.getDisplay();
    Image[] icons = InTraceUI.getIcons(display);
    window.setImages(icons);

    // Fill in UI
    InTraceUI ui = new InTraceUI(window, window, UIMode.STANDALONE, null, null);

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
