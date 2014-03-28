<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
  <title>Anslutningstest</title>
</head>
<body>
<h1>Anslutningstest</h1>
<c:choose>
  <c:when test="${connected}">
    Lyckades ansluta till mottagare på ${receiverUrl}!
  </c:when>
  <c:otherwise>
    Kunde inte ansluta till mottagare på ${receiverUrl}.<br>
    ${errorMessage}
  </c:otherwise>
</c:choose>

<p>
  <a href="<%= request.getContextPath() %>/d/home">Tillbaka till startsidan</a>
</p>

</body>
</html>