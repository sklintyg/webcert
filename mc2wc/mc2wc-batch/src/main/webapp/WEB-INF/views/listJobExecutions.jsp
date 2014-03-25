<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="spring-form" uri="http://www.springframework.org/tags/form" %>
<html>
<head>
    <title>Job executions</title>
</head>
<body>
<h1>Stored executions</h1>
<table>
    <thead>
    <td>Execution Id</td>
    <td>Status</td>
    <td>Start</td>
    <td>End</td>
    <td>Certificate count</td>
    <td>Question count</td>
    <td>Answer count</td>
    <td>Restart job</td>
    </thead>
    <c:forEach items="${jobExecutions}" var="execution">
        <tr>
            <td>${execution.id}</td>
            <td>${execution.status}</td>
            <td><fmt:formatDate value="${execution.startTime}" pattern="yyyyMMdd HH:mm:ss"/></td>
            <td><fmt:formatDate value="${execution.endTime}" pattern="yyyyMMdd HH:mm:ss"/></td>
            <td>${execution.executionContext.getLong('certificateWriteCount')}</td>
            <td>${execution.executionContext.getLong('questionWriteCount')}</td>
            <td>${execution.executionContext.getLong('answerWriteCount')}</td>
            <td>
                <c:if test="${execution.status eq 'FAILED' || execution.status eq 'STOPPED'}">
                    <a href="<%= request.getContextPath()%>/d/restartJob?executionId=${execution.id}">Restart</a>
                </c:if>
            </td>
        </tr>
    </c:forEach>
</table>

<p>
    <a href="<%= request.getContextPath() %>/d/home">Home</a>
</p>
</body>
</html>
