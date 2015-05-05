<%@ page language="java" isErrorPage="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html lang="sv">
<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=Edge" />
<meta name="ROBOTS" content="nofollow, noindex" />
<meta name="viewport" content="width=device-width, initial-scale=1">

<title><spring:message code="application.name" /></title>

<link rel="stylesheet" href="/web/webjars/bootstrap/3.1.1/css/bootstrap.min.css" />
<link rel="stylesheet" href="/web/webjars/bootstrap/3.1.1/css/bootstrap-theme.min.css" />
<link rel="stylesheet" href="/web/webjars/common/webcert/css/inera-webcert.css" />
<link rel="stylesheet" href="/web/webjars/common/css/inera-certificate.css" />

</head>
<body class="start">

  <div class="container-fluid">
    <div class="content-container">
      <div class="row">
        <div class="col-xs-6">
          <img class="pull-right" src="/img/webcert_big.png" />
        </div>
        <div class="col-xs-6">
          <c:choose>
            <c:when test="${param.reason eq \"logout\"}">
              <h1><spring:message code="info.loggedout.title" /></h1>
              <div id="loggedOut" class="alert alert-info">
                <spring:message code="info.loggedout.text" />
              </div>
              <a href="/saml/login" class="btn btn-success" id="loginBtn">Logga in</a>
            </c:when>

            <c:when test="${param.reason eq \"timeout\"}">
              <h1><spring:message code="info.loggedout.title" /></h1>
              <div id="loggedOut" class="alert alert-info">
                <spring:message code="error.sessiontimeout.text" />
              </div>
              <a href="/saml/login" class="btn btn-success" id="loginBtn">Logga in</a>
            </c:when>

            <c:when test="${param.reason eq \"timeout_integration\"}">
              <h1><spring:message code="info.loggedout.title" /></h1>
              <div id="loggedOut" class="alert alert-info">
                <spring:message code="error.sessiontimeout.integration.text" />
              </div>
            </c:when>

            <c:when test="${param.reason eq \"denied\"}">
              <h1><spring:message code="error.noauth.title" /></h1>
              <div id="noAuth" class="alert alert-warning">
                <spring:message code="error.noauth.text" />
              </div>
              <a href="/saml/login" class="btn btn-success" id="loginBtn">Logga in</a>
            </c:when>


            <c:when test="${param.reason eq \"login.medarbetaruppdrag\"}">
              <h1><spring:message code="error.login.medarbetaruppdrag.title" /></h1>
              <div id="noAuth" class="alert alert-danger">
                <spring:message code="error.login.medarbetaruppdrag.text" />
              </div>
            </c:when>

            <c:when test="${param.reason eq \"login.failed\"}">
              <h1><spring:message code="error.login.failed.title" /></h1>
              <div id="noAuth" class="alert alert-danger">
                <spring:message code="error.login.failed.text" />
              </div>
            </c:when>

            <c:when test="${param.reason eq \"login.hsaerror\"}">
              <h1><spring:message code="error.login.hsaerror.title" /></h1>
              <div id="noAuth" class="alert alert-danger">
                <spring:message code="error.login.hsaerror.text" />
              </div>
            </c:when>

            <c:when test="${param.reason eq \"notfound\"}">
              <h1><spring:message code="error.notfound.title" /></h1>
              <div id="notFound" class="alert alert-danger">
                <spring:message code="error.notfound.text" />
              </div>
            </c:when>

            <c:otherwise>
              <h1><spring:message code="error.generictechproblem.title" /></h1>
              <div id="genericTechProblem" class="alert alert-danger">
                <spring:message code="error.generictechproblem.text" />
              </div>
            </c:otherwise>
          </c:choose>

          <!--
            <c:catch>
              Error:
              <c:out value="${pageContext.errorData.throwable.message}" />,
              Stacktrace:
              <c:forEach items="${pageContext.errorData.throwable.stackTrace}" var="element">
                <c:out value="${element}" />,
              </c:forEach>
            </c:catch>
          -->
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
          <a href='http://www.pts.se/sv/Bransch/Regler/Lagar/Lag-om-elektronisk-kommunikation/Cookies-kakor/'
             target='_blank'>Mer om kakor (cookies) på Post- och telestyrelsens webbplats</a>
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
    window.dialogDoneLoading = true;
    window.rendered = true;

  </script>

</body>
</html>
