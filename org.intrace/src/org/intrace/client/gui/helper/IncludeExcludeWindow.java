package org.intrace.client.gui.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
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

  private final Pattern allowedStrings;

  public IncludeExcludeWindow(String windowTitle, String helpText,
      UIMode mode,
      PatternInputCallback callback, 
      java.util.List<String> initIncludePatterns,
      java.util.List<String> initExcludePatterns,
      Pattern allowedStrings)
  {
    this.callback = callback;
    this.allowedStrings = allowedStrings;
    MigLayout windowLayout = new MigLayout("fill", "[grow][100][100][grow]",
                                           "[grow][25]");
    if (initExcludePatterns == null)
    {
      windowLayout.setRowConstraints("[grow][25]");
    }

    sWindow = new Shell(SWT.APPLICATION_MODAL | SWT.CLOSE | SWT.TITLE
                        | SWT.RESIZE);
    
    try
    {
      Display display = sWindow.getDisplay();
      Image[] icons = InTraceUI.getIcons(display);
      sWindow.setImages(icons);
    }
    catch (IOException ex)
    {
      // Ignore
    }
    
    sWindow.setText(windowTitle);
    sWindow.setLayout(windowLayout);
    sWindow.setSize(new Point(400, 400));
    sWindow.setMinimumSize(new Point(400, 400));
    sWindow.addListener(SWT.Traverse, new Listener() {
      public void handleEvent(Event event) {
        switch (event.detail) {
        case SWT.TRAVERSE_ESCAPE:
          sWindow.close();
          event.detail = SWT.TRAVERSE_NONE;
          event.doit = false;
          break;
        }
      }
    });

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
    
    // Focus on text input
    patterns.newPattern.patternInput.setFocus();
  }  

  private void fillTabs(TabFolder tabFolder, 
      java.util.List<String> initIncludePatterns, 
      java.util.List<String> initExcludePatterns, String helpText)
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

  private void fillCTabs(CTabFolder tabFolder, 
      java.util.List<String> initIncludePatterns, 
      java.util.List<String> initExcludePatterns, String helpText)
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

    private int includeIndex = -1;
    private int excludeIndex = -1;    
    
    public PatternInput(Composite parent, 
        java.util.List<String> initIncludePatterns, 
        java.util.List<String> initExcludePatterns)
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
        
        patternInput.addVerifyListener(new VerifyListener()
        {          
          @Override
          public void verifyText(VerifyEvent event)
          {
            String str = event.text;
            if  (str.length() > 0)
            {
              String existingStr = newPattern.patternInput.getText();
              String newStr = existingStr + str;
              event.doit = allowedStrings.matcher(newStr).matches();
            }
          }
        });
        
        sWindow.addListener(SWT.Traverse, new Listener() {
          public void handleEvent(Event event) {
            switch (event.detail) {
            case SWT.TRAVERSE_ESCAPE:
              sWindow.close();
              event.detail = SWT.TRAVERSE_NONE;
              event.doit = false;
              break;
            }
          }
        });
      }
    }

    private class PatternList
    {
      private static final String INCLUDE_TITLE = "Include:";      
      private static final String EXCLUDE_TITLE = "Exclude:";
      
      private final List patternSet;

      private PatternList(Composite parent, 
          java.util.List<String> initIncludePatterns,
          java.util.List<String> initExcludePatterns)
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

      private void parsePatternSet(boolean xiInclude, 
                    java.util.List<String> initPattern)
      {
        for (String pattern : initPattern)
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
            endItem = excludeIndex - 1;
          }
        }
        else
        {
          startItem = excludeIndex + 1;
          endItem = currentItems.length;
        }
        
        for (int ii = startItem; ii < endItem; ii++)
        {
          String item = currentItems[ii].trim();
          if (item.equals(newItem))
          {
            addItem = false;
            break;
          }
        }
        
        if (addItem)
        {
          patternSet.add("   " + newItem, endItem);
          
          if ((excludeIndex != -1) && (endItem < excludeIndex))
          {
            excludeIndex++;
          }
        }
      }

      private void removeItem()
      {
        int[] selectedItems = patternSet.getSelectionIndices();

        if (selectedItems.length == 1)
        {
          int selectedItem = selectedItems[0];
          if ((selectedItem == includeIndex) ||
              (selectedItem == excludeIndex) ||
              (selectedItem == (excludeIndex - 1)))
          {
            // Ignore
          }
          else
          {
            patternSet.remove(selectedItem);
            removeIndex(selectedItem);
          }
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
          int selectedItem = selectedItems[0];
          if ((selectedItem == includeIndex) ||
              (selectedItem == excludeIndex) ||
              (selectedItem == (excludeIndex - 1)))
          {
            // Ignore
          }
          else
          {
            newPattern.patternInput.setText(patternSet.getItem(selectedItem).trim());
            patternSet.remove(selectedItem);
            removeIndex(selectedItem);
            newPattern.patternInput.setFocus();
            newPattern.patternInput
                                   .setSelection(newPattern.patternInput
                                                                        .getText()
                                                                        .length());
          }
        }
      }
      
      private void removeIndex(int index)
      {
        int count = patternSet.getItemCount();
        
        if ((excludeIndex != -1) && (index < excludeIndex))
        {
          // Removed an item above the exclude header
          excludeIndex--;
        }
        
        if ((includeIndex != -1) && 
            ((excludeIndex == 2) || (count == 1)))
        {
          // We have the include: header and no include
          // items. We detect this either by the position
          // of the exclude header or the overall count
          // of items
          patternSet.remove(includeIndex);
          includeIndex = -1;          
        }
        
        if ((excludeIndex != -1) && 
            (excludeIndex == (count - 1)))
        {
          // We have the exclude: header and the index
          // of the last entry is the same as the index
          // of the exclude header
          patternSet.remove(excludeIndex);
          excludeIndex = -1;
        }
      }
    }

    private java.util.List<String> getIncludePattern()
    {
      java.util.List<String> includes = new ArrayList<String>();

      if (includeIndex != -1)
      {
        String[] items = patternList.patternSet.getItems();
        int endIndex = patternList.patternSet.getItemCount();
        if (excludeIndex != -1)
        {
          endIndex = excludeIndex - 1;
        }
        for (int ii = 1; ii < endIndex; ii++)
        {
          String item = items[ii].trim();
          includes.add(item);
        }
      }
      
      if (newPattern.includeButton.getSelection())
      {
        String newPatternStr = newPattern.patternInput.getText();
        if (newPatternStr.length() > 0)
        {
          includes.add(newPatternStr);
        }
      }

      return includes;
    }

    private java.util.List<String> getExcludePattern()
    {
      java.util.List<String> excludes = new ArrayList<String>();

      if (excludeIndex != -1)
      {
        String[] items = patternList.patternSet.getItems();
        int endIndex = patternList.patternSet.getItemCount();
        for (int ii = (excludeIndex + 1); ii < endIndex; ii++)
        {
          String item = items[ii].trim();
          excludes.add(item);
        }
      }
      
      if (newPattern.excludeButton.getSelection())
      {
        String newPatternStr = newPattern.patternInput.getText();
        if (newPatternStr.length() > 0)
        {
          excludes.add(newPatternStr);
        }
      }

      return excludes;
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
                    callback.setIncludePattern(patterns.getIncludePattern());                    
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
    public void setIncludePattern(java.util.List<String> newIncludePattern);

    public void setExcludePattern(java.util.List<String> newExcludePattern);
  }
}
