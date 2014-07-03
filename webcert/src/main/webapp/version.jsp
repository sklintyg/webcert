<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html lang="sv">
<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=Edge" />
<meta name="ROBOTS" content="nofollow, noindex" />
<meta name="viewport" content="width=device-width, initial-scale=1">

<title><spring:message code="application.name" /> : Version</title>

<link rel="stylesheet" href="/web/webjars/bootstrap/3.1.1/css/bootstrap.min.css" />
<link rel="stylesheet" href="/web/webjars/bootstrap/3.1.1/css/bootstrap-theme.min.css">

</head>
<body>
  <div style="padding: 20px;">
    <div class="page-header" style="margin-top: 0;">
      <h3><spring:message code="application.name" /></h3>
    </div>
    <div class="alert alert-warning">
      <h4>Configuration info</h4>
      <div>
        Application version:
        <span class="label label-warning"><spring:message code="project.version" /></span>
      </div>
      <div>
        Build number:
        <span class="label label-warning"><spring:message code="buildNumber" /></span>
      </div>
      <div>
        Build time:
        <span class="label label-warning"><spring:message code="buildTime" /></span>
      </div>
      <div>
        Spring profiles:
        <span class="label label-warning"><%= System.getProperty("spring.profiles.active") %></span>
      </div>
    </div>
  </div>
</body>
</html>
