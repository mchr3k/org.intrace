package org.intrace.client.gui.helper;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PatternInputWindow
{
  public final Shell sWindow;

  private final PatternInputCallback callback;

  private final PatternInput includePatterns;

  private final PatternInput excludePatterns;

  private final SaveCancelButtons saveCancelButtons;

  public PatternInputWindow(String windowTitle, String helpText,
      PatternInputCallback callback, String initIncludePatterns,
      String initExcludePatterns)
  {
    this.callback = callback;
    MigLayout windowLayout = new MigLayout("fill", "[grow][100][100][grow]",
                                           "[grow][grow][25]");
    if (initExcludePatterns == null)
    {
      windowLayout.setRowConstraints("[grow][25]");
    }

    sWindow = new Shell(SWT.APPLICATION_MODAL | SWT.CLOSE | SWT.TITLE
                        | SWT.RESIZE);
    sWindow.setText(windowTitle);
    sWindow.setLayout(windowLayout);
    if (initExcludePatterns != null)
    {
      sWindow.setSize(new Point(400, 600));
      sWindow.setMinimumSize(new Point(400, 600));
    }
    else
    {
      sWindow.setSize(new Point(400, 300));
      sWindow.setMinimumSize(new Point(400, 300));
    }

    includePatterns = new PatternInput(sWindow, "Include", helpText,
                                       initIncludePatterns);
    if (initExcludePatterns != null)
    {
      excludePatterns = new PatternInput(sWindow, "Exclude", helpText,
                                         initExcludePatterns);
    }
    else
    {
      excludePatterns = null;
    }

    saveCancelButtons = new SaveCancelButtons(sWindow);

  }

  private class PatternInput
  {
    private final NewPattern newPattern;
    private final PatternList patternList;

    public PatternInput(Composite parent, String patternText, String helpText,
        String initPatterns)
    {
      Group patternInputGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
      MigLayout connGroupLayout = new MigLayout("fill", "[grow]", "[25][grow]");
      patternInputGroup.setLayout(connGroupLayout);
      patternInputGroup.setText(patternText);
      patternInputGroup.setLayoutData("grow,wrap,spanx");

      newPattern = new NewPattern(patternInputGroup, helpText);
      patternList = new PatternList(patternInputGroup, initPatterns);
    }

    private class NewPattern
    {
      private final Text patternInput;

      private NewPattern(Composite parent, String helpText)
      {
        Group newPatternGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
        newPatternGroup.setLayoutData("grow,wrap");
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

        addButton
                 .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
                 {
                   @Override
                   public void widgetSelected(SelectionEvent arg0)
                   {
                     patternList.addItem();
                   }
                 });
        removeButton
                    .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
                    {
                      @Override
                      public void widgetSelected(SelectionEvent arg0)
                      {
                        patternList.removeItem();
                      }
                    });
        patternInput
                    .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
                    {
                      @Override
                      public void widgetDefaultSelected(SelectionEvent arg0)
                      {
                        patternList.addItem();
                        saveCancelButtons.saveButton.setFocus();
                      }
                    });
      }
    }

    private class PatternList
    {
      private final List patternSet;

      private PatternList(Composite parent, String initPatterns)
      {
        Group patternGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
        patternGroup.setLayoutData("grow");
        MigLayout patternLayout = new MigLayout("fill");
        patternGroup.setLayout(patternLayout);
        patternGroup.setText("Patterns (Double click to Edit)");

        patternSet = new List(patternGroup, SWT.BORDER);
        patternSet.setLayoutData("grow");

        patternSet.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
        {
          @Override
          public void mouseDoubleClick(MouseEvent mouseevent)
          {
            editItem();
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
        String newItem = newPattern.patternInput.getText();
        addItem(newItem);
        newPattern.patternInput.setText("");
      }

      private void addItem(String newItem)
      {
        if (!newItem.equals("")
            && !newItem.equals(TraceFilterThread.MATCH_NONE.pattern()))
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
        else if (newPattern.patternInput.getText().length() > 0)
        {
          newPattern.patternInput.setText("");
        }
      }

      private void editItem()
      {
        int[] selectedItems = patternSet.getSelectionIndices();

        if (selectedItems.length == 1)
        {
          newPattern.patternInput.setText(patternSet.getItem(selectedItems[0]));
          patternSet.remove(selectedItems[0]);
          newPattern.patternInput.setFocus();
          newPattern.patternInput
                                 .setSelection(newPattern.patternInput
                                                                      .getText()
                                                                      .length());
        }
      }
    }

    private String getPattern()
    {
      StringBuffer pattern = new StringBuffer("");
      String patternStr = pattern.toString();
      boolean gotPattern = false;

      String newPatternStr = newPattern.patternInput.getText();
      if (newPatternStr.length() > 0)
      {
        pattern.append(newPatternStr.replace(".", "\\.").replace("*", ".*"));
        pattern.append("|");
        gotPattern = true;
      }

      String[] items = patternList.patternSet.getItems();
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

      return patternStr;
    }
  }

  private class SaveCancelButtons
  {
    final Button saveButton;

    private SaveCancelButtons(Composite sParent)
    {
      saveButton = new Button(sParent, SWT.PUSH);
      saveButton.setText("Apply Changes");
      saveButton.setAlignment(SWT.CENTER);
      saveButton.setLayoutData("skip,grow");

      Button discardButton = new Button(sParent, SWT.PUSH);
      discardButton.setText("Discard Changes");
      discardButton.setAlignment(SWT.CENTER);
      discardButton.setLayoutData("grow");

      saveButton
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
                {
                  @Override
                  public void widgetSelected(SelectionEvent arg0)
                  {
                    callback.setIncludePattern(includePatterns.getPattern());
                    if (excludePatterns != null)
                    {
                      callback.setExcludePattern(excludePatterns.getPattern());
                    }
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
    }
  }

  public static interface PatternInputCallback
  {
    public void setIncludePattern(String newIncludePattern);

    public void setExcludePattern(String newExcludePattern);
  }
}
