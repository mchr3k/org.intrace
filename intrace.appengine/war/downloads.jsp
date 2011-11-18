<%@ page import="javax.jdo.PersistenceManager" 
%><%@ page import="javax.jdo.Query" 
%><%@ page import="org.gaecounter.data.Counter" 
%><%@ page import="org.gaecounter.data.Counter.Type" 
%><%@ page import="org.gaecounter.data.PMF" 
%><%@ page import="org.gaecounter.Utils" 
%><%@ page import="java.util.List" 
%><%@ page import="java.util.ArrayList" 
%><%@ page import="java.util.Iterator" 
%><%@ page import="java.util.Map" 
%><%@ page import="java.util.Map.Entry" 
%><%@ page import="java.util.HashMap" 
%><%@ page import="java.util.Collections" 
%><%@ page import="java.util.Set" 
%><%@ page import="java.util.HashSet" 
%><%@ page import="org.joda.time.DateTime" 
%><%@ page import="com.google.appengine.api.users.UserService" 
%><%@ page import="com.google.appengine.api.users.UserServiceFactory" 
%><!DOCTYPE html>
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
    // Set type
    Type lType = Type.DAY;
    
    // Check file
    String lFileStr = request.getParameter("file");    
  
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
    Map<String,Map<String,Integer>> lPerFilePerDateStrDownloads = Counter.getPerFilePerDateMap(lRecords);
    
    // Filenames
    List<String> lFilenames = new ArrayList<String>(lPerFilePerDateStrDownloads.keySet());
    Collections.sort(lFilenames);
    
    // Restrict Files
    if ((lFileStr != null) && !lFileStr.equals("all"))
    {
      lFileStr = Utils.dec(lFileStr);
      Map<String,Integer> lSingleValue = lPerFilePerDateStrDownloads.get(lFileStr);
      lPerFilePerDateStrDownloads.clear();
      lPerFilePerDateStrDownloads.put(lFileStr, lSingleValue);
    }
    
    // Date strings
    Set<String> dateStrsSet = new HashSet<String>();
    for (String file : lPerFilePerDateStrDownloads.keySet())
    {
      dateStrsSet.addAll(lPerFilePerDateStrDownloads.get(file).keySet()); 
    }    
    DateTime lNow = new DateTime();
    List<String> dateStrs = Counter.getDateStrs(lType, 
                                                lType.getPartial(lNow.getYear(), lNow.getMonthOfYear(), lNow.getDayOfMonth()), 
                                                dateStrsSet);
  %><script type="text/javascript">
      function drawVisualization() {
        // Raw data
        var filenames = 
        [
        <%Iterator<String> lFiles = lPerFilePerDateStrDownloads.keySet().iterator(); while (lFiles.hasNext()) {
        %>  '<%=lFiles.next()%>'<%=(lFiles.hasNext() ? "," : " ")%>
        <%}%>];
        var datestrs = 
        [
        <%Iterator<String> dateStrsIter = dateStrs.iterator(); while (dateStrsIter.hasNext()) {
        %>  '<%=dateStrsIter.next()%>'<%=(dateStrsIter.hasNext() ? "," : " ")%>
        <%}%>];
        var downloadsByFile = 
        [<%Iterator<Entry<String,Map<String,Integer>>> iter = lPerFilePerDateStrDownloads.entrySet().iterator();
           while (iter.hasNext()) {
           Entry<String,Map<String,Integer>> entry = iter.next();
           Map<String,Integer> perDateCount = entry.getValue();
           dateStrsIter = dateStrs.iterator();%>  
          [
          <%while(dateStrsIter.hasNext()) { 
             String dateStr = dateStrsIter.next();
             Integer lVal = perDateCount.get(dateStr);
             if (lVal == null) lVal = 0;
            %>  <%=lVal%><%=(dateStrsIter.hasNext() ? "," : " ")%> // <%=dateStr%>
          <%}%>]<%=(iter.hasNext() ? "," : "")%> // <%=entry.getKey()
          %><%}%>
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
    
    function adminMode()
    { 
      var normalEl = document.getElementById("normaltable");
      normalEl.style.display = "none";
      var adminEl = document.getElementById("admintable");      
      adminEl.style.display = "block"; 
      var adminButtonEl = document.getElementById("adminbutton");
      adminButtonEl.style.display = "none"; 
    }
    
    function setMode(mode)
    {       
      if (getUrlVars()['file'])
      {
        window.location.href = "downloads.jsp?file=" + getUrlVars()['file'] +  
                               "&mode=" + mode;
      }
      else
      {
        window.location.href = "downloads.jsp?mode=" + mode;
      } 
    }
    
    function selectFile()
    { 
      var picker = document.getElementById("filePicker");
      var file = picker.value;
      if (file)
      {
        if (getUrlVars()['mode'])
        {
          window.location.href = "downloads.jsp?file=" + file + 
                                 "&mode=" + getUrlVars()['mode'];
        }
        else
        {
          window.location.href = "downloads.jsp?file=" + file;
        }
      } 
    }
    
    // Read a page's GET URL variables and return them as an associative array.
    function getUrlVars()
    {
        var vars = [], hash;
        var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
        for(var i = 0; i < hashes.length; i++)
        {
            hash = hashes[i].split('=');
            vars.push(hash[0]);
            vars[hash[0]] = hash[1];
        }
        return vars;
    }
    </script>
    <select id="filePicker" onchange="selectFile()">
      <option selected value="">Choose File...</option>
      <option value="all">All Files</option>
      <% for (String lFilename : lFilenames) { 
      %><option value="<%=Utils.enc(lFilename)%>"><%=lFilename%></option>
    <% } %></select>
    <input type="submit" value="Year" onclick="setMode('year')" />
    <input type="submit" value="Month" onclick="setMode('month')" />
    <input type="submit" value="Day" onclick="setMode('day')" /><br>
    <div style="width: 100%;" id="visualization"></div>    
    <div id="normaltable">    
      <table>
        <tr>
          <td>Total</td>
          <td>Filename</td>
        </tr><%
      for (Entry<String,Map<String,Integer>> entry : lPerFilePerDateStrDownloads.entrySet()) {
        Map<String,Integer> lCounterMap = entry.getValue();
        int lTotal = 0;
        for (Integer lCount : lCounterMap.values())
        {
          lTotal += lCount;
        }
    %>
        <tr>
          <td><%=lTotal%></td>
          <td><%=entry.getKey()%></td>
        </tr><% 
      } %>
      </table>
    </div>
    <div id="admintable" style="display:none;">    
      <table>
        <tr>
          <td>Total</td>
          <td>Filename</td>
          <td></td>
        </tr><%
      for (Entry<String,Map<String,Integer>> entry : lPerFilePerDateStrDownloads.entrySet()) {
        Map<String,Integer> lCounterMap = entry.getValue();
        int lTotal = 0;
        for (Integer lCount : lCounterMap.values())
        {
          lTotal += lCount;
        }
    %>
        <tr>
          <td><%=lTotal%></td>
          <td><%=entry.getKey()%></td>
          <td><input type="submit" value="Clear File" onclick="doClearFile('<%=Utils.enc(entry.getKey())%>')" /></td>
        </tr><% 
      } %>
      </table><br>
      <input type="submit" value="Clear All Data (<%=lPerFilePerDateStrDownloads.keySet().size()%> Files)" onclick="doClear()" />
      <br><br>
      <input type="submit" value="Create Test Data" onclick="doTestData()" />
    </div>
    <div id="adminbutton">
    <br>
    <input type="submit" value="Admin" onclick="adminMode()" />
    </div>
  </body>
</html>
<%}%>