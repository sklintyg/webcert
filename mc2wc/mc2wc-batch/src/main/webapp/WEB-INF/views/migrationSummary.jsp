<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>Migrering avslutad</title>
</head>
<body>
<h1>Migrering avslutad</h1>
${status}<br>

Antal migrerade intyg: ${certificateWriteCount}<br>
Antal migrerade frågor: ${questionWriteCount}<br>
Antal migrerade svar: ${answerWriteCount}<br>


<p>
  <a href="<%= request.getContextPath() %>/d/statistics">Jämför innehåll mellan Mecert och Webcert</a>
</p>

<p>
  <a href="<%= request.getContextPath() %>/d/home">Tillbaka till startsidan</a>
</p>
</body>
</html>
