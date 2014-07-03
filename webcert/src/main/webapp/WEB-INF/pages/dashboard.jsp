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
<html lang="sv" id="ng-app" ng-app="webcert">
<head>
<meta http-equiv="X-UA-Compatible" content="IE=Edge" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="ROBOTS" content="nofollow, noindex" />
<meta name="viewport" content="width=device-width, initial-scale=1">

<title><spring:message code="application.name" /></title>

<link rel="icon" href="<c:url value="/favicon.ico" />" type="image/vnd.microsoft.icon" />

<link rel="stylesheet" href="<c:url value="/web/webjars/bootstrap/3.1.1/css/bootstrap.min.css"/>">
<link rel="stylesheet" href="<c:url value="/web/webjars/bootstrap/3.1.1/css/bootstrap-theme.min.css"/>">
<link rel="stylesheet" href="<c:url value="/web/webjars/common/webcert/css/inera-webcert.css"/>" media="screen">
<link rel="stylesheet" href="<c:url value="/web/webjars/common/css/inera-certificate.css"/>" media="screen">
<link rel="stylesheet" href="<c:url value="/web/webjars/common/webcert/css/print.css"/>" media="print">

<SCRIPT LANGUAGE="VBScript">
    Function ControlExists(objectID)
    on error resume next
    ControlExists = IsObject(CreateObject(objectID))
    End Function
</SCRIPT>


<script type="text/javascript">
  /**
   Global JS config/constants for this app, to be used by scripts
   **/
  var MODULE_CONFIG = {
    USERCONTEXT : <sec:authentication property="principal.asJson" htmlEscape="false"/>,
    REQUIRE_DEV_MODE : '<c:out value="${requireDevMode}"/>'
  }
</script>

</head>

<body>
    <%-- ng-view that holds dynamic content managed by angular app --%>
    <div id="view" ng-view autoscroll="true"></div>

    <%-- No script to show at least something when javascript is off --%>
    <noscript>
        <h1>
          <span><spring:message code="error.noscript.title" /></span>
        </h1>
        <div class="alert alert-danger">
          <spring:message code="error.noscript.text" />
        </div>
    </noscript>

    <c:choose>
      <c:when test="${requireDevMode == 'true'}">
        <script type="text/javascript" data-main="/js/main" src="<c:url value="/web/webjars/requirejs/2.1.10/require.js"/>"></script>
      </c:when>
      <c:otherwise>
        <script type="text/javascript" data-main="/js/main.min" src="<c:url value="/web/webjars/requirejs/2.1.10/require.js"/>"></script>
      </c:otherwise>
    </c:choose>
      <script type="text/javascript" src="<c:url value="/vendor/netid.js"/>"></script>
      <script type="text/javascript" src="<c:url value="/siths.jsp"/>"></script>
    <!--[if lte IE 8]>
	    <script type="text/javascript" src="<c:url value="/web/webjars/respond/1.4.2/src/respond.js"/>"></script>
	 <![endif]-->
</body>
</html>
