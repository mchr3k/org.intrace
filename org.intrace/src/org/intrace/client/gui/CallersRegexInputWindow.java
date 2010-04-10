package org.intrace.client.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CallersRegexInputWindow
{
  private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="10,10"
  private Text regexInput = null;
  private Button setRegexButton = null;
  private Button cancelButton = null;

  private TraceWindow mainWindowRef = null;  //  @jve:decl-index=0:

  /**
   * This method initializes sShell
   */
  private void createSShell()
  {
    GridData gridData2 = new GridData();
    gridData2.widthHint = 100;
    gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
    GridData gridData1 = new GridData();
    gridData1.widthHint = 100;
    gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
    GridData gridData = new GridData();
    gridData.horizontalSpan = 2;
    gridData.widthHint = 300;
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    gridLayout.makeColumnsEqualWidth = true;
    sShell = new Shell(SWT.APPLICATION_MODAL | SWT.CLOSE | SWT.TITLE | SWT.MIN);
    sShell.setText("Enter Method Regex");
    sShell.setLayout(gridLayout);
    sShell.setSize(new Point(326, 82));
    sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter()
    {
      @Override
      public void shellClosed(org.eclipse.swt.events.ShellEvent e)
      {
        sShell.dispose();
      }
    });
    regexInput = new Text(sShell, SWT.BORDER);
    regexInput.setLayoutData(gridData);
    setRegexButton = new Button(sShell, SWT.NONE);
    setRegexButton.setText("Set Method Regex");
    setRegexButton.setLayoutData(gridData1);
    setRegexButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        mainWindowRef.setCallersRegex(regexInput.getText());
        sShell.close();
      }
    });
    cancelButton = new Button(sShell, SWT.NONE);
    cancelButton.setText("Cancel");
    cancelButton.setLayoutData(gridData2);
    cancelButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        sShell.close();
      }
    });
  }

  public void open(TraceWindow instanceWindowRef, String initText)
  {
    createSShell();
    sShell.open();
    regexInput.setText(initText);
    mainWindowRef = instanceWindowRef;
  }
}
