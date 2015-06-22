<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>
<html lang="sv" id="ng-app" ng-app="webcert">
<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=Edge" />
<meta name="ROBOTS" content="nofollow, noindex" />
<meta name="viewport" content="width=device-width, initial-scale=1">

<title><spring:message code="application.name" /></title>

<link rel="stylesheet" href="/web/webjars/bootstrap/3.1.1/css/bootstrap.min.css">
<link rel="stylesheet" href="/web/webjars/bootstrap/3.1.1/css/bootstrap-theme.min.css">
<link rel="stylesheet" href="/web/webjars/common/webcert/css/inera-webcert.css?<spring:message code="buildNumber" />"
      media="screen">
<link rel="stylesheet" href="/web/webjars/common/css/animate.min.css?<spring:message code="buildNumber" />"
      media="screen">
<link rel="stylesheet" href="/web/webjars/common/css/inera-certificate.css?<spring:message code="buildNumber" />"
      media="screen">
<link rel="stylesheet" href="/web/webjars/common/webcert/css/print.css?<spring:message code="buildNumber" />"
      media="print">

<script type="text/javascript">
  // Global JS config/constants for this app, to be used by scripts
  var MODULE_CONFIG = {
    BUILD_NUMBER: '<spring:message code="buildNumber" />',
    USERCONTEXT: <sec:authentication property="principal.asJson" htmlEscape="false"/>,
    USE_MINIFIED_JAVASCRIPT: '<c:out value="${useMinifiedJavaScript}"/>'
  }
</script>

</head>
<body>

  <%-- ui-view that holds dynamic content managed by angular app --%>
  <div ui-view autoscroll="true" id="view">
  </div>

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
    <c:when test="${useMinifiedJavaScript == 'true'}">
      <script type="text/javascript" src="/web/webjars/jquery/1.9.0/jquery.min.js"></script>
      <script type="text/javascript" src="/web/webjars/angularjs/1.2.27/angular.min.js"></script>
      <script type="text/javascript" src="/web/webjars/angularjs/1.2.27/i18n/angular-locale_sv-se.js"></script>
      <script type="text/javascript" src="/web/webjars/angularjs/1.2.27/angular-cookies.min.js"></script>
      <script type="text/javascript" src="/web/webjars/angularjs/1.2.27/angular-route.min.js"></script>
      <script type="text/javascript" src="/web/webjars/angularjs/1.2.27/angular-sanitize.min.js"></script>
      <script type="text/javascript" src="/web/webjars/angular-ui-bootstrap/0.11.2/ui-bootstrap-tpls.min.js"></script>
      <script type="text/javascript" src="/web/webjars/angular-ui-router/0.2.13/angular-ui-router.min.js"></script>
      <script type="text/javascript" src="/web/webjars/angularjs/1.2.27/angular-animate.min.js"></script>
      <script type="text/javascript" src="/web/webjars/momentjs/2.7.0/min/moment.min.js"></script>
      <script type="text/javascript" src="/vendor/polyfill.min.js?<spring:message code="buildNumber" />"></script>
      <script type="text/javascript" src="/vendor/angular-smooth-scroll.js"></script>
      <script type="text/javascript" src="/js/app.min.js?<spring:message code="buildNumber" />"></script>
    </c:when>
    <c:otherwise>
	  <script type="text/javascript" src="/web/webjars/jquery/1.9.0/jquery.js"></script>
      <script type="text/javascript" src="/web/webjars/angularjs/1.2.27/angular.js"></script>
      <script type="text/javascript" src="/web/webjars/angularjs/1.2.27/i18n/angular-locale_sv-se.js"></script>
      <script type="text/javascript" src="/web/webjars/angularjs/1.2.27/angular-cookies.js"></script>
      <script type="text/javascript" src="/web/webjars/angularjs/1.2.27/angular-sanitize.js"></script>
      <script type="text/javascript" src="/web/webjars/angular-ui-bootstrap/0.11.2/ui-bootstrap-tpls.js"></script>
      <script type="text/javascript" src="/web/webjars/angular-ui-router/0.2.13/angular-ui-router.js"></script>
      <script type="text/javascript" src="/web/webjars/angularjs/1.2.27/angular-animate.js"></script>
      <script type="text/javascript" src="/web/webjars/momentjs/2.7.0/moment.js"></script>
      <script type="text/javascript" src="/vendor/polyfill.js"></script>
      <script type="text/javascript" src="/vendor/angular-smooth-scroll.js"></script>
      <script type="text/javascript" src="/js/app.js"></script>
    </c:otherwise>
  </c:choose>
  <script type="text/javascript" src="/vendor/netid-1.0.5.js"></script>

  <!--[if lte IE 8]>
  <script type="text/javascript" src="/web/webjars/respond/1.4.2/src/respond.js"></script>
  <![endif]-->
</body>
</html>
