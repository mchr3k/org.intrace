package org.intrace.client.gui;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class DevTraceWindow
{
  private Shell sWindow = null;

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    DevTraceWindow window = new DevTraceWindow();
    window.open();
  }

  private void open()
  {
    createWindow();
    sWindow.open();
    Display display = Display.getDefault();
    while (!sWindow.isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }

  private void createWindow()
  {
    MigLayout windowLayout = new MigLayout("fill", // Fill all the space
        "",
        "[100][grow]"); // 2 Rows

    sWindow = new Shell();
    sWindow.setText("Trace Window");
    sWindow.setLayout(windowLayout);
    sWindow.setSize(new Point(700, 750));
    sWindow.setMinimumSize(new Point(700, 750));
    
    TabFolder buttonTabs = new TabFolder(sWindow, SWT.NONE);
    TabFolder outputTabs = new TabFolder(sWindow, SWT.NONE);
    buttonTabs.setLayoutData("grow,wrap");
    outputTabs.setLayoutData("grow");
    
    fillButtonTabs(buttonTabs);
    fillOutputTabs(outputTabs);
  }

  private void fillButtonTabs(TabFolder tabFolder)
  {
    TabItem connTab = new TabItem(tabFolder, SWT.NONE);
    connTab.setText("Connection");
    
    fillConnectionTab(tabFolder, connTab); 
  }

  private void fillConnectionTab(TabFolder tabFolder, TabItem connTab)
  {
    MigLayout windowLayout = new MigLayout("fill"); // 2 Rows
    
    Composite composite = new Composite(tabFolder, SWT.NONE);
    composite.setLayout(windowLayout);    
    connTab.setControl(composite);
    
    Button connect = new Button(composite, SWT.LEFT);
    connect.setText(ClientStrings.CONNECT);
    connect.setLayoutData("spany,grow");
    
    Button foo = new Button(composite, SWT.LEFT);
    foo.setText("Foo");
    foo.setLayoutData("wrap,grow");
    
    Button bar = new Button(composite, SWT.LEFT);
    bar.setText("Bar");
    bar.setLayoutData("grow");
  }

  private void fillOutputTabs(TabFolder tabFolder)
  {
    TabItem connTab = new TabItem(tabFolder, SWT.NONE);
    connTab.setText("Output");

    Text statusTextArea = new Text(tabFolder, 
                                   SWT.MULTI | SWT.WRAP | 
                                   SWT.V_SCROLL | SWT.BORDER);
    statusTextArea.setEditable(false);
    statusTextArea.setBackground(Display.getCurrent().getSystemColor(
                                                      SWT.COLOR_WHITE));

    connTab.setControl(statusTextArea);

  }
}
