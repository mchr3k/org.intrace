package org.gaecounter;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gaecounter.data.Counter;
import org.gaecounter.data.PMF;

public class ActionServlet extends HttpServlet
{
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest xiReq,
                       HttpServletResponse xiResp)
                       throws ServletException,
                              IOException
  {
    String lAction = xiReq.getParameter("action");
    String lRedir = xiReq.getParameter("redir");

    if ("clear".equals(lAction) ||
        "clearfile".equals(lAction))
    {
      PersistenceManager pm = PMF.get().getPersistenceManager();
      try
      {
        Query lDeletion = pm.newQuery(Counter.class);
        if ("clearfile".equals(lAction))
        {
          String lFile = Utils.dec(xiReq.getParameter("file"));
          lDeletion.setFilter("mFile == mFilenameParam");
          lDeletion.declareParameters("String mFilenameParam");
          lDeletion.deletePersistentAll(lFile);
        }
        else
        {
          lDeletion.deletePersistentAll();
        }
      }
      finally
      {
        pm.close();
      }
    }
    else if ("testdata".equals(lAction))
    {
      for (int ii = 13; ii <= 25; ii++)
        CountDownloadsFilter.countDownload("test_0.1.jar", 2012, 2, ii);
      for (int ii = 28; ii <= 29; ii++)
        CountDownloadsFilter.countDownload("test_0.1.jar", 2012, 2, ii);
      for (int ii = 1; ii <= 2; ii++)
        CountDownloadsFilter.countDownload("test_0.1.jar", 2012, 3, ii);
      for (int ii = 5; ii <= 10; ii++)
        CountDownloadsFilter.countDownload("test_0.1.jar", 2012, 3, ii);
      for (int ii = 12; ii <= 15; ii++)
        CountDownloadsFilter.countDownload("test_0.1.jar", 2012, 3, ii);
    }

    xiResp.sendRedirect(lRedir);
  }
}
