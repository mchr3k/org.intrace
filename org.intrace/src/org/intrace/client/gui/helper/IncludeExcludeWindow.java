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
import org.eclipse.swt.graphics.Color;
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
import org.intrace.client.gui.helper.InTraceUI.UIModeData;

public class IncludeExcludeWindow
{
  public final Shell sWindow;

  private final TabFolder tabs;
  private final CTabFolder ctabs;

  private final PatternInputCallback callback;

  private PatternInput patterns;

  private final SaveCancelButtons saveCancelButtons;

  private final Pattern allowedStrings;

  private final UIModeData modedata;

  public IncludeExcludeWindow(String windowTitle, String helpText,
      UIMode mode,
      UIModeData modedata,
      PatternInputCallback callback,
      java.util.List<String> initIncludePatterns,
      java.util.List<String> initExcludePatterns,
      Pattern allowedStrings)
  {
    this.modedata = modedata;
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
      @Override
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
      tabs.setLayoutData("grow,wrap,wmin 0,hmin 0,spanx 4");
      fillTabs(tabs, initIncludePatterns, initExcludePatterns, helpText);
    }
    else
    {
      tabs = null;
      ctabs = new CTabFolder(sWindow, SWT.TOP | SWT.BORDER);
      ctabs.setSimple(false);
      ctabs.setLayoutData("grow,wrap,wmin 0,hmin 0,spanx 4");
      fillCTabs(ctabs, initIncludePatterns, initExcludePatterns, helpText);
      ctabs.setSelection(0);
      if (this.modedata != null)
      {
        ctabs.setSelectionBackground(
            new Color[]{this.modedata.activeColorOne,
                        this.modedata.activeColorTwo},
                        new int[]{100}, true);
      }
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

    private IncludeExcludeHeaders model;

    public PatternInput(Composite parent,
        java.util.List<String> initIncludePatterns,
        java.util.List<String> initExcludePatterns)
    {
      patternInputComp = new Composite(parent, SWT.NONE);
      MigLayout inputLayout = new MigLayout("fill", "[grow]", "[25][grow]");
      patternInputComp.setLayout(inputLayout);
      patternInputComp.setLayoutData("grow,wrap,spanx,hmin 0");

      newPattern = new NewPattern(patternInputComp);
      patternList = new PatternList(patternInputComp);

      model = new IncludeExcludeHeaders(new IncludeExcludeHeaders.IncludeExcludeList()
      {
        @Override
        public void remove(int xiIndex)
        {
          patternList.patternSet.remove(xiIndex);
        }

        @Override
        public String[] getItems()
        {
          return patternList.patternSet.getItems();
        }

        @Override
        public void add(String xiItem, int xiIndex)
        {
          patternList.patternSet.add(xiItem, xiIndex);
        }

        @Override
        public void add(String xiItem)
        {
          patternList.patternSet.add(xiItem);
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
        model.addItem(xiInclude, pattern);
      }
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

              if (newStr.contains("|") &&
                  !allowedStrings.matcher("|").matches())
              {
                String[] parts = newStr.split("\\|");
                boolean allowed = true;
                for (String part : parts)
                {
                  allowed = allowed && allowedStrings.matcher(part).matches();
                }
                event.doit = allowed;
              }
              else
              {
                event.doit = allowedStrings.matcher(newStr).matches();
              }
            }
          }
        });

        sWindow.addListener(SWT.Traverse, new Listener() {
          @Override
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
      private final List patternSet;

      private PatternList(Composite parent)
      {
        Group patternGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
        patternGroup.setLayoutData("grow,hmin 0");
        MigLayout patternLayout = new MigLayout("fill");
        patternGroup.setLayout(patternLayout);
        patternGroup.setText("Patterns (Double click to Edit)");

        patternSet = new List(patternGroup, SWT.BORDER | SWT.V_SCROLL);
        patternSet.setLayoutData("grow,hmin 0");

        patternSet.addMouseListener(new org.eclipse.swt.events.MouseAdapter()
        {
          @Override
          public void mouseDoubleClick(MouseEvent mouseevent)
          {
            editItem();
          }
        });
      }

      private void addItem()
      {
        String newItem = newPattern.patternInput.getText();
        if (newItem.length() > 0)
        {
          boolean includeItem = newPattern.includeButton.getSelection();

          if (newItem.contains("|") &&
              !allowedStrings.matcher("|").matches())
          {
            String[] parts = newItem.split("\\|");

            for (String part : parts)
            {
              if (part.length() > 0)
              {
                model.addItem(includeItem, part);
              }
            }
            newPattern.patternInput.setText("");
          }
          else
          {
            model.addItem(includeItem, newItem);
            newPattern.patternInput.setText("");
          }
        }
      }

      private void removeItem()
      {
        int[] selectedItems = patternSet.getSelectionIndices();

        if (selectedItems.length == 1)
        {
          int selectedItem = selectedItems[0];
          model.removeIndex(selectedItem);
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
          String item = patternSet.getItem(selectedItem).trim();
          if (model.removeIndex(selectedItem))
          {
            newPattern.patternInput.setText(item);
            newPattern.patternInput.setFocus();
            newPattern.patternInput
                                   .setSelection(newPattern.patternInput
                                                                        .getText()
                                                                          .length());
          }
        }
      }
    }

    private java.util.List<String> getIncludePattern()
    {
      java.util.List<String> includes = new ArrayList<String>();

      includes.addAll(model.getIncludePattern());

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

      excludes.addAll(model.getExcludePattern());

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

  /**
   * Class which managed the presence of include/exclude headers
   * within a list of items.
   */
  public static class IncludeExcludeHeaders
  {
    public static interface IncludeExcludeList
    {
      public void add(String xiItem);
      public void add(String xiItem, int xiIndex);
      public void remove(int xiIndex);
      public String[] getItems();
    }

    private static final String INCLUDE_TITLE = "Include:";
    private static final String EXCLUDE_TITLE = "Exclude:";

    private int includeIndex = -1;
    private int excludeIndex = -1;

    private final IncludeExcludeList list;

    public IncludeExcludeHeaders(IncludeExcludeList view)
    {
      this.list = view;
    }

    public void addItem(boolean xiInclude, String newItem)
    {
      boolean addItem = true;

      // Add headers
      if (xiInclude && (includeIndex == -1))
      {
        list.add(INCLUDE_TITLE, 0);
        includeIndex = 0;
        if (excludeIndex > -1)
        {
          // Include header was already added
          excludeIndex++;

          // Add spacer
          list.add("", 1);
          excludeIndex++;
        }
      }
      else if (!xiInclude && (excludeIndex == -1))
      {
        if (includeIndex > -1)
        {
          list.add("");
        }
        list.add(EXCLUDE_TITLE);
        excludeIndex = list.getItems().length - 1;
      }

      String[] currentItems = list.getItems();
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
        list.add("   " + newItem, endItem);

        if ((excludeIndex != -1) && (endItem < excludeIndex))
        {
          excludeIndex++;
        }
      }
    }

    public boolean removeIndex(int index)
    {
      if ((index == includeIndex) ||
          (index == excludeIndex) ||
          (index == (excludeIndex - 1)))
      {
        // Ignore
        return false;
      }

      // Remove item
      list.remove(index);

      // Check if we removed an included item when we already
      // had an excluded item
      int count = list.getItems().length;
      if ((excludeIndex != -1) && (index < excludeIndex))
      {
        // Removed an item above the exclude header
        excludeIndex--;
      }

      if ((includeIndex != -1) &&
          ((excludeIndex == 2) || (count == 1)))
      {
        // We need to remove the include header
        // as we have no include items left
        list.remove(includeIndex);
        includeIndex = -1;

        if (excludeIndex > -1)
        {
          // We need to remove the spacer above
          // the exclude header

          // Include element was removed
          excludeIndex--;

          // Remove spacer element
          list.remove(0);
          excludeIndex--;
        }
      }

      if ((excludeIndex != -1) &&
          (excludeIndex == (count - 1)))
      {
        // We need to remove the exclude header
        // as we have no exclude items left
        if (includeIndex != -1)
        {
          // We need to remove the spacer above
          // the exclude header
          list.remove(excludeIndex - 1);
          excludeIndex--;
        }

        list.remove(excludeIndex);
        excludeIndex = -1;
      }
      return true;
    }

    public java.util.List<String> getIncludePattern()
    {
      java.util.List<String> includes = new ArrayList<String>();

      if (includeIndex != -1)
      {
        String[] items = list.getItems();
        int endIndex = items.length;
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

      return includes;
    }

    public java.util.List<String> getExcludePattern()
    {
      java.util.List<String> excludes = new ArrayList<String>();

      if (excludeIndex != -1)
      {
        String[] items = list.getItems();
        int endIndex = items.length;
        for (int ii = (excludeIndex + 1); ii < endIndex; ii++)
        {
          String item = items[ii].trim();
          excludes.add(item);
        }
      }

      return excludes;
    }
  }
}
