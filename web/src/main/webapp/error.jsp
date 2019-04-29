<%@ page language="java" isErrorPage="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

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
<html lang="sv" id="ng-app" ng-app="webcert.pub.login">
<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=Edge" />
<meta name="ROBOTS" content="nofollow, noindex" />
<meta name="viewport" content="width=device-width, initial-scale=1">

<title><spring:message code="application.name" /></title>

<!-- bower:css -->
<!-- endbower -->
<link rel="stylesheet" href="/app/webcert.css?<spring:message code="buildNumber" />">
<link rel="stylesheet" href="/web/webjars/common/webcert/wc-common.css?<spring:message code="buildNumber" />">

<c:choose>
  <c:when test="${pageAttributes.useMinifiedJavaScript == 'true'}">
    <script type="text/javascript" src="/bower_components/angular/angular.min.js?<spring:message code="buildNumber" />"></script>
    <script type="text/javascript" src="/bower_components/angular-i18n/angular-locale_sv-se.js?<spring:message code="buildNumber" />"></script>
    <script type="text/javascript" src="/bower_components/angular-bootstrap/ui-bootstrap-tpls.min.js?<spring:message code="buildNumber" />"></script>
    <script type="text/javascript" src="/bower_components/angular-ui-router/release/angular-ui-router.min.js?<spring:message code="buildNumber" />"></script>
    <script type="text/javascript" src="/public/login.controller.js"></script>
  </c:when>
  <c:otherwise>
    <!-- bower:js -->
    <script type="text/javascript" src="/bower_components/jquery/dist/jquery.js"></script>
    <script type="text/javascript" src="/bower_components/angular/angular.js"></script>
    <script type="text/javascript" src="/bower_components/angular-animate/angular-animate.js"></script>
    <script type="text/javascript" src="/bower_components/angular-cookies/angular-cookies.js"></script>
    <script type="text/javascript" src="/bower_components/angular-i18n/angular-locale_sv-se.js"></script>
    <script type="text/javascript" src="/bower_components/angular-sanitize/angular-sanitize.js"></script>
    <script type="text/javascript" src="/bower_components/angular-bootstrap/ui-bootstrap-tpls.js"></script>
    <script type="text/javascript" src="/bower_components/angular-ui-router/release/angular-ui-router.js"></script>
    <script type="text/javascript" src="/bower_components/bootstrap-sass/assets/javascripts/bootstrap.js"></script>
    <script type="text/javascript" src="/bower_components/momentjs/moment.js"></script>
    <script type="text/javascript" src="/bower_components/oclazyload/dist/ocLazyLoad.js"></script>
    <script type="text/javascript" src="/bower_components/highcharts/highcharts.js"></script>
    <!-- endbower -->
    <script type="text/javascript" src="/public/login.controller.js"></script>
  </c:otherwise>
</c:choose>

<script type="text/javascript" src="/web/webjars/common/app-shared/dynamiclink/dynamicLink.service.js"></script>
<script type="text/javascript" src="/web/webjars/common/webcert/components/text/dynamicLink.directive.js"></script>
<script type="text/javascript" src="/web/webjars/common/app-shared/unified-view/uvDomId.filter.js"></script>
<script type="text/javascript" src="/web/webjars/common/webcert/components/wcAlertMessage/wcAlertMessage.directive.js"></script>

</head>
<body class="start jsp" id="errorPage" ng-controller="LoginController">
  <div class="container-fluid">
    <div class="content-container">
      <div class="row">
        <div class="col-xs-6">
          <img class="pull-right" src="/img/webcert_big.png" />
        </div>
        <div class="col-xs-6">
          <c:choose>
            <c:when test="${param.reason eq 'logout'}">
              <h1><spring:message code="info.loggedout.title" /></h1>
              <wc-alert-message alert-id="loggedOut" alert-severity="info">
                <spring:message code="info.loggedout.text" />
              </wc-alert-message>
              <jsp:include page="login.jsp" />
            </c:when>

            <c:when test="${param.reason eq 'timeout'}">
              <h1><spring:message code="info.loggedout.title" /></h1>
              <wc-alert-message alert-id="loggedOut" alert-severity="info">
                <spring:message code="error.sessiontimeout.text" />
              </wc-alert-message>
              <jsp:include page="login.jsp" />
            </c:when>

            <c:when test="${param.reason eq 'timeout_integration'}">
              <h1><spring:message code="info.loggedout.title" /></h1>
              <wc-alert-message alert-id="loggedOut" alert-severity="info">
                <spring:message code="error.sessiontimeout.integration.text" />
              </wc-alert-message>
            </c:when>

            <c:when test="${param.reason eq 'denied'}">
              <h1><spring:message code="error.noauth.title" /></h1>
              <wc-alert-message alert-id="noAuth" alert-severity="warning">
                <spring:message code="error.noauth.text" />
              </wc-alert-message>
              <jsp:include page="login.jsp" />
            </c:when>

            <c:when test="${param.reason eq 'login.medarbetaruppdrag'}">
              <h1><spring:message code="error.login.medarbetaruppdrag.title" /></h1>
              <wc-alert-message alert-id="noAuth" alert-severity="danger">
                <spring:message code="error.login.medarbetaruppdrag.text" />
              </wc-alert-message>
            </c:when>

            <c:when test="${param.reason eq 'login.failed'}">
              <h1><spring:message code="error.login.failed.title" /></h1>
              <wc-alert-message alert-id="noAuth" alert-severity="danger">
                <spring:message code="error.login.failed.text" />
              </wc-alert-message>
            </c:when>

            <c:when test="${param.reason eq 'login.hsaerror'}">
              <h1><spring:message code="error.login.hsaerror.title" /></h1>
              <wc-alert-message alert-id="noAuth" alert-severity="danger">
                <spring:message code="error.login.hsaerror.text" />
              </wc-alert-message>
            </c:when>

            <c:when test="${param.reason eq 'missing-parameter'}">
              <h1><spring:message code="error.missing-parameter.title" /></h1>
              <wc-alert-message alert-id="missingParameter" alert-severity="danger">
                <spring:message code="error.missing-parameter.text" />
                <div>${fn:escapeXml(param.message)}</div>
              </wc-alert-message>
            </c:when>
            <c:when test="${param.reason eq 'integration.nocontent'}">
              <h1><spring:message code="error.integration.nocontent.title" /></h1>
              <wc-alert-message alert-id="noContent" alert-severity="danger">
                <spring:message code="error.integration.nocontent.text" />
              </wc-alert-message>
            </c:when>
            <c:when test="${param.reason eq 'notfound'}">
              <h1><spring:message code="error.notfound.title" /></h1>
              <wc-alert-message alert-id="notFound" alert-severity="danger">
                <spring:message code="error.notfound.text" />
              </wc-alert-message>
            </c:when>
            <c:when test="${param.reason eq 'auth-exception'}">
              <h1><spring:message code="error.auth-exception.title" /></h1>
              <wc-alert-message alert-id="notFound" alert-severity="danger">
                <spring:message code="error.auth-exception.text" />
              </wc-alert-message>
            </c:when>
            <c:when test="${param.reason eq 'auth-exception-sekretessmarkering'}">
              <h1><spring:message code="error.auth-exception-sekretessmarkering.title" /></h1>
              <wc-alert-message alert-id="notFound" alert-severity="danger">
                <spring:message code="error.auth-exception-sekretessmarkering.text" />
              </wc-alert-message>
            </c:when>
            <c:when test="${param.reason eq 'enhet.auth.exception'}">
              <h1><spring:message code="error.enhet.auth.exception.title" /></h1>
              <wc-alert-message alert-id="notFound" alert-severity="danger">
                <spring:message code="error.enhet.auth.exception.text" arguments="${fn:escapeXml(param.enhetHsaId)}" />
              </wc-alert-message>
            </c:when>
            <c:when test="${param.reason eq 'pu-problem'}">
              <h1><spring:message code="error.pu-problem.title" /></h1>
              <wc-alert-message alert-id="notFound" alert-severity="danger">
                <spring:message code="error.pu-problem.text" />
              </wc-alert-message>
            </c:when>
            <c:when test="${param.reason eq 'sekretessapproval.needed'}">
              <h1><spring:message code="error.sekretessapproval.needed.title" /></h1>
              <wc-alert-message alert-id="notFound" alert-severity="danger">
                <spring:message code="error.sekretessapproval.needed.text" />
              </wc-alert-message>
              <c:if test="${param.showlogin eq 'true'}">
                <spring:message code="error.sekretessapproval.needed.showlogin" />
              </c:if>

            </c:when>
            <c:otherwise>
              <h1><spring:message code="error.generictechproblem.title" /></h1>
              <wc-alert-message alert-id="genericTechProblem" alert-severity="danger">
                <spring:message code="error.generictechproblem.text" />
              </wc-alert-message>
            </c:otherwise>
          </c:choose>

        </div>
      </div>
    </div>

    <div class="content-footer">
      <p>Webcert använder kakor. <a href="#" onclick="toggle(); return false;">Läs mer om Kakor (cookies)</a></p>
      <div class="bluebox" id="cookiejar">
        <h3>Om Kakor (cookies)</h3>
        <p>
          Så kallade kakor (cookies) används för att underlätta för besökaren på webbplatsen. En kaka är en textfil som
          lagras på din dator och som innehåller information. Denna webbplats använder så kallade sessionskakor.
          Sessionskakor lagras temporärt i din dators minne under tiden du är inne på en webbsida. Sessionskakor
          försvinner när du stänger din webbläsare. Ingen personlig information om dig sparas vid användning av
          sessionskakor. Om du inte accepterar användandet av kakor kan du stänga av det via din webbläsares
          säkerhetsinställningar. Du kan även ställa in webbläsaren så att du får en varning varje gång webbplatsen
          försöker sätta en kaka på din dator.</p>
        <p>
          <strong>Observera!</strong> Om du stänger av kakor i din webbläsare kan du inte logga in i Webcert.</p>
        <p>
          Allmän information om kakor (cookies) och lagen om elektronisk kommunikation finns på Post- och telestyrelsens
          webbplats.</p>
        <p>
          <span dynamiclink key="ptsCookiesMerOm" />
        </p>
      </div>
    </div>
  </div>

  <script>
    var cookiejar = document.getElementById('cookiejar');
    cookiejar.style.visibility = 'hidden';

    function toggle() {
      if (cookiejar.style.visibility === 'hidden') {
        cookiejar.style.visibility = 'visible';
      } else {
        cookiejar.style.visibility = 'hidden';
      }
    };

    window.doneLoading = true;
    window.rendered = true;

  </script>

</body>
</html>
