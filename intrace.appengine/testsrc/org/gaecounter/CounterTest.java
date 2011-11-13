package org.gaecounter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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
}
