<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Migration finished</title>
</head>
<body>
<h1>We are done!</h1>
${status}<br>

ReadCount: ${readCount}<br>
ReadError: ${readError}<br>
SkipCount: ${skipCount}<br>
WriteCount: ${writeCount}<br>
WriteError: ${writeError}<br>

<p>
    <a href="<%= request.getContextPath() %>/d/home">Home</a>
</p>
</body>
</html>
