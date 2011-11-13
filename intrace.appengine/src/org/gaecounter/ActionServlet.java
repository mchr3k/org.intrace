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
      CountDownloadsFilter.countDownload("test_0.1.jar", 2010, 1, 1);
      CountDownloadsFilter.countDownload("test_0.1.jar", 2010, 1, 1);
      CountDownloadsFilter.countDownload("test_0.1.jar", 2010, 1, 2);
      CountDownloadsFilter.countDownload("test_0.1.jar", 2010, 2, 2);
      CountDownloadsFilter.countDownload("test_0.1.jar", 2010, 3, 2);
      CountDownloadsFilter.countDownload("test_0.2.jar", 2010, 4, 2);
      CountDownloadsFilter.countDownload("test_0.2.jar", 2010, 5, 2);
      CountDownloadsFilter.countDownload("test_0.2.jar", 2010, 5, 2);
      CountDownloadsFilter.countDownload("test_0.2.jar", 2010, 6, 2);
      CountDownloadsFilter.countDownload("test_0.3.jar", 2011, 1, 1);
      CountDownloadsFilter.countDownload("test_0.3.jar", 2011, 1, 1);
      CountDownloadsFilter.countDownload("test_0.3.jar", 2011, 1, 2);
      CountDownloadsFilter.countDownload("test_0.3.jar", 2011, 2, 2);
      CountDownloadsFilter.countDownload("test_0.3.jar", 2011, 3, 2);
      CountDownloadsFilter.countDownload("test_0.4.jar", 2011, 4, 2);
      CountDownloadsFilter.countDownload("test_0.4.jar", 2011, 5, 2);
      CountDownloadsFilter.countDownload("test_0.4.jar", 2011, 5, 2);
      CountDownloadsFilter.countDownload("test_0.4.jar", 2011, 6, 2);
    }

    xiResp.sendRedirect(lRedir);
  }
}
