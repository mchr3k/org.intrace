package org.intracetest.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.intrace.shared.SerializationHelper;
import org.intrace.output.NetworkDataSenderThread.TraceEventForBatch;
import org.intrace.output.NetworkDataSenderThread;
import org.junit.Before;
import org.junit.Test;

public class SerializationTest {
	
	String[] myData = { "foo", "bar" };
	String sampleEvent = "[00:48:55.797]:[19]:org.hsqldb.jdbc.jdbcStatement:<init>: }~org.hsqldb.jdbc.jdbcStatement.<init>(Unknown Source),org.hsqldb.jdbc.jdbcConnection.createStatement(Unknown Source),org.apache.commons.dbcp.DelegatingConnection.createStatement(DelegatingConnection.java:257),org.apache.commons.dbcp.PoolingDataSource$PoolGuardConnectionWrapper.createStatement(PoolingDataSource.java:216),org.springframework.jdbc.core.JdbcTemplate.execute(JdbcTemplate.java:389),org.springframework.jdbc.core.JdbcTemplate.query(JdbcTemplate.java:455),org.springframework.jdbc.core.JdbcTemplate.query(JdbcTemplate.java:463),org.springframework.jdbc.core.JdbcTemplate.queryForObject(JdbcTemplate.java:471),org.springframework.jdbc.core.JdbcTemplate.queryForObject(JdbcTemplate.java:476),org.springframework.jdbc.core.JdbcTemplate.queryForInt(JdbcTemplate.java:485),example.webapp.dao.JdbcEventDAO.countAll(JdbcEventDAO.java:64),sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method),sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57),sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43),java.lang.reflect.Method.invoke(Method.java:606),org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:309),org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:183),org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:150),org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:110),org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:172),org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:202),com.sun.proxy.$Proxy6.countAll(Unknown Source),example.webapp.servlet.HelloExecuteQuery.doGet(HelloExecuteQuery.java:25),javax.servlet.http.HttpServlet.service(HttpServlet.java:668),javax.servlet.http.HttpServlet.service(HttpServlet.java:770),org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:669),org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:455),org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137),org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:560),org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231),org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1072),org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:382),org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193),org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1006),org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135),org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116),org.eclipse.jetty.server.Server.handle(Server.java:365),org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:485),org.eclipse.jetty.server.BlockingHttpConnection.handleRequest(BlockingHttpConnection.java:53),org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:926),org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:988),org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:635),org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235),org.eclipse.jetty.server.BlockingHttpConnection.handle(BlockingHttpConnection.java:72),org.eclipse.jetty.server.bio.SocketConnector$ConnectorEndPoint.run(SocketConnector.java:264),org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608),org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)";
	@Before
	public void setup() {
		sampleEvents[0] = "[00:48:55.797]:[19]:org.hsqldb.jdbc.jdbcStatement:<init>: }~org.hsqldb.jdbc.jdbcStatement.<init>(Unknown Source),org.hsqldb.jdbc.jdbcConnection.createStatement(Unknown Source),org.apache.commons.dbcp.DelegatingConnection.createStatement(DelegatingConnection.java:257),org.apache.commons.dbcp.PoolingDataSource$PoolGuardConnectionWrapper.createStatement(PoolingDataSource.java:216),org.springframework.jdbc.core.JdbcTemplate.execute(JdbcTemplate.java:389),org.springframework.jdbc.core.JdbcTemplate.query(JdbcTemplate.java:455),org.springframework.jdbc.core.JdbcTemplate.query(JdbcTemplate.java:463),org.springframework.jdbc.core.JdbcTemplate.queryForObject(JdbcTemplate.java:471),org.springframework.jdbc.core.JdbcTemplate.queryForObject(JdbcTemplate.java:476),org.springframework.jdbc.core.JdbcTemplate.queryForInt(JdbcTemplate.java:485),example.webapp.dao.JdbcEventDAO.countAll(JdbcEventDAO.java:64),sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method),sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57),sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43),java.lang.reflect.Method.invoke(Method.java:606),org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:309),org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:183),org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:150),org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:110),org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:172),org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:202),com.sun.proxy.$Proxy6.countAll(Unknown Source),example.webapp.servlet.HelloExecuteQuery.doGet(HelloExecuteQuery.java:25),javax.servlet.http.HttpServlet.service(HttpServlet.java:668),javax.servlet.http.HttpServlet.service(HttpServlet.java:770),org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:669),org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:455),org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137),org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:560),org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231),org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1072),org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:382),org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193),org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1006),org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135),org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116),org.eclipse.jetty.server.Server.handle(Server.java:365),org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:485),org.eclipse.jetty.server.BlockingHttpConnection.handleRequest(BlockingHttpConnection.java:53),org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:926),org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:988),org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:635),org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235),org.eclipse.jetty.server.BlockingHttpConnection.handle(BlockingHttpConnection.java:72),org.eclipse.jetty.server.bio.SocketConnector$ConnectorEndPoint.run(SocketConnector.java:264),org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608),org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)";
		sampleEvents[1] = "[00:48:55.797]:[19]:org.apache.commons.dbcp.DelegatingStatement:<init>: {:61";
		sampleEvents[2] = "[00:48:55.797]:[19]:org.apache.commons.dbcp.DelegatingStatement:<init>: Arg (c): jdbc:hsqldb:mem:event, UserName=SA, HSQL Database Engine Driver";
		sampleEvents[3] = "[00:48:55.797]:[19]:org.apache.commons.dbcp.DelegatingStatement:<init>: Arg (s): org.hsqldb.jdbc.jdbcStatement@2300de71";
		sampleEvents[4] = "[00:48:55.798]:[19]:org.apache.commons.dbcp.DelegatingStatement:<init>: }:64~org.apache.commons.dbcp.DelegatingStatement.<init>(DelegatingStatement.java:64),org.apache.commons.dbcp.DelegatingConnection.createStatement(DelegatingConnection.java:257),org.apache.commons.dbcp.PoolingDataSource$PoolGuardConnectionWrapper.createStatement(PoolingDataSource.java:216),org.springframework.jdbc.core.JdbcTemplate.execute(JdbcTemplate.java:389),org.springframework.jdbc.core.JdbcTemplate.query(JdbcTemplate.java:455),org.springframework.jdbc.core.JdbcTemplate.query(JdbcTemplate.java:463),org.springframework.jdbc.core.JdbcTemplate.queryForObject(JdbcTemplate.java:471),org.springframework.jdbc.core.JdbcTemplate.queryForObject(JdbcTemplate.java:476),org.springframework.jdbc.core.JdbcTemplate.queryForInt(JdbcTemplate.java:485),example.webapp.dao.JdbcEventDAO.countAll(JdbcEventDAO.java:64),sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method),sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57),sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43),java.lang.reflect.Method.invoke(Method.java:606),org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:309),org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:183),org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:150),org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:110),org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:172),org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:202),com.sun.proxy.$Proxy6.countAll(Unknown Source),example.webapp.servlet.HelloExecuteQuery.doGet(HelloExecuteQuery.java:25),javax.servlet.http.HttpServlet.service(HttpServlet.java:668),javax.servlet.http.HttpServlet.service(HttpServlet.java:770),org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:669),org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:455),org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137),org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:560),org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231),org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1072),org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:382),org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193),org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1006),org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135),org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116),org.eclipse.jetty.server.Server.handle(Server.java:365),org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:485),org.eclipse.jetty.server.BlockingHttpConnection.handleRequest(BlockingHttpConnection.java:53),org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:926),org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:988),org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:635),org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235),org.eclipse.jetty.server.BlockingHttpConnection.handle(BlockingHttpConnection.java:72),org.eclipse.jetty.server.bio.SocketConnector$ConnectorEndPoint.run(SocketConnector.java:264),org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608),org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)";
		sampleEvents[5] = "[00:48:55.798]:[19]:org.apache.commons.dbcp.DelegatingStatement:<init>: {:61";
		sampleEvents[6] = "[00:48:55.798]:[19]:org.apache.commons.dbcp.DelegatingStatement:<init>: Arg (c): jdbc:hsqldb:mem:event, UserName=SA, HSQL Database Engine Driver";
		sampleEvents[7] = "[00:48:55.799]:[19]:org.apache.commons.dbcp.DelegatingStatement:<init>: Arg (s): org.hsqldb.jdbc.jdbcStatement@2300de71";
		sampleEvents[8] = "[00:48:55.799]:[19]:org.apache.commons.dbcp.DelegatingStatement:<init>: }:64~org.apache.commons.dbcp.DelegatingStatement.<init>(DelegatingStatement.java:64),org.apache.commons.dbcp.PoolingDataSource$PoolGuardConnectionWrapper.createStatement(PoolingDataSource.java:216),org.springframework.jdbc.core.JdbcTemplate.execute(JdbcTemplate.java:389),org.springframework.jdbc.core.JdbcTemplate.query(JdbcTemplate.java:455),org.springframework.jdbc.core.JdbcTemplate.query(JdbcTemplate.java:463),org.springframework.jdbc.core.JdbcTemplate.queryForObject(JdbcTemplate.java:471),org.springframework.jdbc.core.JdbcTemplate.queryForObject(JdbcTemplate.java:476),org.springframework.jdbc.core.JdbcTemplate.queryForInt(JdbcTemplate.java:485),example.webapp.dao.JdbcEventDAO.countAll(JdbcEventDAO.java:64),sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method),sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57),sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43),java.lang.reflect.Method.invoke(Method.java:606),org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:309),org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:183),org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:150),org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:110),org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:172),org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:202),com.sun.proxy.$Proxy6.countAll(Unknown Source),example.webapp.servlet.HelloExecuteQuery.doGet(HelloExecuteQuery.java:25),javax.servlet.http.HttpServlet.service(HttpServlet.java:668),javax.servlet.http.HttpServlet.service(HttpServlet.java:770),org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:669),org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:455),org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137),org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:560),org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231),org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1072),org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:382),org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193),org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1006),org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135),org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116),org.eclipse.jetty.server.Server.handle(Server.java:365),org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:485),org.eclipse.jetty.server.BlockingHttpConnection.handleRequest(BlockingHttpConnection.java:53),org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:926),org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:988),org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:635),org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235),org.eclipse.jetty.server.BlockingHttpConnection.handle(BlockingHttpConnection.java:72),org.eclipse.jetty.server.bio.SocketConnector$ConnectorEndPoint.run(SocketConnector.java:264),org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608),org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)";
		sampleEvents[9] = "[00:48:55.799]:[19]:org.apache.commons.dbcp.DelegatingStatement:executeQuery: {:206";
	}
	
	@Test
	public void canSerializeAndDeserializeEvents() throws IOException, ClassNotFoundException {
		
		byte[] myBytes = SerializationHelper.toWire(this.myData);
		assertEquals("Didn't find right size of data-on-the-wire.", 75, myBytes.length) ;
		
		String[] surprise = SerializationHelper.fromWire(myBytes);
		
		assertEquals("Could not find 1st element in deserialized array of strings","foo",surprise[0]);
		assertEquals("Could not find 2nd element in deserialized array of strings","bar",surprise[1]);
	}
	/**
	 * These are typical times for serializing/compressing about 12mb of events 
	 * on my 2.3Ghz i7 MacBook 
	 * Serialize time [199]
	 * Deserialize time [171]

	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void canCalculateSizeOfDataSerializedAndCompressed() throws IOException, ClassNotFoundException {
		
		List<String> myEvents = new ArrayList<String>();
		int unCompressedByteCount = 0;
		for(int i = 0; i < 10000;i++) {
			long timestamp = System.currentTimeMillis();
			Long longTimestamp = new Long(timestamp);
			String tmp = 
					longTimestamp.toString()		//Throw in some randomness, more realism for the compression algo.  
					+ sampleEvents [ i % 10  ]
					+ Long.toHexString(2*timestamp) ;
			unCompressedByteCount+=tmp.length();	//Throw in some randomness, more realism for the compression algo.
			
			myEvents.add(tmp);
		}
		assertTrue("Did not find correct total size for 10k uncompressed strings ",unCompressedByteCount > 12000000 );		
		assertTrue("Did not find correct total size for 10k uncompressed strings ",unCompressedByteCount < 12500000 );		

		String[] myEventsArray;// = new String[myEvents.size()];
		myEventsArray = myEvents.toArray(new String[0]);
		assertEquals("Unable to create array from list", 10000,myEventsArray.length);
		
		long start = System.currentTimeMillis();
		byte[] myBytes = SerializationHelper.toWire(myEventsArray);

		long finish = System.currentTimeMillis();
		System.out.println("Serialize time [" + (finish-start) + "]");
		
		assertTrue("Did not find correct size for String array compressed into a byte array",myBytes.length > 80000);
		assertTrue("Did not find correct size for String array compressed into a byte array",myBytes.length < 90000);
		
		start = System.currentTimeMillis();
		String[] surprise = SerializationHelper.fromWire(myBytes);
		finish = System.currentTimeMillis();
		System.out.println("Deserialize time [" + (finish-start) + "]");

	}
	@Test
	public void canSendOneBurstsOfEvents() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream o = new ObjectOutputStream(baos);

		List<TraceEventForBatch> events = new ArrayList<TraceEventForBatch>();
		events.add( new TraceEventForBatch("foo",System.currentTimeMillis(), 1));
		events.add( new TraceEventForBatch("bar",System.currentTimeMillis(), 2));

		NetworkDataSenderThread sender = new NetworkDataSenderThread(null,null);
		int numBursts = sender.transmitBatch( o, events, 10 );	
		assertEquals("Sent just two events with a larger burst size.  should have sent exactly 1 burst.", 1, numBursts);
		//o.close();

		byte[] compressedObjects = baos.toByteArray() ;
		assertTrue( "didn't find enough raw compressed data", compressedObjects.length > 4);//surely the data has to be bigger than 4 bytes, right?

		ByteArrayInputStream bais = new ByteArrayInputStream(compressedObjects);
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object objectFromWire = ois.readObject();
		ois.close();
		if (objectFromWire instanceof byte[]) {
			byte[] freshFromWire = (byte[]) objectFromWire;
			assertEquals("Didn't find size of byte array on wire", 75, freshFromWire.length);

			String results[] = SerializationHelper.fromWire( freshFromWire );
			

			assertEquals( "Didn't find right count of String in the array after sending bursts", 2, results.length );

			assertEquals( "Couldn't receive multiple bursts with just two events", "foo", results[0] );
			assertEquals( "Couldn't receive multiple bursts with just two events", "bar", results[1] );
		} else {
			fail("Didn't serialize right kind of object");
		}
		
			
	}

       @Test
        public void canSendThreeBurstsOfEvents() throws Exception {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream o = new ObjectOutputStream(baos);

                List<TraceEventForBatch> events = new ArrayList<TraceEventForBatch>();
                events.add( new TraceEventForBatch("yankee",System.currentTimeMillis(), 1));
                events.add( new TraceEventForBatch("doodle",System.currentTimeMillis(), 2));
                events.add( new TraceEventForBatch("went",System.currentTimeMillis(), 2));
                events.add( new TraceEventForBatch("to",System.currentTimeMillis(), 2));
                events.add( new TraceEventForBatch("town", System.currentTimeMillis(), 2));
                events.add( new TraceEventForBatch("riding",System.currentTimeMillis(), 2));
                events.add( new TraceEventForBatch("on",System.currentTimeMillis(), 2));
                events.add( new TraceEventForBatch("a",System.currentTimeMillis(), 2));
                events.add( new TraceEventForBatch("pony",System.currentTimeMillis(), 2));

                NetworkDataSenderThread sender = new NetworkDataSenderThread(null,null);
                int numBursts = sender.transmitBatch( o, events, 3 );
                assertEquals("Sent just 9 events with a burst size of 3.  should have sent exactly 3 burst.", 3, numBursts);
                //o.close();

                byte[] compressedObjects = baos.toByteArray() ;
                assertTrue( "didn't find enough raw compressed data", compressedObjects.length > 4);//surely the data has to be bigger than 4 bytes, right?

                ByteArrayInputStream bais = new ByteArrayInputStream(compressedObjects);
                ObjectInputStream ois = new ObjectInputStream(bais);
                Object objectFromWire1 = ois.readObject();
                Object objectFromWire2 = ois.readObject();
                Object objectFromWire3 = ois.readObject();
                ois.close();
                if (objectFromWire1 instanceof byte[]) {
                        byte[] freshFromWire = (byte[]) objectFromWire1;
                        assertEquals("Didn't find size of byte array on wire", 87, freshFromWire.length);

                        String results[] = SerializationHelper.fromWire( freshFromWire );


                        assertEquals( "Didn't find right count of String in the array after sending bursts", 3, results.length );

                        assertEquals( "Couldn't receive multiple bursts with just 9 events", "yankee", results[0] );
                        assertEquals( "Couldn't receive multiple bursts with just 9 events", "doodle", results[1] );
                        assertEquals( "Couldn't receive multiple bursts with just 9 events", "went", results[2] );
                } else {
                        fail("Didn't serialize right kind of object");
                }

                if (objectFromWire2 instanceof byte[]) {
                        byte[] freshFromWire = (byte[]) objectFromWire2;
                        assertEquals("Didn't find size of byte array on wire", 84, freshFromWire.length);

                        String results[] = SerializationHelper.fromWire( freshFromWire );


                        assertEquals( "Didn't find right count of String in the array for the 2nd burst", 3, results.length );

                        assertEquals( "Couldn't receive multiple bursts with just 9 events", "to", results[0] );
                        assertEquals( "Couldn't receive multiple bursts with just 9 events", "town", results[1] );
                        assertEquals( "Couldn't receive multiple bursts with just 9 events", "riding", results[2] );
                } else {
                        fail("Didn't serialize right kind of object");
                }
                if (objectFromWire1 instanceof byte[]) {
                        byte[] freshFromWire = (byte[]) objectFromWire3;
                        assertEquals("Didn't find size of byte array on wire", 80, freshFromWire.length);

                        String results[] = SerializationHelper.fromWire( freshFromWire );


                        assertEquals( "Didn't find right count of String in the array for the 3rd burst", 3, results.length );

                        assertEquals( "Couldn't receive multiple bursts with just 9 events", "on", results[0] );
                        assertEquals( "Couldn't receive multiple bursts with just 9 events", "a", results[1] );
                        assertEquals( "Couldn't receive multiple bursts with just 9 events", "pony", results[2] );
                } else {
                        fail("Didn't serialize right kind of object");
                }



        }


	private String[] sampleEvents = new String[10];

	

}
