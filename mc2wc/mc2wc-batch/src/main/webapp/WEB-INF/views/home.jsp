<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Migration Tool</title>
</head>
<body>
<h1>Migration Tool</h1>

<p>
    <a href="<%= request.getContextPath() %>/d/sendPing">Send ping to receiver</a>
</p>

<p>
    <a href="<%= request.getContextPath() %>/d/listJobExecutions">List job executions</a>
</p>

<p>

<form action="<%= request.getContextPath() %>/d/startMigration" method="POST">
    <label for="dryRun">Test run, to validate contents of Medcert database</label>
    <input id="dryRun" type="checkbox" name="dryRun" value="true" checked>
    <label for="sender">Sender</label>
    <input id="sender" type="text" name="sender">
    <input type="submit" value="Start batch"/>
</form>
</p>
</body>
</html>