<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Checking job status</title>
    <meta http-equiv="refresh" content="3;url=<%= request.getContextPath() %>/d/checkMigration"/>

</head>
<body>
${status}
<br>
${readCount}
</body>
</html>
