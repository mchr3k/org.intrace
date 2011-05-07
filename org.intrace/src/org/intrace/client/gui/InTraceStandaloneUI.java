package org.intrace.client.gui;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.intrace.client.gui.helper.InTraceUI;

public class InTraceStandaloneUI
{
  /**
   * @param args
   */
  public static void main(String[] args)
  {
    // Prepare window
    Shell window = new Shell();
    window.setText("InTrace UI");
    window.setSize(new Point(800, 800));
    window.setMinimumSize(new Point(800, 480));
    
    // Fill in UI
    new InTraceUI(window, window).open();
  }

}
