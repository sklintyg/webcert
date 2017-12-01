<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%--
  ~ Copyright (C) 2016 Inera AB (http://www.inera.se)
  ~
  ~ This file is part of sklintyg (https://github.com/sklintyg).
  ~
  ~ sklintyg is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ sklintyg is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<!DOCTYPE html>
<html lang="sv">
<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=Edge" />
<meta name="ROBOTS" content="nofollow, noindex" />
<meta name="viewport" content="width=device-width, initial-scale=1">

<title><spring:message code="application.name" /> : Version</title>

<!-- bower:css -->
<link rel="stylesheet" href="/bower_components/angular-ui-select/dist/select.css" />
<!-- endbower -->

</head>
<body>
  <div style="padding: 20px;">
    <div class="page-header" style="margin-top: 0;">
      <h3><spring:message code="application.name" /></h3>
    </div>
    <div class="alert alert-info">
      <h4>Configuration info</h4>
      <div>
        Application version:
        <span class="label label-info"><spring:message code="project.version" /></span>
      </div>
      <div>
        Build number:
        <span class="label label-info"><spring:message code="buildNumber" /></span>
      </div>
      <div>
        Build time:
        <span class="label label-info"><spring:message code="buildTime" /></span>
      </div>
      <div>
        Spring profiles:
        <span class="label label-info"><%= System.getProperty("spring.profiles.active") %></span>
      </div>
    </div>
  </div>
</body>
</html>
