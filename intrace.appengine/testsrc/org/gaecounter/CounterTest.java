package org.gaecounter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gaecounter.data.Counter;
import org.gaecounter.data.Counter.Type;
import org.junit.Test;

public class CounterTest
{
  @Test
  public void testMapping()
  {
    List<Counter> lTestData = new ArrayList<Counter>();
    lTestData.add(new Counter(Type.DAY,
                  Type.DAY.getPartialStr(2010, 1, 1),
                  "test.wav"));
    lTestData.add(new Counter(Type.DAY,
                  Type.DAY.getPartialStr(2011, 2, 2),
                  "test.wav"));
    lTestData.add(new Counter(Type.DAY,
                  Type.DAY.getPartialStr(2011, 2, 2),
                  "foo.wav"));

    Map<String, Map<String, Integer>> map = Counter.getPerFilePerDateMap(lTestData);
    assertEquals(2, map.keySet().size());
    assertTrue(map.keySet().toString(), map.keySet().contains("test.wav"));
    assertTrue(map.keySet().toString(), map.keySet().contains("foo.wav"));
    Map<String, Integer> lData = map.get("test.wav");
    assertEquals(2, lData.keySet().size());
    assertTrue(lData.keySet().toString(), lData.keySet().contains("2010/01/01"));
    assertTrue(lData.keySet().toString(), lData.keySet().contains("2011/02/02"));
  }

  @Test
  public void testDateStrs()
  {
    Type t = Type.DAY;
    Set<String> lDateStrs = new HashSet<String>();
    lDateStrs.add(t.getPartialStr(2010, 12, 18));
    lDateStrs.add(t.getPartialStr(2010, 12, 16));

    List<String> dateStrs = Counter.getDateStrs(Type.DAY, Type.DAY.getPartial(2010, 12, 20), lDateStrs);
    assertEquals(6, dateStrs.size());
    assertEquals("2010/12/15", dateStrs.get(0));
    assertEquals("2010/12/16", dateStrs.get(1));
    assertEquals("2010/12/17", dateStrs.get(2));
    assertEquals("2010/12/18", dateStrs.get(3));
    assertEquals("2010/12/19", dateStrs.get(4));
    assertEquals("2010/12/20", dateStrs.get(5));
  }

  @Test
  public void testWeekConversion()
  {
    {
      // Prepare data
      Map<String,Map<String,Integer>> lData = new HashMap<String, Map<String,Integer>>();
      Map<String,Integer> lDayData = new HashMap<String, Integer>();
      lData.put("testfile", lDayData);
      lDayData.put("2011/12/09", 1);
      lDayData.put("2011/12/10", 1);
      lDayData.put("2011/12/11", 1);
      lDayData.put("2011/12/12", 1); // Monday
      lDayData.put("2011/12/13", 1);
      lDayData.put("2011/12/14", 1);
      lDayData.put("2011/12/15", 1);
      lDayData.put("2011/12/16", 1);
      lDayData.put("2011/12/17", 1);
      lDayData.put("2011/12/18", 1);
      lDayData.put("2011/12/19", 1); // Monday
      lDayData.put("2011/12/20", 1);

      // Test method
      Map<String, Map<String, Integer>> lResult = Counter.getFilePerWeekMap(lData);
      Map<String, Integer> lWeekData = lResult.get("testfile");
      assertEquals(3,lWeekData.size());
      assertEquals(3,(int)lWeekData.get("2011/12/05"));
      assertEquals(7,(int)lWeekData.get("2011/12/12"));
      assertEquals(2,(int)lWeekData.get("2011/12/19"));
    }

    {
      // Prepare data
      Map<String,Map<String,Integer>> lData = new HashMap<String, Map<String,Integer>>();
      Map<String,Integer> lDayData = new HashMap<String, Integer>();
      lData.put("testfile", lDayData);
      lDayData.put("2011/12/09", 1);
      lDayData.put("2011/12/10", 1);
      //lDayData.put("2011/12/12", 1); // Monday
      //lDayData.put("2011/12/19", 1); // Monday
      lDayData.put("2011/12/20", 1);

      // Test method
      Map<String, Map<String, Integer>> lResult = Counter.getFilePerWeekMap(lData);
      Map<String, Integer> lWeekData = lResult.get("testfile");
      assertEquals(2,lWeekData.size());
      assertEquals(2,(int)lWeekData.get("2011/12/05"));
      assertEquals(1,(int)lWeekData.get("2011/12/19"));
    }
  }
}
