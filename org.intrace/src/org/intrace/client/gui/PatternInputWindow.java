package org.intrace.client.gui;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PatternInputWindow
{

  public final Shell sWindow;
  private final Text patternInput;
  private final List patternSet;
  private final PatternInputCallback callback;

  public PatternInputWindow(String windowTitle, String helpText,
      PatternInputCallback callback, String initPatterns)
  {
    this.callback = callback;
    MigLayout windowLayout = new MigLayout("fill", "[grow][100][100][grow]",
                                           "[25][grow][25]");

    sWindow = new Shell(SWT.APPLICATION_MODAL | SWT.CLOSE | SWT.TITLE
                        | SWT.RESIZE);
    sWindow.setText(windowTitle);
    sWindow.setLayout(windowLayout);
    sWindow.setSize(new Point(400, 300));
    sWindow.setMinimumSize(new Point(400, 300));

    Group newPatternGroup = new Group(sWindow, SWT.SHADOW_ETCHED_IN);
    newPatternGroup.setLayoutData("grow,wrap,spanx");
    MigLayout newPatternLayout = new MigLayout("fill", "[grow][60]");
    newPatternGroup.setLayout(newPatternLayout);
    newPatternGroup.setText("New Pattern");

    patternInput = new Text(newPatternGroup, SWT.BORDER);
    patternInput.setLayoutData("grow");

    final Button addButton = new Button(newPatternGroup, SWT.PUSH);
    addButton.setText("Add");
    addButton.setAlignment(SWT.CENTER);
    addButton.setLayoutData("grow");
    addButton.setToolTipText("Help: " + helpText);

    Button removeButton = new Button(newPatternGroup, SWT.PUSH);
    removeButton.setText("Remove");
    removeButton.setAlignment(SWT.CENTER);
    removeButton.setLayoutData("grow,wrap");

    Group patternGroup = new Group(sWindow, SWT.SHADOW_ETCHED_IN);
    patternGroup.setLayoutData("grow,spanx,wrap");
    MigLayout patternLayout = new MigLayout("fill");
    patternGroup.setLayout(patternLayout);
    patternGroup.setText("Patterns (Double click to Edit)");

    patternSet = new List(patternGroup, SWT.BORDER);
    patternSet.setLayoutData("grow");

    final Button saveButton = new Button(sWindow, SWT.PUSH);
    saveButton.setText("Apply Changes");
    saveButton.setAlignment(SWT.CENTER);
    saveButton.setLayoutData("skip,grow");

    Button discardButton = new Button(sWindow, SWT.PUSH);
    discardButton.setText("Discard Changes");
    discardButton.setAlignment(SWT.CENTER);
    discardButton.setLayoutData("grow");

    patternInput
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
                {
                  @Override
                  public void widgetDefaultSelected(SelectionEvent arg0)
                  {
                    addItem();
                    saveButton.setFocus();
                  }
                });

    addButton
             .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
             {
               @Override
               public void widgetSelected(SelectionEvent arg0)
               {
                 addItem();
               }
             });
    removeButton
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
                {
                  @Override
                  public void widgetSelected(SelectionEvent arg0)
                  {
                    removeItem();
                  }
                });
    patternSet.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
    {
      @Override
      public void mouseDoubleClick(MouseEvent mouseevent)
      {
        editItem();
      }
    });
    saveButton
              .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
              {
                @Override
                public void widgetSelected(SelectionEvent arg0)
                {
                  savePattern();
                  sWindow.close();
                }
              });
    discardButton
                 .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
                 {
                   @Override
                   public void widgetSelected(SelectionEvent arg0)
                   {
                     sWindow.close();
                   }
                 });

    parsePatternSet(initPatterns);
  }

  private void parsePatternSet(String initPattern)
  {
    if (initPattern.indexOf("|") > -1)
    {
      String[] initPatterns = initPattern.split("\\|");
      for (String pattern : initPatterns)
      {
        parsePattern(pattern);
      }
    }
    else
    {
      parsePattern(initPattern);
    }
  }

  private void parsePattern(String pattern)
  {
    if ((pattern != null) && (pattern.length() > 0))
    {
      pattern = pattern.replace(".*", "*").replace("\\.", ".");
      addItem(pattern);
    }
  }

  private void addItem()
  {
    String newItem = patternInput.getText();
    addItem(newItem);
    patternInput.setText("");
  }

  private void addItem(String newItem)
  {
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
    }
  }

  private void removeItem()
  {
    int[] selectedItems = patternSet.getSelectionIndices();

    if (selectedItems.length == 1)
    {
      patternSet.remove(selectedItems[0]);
    }
    else if (patternInput.getText().length() > 0)
    {
      patternInput.setText("");
    }
  }

  private void editItem()
  {
    int[] selectedItems = patternSet.getSelectionIndices();

    if (selectedItems.length == 1)
    {
      patternInput.setText(patternSet.getItem(selectedItems[0]));
      patternSet.remove(selectedItems[0]);
      patternInput.setFocus();
      patternInput.setSelection(patternInput.getText().length());
    }
  }

  private void savePattern()
  {
    StringBuffer pattern = new StringBuffer("");
    String patternStr = pattern.toString();
    boolean gotPattern = false;

    String newPatternStr = patternInput.getText();
    if (newPatternStr.length() > 0)
    {
      pattern.append(newPatternStr.replace(".", "\\.").replace("*", ".*"));
      pattern.append("|");
      gotPattern = true;
    }

    String[] items = patternSet.getItems();
    if (items.length > 0)
    {
      for (String item : items)
      {
        pattern.append(item.replace(".", "\\.").replace("*", ".*"));
        pattern.append("|");
      }
      gotPattern = true;
    }

    if (gotPattern)
    {
      patternStr = pattern.toString();
      patternStr = patternStr.substring(0, patternStr.length() - 1);
    }

    callback.setPattern(patternStr);
  }

  public static interface PatternInputCallback
  {
    public void setPattern(String newPattern);
  }
}
