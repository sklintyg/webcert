<%--

    Copyright (C) 2013 Inera AB (http://www.inera.se)

    This file is part of Inera Certificate Web (http://code.google.com/p/inera-certificate-web).

    Inera Certificate Web is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Inera Certificate Web is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta name="ROBOTS" content="nofollow, noindex"/>

  <title><spring:message code="application.name"/></title>

  <link rel="stylesheet" href="<c:url value="/css/bootstrap.css"/>">

</head>

<body ng-app="WebcertApp">

<div class="container">

  <div id="content-container">
    <div class="content">
      <div class="row-fluid">
        <div id="content-body" class="span12">
          <%-- No script to show at least something when javascript is off --%>
          <noscript>
            <h1>
              <span><spring:message code="error.noscript.title"/></span>
            </h1>

            <div class="alert alert-error">
              <spring:message code="error.noscript.text"/>
            </div>
          </noscript>
          <%-- ng-view that holds dynamic content managed by angular app --%>
          <div ng-view></div>
        </div>
      </div>
    </div>
  </div>
</div>


<script type="text/javascript" src="<c:url value="/js/vendor/angular/angular.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/vendor/angular/i18n/angular-locale_sv-se.js"/>"></script>
<script type="text/javascript" src='<c:url value="/js/vendor/ui-bootstrap/ui-bootstrap-tpls-0.3.0.js"/>'></script>

<!-- Application files -->
<script type="text/javascript" src="<c:url value="/js/app/app.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/app/filters.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/app/controllers.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/app/services.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/app/messages.js"/>"></script>

<script type="text/javascript" src="<c:url value="/js/modules/message-module.js"/>"></script>

</body>
</html>
