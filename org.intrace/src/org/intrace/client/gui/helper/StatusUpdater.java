package org.intrace.client.gui.helper;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class StatusUpdater
{

  private final Shell sWindow;
  private final Label statusLabel;

  public StatusUpdater(Shell sWindow, Label statusLabel)
  {
    this.sWindow = sWindow;
    this.statusLabel = statusLabel;
  }

  public void setStatusText(final String statusText)
  {
    if (!sWindow.isDisposed())
    {
      sWindow.getDisplay().syncExec(new Runnable()
      {
        @Override
        public void run()
        {
          statusLabel.setText(statusText);
        }
      });
    }
  }

}
