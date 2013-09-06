package org.gaecounter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jdo.JDOCanRetryException;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.gaecounter.data.Counter;
import org.gaecounter.data.Counter.Type;
import org.gaecounter.data.PMF;
import org.joda.time.DateTime;

public class CountDownloadsFilter implements Filter
{
  @Override
  public void init(FilterConfig xiConfig) throws ServletException
  {
    // Do nothing
  }

  @Override
  public void doFilter(ServletRequest xiReq,
                       ServletResponse xiResp,
                       FilterChain xiChain)
                       throws IOException,
                              ServletException
  {
    final AtomicInteger error = new AtomicInteger(-1);
    if (xiResp instanceof HttpServletResponse)
    {
      xiResp = new HttpServletResponseWrapper((HttpServletResponse)xiResp) {
        @Override
        public void sendError(int sc) throws IOException {
          error.set(sc);
          super.sendError(sc);
        }
        @Override
        public void sendError(int sc, String msg) throws IOException {
          error.set(sc);
          super.sendError(sc, msg);
        }
      };
    }

    xiChain.doFilter(xiReq, xiResp);

    if (error.get() == -1 && xiReq instanceof HttpServletRequest)
    {
      HttpServletRequest lHttpReq = (HttpServletRequest)xiReq;
      String lReqURI = lHttpReq.getRequestURI();
      DateTime lNow = new DateTime();
      countDownload(lReqURI, lNow.getYear(), lNow.getMonthOfYear(), lNow.getDayOfMonth());
      if (lReqURI.endsWith(".so"))
      {
        xiResp.setContentType("application/octet-stream");
      }
    }
  }

  public static void countDownload(String xiReqURI, int y, int m, int d)
  {
    String lYStr = Type.YEAR.getPartialStr(y, m, d);
    String lYStrKey = Counter.getKey(xiReqURI, lYStr);
    String lYMStr = Type.MONTH.getPartialStr(y, m, d);
    String lYMStrKey = Counter.getKey(xiReqURI, lYMStr);
    String lYMDStr = Type.DAY.getPartialStr(y, m, d);
    String lYMDStrKey = Counter.getKey(xiReqURI, lYMDStr);

    PersistenceManager pm = PMF.get().getPersistenceManager();
    try
    {
      incrementCounter(pm, lYStrKey, Type.YEAR, lYStr, xiReqURI);
      incrementCounter(pm, lYMStrKey, Type.MONTH, lYMStr, xiReqURI);
      incrementCounter(pm, lYMDStrKey, Type.DAY, lYMDStr, xiReqURI);
    }
    finally
    {
      pm.close();
    }
  }

  private static void incrementCounter(PersistenceManager xiPm,
                                       String xiKey,
                                       Type xiType,
                                       String xiDateStr,
                                       String xiReqURI)
  {
    try
    {
      int retries = 0;
      int NUM_RETRIES = 10;
      while (true) // break out below
      {
        retries++;

        xiPm.currentTransaction().begin();

        Counter lCounter = null;
        try
        {
          lCounter = xiPm.getObjectById(Counter.class, xiKey);
          lCounter.incrementCount();
        }
        catch (JDOObjectNotFoundException ex)
        {
          lCounter = new Counter(xiType, xiDateStr, xiReqURI);
        }
        xiPm.makePersistent(lCounter);

        try
        {
          xiPm.currentTransaction().commit();
          break;
        }
        catch (JDOCanRetryException ex)
        {
          if (retries == NUM_RETRIES)
          {
            throw ex;
          }
        }
      }
    }
    finally
    {
      if (xiPm.currentTransaction().isActive())
      {
        xiPm.currentTransaction().commit();
      }
    }
  }

  @Override
  public void destroy()
  {
    // Do nothing
  }
}
