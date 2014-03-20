<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Migration Tool</title>
</head>
<body>
<h1>Migration Tool</h1>

<p>
    <a href="<%= request.getContextPath() %>/d/sendPing">Send Ping to receiver</a>
</p>

<p>
    <a href="<%= request.getContextPath() %>/d/startMigration">Start Migration</a>
</p>
</body>
</html>