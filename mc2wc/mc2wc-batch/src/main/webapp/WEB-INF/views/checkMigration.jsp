<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Checking job status</title>
    <meta http-equiv="refresh" content="3;url=<%= request.getContextPath() %>/d/checkMigration"/>

</head>
<body>
${status}
<br>
CertificateWriteCount: ${certificateWriteCount}<br>
QuestionWriteCount: ${questionWriteCount}<br>
AnswerWriteCount: ${answerWriteCount}<br>

</body>
</html>
