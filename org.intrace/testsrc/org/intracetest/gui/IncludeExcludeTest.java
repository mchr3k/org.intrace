package org.intracetest.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.intrace.client.gui.helper.IncludeExcludeWindow.IncludeExcludeHeaders;

import junit.framework.TestCase;

public class IncludeExcludeTest extends TestCase
{
  private List<String> list;
  private IncludeExcludeHeaders headers;

  @Override
  protected void setUp() throws Exception
  {
    list = new ArrayList<String>(); 
    headers = new IncludeExcludeHeaders(new IncludeExcludeHeaders.IncludeExcludeList()
    {      
      @Override
      public void remove(int xiIndex)
      {
        list.remove(xiIndex);
      }
      
      @Override
      public String[] getItems()
      {
        return list.toArray(new String[0]);
      }
      
      @Override
      public void add(String xiItem, int xiIndex)
      {
        list.add(xiIndex, xiItem);
      }
      
      @Override
      public void add(String xiItem)
      {
        list.add(xiItem);
      }
    });
  }
  
  public void testIncludeExclude()
  {
    // Add first include item
    headers.addItem(true, "Foo");
    assertEquals(Arrays.asList("Include:", 
                               "   Foo"), list);
    
    // Add second include item
    headers.addItem(true, "Bar");
    assertEquals(Arrays.asList("Include:", 
                               "   Foo",
                               "   Bar"), list);
    
    // Add first exclude item with include items
    headers.addItem(false, "Goo");
    assertEquals(Arrays.asList("Include:", 
                               "   Foo",
                               "   Bar",
                               "",
                               "Exclude:",
                               "   Goo"), list);
    
    // Add second exclude item with include items
    headers.addItem(false, "Gar");
    assertEquals(Arrays.asList("Include:", 
                               "   Foo",
                               "   Bar",
                               "",
                               "Exclude:",
                               "   Goo",
                               "   Gar"), list);
    
    // Try and add a duplicate item
    headers.addItem(false, "Gar");
    assertEquals(Arrays.asList("Include:", 
                               "   Foo",
                               "   Bar",
                               "",
                               "Exclude:",
                               "   Goo",
                               "   Gar"), list);
    
    // Test returned patterns
    {
      assertEquals(Arrays.asList("Foo","Bar"),headers.getIncludePattern());
      assertEquals(Arrays.asList("Goo","Gar"),headers.getExcludePattern());
    }
    
     // Try and remove headers and spacer
    {
      assertFalse(headers.removeIndex(0));
      assertEquals(Arrays.asList("Include:", 
                                 "   Foo",
                                 "   Bar",
                                 "",
                                 "Exclude:",
                                 "   Goo",
                                 "   Gar"), list);
      assertFalse(headers.removeIndex(3));
      assertEquals(Arrays.asList("Include:", 
                                 "   Foo",
                                 "   Bar",
                                 "",
                                 "Exclude:",
                                 "   Goo",
                                 "   Gar"), list);
      assertFalse(headers.removeIndex(4));
      assertEquals(Arrays.asList("Include:", 
                                 "   Foo",
                                 "   Bar",
                                 "",
                                 "Exclude:",
                                 "   Goo",
                                 "   Gar"), list);
    }
    
    // Remove top include item
    assertTrue(headers.removeIndex(1));
    assertEquals(Arrays.asList("Include:", 
                               "   Bar",
                               "",
                               "Exclude:",
                               "   Goo",
                               "   Gar"), list);
    
    // Remove last include item
    assertTrue(headers.removeIndex(1));
    assertEquals(Arrays.asList("Exclude:",
                               "   Goo",
                               "   Gar"), list);
    
    // Remove top exclude item
    assertTrue(headers.removeIndex(1));
    assertEquals(Arrays.asList("Exclude:",
                               "   Gar"), list);
    
    // Remove last include item
    assertTrue(headers.removeIndex(1));
    assertEquals(Arrays.asList(), list);
    
    // Add back an exclude then an include item
    {
      headers.addItem(false, "Goo");      
      assertEquals(Arrays.asList("Exclude:", 
                                 "   Goo"), list);
      
      headers.addItem(true, "Foo");
      assertEquals(Arrays.asList("Include:", 
                                 "   Foo",
                                 "",
                                 "Exclude:",
                                 "   Goo"), list);
    }
    
    // Remove the last exclude item first, then the last include item
    assertTrue(headers.removeIndex(4));
    assertEquals(Arrays.asList("Include:", 
                               "   Foo"), list);
    
    assertTrue(headers.removeIndex(1));
    assertEquals(Arrays.asList(), list);
  }
}
