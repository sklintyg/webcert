<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Medcert statistics</title>
</head>
<body>
<h1>Comparison between Medcert and Webcert</h1>

<table>
    <thead>
    <tr>
        <th></th>
        <th>Medcert</th>
        <th>Webcert</th>
    </tr>
    </thead>
    <tr>
        <td>Certificates</td>
        <td>${certificateCountMedcert}</td>
        <td>${certificateCountWebcert}</td>
    </tr>
    <tr>
        <td>Questions</td>
        <td>${questionCountMedcert}</td>
        <td>${questionCountWebcert}</td>
    </tr>
    <tr>
        <td>Answers</td>
        <td>${answerCountMedcert}</td>
        <td>${answerCountWebcert}</td>
    </tr>
</table>
<p>
    <a href="<%= request.getContextPath() %>/d/home">Home</a>
</p>
</body>
</html>
