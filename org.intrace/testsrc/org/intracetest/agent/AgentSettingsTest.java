package org.intracetest.agent;

import java.util.Map;
import org.intrace.agent.InstrCriteria;

import junit.framework.TestCase;

import org.intrace.agent.AgentSettings;
import org.intrace.shared.AgentConfigConstants;

public class AgentSettingsTest extends TestCase
{
        private String M1 = "org.intracetest.agent.ArgumentTypes#boolArrayArrayArg({{Z)V";
        private String M2 = "org.intracetest.agent.ArgumentTypes#objArrayArg({Ljava/lang/Object;)V";
        private String M3 = "org.intracetest.agent.ArgumentTypes#doubleArg(D)V";

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

    assertTrue( "foo".equals(as.getClassRegex()[0]) || "foo".equals(as.getClassRegex()[1]) );
    assertTrue( "bar".equals(as.getClassRegex()[0]) || "bar".equals(as.getClassRegex()[1]) );
    assertTrue( as.getClassesToInclude().allMethodsSpecified("foo") );
    assertFalse( as.getClassesToInclude().allMethodsSpecified("unknownClass"));

    assertEquals(as.isInstrumentationEnabled(), true);
    assertEquals(as.saveTracedClassfiles(), true);
    assertEquals(as.isVerboseMode(), true);

    as = new AgentSettings(as);
    assertNotNull(as.getClassRegex());
    assertEquals(as.getClassRegex().length, 2);
    assertTrue( "foo".equals(as.getClassRegex()[0]) || "foo".equals(as.getClassRegex()[1]) );
    assertTrue( "bar".equals(as.getClassRegex()[0]) || "bar".equals(as.getClassRegex()[1]) );

    assertTrue( as.getClassesToInclude().allMethodsSpecified("foo") );
    assertFalse( as.getClassesToInclude().allMethodsSpecified("unknownClass"));

    assertEquals(as.isInstrumentationEnabled(), true);
    assertEquals(as.saveTracedClassfiles(), true);
    assertEquals(as.isVerboseMode(), true);

    String toString = as.toString();
    assertNotNull(toString);

    Map<String, String> settingsMap = as.getSettingsMap();

    assertTrue(	settingsMap.get(AgentConfigConstants.CLASS_REGEX).equals("foo|bar") || 
		settingsMap.get(AgentConfigConstants.CLASS_REGEX).equals("bar|foo") );

    assertEquals(settingsMap.get(AgentConfigConstants.INSTRU_ENABLED), "true");
    assertEquals(settingsMap.get(AgentConfigConstants.SAVE_TRACED_CLASSFILES),
                 "true");
    assertEquals(settingsMap.get(AgentConfigConstants.VERBOSE_MODE), "true");
  }

  public void testInstrCriteria() {
	InstrCriteria ic = new InstrCriteria("foo|bar");

	assertTrue(ic.allMethodsSpecified("foo") );  //This is criteria for 'foo' class, no methods here.  When no methods were specified, then instrument all methods.
	assertTrue(ic.allMethodsSpecified("bar") );  //This is criteria for the 'bar' class; no methods here.  When no methods were specified, then instrument all methods.
	assertFalse(ic.allMethodsSpecified("doesnotexist") );  //If class name was never specified, then no methods should be instrumented.

   	assertTrue("foo".equals(ic.getClassRegex()[0]) || "foo".equals(ic.getClassRegex()[1]) );
   	assertTrue("bar".equals(ic.getClassRegex()[0]) || "bar".equals(ic.getClassRegex()[1]) );

   assertEquals("Added one class with a single 'instrument all', but count isn't right",
                1,
                ic.methodCountPerClass("foo")
	);

   assertEquals("Added one class with a single 'instrument all', but count isn't right",
                1,
                ic.methodCountPerClass("bar")
        );

	assertTrue("toString in InstrCriteria is not working", ic.toString().equals("foo|bar") || ic.toString().equals("bar|foo") );
  }

  public void testIfWeCanParseMethodCriteria() {
	InstrCriteria ic2 = new InstrCriteria("org.hsqldb.jdbc.JDBCConnection#onStartEscapeSequence(Ljava/lang/String;Ljava/lang/StringBuffer;I)I");
	assertFalse( ic2.allMethodsSpecified("org.hsqldb.jdbc.JDBCConnection") );

	assertTrue( ic2.thisMethodSpecified("org.hsqldb.jdbc.JDBCConnection", "onStartEscapeSequence", "(Ljava/lang/String;Ljava/lang/StringBuffer;I)I"));
	assertFalse( ic2.thisMethodSpecified("Xorg.hsqldb.jdbc.JDBCConnection", "onStartEscapeSequence", "(Ljava/lang/String;Ljava/lang/StringBuffer;I)I"));
	assertFalse( ic2.thisMethodSpecified("org.hsqldb.jdbc.JDBCConnection", "XonStartEscapeSequence", "(Ljava/lang/String;Ljava/lang/StringBuffer;I)I"));
	assertFalse( ic2.thisMethodSpecified("org.hsqldb.jdbc.JDBCConnection", "onStartEscapeSequence", "X(Ljava/lang/String;Ljava/lang/StringBuffer;I)I"));
  }
  public void testIfWeCanParseMix() {

        InstrCriteria ic2 = new InstrCriteria("org.hsqldb.jdbc.JDBCConnection#onStartEscapeSequence(Ljava/lang/String;Ljava/lang/StringBuffer;I)I|foo");
        assertFalse( ic2.allMethodsSpecified("org.hsqldb.jdbc.JDBCConnection") );
  
        assertTrue( ic2.thisMethodSpecified("org.hsqldb.jdbc.JDBCConnection", "onStartEscapeSequence", "(Ljava/lang/String;Ljava/lang/StringBuffer;I)I"));
        assertFalse( ic2.thisMethodSpecified("Xorg.hsqldb.jdbc.JDBCConnection", "onStartEscapeSequence", "(Ljava/lang/String;Ljava/lang/StringBuffer;I)I"));
        assertFalse( ic2.thisMethodSpecified("org.hsqldb.jdbc.JDBCConnection", "XonStartEscapeSequence", "(Ljava/lang/String;Ljava/lang/StringBuffer;I)I"));
        assertFalse( ic2.thisMethodSpecified("org.hsqldb.jdbc.JDBCConnection", "onStartEscapeSequence", "X(Ljava/lang/String;Ljava/lang/StringBuffer;I)I"));

       assertTrue(ic2.allMethodsSpecified("foo") );  //If not methods were specified, then instrument all methods.
        assertFalse(ic2.allMethodsSpecified("doesnotexist") );  //If class name was never specified, then no methods should be instrumented.

        //assertEquals(ic2.getClassRegex()[0], "foo");
	assertTrue("foo".equals(ic2.getClassRegex()[0]) || "foo".equals(ic2.getClassRegex()[1]) );
	assertTrue("org.hsqldb.jdbc.JDBCConnection".equals(ic2.getClassRegex()[0]) || "org.hsqldb.jdbc.JDBCConnection".equals(ic2.getClassRegex()[1]) );



  }
    public void testIfWeCanParseThreeComplicatedMethods() {
	String myCriteria = M1+"|"+M2+"|"+M3;
	//System.out.println("@@@@@@@Start of testIfWeCanParseThreeComplicatedMethods() ");
	InstrCriteria ic = new InstrCriteria(myCriteria);
	//System.out.println("@@@@@@@@@@@@@@Before assert for testIfWeCanParseThreeComplicatedMethods() ");
	assertEquals("Added three methods for a single class, but didn't find the right count of methods",
		3,
		ic.methodCountPerClass("org.intracetest.agent.ArgumentTypes")
	);
	//System.out.println("@@@@@@@@@@@@@@  AFTER assert for testIfWeCanParseThreeComplicatedMethods() ");
    }
    public void testMethodsFromDifferentClasses() {
	InstrCriteria ic = new InstrCriteria("ArgumentTypes#byteArrayArg({B)V|OtherTypes#byteArrayArg({B)V");
	assertFalse( ic.allMethodsSpecified("ArgumentTypes") );
	assertFalse( ic.allMethodsSpecified("OtherTypes") );

	String[] myClasses = ic.getClassRegex();
	assertTrue( "ArgumentTypes".equals(myClasses[0]) || "ArgumentTypes".equals(myClasses[1]) );
	assertTrue( "OtherTypes".equals(myClasses[0]) || "OtherTypes".equals(myClasses[1]) );

	assertTrue( ic.thisMethodSpecified("ArgumentTypes","byteArrayArg", "({B)V") );
	assertTrue( ic.thisMethodSpecified("OtherTypes", "byteArrayArg", "({B)V") );

	assertTrue(	   ic.toString().equals("ArgumentTypes#byteArrayArg({B)V|OtherTypes#byteArrayArg({B)V")
			|| ic.toString().equals("OtherTypes#byteArrayArg({B)V|ArgumentTypes#byteArrayArg({B)V") );
	
    }
    public void testMethodsFromSameClasses() {
        InstrCriteria ic = new InstrCriteria("MyTypes#bar({B)V|MyTypes#foo({B)V");
        assertFalse( ic.allMethodsSpecified("MyTypes") );

        assertTrue( ic.thisMethodSpecified("MyTypes","foo", "({B)V") );
        assertTrue( ic.thisMethodSpecified("MyTypes", "bar", "({B)V") );
	
	assertTrue( 	   ic.toString().equals("MyTypes#bar({B)V|MyTypes#foo({B)V")
			|| ic.toString().equals("MyTypes#foo({B)V|MyTypes#bar({B)V") );


    }

}
