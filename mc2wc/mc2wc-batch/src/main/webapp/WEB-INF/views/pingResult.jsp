<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Ping result</title>
</head>
<body>
<h1>Ping result</h1>
<c:choose>
    <c:when test="${connected}">
        Connected successfully to server at ${receiverUrl}!
    </c:when>
    <c:otherwise>
        Could not connect to server at ${receiverUrl}.<br>
        ${errorMessage}
    </c:otherwise>
</c:choose>
<p>
    <a href="<%= request.getContextPath() %>/d/home">Home</a>
</p>

</body>
</html>