package org.intrace.client.gui.helper;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.intrace.client.gui.helper.InTraceUI.UIMode;

public class IncludeExcludeWindow
{
  public final Shell sWindow;

  private final TabFolder tabs;  
  private final CTabFolder ctabs;
  
  private final PatternInputCallback callback;

  private PatternInput patterns;

  private final SaveCancelButtons saveCancelButtons;

  public IncludeExcludeWindow(String windowTitle, String helpText,
      UIMode mode,
      PatternInputCallback callback, String initIncludePatterns,
      String initExcludePatterns)
  {
    this.callback = callback;
    MigLayout windowLayout = new MigLayout("fill", "[grow][100][100][grow]",
                                           "[grow][25]");
    if (initExcludePatterns == null)
    {
      windowLayout.setRowConstraints("[grow][25]");
    }

    sWindow = new Shell(SWT.APPLICATION_MODAL | SWT.CLOSE | SWT.TITLE
                        | SWT.RESIZE);
    sWindow.setText(windowTitle);
    sWindow.setLayout(windowLayout);
    sWindow.setSize(new Point(400, 400));
    sWindow.setMinimumSize(new Point(400, 400));
    

    if (mode == UIMode.STANDALONE)
    {
      ctabs = null;           
      tabs = new TabFolder(sWindow, SWT.NONE);
      tabs.setLayoutData("grow,wrap,wmin 0,spanx 4");      
      fillTabs(tabs, initIncludePatterns, initExcludePatterns, helpText);
    }
    else
    {
      tabs = null;      
      ctabs = new CTabFolder(sWindow, SWT.TOP | SWT.BORDER);
      ctabs.setSimple(false);
      ctabs.setLayoutData("grow,wrap,wmin 0,spanx 4");
      fillCTabs(ctabs, initIncludePatterns, initExcludePatterns, helpText);
      ctabs.setSelection(0);
    }

    saveCancelButtons = new SaveCancelButtons(sWindow);

  }  

  private void fillTabs(TabFolder tabFolder, String initIncludePatterns, String initExcludePatterns, String helpText)
  {
    TabItem patternsTabItem = new TabItem(tabFolder, SWT.NONE);
    patternsTabItem.setText("Include/Exclude");
    patterns = new PatternInput(tabFolder, initIncludePatterns, initExcludePatterns);        
    patternsTabItem.setControl(patterns.patternInputComp);

    TabItem helpTabItem = new TabItem(tabFolder, SWT.NONE);
    helpTabItem.setText("Help");
    HelpOutputTab helpOutputTab = new HelpOutputTab(tabFolder, helpText);
    helpTabItem.setControl(helpOutputTab.composite);
  }  

  private void fillCTabs(CTabFolder tabFolder, String initIncludePatterns, String initExcludePatterns, String helpText)
  {
    CTabItem patternsTabItem = new CTabItem(tabFolder, SWT.NONE);
    patternsTabItem.setText("Include/Exclude");
    patterns = new PatternInput(tabFolder, initIncludePatterns, initExcludePatterns);        
    patternsTabItem.setControl(patterns.patternInputComp);

    CTabItem helpTabItem = new CTabItem(tabFolder, SWT.NONE);
    helpTabItem.setText("Help");
    HelpOutputTab helpOutputTab = new HelpOutputTab(tabFolder, helpText);
    helpTabItem.setControl(helpOutputTab.composite);
  }
  
  private class HelpOutputTab
  {
    final StyledText textOutput;
    final Composite composite;

    private HelpOutputTab(Composite parent, String helpStr)
    {
      MigLayout windowLayout = new MigLayout("fill,wmin 0,hmin 0",
          "[grow]", "[grow]");

      composite = new Composite(parent, SWT.NONE);
      composite.setLayout(windowLayout);
      
      textOutput = new StyledText(composite, SWT.MULTI | SWT.V_SCROLL
          | SWT.BORDER | SWT.WRAP);
      textOutput.setEditable(false);
      textOutput.setLayoutData("spanx,grow,wmin 0,hmin 0");
      textOutput.setBackground(Display.getCurrent().getSystemColor(
          SWT.COLOR_WHITE));
      
      textOutput.setText(helpStr);
    }
  }

  private class PatternInput
  {
    private final NewPattern newPattern;
    private final PatternList patternList;
    private final Composite patternInputComp;

    public PatternInput(Composite parent, 
                        String initIncludePatterns, 
                        String initExcludePatterns)
    {
      patternInputComp = new Composite(parent, SWT.NONE);
      MigLayout inputLayout = new MigLayout("fill", "[grow]", "[25][grow]");
      patternInputComp.setLayout(inputLayout);
      patternInputComp.setLayoutData("grow,wrap,spanx");

      newPattern = new NewPattern(patternInputComp);
      patternList = new PatternList(patternInputComp, 
                                    initIncludePatterns,
                                    initExcludePatterns);
    }

    private class NewPattern
    {
      private final Text patternInput;
      private final Button includeButton;
      private final Button excludeButton;

      private NewPattern(Composite parent)
      {
        Group newPatternGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
        newPatternGroup.setLayoutData("grow,wrap");
        MigLayout newPatternLayout = new MigLayout("fill", "[grow][grow][60][60]", "[][]");
        newPatternGroup.setLayout(newPatternLayout);
        newPatternGroup.setText("New Pattern");

        includeButton = new Button(newPatternGroup, SWT.RADIO);
        includeButton.setText("Include");
        includeButton.setSelection(true);
        
        excludeButton = new Button(newPatternGroup, SWT.RADIO);
        excludeButton.setText("Exclude");
        excludeButton.setLayoutData("wrap");
        
        patternInput = new Text(newPatternGroup, SWT.BORDER);
        patternInput.setLayoutData("grow,spanx 2");

        final Button addButton = new Button(newPatternGroup, SWT.PUSH);
        addButton.setText("Add");
        addButton.setAlignment(SWT.CENTER);
        addButton.setLayoutData("grow");

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
      private static final String INCLUDE_TITLE = "Include:";      
      private static final String EXCLUDE_TITLE = "Exclude:";
      
      private int includeIndex = -1;
      private int excludeIndex = -1;
      
      private final List patternSet;

      private PatternList(Composite parent, 
                          String initIncludePatterns,
                          String initExcludePatterns)
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

        parsePatternSet(true, initIncludePatterns);
        parsePatternSet(false, initExcludePatterns);
      }

      private void parsePatternSet(boolean xiInclude, String initPattern)
      {
        String[] initPatterns = initPattern.split("\\|");
        for (String pattern : initPatterns)
        {
          parsePattern(xiInclude, pattern);
        }
      }

      private void parsePattern(boolean xiInclude, String pattern)
      {
        if ((pattern != null) && 
            (pattern.length() > 0) &&
            (!pattern.equals(TraceFilterThread.MATCH_ALL)))
        {
          addItem(xiInclude, pattern);
        }
      }

      private void addItem()
      {
        String newItem = newPattern.patternInput.getText();
        boolean includeItem = newPattern.includeButton.getSelection();
        addItem(includeItem, newItem);
        newPattern.patternInput.setText("");
      }

      private void addItem(boolean xiInclude, String newItem)
      {
        if (!newItem.equals(TraceFilterThread.MATCH_NONE))
        {
          boolean addItem = true;
          
          // Add headers
          if (xiInclude && (includeIndex == -1))
          {
            patternSet.add(INCLUDE_TITLE, 0);
            includeIndex = 0;
          }
          else if (!xiInclude && (excludeIndex == -1))
          {
            patternSet.add("");
            patternSet.add(EXCLUDE_TITLE);
            excludeIndex = patternSet.getItemCount() - 1;
          }
          
          String[] currentItems = patternSet.getItems();
          int startItem, endItem;
          if (xiInclude)
          {
            startItem = 1;
            if (excludeIndex == -1)
            {
              endItem = currentItems.length;
            }
            else
            {
              endItem = excludeIndex;
            }
          }
          else
          {
            startItem = excludeIndex + 1;
            endItem = currentItems.length;
          }
          
          for (int ii = startItem; ii < endItem; ii++)
          {
            String item = currentItems[ii];
            if (item.equals(newItem))
            {
              addItem = false;
              break;
            }
          }
          
          if (addItem)
          {
            patternSet.add("   " + newItem, endItem);
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

    private String getIncludePattern()
    {
      StringBuffer pattern = new StringBuffer("");
      String patternStr = pattern.toString();
      boolean gotPattern = false;

      String newPatternStr = newPattern.patternInput.getText();
      if (newPatternStr.length() > 0)
      {
        pattern.append(newPatternStr);
        pattern.append("|");
        gotPattern = true;
      }

      String[] items = patternList.patternSet.getItems();
      if (items.length > 0)
      {
        for (String item : items)
        {
          pattern.append(item);
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

    public String getExcludePattern()
    {
      // TODO Auto-generated method stub
      return null;
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
                    String includePattern = patterns.getIncludePattern();
                    if (includePattern.length() == 0)
                    {
                      includePattern = TraceFilterThread.MATCH_ALL;
                    }
                    callback.setIncludePattern(includePattern);
                    
                    callback.setExcludePattern(patterns.getExcludePattern());
                    
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
