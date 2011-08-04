package org.intracetest.gui;

import java.util.ArrayList;
import java.util.List;

import org.intrace.client.gui.helper.TraceFilterThread;
import org.intrace.client.gui.helper.TraceFilterThread.FilterCallback;
import org.intrace.client.gui.helper.TraceFilterThread.TimeSource;
import org.intrace.client.gui.helper.TraceFilterThread.TraceFilterProgressHandler;
import org.intrace.client.gui.helper.TraceFilterThread.TraceTextHandler;

import junit.framework.TestCase;

public class TraceFilterThreadTest extends TestCase
{
  public void testFilterThread() throws InterruptedException
  {
    final String[] setTextHolder = new String[1];
    final String[] appendTextHolder = new String[1];
    final int[] displayedHolder = new int[1];
    final int[] totalHolder = new int[1];
    TraceTextHandler textHandler = new TraceTextHandler()
    {      
      @Override
      public void setText(String traceText)
      {
        setTextHolder[0] = traceText;
      }
      
      @Override
      public void setStatus(int displayed, int total)
      {
        displayedHolder[0] = displayed;
        totalHolder[0] = total;
      }
      
      @Override
      public void appendText(String traceText)
      {
        appendTextHolder[0] = traceText;        
      }
    };
    TraceFilterThread filter = new TraceFilterThread(textHandler);
    final long[] timeSource = new long[1];
    TimeSource time = new TimeSource()
    {
      @Override
      public long currentTimeMillis()
      {
        return timeSource[0];
      }
    };
    filter.time = time;
    FilterCallback cb = new FilterCallback()
    {
      @Override
      public synchronized void callback()
      {
        this.notifyAll();
      }
    };
    filter.cb = cb;
    
    // Add a single line, this is buffered
    synchronized (cb)
    {
      filter.addSystemTraceLine("System line 1");
      cb.wait();
    }    
    assertEquals(0, displayedHolder[0]);
    assertEquals(0, totalHolder[0]);
    assertTrue(appendTextHolder[0], 
               appendTextHolder[0] == null);
    assertTrue(setTextHolder[0], 
               setTextHolder[0] == null);
    
    // Wait for a little bit and write another line    
    appendTextHolder[0] = null;
    setTextHolder[0] = null;
    timeSource[0] += 1000;
    synchronized (cb)
    {
      filter.addSystemTraceLine("System line 2");
      cb.wait();
    }
    timeSource[0] += 1000;
    assertEquals(2, displayedHolder[0]);
    assertEquals(2, totalHolder[0]);
    assertTrue(appendTextHolder[0], 
               appendTextHolder[0].contains("System line"));
    assertTrue(setTextHolder[0], 
               setTextHolder[0] == null);
    
    // Clear trace
    appendTextHolder[0] = null;
    setTextHolder[0] = null;
    synchronized (cb)
    {
      filter.setClearTrace();
      cb.wait();
    }    
    assertEquals(0, displayedHolder[0]);
    assertEquals(0, totalHolder[0]);
    assertTrue(appendTextHolder[0], 
               appendTextHolder[0] == null);
    assertTrue(setTextHolder[0], 
               setTextHolder[0].equals(""));
    
    // Append normal trace line 1, 1b
    appendTextHolder[0] = null;
    setTextHolder[0] = null;
    synchronized (cb)
    {
      filter.addTraceLine("Line 1");
      cb.wait();
    }    
    synchronized (cb)
    {
      filter.addTraceLine("Line 1b");
      cb.wait();
    }    
    
    // Append normal trace line 2
    timeSource[0] += 1000;
    synchronized (cb)
    {
      filter.addTraceLine("Line 2");
      cb.wait();
    }    
    timeSource[0] += 1000;
    assertEquals(3, displayedHolder[0]);
    assertEquals(3, totalHolder[0]);
    assertTrue(appendTextHolder[0], 
               appendTextHolder[0].contains("Line 2"));
    assertTrue(setTextHolder[0], 
               setTextHolder[0] == null);
    
    // Filter trace
    appendTextHolder[0] = null;
    setTextHolder[0] = null;
    TraceFilterProgressHandler progress = new TraceFilterProgressHandler()
    {
      @Override
      public synchronized boolean setProgress(int percent)
      {
        if (percent == 100)
        {
          this.notifyAll();
        }
        return false;
      }
      
      @Override
      public List<String> getIncludePattern()
      {
        List<String> include = new ArrayList<String>();
        include.add("1");
        return include;
      }
      
      @Override
      public List<String> getExcludePattern()
      {
        List<String> exclude = new ArrayList<String>();
        exclude.add("1b");
        return exclude;
      }
      
      @Override
      public boolean discardFiltered()
      {
        return false;
      }
    };
    
    // do filtering and wait until we have finished
    synchronized (progress)
    {
      filter.applyFilter(progress);
      progress.wait();
    } 
    
    assertEquals(1, displayedHolder[0]);
    assertEquals(3, totalHolder[0]);
    assertTrue(appendTextHolder[0], 
               appendTextHolder[0] == null);
    assertTrue(setTextHolder[0], 
               setTextHolder[0].contains("Line 1"));
  }
}
