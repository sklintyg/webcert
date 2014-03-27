<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Migration finished</title>
</head>
<body>
<h1>We are done!</h1>
${status}<br>

CertificateWriteCount: ${certificateWriteCount}<br>
QuestionWriteCount: ${questionWriteCount}<br>
AnswerWriteCount: ${answerWriteCount}<br>

<p>
    <a href="<%= request.getContextPath() %>/d//statistics">Compare contents</a>
</p>

<p>
    <a href="<%= request.getContextPath() %>/d/home">Home</a>
</p>
</body>
</html>
