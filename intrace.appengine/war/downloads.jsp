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
   {
     response.sendRedirect(userService.createLoginURL("/downloads.jsp"));
   }
   else
   {
    // Set type
    Type lType = Type.WEEK;
    
    // Check file
    String lFileStr = request.getParameter("file");
    
    // Check whether to collapse versions
    String lCollapseVersions = request.getParameter("collapseversions");    
  
    // Check type
    String lModeStr = request.getParameter("mode");
    if ("day".equals(lModeStr))
    {
      lType = Type.DAY;
    }
    else if ("month".equals(lModeStr))
    {
      lType = Type.MONTH;
    }
    else if ("year".equals(lModeStr))
    {
      lType = Type.YEAR;
    }
    
    // Fetch data
    Type lFetchType = lType;
    if (lFetchType == Type.WEEK)
    {
      lFetchType = Type.DAY;
    }
    List<Counter> lRecords = Counter.getAllByType(lFetchType);
    
    // Filename -> DateStr -> Count
    Map<String,Map<String,Integer>> lPerFilePerDateStrDownloads = Counter.getPerFilePerDateMap(lRecords,
                                                                                               (lCollapseVersions != null));
    if (lType == Type.WEEK)
    {
      lPerFilePerDateStrDownloads = Counter.getFilePerWeekMap(lPerFilePerDateStrDownloads);
    }

    // Filenames
    List<String> lFilenames = new ArrayList<String>(lPerFilePerDateStrDownloads.keySet());
    Collections.sort(lFilenames);
    
    // Restrict Files
    if ((lFileStr != null) && !lFileStr.equals("all"))
    {
      lFileStr = Utils.dec(lFileStr);
      Map<String,Integer> lSingleValue = lPerFilePerDateStrDownloads.get(lFileStr);
      lPerFilePerDateStrDownloads.clear();
      if (lSingleValue != null)
      {
        lPerFilePerDateStrDownloads.put(lFileStr, lSingleValue);
      }
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
        <%
        Iterator<String> lFiles = lFilenames.iterator(); while (lFiles.hasNext()) {
          String lFile = lFiles.next();
          if ((lFileStr != null) && !lFileStr.equals("all") && !lFileStr.equals(lFile)) continue;
        %>  '<%=lFile%>'<%=(lFiles.hasNext() ? "," : " ")%>
        <%}%>];
        var datestrs = 
        [
        <%Iterator<String> dateStrsIter = dateStrs.iterator(); while (dateStrsIter.hasNext()) {
        %>  '<%=dateStrsIter.next()%>'<%=(dateStrsIter.hasNext() ? "," : " ")%>
        <%}%>];
        var downloadsByFile = 
        [<%lFiles = lFilenames.iterator(); while (lFiles.hasNext()) {
           String lFile = lFiles.next();
           Map<String,Integer> perDateCount = lPerFilePerDateStrDownloads.get(lFile);
           if (perDateCount == null) continue;
           dateStrsIter = dateStrs.iterator();%>  
          [
          <%while(dateStrsIter.hasNext()) { 
             String dateStr = dateStrsIter.next();
             Integer lVal = perDateCount.get(dateStr);
             if (lVal == null) lVal = 0;
            %>  <%=lVal%><%=(dateStrsIter.hasNext() ? "," : " ")%> // <%=dateStr%>
          <%}%>]<%=(lFiles.hasNext() ? "," : "")%> // <%=lFile
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
      loadWithUrlArg('mode', mode);
    }
    
    function selectFile()
    { 
      var picker = document.getElementById("filePicker");
      var file = picker.value;
      if (file)
      {
        loadWithUrlArg('file', file);
      }
    }
    
    function loadWithUrlArg(key, value)
    {
      var args = getUrlVars();
      args[key] = value;
      
      var newUrl = "downloads.jsp?";
      for each (var item in ['mode','file','collapseversions'])
      {
        if (args[item])
        {
          newUrl += item + "=" + args[item] + "&";
        }
      }
      
      window.location.href = newUrl;
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
    
    function setCollapse()
    { 
      var collapseEl = document.getElementById("collapseversions");
      if (collapseEl.checked)
      {
        loadWithUrlArg('collapseversions', true);
      }
      else
      {
        loadWithUrlArg('collapseversions');
      }
    }
    </script>
    <select id="filePicker" onchange="selectFile()">
      <option selected value="">Choose File...</option>
      <option value="all">All Files</option><% 
      for (String lFilename : lFilenames) {%>
      <option value="<%=Utils.enc(lFilename)%>"><%=lFilename%></option><% 
    } %>
    </select>
    <input type="submit" value="Year" onclick="setMode('year')" />
    <input type="submit" value="Month" onclick="setMode('month')" />
    <input type="submit" value="Week" onclick="setMode('week')" />
    <input type="submit" value="Day" onclick="setMode('day')" />
    <input type="checkbox" 
           id="collapseversions" 
           value="collapseversions" 
           onclick="setCollapse()" 
           <% if (lCollapseVersions != null) { %>checked="checked" <% } %>/> Collapse Versions
    <br>
    <div style="width: 100%;" id="visualization"></div>    
    <div id="normaltable">    
      <table>
        <tr>
          <td>Total</td>
          <td>Filename</td>
        </tr><%
      for (String lFilename : lFilenames) {        
        Map<String,Integer> lCounterMap = lPerFilePerDateStrDownloads.get(lFilename);
        if (lCounterMap == null) continue;
        int lTotal = 0;
        for (Integer lCount : lCounterMap.values())
        {
          lTotal += lCount;
        }
    %>
        <tr>
          <td><%=lTotal%></td>
          <td><%=lFilename%></td>
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
      for (String lFilename : lFilenames) {
        Map<String,Integer> lCounterMap = lPerFilePerDateStrDownloads.get(lFilename);
        if (lCounterMap == null) continue;
        int lTotal = 0;
        for (Integer lCount : lCounterMap.values())
        {
          lTotal += lCount;
        }
    %>
        <tr>
          <td><%=lTotal%></td>
          <td><%=lFilename%></td>
          <td><input type="submit" value="Clear File" onclick="doClearFile('<%=Utils.enc(lFilename)%>')" /></td>
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