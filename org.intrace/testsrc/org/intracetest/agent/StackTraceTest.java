package org.intracetest.agent;

import java.util.Map;

import junit.framework.TestCase;

import org.intrace.agent.AgentSettings;
import org.intrace.output.trace.TraceHandler;
import org.intrace.output.trace.TraceSettings;
import org.intrace.shared.AgentConfigConstants;
import org.intrace.shared.TraceConfigConstants;

public class StackTraceTest extends TestCase
{
  private static final String MY_PACKAGE_AND_CLASS = "org.intracetest.agent.StackTraceTest";
  private String m_stackTrace;
  /** Here is the raw stack trace that we want to validate:
   * <PRE>
org.intracetest.agent.StackTraceTest.c(StackTraceTest.java:57),org.intracetest.agent.StackTraceTest.b(StackTraceTest.java:54),org.intracetest.agent.StackTraceTest.a(StackTraceTest.java:51),org.intracetest.agent.StackTraceTest.testStackTrace(StackTraceTest.java:27),sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method),sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57),sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43),java.lang.reflect.Method.invoke(Method.java:601),junit.framework.TestCase.runTest(TestCase.java:168),junit.framework.TestCase.runBare(TestCase.java:134),junit.framework.TestResult$1.protect(TestResult.java:110),junit.framework.TestResult.runProtected(TestResult.java:128),junit.framework.TestResult.run(TestResult.java:113),junit.framework.TestCase.run(TestCase.java:124),junit.framework.TestSuite.runTest(TestSuite.java:243),junit.framework.TestSuite.run(TestSuite.java:238),org.junit.internal.runners.JUnit38ClassRunner.run(JUnit38ClassRunner.java:83),org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:50),org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38),org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:467),org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:683),org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:390),org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:197)
   * </PRE>
   * Among other things, this test validates that the following are removed from the stack trace:
   * <ul>
   * 	<li>java.lang.Thread.getStackTrace</li>
   * 	<li>all org.intrace. activity</li>
   * </ul>
   *  
   */
public void testStackTrace()
  {
	  a();
	  String[] parts = m_stackTrace.split(",");
	  //System.out.println(m_stackTrace);
	  //JUnit has a thick call stack -- when I checked, there were 23 stack trace elements.
	  //Application developers only care about the ones on the very top.
	  assertTrue("Didn't not find the right number of StackTraceElements", parts.length > 4);
	  validateStackTraceElement("org.intracetest.agent.StackTraceTest.c", parts[0]);
	  validateStackTraceElement("org.intracetest.agent.StackTraceTest.b", parts[1]);
	  validateStackTraceElement("org.intracetest.agent.StackTraceTest.a", parts[2]);
	  validateStackTraceElement("org.intracetest.agent.StackTraceTest.testStackTrace", parts[3]);
	  //validateStackTraceElement("sun.reflect.NativeMethodAccessorImpl.invoke0", parts[4]);
  }
	private void validateStackTraceElement(String expectedPackageAndClassAndMethod, String actual) {
		String[] partsOfStackTraceElement = actual.split("[\\(:\\)]");
		assertEquals("The package and class and method name were not found in the right place", 
				expectedPackageAndClassAndMethod, 
				partsOfStackTraceElement[0]);
		
		assertEquals("source file not found in the right place", 
				"StackTraceTest.java", 
				partsOfStackTraceElement[1]);
		
	}
private void a() {
	  b();
  }
  private void b() {
	  c();
  }
  private void c() {
	  m_stackTrace = TraceHandler.INSTANCE.getStackTrace();
  }

}
