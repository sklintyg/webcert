<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>Migrering Medcert-till-Webcert</title>
</head>
<body>
<h1>Migrering Medcert-till-Webcert</h1>

<p>
  <a href="<%= request.getContextPath() %>/d/sendPing">Anslutningstest, verifiera kontakt med mottagar-applikation</a>
</p>

<p>
  <a href="<%= request.getContextPath() %>/d/statistics">Jämför innehåll mellan Mecert och Webcert</a>
</p>

<p>
  <a href="<%= request.getContextPath() %>/d/listJobExecutions">Lista tidigare körningar</a>
</p>

<p>

<form action="<%= request.getContextPath() %>/d/startMigration" method="POST">
  <label for="dryRun">Kör testomgång, utan att överföra data</label>
  <input id="dryRun" type="checkbox" name="dryRun" value="true" checked><br>
  <input type="submit" value="Starta migrering"/>
</form>
</p>
</body>
</html>