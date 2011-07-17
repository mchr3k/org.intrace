package org.intracetest.agent;

import java.util.Map;

import junit.framework.TestCase;

import org.intrace.agent.AgentSettings;
import org.intrace.shared.AgentConfigConstants;

public class AgentSettingsTest extends TestCase
{
  public void testAgentSettings()
  {
    AgentSettings as = new AgentSettings(
                                         AgentConfigConstants.CLASS_REGEX
                                             + "foo|bar"
                                             + AgentConfigConstants.INSTRU_ENABLED
                                             + "true"
                                             + AgentConfigConstants.SAVE_TRACED_CLASSFILES
                                             + "true"
                                             + AgentConfigConstants.VERBOSE_MODE
                                             + "true");
    assertNotNull(as.getClassRegex());
    assertEquals(as.getClassRegex().length, 2);
    assertEquals(as.getClassRegex()[0], "foo");
    assertEquals(as.getClassRegex()[1], "bar");
    assertEquals(as.isInstrumentationEnabled(), true);
    assertEquals(as.saveTracedClassfiles(), true);
    assertEquals(as.isVerboseMode(), true);

    as = new AgentSettings(as);
    assertNotNull(as.getClassRegex());
    assertEquals(as.getClassRegex().length, 2);
    assertEquals(as.getClassRegex()[0], "foo");
    assertEquals(as.getClassRegex()[1], "bar");
    assertEquals(as.isInstrumentationEnabled(), true);
    assertEquals(as.saveTracedClassfiles(), true);
    assertEquals(as.isVerboseMode(), true);

    String toString = as.toString();
    assertNotNull(toString);

    Map<String, String> settingsMap = as.getSettingsMap();
    assertEquals(settingsMap.get(AgentConfigConstants.CLASS_REGEX), "foo|bar");
    assertEquals(settingsMap.get(AgentConfigConstants.INSTRU_ENABLED), "true");
    assertEquals(settingsMap.get(AgentConfigConstants.SAVE_TRACED_CLASSFILES),
                 "true");
    assertEquals(settingsMap.get(AgentConfigConstants.VERBOSE_MODE), "true");
  }

}
