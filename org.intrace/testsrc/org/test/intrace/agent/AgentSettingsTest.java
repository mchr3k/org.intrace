package org.test.intrace.agent;

import java.util.Map;

import junit.framework.TestCase;

import org.intrace.agent.AgentSettings;
import org.intrace.shared.AgentConfigConstants;

public class AgentSettingsTest extends TestCase
{
  public void testAgentSettings()
  {
    AgentSettings as = new AgentSettings(
                                         AgentConfigConstants.ALLOW_JARS_TO_BE_TRACED
                                             + "true"
                                             + AgentConfigConstants.CLASS_REGEX
                                             + ".*"
                                             + AgentConfigConstants.INSTRU_ENABLED
                                             + "true"
                                             + AgentConfigConstants.SAVE_TRACED_CLASSFILES
                                             + "true"
                                             + AgentConfigConstants.VERBOSE_MODE
                                             + "true");
    assertEquals(as.allowJarsToBeTraced(), true);
    assertEquals(as.getClassRegex().pattern(), ".*");
    assertEquals(as.isInstrumentationEnabled(), true);
    assertEquals(as.saveTracedClassfiles(), true);
    assertEquals(as.isVerboseMode(), true);

    as = new AgentSettings(as);
    assertEquals(as.allowJarsToBeTraced(), true);
    assertEquals(as.getClassRegex().pattern(), ".*");
    assertEquals(as.isInstrumentationEnabled(), true);
    assertEquals(as.saveTracedClassfiles(), true);
    assertEquals(as.isVerboseMode(), true);

    String toString = as.toString();
    assertNotNull(toString);

    Map<String, String> settingsMap = as.getSettingsMap();
    assertEquals(settingsMap.get(AgentConfigConstants.ALLOW_JARS_TO_BE_TRACED),
                 "true");
    assertEquals(settingsMap.get(AgentConfigConstants.CLASS_REGEX), ".*");
    assertEquals(settingsMap.get(AgentConfigConstants.INSTRU_ENABLED), "true");
    assertEquals(settingsMap.get(AgentConfigConstants.SAVE_TRACED_CLASSFILES),
                 "true");
    assertEquals(settingsMap.get(AgentConfigConstants.VERBOSE_MODE), "true");
  }

}
