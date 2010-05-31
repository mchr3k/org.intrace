package org.intrace.client.gui;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PatternInputWindow
{
  /**
   * @param args
   */
  public static void main(String[] args)
  {
    new PatternInputWindow("Test").open();
  }
  
  public void open()
  {
    sWindow.open();
    Display display = Display.getDefault();
    while (!sWindow.isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }
  
  private Shell sWindow = null;
  private final Text patternInput; 
  private final List patternSet;
  
  public PatternInputWindow(String windowTitle)
  {
    MigLayout windowLayout = new MigLayout("fill","[grow][60]","[25][grow]");

    sWindow = new Shell(SWT.APPLICATION_MODAL | SWT.CLOSE | SWT.TITLE | SWT.RESIZE);
    sWindow.setText(windowTitle);
    sWindow.setLayout(windowLayout);
    sWindow.setSize(new Point(400, 300));
    sWindow.setMinimumSize(new Point(400, 300));
    
    patternInput = new Text(sWindow, SWT.BORDER);
    patternInput.setLayoutData("grow");
    
    Button addButton = new Button(sWindow, SWT.PUSH);
    addButton.setText("Add");
    addButton.setAlignment(SWT.CENTER);
    addButton.setLayoutData("grow,wrap");
    
    patternSet = new List(sWindow, SWT.BORDER);
    patternSet.setLayoutData("spanx,grow");
    
    addButton.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e)
      {
        addItem();
      }
    });
  }
  
  private void addItem()
  {
    String newItem = patternInput.getText();
    
    if (!newItem.equals(""))
    {    
      boolean addItem = true;
      for (String item : patternSet.getItems())
      {
        if (item.equals(newItem))
        {
          addItem = false;
          break;
        }
      }
      if (addItem)
      {
        patternSet.add(newItem);
      }
      patternInput.setText("");
    }
  }
}
