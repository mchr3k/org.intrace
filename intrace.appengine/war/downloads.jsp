<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="javax.jdo.Query" %>
<%@ page import="org.gaecounter.data.Counter" %>
<%@ page import="org.gaecounter.data.Counter.Type" %>
<%@ page import="org.gaecounter.data.PMF" %>
<%@ page import="org.gaecounter.Utils" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Map.Entry" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.joda.time.DateTime" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<!DOCTYPE html>
<html>
  <head>
    <title>Downloads</title>
    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
    <script type="text/javascript">
      google.load('visualization', '1', {packages: ['corechart']});
    </script>
  </head>
  <body>
<%
   UserService userService = UserServiceFactory.getUserService();
   if (!(userService.isUserLoggedIn() && userService.isUserAdmin())) 
   {%>
     Please <a href="<%=userService.createLoginURL("/downloads.jsp")%>">log in</a>
 <%}
   else
   {
 %>  
  <%
    // Set type
    Type lType = Type.DAY;
    
    // Check type
    String lModeStr = request.getParameter("mode");
    if ("month".equals(lModeStr))
    {
      lType = Type.MONTH;
    }
    else if ("year".equals(lModeStr))
    {
      lType = Type.YEAR;
    }
    
    // Fetch data
    List<Counter> lRecords = Counter.getAllByType(lType);
    
    // Filename -> DateStr -> Count
    Map<String,Map<String,Integer>> lPerDateStrDownloads = Counter.getPerFilePerDateMap(lRecords);
    
    // Date strings
    Set<String> dateStrsSet = new HashSet<String>();
    for (String file : lPerDateStrDownloads.keySet())
    {
      dateStrsSet.addAll(lPerDateStrDownloads.get(file).keySet()); 
    }    
    DateTime lNow = new DateTime();
    List<String> dateStrs = Counter.getDateStrs(lType, 
                                                lType.getPartial(lNow.getYear(), lNow.getMonthOfYear(), lNow.getDayOfMonth()), 
                                                dateStrsSet);
  %>
    <script type="text/javascript">
      function drawVisualization() {
        // Raw data
        var filenames = 
        [<%Iterator<String> lFiles = lPerDateStrDownloads.keySet().iterator(); while (lFiles.hasNext()) {%>
          '<%=lFiles.next()%>'<%=(lFiles.hasNext() ? "," : "")%>
        <%}%>];
        var datestrs = 
        [<%Iterator<String> dateStrsIter = dateStrs.iterator(); while (dateStrsIter.hasNext()) {%>
          '<%=dateStrsIter.next()%>'<%=(dateStrsIter.hasNext() ? "," : "")%>
        <%}%>];
        var downloadsByFile = 
        [
        <%Iterator<Entry<String,Map<String,Integer>>> iter = lPerDateStrDownloads.entrySet().iterator();
           while (iter.hasNext()) {
           Entry<String,Map<String,Integer>> entry = iter.next();
           Map<String,Integer> perDateCount = entry.getValue();
           dateStrsIter = dateStrs.iterator();%>
          [
          <%while(dateStrsIter.hasNext()) { 
             String dateStr = dateStrsIter.next();
             Integer lVal = perDateCount.get(dateStr);
             if (lVal == null) lVal = 0;%>
            <%=lVal%><%=(dateStrsIter.hasNext() ? "," : "")%> 
          <%}%>
          ]<%=(iter.hasNext() ? "," : "")%>  // <%=entry.getKey()%>          
        <%}%>
        ]; 
      
        // Create and populate the data table.
        var data = new google.visualization.DataTable();
        
        // Prepare columns
        data.addColumn('string', 'Date');
        for (var i = 0; i < filenames.length; ++i) {
          data.addColumn('number', filenames[i]);
        }
        
        // Fill in datestr strings
        data.addRows(datestrs.length);
        for (var i = 0; i < datestrs.length; ++i) {
          data.setCell(i, 0, datestrs[i]);
        }
        
        // Fill in download data
        for (var i = 0; i < filenames.length; ++i) {
          var downloads = downloadsByFile[i];
          for (var datestr = 0; datestr < datestrs.length; ++datestr) {
            data.setCell(datestr, i + 1, downloads[datestr]);
          }
        }
        
        // Create and draw the visualization.
        var ac = new google.visualization.LineChart(document.getElementById('visualization'));
        ac.draw(data, {
          title : 'Downloads by Date',
          isStacked: true,
          //width: 600,
          height: 400,
          vAxis: {title: "Downloads"},
          hAxis: {title: "Date"}
        });
      }
     
      google.setOnLoadCallback(drawVisualization);
    </script>
        
    <script>
    function doClearFile(fileName)
    { window.location.href = "action?action=clearfile&file=" + fileName + "&redir=downloads.jsp"; }
    
    function doClear()
    { window.location.href = "action?action=clear&redir=downloads.jsp"; }
    
    function doTestData()
    { window.location.href = "action?action=testdata&redir=downloads.jsp"; }
    
    function setMode(mode)
    { window.location.href = "downloads.jsp?mode=" + mode; }
    </script>
    <input type="submit" value="Year" onclick="setMode('year')" />
    <input type="submit" value="Month" onclick="setMode('month')" />
    <input type="submit" value="Day" onclick="setMode('day')" /><br>
    <div style="width: 100%;" id="visualization"></div>    
    <table>
      <tr>
        <td>Filename</td>
        <td></td>
      </tr>
  <%
    for (Entry<String,Map<String,Integer>> entry : lPerDateStrDownloads.entrySet()) {
  %>
      <tr>
        <td><%=entry.getKey()%></td>
        <td><input type="submit" value="Clear File" onclick="doClearFile('<%=Utils.enc(entry.getKey())%>')" /></td>
      </tr>
  <% } %>
    </table><br>
    <input type="submit" value="Clear All Data (<%=lPerDateStrDownloads.keySet().size()%> Files)" onclick="doClear()" />
    <br><br><br>
    <input type="submit" value="Create Test Data" onclick="doTestData()" />
  </body>
</html>
<%}%>