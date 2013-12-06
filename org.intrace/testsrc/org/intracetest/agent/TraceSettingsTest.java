package org.intracetest.agent;

import java.util.Map;

import junit.framework.TestCase;

import org.intrace.agent.AgentSettings;
import org.intrace.output.trace.TraceSettings;
import org.intrace.shared.AgentConfigConstants;
import org.intrace.shared.TraceConfigConstants;

public class TraceSettingsTest extends TestCase
{
  public void testAgentSettings()
  {
    TraceSettings ts = new TraceSettings(
                                               TraceConfigConstants.ARG
                                             + "true"
                                             + TraceConfigConstants.ARRAYS
                                             + "true"
                                             + TraceConfigConstants.ENTRY_EXIT
                                             + "true"
                                             + TraceConfigConstants.BRANCH
                                             + "true"
                                             );
    
    assertTrue(ts.isArgTraceEnabled());
    assertTrue(ts.isEntryExitTraceEnabled());
    assertTrue(ts.isBranchTraceEnabled());
    assertTrue(ts.isTruncateArraysEnabled());

    ts = new TraceSettings(ts);
    assertTrue(ts.isArgTraceEnabled());
    assertTrue(ts.isEntryExitTraceEnabled());
    assertTrue(ts.isBranchTraceEnabled());
    assertTrue(ts.isTruncateArraysEnabled());

    String toString = ts.toString();
    assertNotNull(toString);

    Map<String, String> settingsMap = ts.getSettingsMap();
    assertEquals(settingsMap.get(TraceConfigConstants.ARG), "true");
    assertEquals(settingsMap.get(TraceConfigConstants.ARRAYS), "true");
    assertEquals(settingsMap.get(TraceConfigConstants.BRANCH), "true");
    assertEquals(settingsMap.get(TraceConfigConstants.ENTRY_EXIT), "true");
  }

}
