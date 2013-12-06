package org.intracetest.agent;

import java.io.IOException;

import junit.framework.TestCase;

import org.intrace.shared.Base64;
/**

This test proves out the following API using text data from an intrace Event

* <code>String encoded = Base64.encode( myByteArray );</code>
 * <br />
 * <code>byte[] myByteArray = Base64.decode( encoded );</code>

<pre>

				Plain ASCII		 Base64		Base64+GZIP
				-----------------------------------------
Tiny		   |	 70			   96			 116
               |
Small		   |	248			  332			 256
               |
Big     	   |   2306		     3076		  	 904
               |
Huge    	   |  12591		    16788		  	1036


</pre>
	How small can we compress the InTrace event text for various sized InTrace events?
	<ul>
		<li>Base64 without GZIP was detrimental to small compression size in all cases</li>
		<li>The 70 and 248 byte events were slightly larger with both Base64 and Base64+GZIP -- bad but a not horrible show stopper.</li>
		<li>The Big event was more than 2x as small with Base64+GZIP -- good.</li>
		<li>The Huge event was more than 10x as small with Base64+GZIP -- great.</li>
		<li> </li>
	</ul>
	
	Conclusions:  
	Will not use Base64 alone -- doesn't provide any compression with these samples.
	Base64+GZIP doesn't hurt much with smaller event
	


  */
public class CompressionTest extends TestCase
{
  	private static final String ORIGINAL_TINY_INTRACE_EVENT = "[18:07:53.683]:[67]:org.hsqldb.jdbc.jdbcConnection:prepareStatement: {";
  	private static final String ORIGINAL_SMALL_INTRACE_EVENT = "[18:07:53.683]:[67]:org.hsqldb.jdbc.jdbcConnection:prepareStatement" +
							": Return: org.hsqldb.jdbc.jdbcPreparedStatement@4b8efa2f[sql=" +
							"[INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)]" + 
							", parameters=[[null], [null], [null], [null]]]";
  	/**
  	 * This isn't a real event (yet)...just adding some text to assess compression for larger messages.
  	 */
  	private static final String ORIGINAL_LARGE_STACK_TRACE = "[18:07:53.683]:[67]:org.hsqldb.jdbc.jdbcConnection:prepareStatement" +
			": Return: org.hsqldb.jdbc.jdbcPreparedStatement@4b8efa2f[sql=" +
			"[INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)]" + 
			", parameters=[[null], [null], [null], [null]]]" +
			"~java.lang.Thread.getStackTrace(Thread.java:1567) example.webapp.servlet.HelloWorld.doGet(HelloWorld.java:38) javax.servlet.http.HttpServlet.service(HttpServlet.java:668) javax.servlet.http.HttpServlet.service(HttpServlet.java:770) org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:669) org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:455) org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137) org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:560) org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231) org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1072) org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:382) org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193) org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1006) org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135) org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116) org.eclipse.jetty.server.Server.handle(Server.java:365) org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:485) org.eclipse.jetty.server.BlockingHttpConnection.handleRequest(BlockingHttpConnection.java:53) org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:926) org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:988) org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:635) org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235) org.eclipse.jetty.server.BlockingHttpConnection.handle(BlockingHttpConnection.java:72) org.eclipse.jetty.server.bio.SocketConnector$ConnectorEndPoint.run(SocketConnector.java:264) org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608) org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543) java.lang.Thread.run(Thread.java:722) ";
  	
	/**
  	 * This isn't a real event at all...just adding some text to assess compression for msg with more bytes.
  	 */
  	private static final String ORIGINAL_HUGE_STACK_TRACE = "[18:07:53.683]:[67]:org.hsqldb.jdbc.jdbcConnection:prepareStatement" +
			": Return: org.hsqldb.jdbc.jdbcPreparedStatement@4b8efa2f[sql=" +
			"[INSERT INTO Event (name, description, date, location) VALUES(?, ?, ?, ?)]" + 
			", parameters=[[null], [null], [null], [null]]]" +
			"~java.lang.Thread.getStackTrace(Thread.java:1567) example.webapp.servlet.HelloWorld.doGet(HelloWorld.java:38) javax.servlet.http.HttpServlet.service(HttpServlet.java:668) javax.servlet.http.HttpServlet.service(HttpServlet.java:770) org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:669) org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:455) org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137) org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:560) org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231) org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1072) org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:382) org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193) org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1006) org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135) org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116) org.eclipse.jetty.server.Server.handle(Server.java:365) org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:485) org.eclipse.jetty.server.BlockingHttpConnection.handleRequest(BlockingHttpConnection.java:53) org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:926) org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:988) org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:635) org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235) org.eclipse.jetty.server.BlockingHttpConnection.handle(BlockingHttpConnection.java:72) org.eclipse.jetty.server.bio.SocketConnector$ConnectorEndPoint.run(SocketConnector.java:264) org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608) org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543) java.lang.Thread.run(Thread.java:722) "+
			"java.lang.Thread.getStackTrace(Thread.java:1567) example.webapp.servlet.HelloWorld.doGet(HelloWorld.java:38) javax.servlet.http.HttpServlet.service(HttpServlet.java:668) javax.servlet.http.HttpServlet.service(HttpServlet.java:770) org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:669) org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:455) org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137) org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:560) org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231) org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1072) org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:382) org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193) org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1006) org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135) org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116) org.eclipse.jetty.server.Server.handle(Server.java:365) org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:485) org.eclipse.jetty.server.BlockingHttpConnection.handleRequest(BlockingHttpConnection.java:53) org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:926) org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:988) org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:635) org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235) org.eclipse.jetty.server.BlockingHttpConnection.handle(BlockingHttpConnection.java:72) org.eclipse.jetty.server.bio.SocketConnector$ConnectorEndPoint.run(SocketConnector.java:264) org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608) org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543) java.lang.Thread.run(Thread.java:722) "+
			"java.lang.Thread.getStackTrace(Thread.java:1567) example.webapp.servlet.HelloWorld.doGet(HelloWorld.java:38) javax.servlet.http.HttpServlet.service(HttpServlet.java:668) javax.servlet.http.HttpServlet.service(HttpServlet.java:770) org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:669) org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:455) org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137) org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:560) org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231) org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1072) org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:382) org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193) org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1006) org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135) org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116) org.eclipse.jetty.server.Server.handle(Server.java:365) org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:485) org.eclipse.jetty.server.BlockingHttpConnection.handleRequest(BlockingHttpConnection.java:53) org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:926) org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:988) org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:635) org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235) org.eclipse.jetty.server.BlockingHttpConnection.handle(BlockingHttpConnection.java:72) org.eclipse.jetty.server.bio.SocketConnector$ConnectorEndPoint.run(SocketConnector.java:264) org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608) org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543) java.lang.Thread.run(Thread.java:722) "+
			"java.lang.Thread.getStackTrace(Thread.java:1567) example.webapp.servlet.HelloWorld.doGet(HelloWorld.java:38) javax.servlet.http.HttpServlet.service(HttpServlet.java:668) javax.servlet.http.HttpServlet.service(HttpServlet.java:770) org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:669) org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:455) org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137) org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:560) org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231) org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1072) org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:382) org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193) org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1006) org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135) org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116) org.eclipse.jetty.server.Server.handle(Server.java:365) org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:485) org.eclipse.jetty.server.BlockingHttpConnection.handleRequest(BlockingHttpConnection.java:53) org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:926) org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:988) org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:635) org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235) org.eclipse.jetty.server.BlockingHttpConnection.handle(BlockingHttpConnection.java:72) org.eclipse.jetty.server.bio.SocketConnector$ConnectorEndPoint.run(SocketConnector.java:264) org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608) org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543) java.lang.Thread.run(Thread.java:722) "+
			"java.lang.Thread.getStackTrace(Thread.java:1567) example.webapp.servlet.HelloWorld.doGet(HelloWorld.java:38) javax.servlet.http.HttpServlet.service(HttpServlet.java:668) javax.servlet.http.HttpServlet.service(HttpServlet.java:770) org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:669) org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:455) org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137) org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:560) org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231) org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1072) org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:382) org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193) org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1006) org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135) org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116) org.eclipse.jetty.server.Server.handle(Server.java:365) org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:485) org.eclipse.jetty.server.BlockingHttpConnection.handleRequest(BlockingHttpConnection.java:53) org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:926) org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:988) org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:635) org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235) org.eclipse.jetty.server.BlockingHttpConnection.handle(BlockingHttpConnection.java:72) org.eclipse.jetty.server.bio.SocketConnector$ConnectorEndPoint.run(SocketConnector.java:264) org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608) org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543) java.lang.Thread.run(Thread.java:722) "+
			"java.lang.Thread.getStackTrace(Thread.java:1567) example.webapp.servlet.HelloWorld.doGet(HelloWorld.java:38) javax.servlet.http.HttpServlet.service(HttpServlet.java:668) javax.servlet.http.HttpServlet.service(HttpServlet.java:770) org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:669) org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:455) org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137) org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:560) org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231) org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1072) org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:382) org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193) org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1006) org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135) org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116) org.eclipse.jetty.server.Server.handle(Server.java:365) org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:485) org.eclipse.jetty.server.BlockingHttpConnection.handleRequest(BlockingHttpConnection.java:53) org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:926) org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:988) org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:635) org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235) org.eclipse.jetty.server.BlockingHttpConnection.handle(BlockingHttpConnection.java:72) org.eclipse.jetty.server.bio.SocketConnector$ConnectorEndPoint.run(SocketConnector.java:264) org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608) org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543) java.lang.Thread.run(Thread.java:722) ";
  	
  
  public void testBasicCompressionAndDecompression_smallerWithGZip() throws IOException
  {
//	System.out.println("Original length of short data [" + ORIGINAL_SMALL_INTRACE_EVENT.length() + "]");
//	System.out.println("Original length of long data [" + ORIGINAL_LARGE_STACK_TRACE.length() + "]");
    byte[] eventData = ORIGINAL_SMALL_INTRACE_EVENT.getBytes();
    String compressedString = Base64.encodeBytes(eventData, Base64.GZIP);
    byte[] reconstitutedData = Base64.decode(compressedString, Base64.GZIP);
    String reconstitutedString = new String(reconstitutedData);
    
    assertEquals("Size of original text", 248, ORIGINAL_SMALL_INTRACE_EVENT.length() );
    assertEquals("Size of compressed text", 256, compressedString.length() );
//    System.out.println("Compressed data [" + compressedString + "]");
//    System.out.println("Compressed with GZIP length - small data  [" + compressedString.length() + "]");
    assertEquals("Unable to compress and decompress intrace event data", ORIGINAL_SMALL_INTRACE_EVENT, reconstitutedString);
  }
  public void testBasicCompressionAndDecompression_largerWithGZip() throws IOException
  {
    byte[] eventData = ORIGINAL_LARGE_STACK_TRACE.getBytes();
    String compressedData = Base64.encodeBytes(eventData, Base64.GZIP);
    //String compressedData = Base64.encodeBytes(eventData, Base64.NO_OPTIONS);
    byte[] reconstitutedData = Base64.decode(compressedData, Base64.GZIP);
    String reconstitutedString = new String(reconstitutedData);

    assertEquals("Size of original text", 2306, ORIGINAL_LARGE_STACK_TRACE.length() );
    assertEquals("Size of compressed text", 904, compressedData.length() );
   
//    System.out.println("Compressed data [" + compressedData + "]");
//    System.out.println("Compressed with GZIP data - long data [" + compressedData.length() + "]");
    assertEquals("Unable to compress and decompress intrace event data", ORIGINAL_LARGE_STACK_TRACE, reconstitutedString);
  }
  public void testBasicCompressionAndDecompression_smallerWithoutGZip() throws IOException
  {
    byte[] eventData = ORIGINAL_SMALL_INTRACE_EVENT.getBytes();
    String compressedString = Base64.encodeBytes(eventData, Base64.NO_OPTIONS);
    byte[] reconstitutedData = Base64.decode(compressedString, Base64.NO_OPTIONS);
    String reconstitutedString = new String(reconstitutedData);

    
    assertEquals("Size of original text", 248, ORIGINAL_SMALL_INTRACE_EVENT.length() );
    assertEquals("Size of compressed text", 332, compressedString.length() );
//    System.out.println("Compressed data [" + compressedString + "]");
//    System.out.println("Compressed without gzip data - small data [" + compressedString.length() + "]");
    assertEquals("Unable to compress and decompress intrace event data", ORIGINAL_SMALL_INTRACE_EVENT, reconstitutedString);
  }
  public void testBasicCompressionAndDecompression_largerWithoutGZip() throws IOException
  {
    byte[] eventData = ORIGINAL_LARGE_STACK_TRACE.getBytes();
    String compressedData = Base64.encodeBytes(eventData, Base64.NO_OPTIONS);
    //String compressedData = Base64.encodeBytes(eventData, Base64.NO_OPTIONS);
    byte[] reconstitutedData = Base64.decode(compressedData, Base64.NO_OPTIONS);
    String reconstitutedString = new String(reconstitutedData);

    assertEquals("Size of original text", 2306, ORIGINAL_LARGE_STACK_TRACE.length() );
    assertEquals("Size of compressed text", 3076, compressedData.length() );
   
//    System.out.println("Compressed data [" + compressedData + "]");
//    System.out.println("Compressed without gzip  - large data [" + compressedData.length() + "]");
    assertEquals("Unable to compress and decompress intrace event data", ORIGINAL_LARGE_STACK_TRACE, reconstitutedString);
  }

  public void testBasicCompressionAndDecompression_tinyWithGZip() throws IOException
  {
    byte[] eventData = ORIGINAL_TINY_INTRACE_EVENT.getBytes();
    String compressedString = Base64.encodeBytes(eventData, Base64.GZIP);
    byte[] reconstitutedData = Base64.decode(compressedString, Base64.GZIP);
    String reconstitutedString = new String(reconstitutedData);

    
    assertEquals("Size of original text", 70, ORIGINAL_TINY_INTRACE_EVENT.length() );
    assertEquals("Size of compressed text", 116, compressedString.length() );
    assertEquals("Unable to compress and decompress intrace event data", ORIGINAL_TINY_INTRACE_EVENT, reconstitutedString);
  }
  public void testBasicCompressionAndDecompression_tinyWithoutGZip() throws IOException
  {
    byte[] eventData = ORIGINAL_TINY_INTRACE_EVENT.getBytes();
    String compressedString = Base64.encodeBytes(eventData, Base64.NO_OPTIONS);
    byte[] reconstitutedData = Base64.decode(compressedString, Base64.NO_OPTIONS);
    String reconstitutedString = new String(reconstitutedData);

    
    assertEquals("Size of original text", 70, ORIGINAL_TINY_INTRACE_EVENT.length() );
    assertEquals("Size of compressed text", 96, compressedString.length() );
    assertEquals("Unable to compress and decompress intrace event data", ORIGINAL_TINY_INTRACE_EVENT, reconstitutedString);
  }
  public void testBasicCompressionAndDecompression_hugeWithGZip() throws IOException
  {
    byte[] eventData = ORIGINAL_HUGE_STACK_TRACE.getBytes();
    String compressedString = Base64.encodeBytes(eventData, Base64.GZIP);
    byte[] reconstitutedData = Base64.decode(compressedString, Base64.GZIP);
    String reconstitutedString = new String(reconstitutedData);

    
    assertEquals("Size of original text", 12591, ORIGINAL_HUGE_STACK_TRACE.length() );
    assertEquals("Size of compressed text", 1036, compressedString.length() );
    assertEquals("Unable to compress and decompress intrace event data", ORIGINAL_HUGE_STACK_TRACE, reconstitutedString);
  }
  public void testBasicCompressionAndDecompression_hugeWithoutGZip() throws IOException
  {
    byte[] eventData = ORIGINAL_HUGE_STACK_TRACE.getBytes();
    String compressedString = Base64.encodeBytes(eventData, Base64.NO_OPTIONS);
    byte[] reconstitutedData = Base64.decode(compressedString, Base64.NO_OPTIONS);
    String reconstitutedString = new String(reconstitutedData);

    
    assertEquals("Size of original text", 12591, ORIGINAL_HUGE_STACK_TRACE.length() );
    assertEquals("Size of compressed text", 16788, compressedString.length() );
    assertEquals("Unable to compress and decompress intrace event data", ORIGINAL_HUGE_STACK_TRACE, reconstitutedString);
  }
}
