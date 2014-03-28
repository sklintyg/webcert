<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>Migrering pågår</title>
  <meta http-equiv="refresh" content="3;url=<%= request.getContextPath() %>/d/checkMigration"/>

</head>
<body>
${status}
<br>
Antal migrerade intyg: ${certificateWriteCount}<br>
Antal migrerade frågor: ${questionWriteCount}<br>
Antal migrerade svar: ${answerWriteCount}<br>


<p>
  <a href="<%= request.getContextPath() %>/d/home">Tillbaka till startsidan</a>
</p>

</body>
</html>
