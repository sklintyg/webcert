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
<%@ page language="java" isErrorPage="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="ROBOTS" content="nofollow, noindex"/>

    <title><spring:message code="application.name"/></title>

    <link rel="icon" href="<c:url value="/favicon.ico" />" type="image/vnd.microsoft.icon"/>

    <link rel="stylesheet" href="<c:url value="/web/webjars/bootstrap/3.1.1/css/bootstrap.min.css"/>">
    <link rel="stylesheet" href="<c:url value="/web/webjars/bootstrap/3.1.1/css/bootstrap-theme.min.css"/>">
    <link rel="stylesheet" href="<c:url value="/web/webjars/common/css/inera-webcert.css"/>">
    <link rel="stylesheet" href="<c:url value="/web/webjars/common/css/inera-certificate.css"/>">
</head>

<body class="start">

<div class="container-fluid">

  <div class="content-container">
    <div class="row">
        <div class="col-xs-6">
            <img class="pull-right" src="/img/webcert_big.png"/>
        </div>
        <div class="col-xs-6">

            <c:choose>
                <c:when test="${param.reason eq \"logout\"}">
                    <h1>
                        <spring:message code="info.loggedout.title"/>
                    </h1>

                    <div id="loggedOut" class="alert alert-info">
                        <spring:message code="info.loggedout.text"/>
                    </div>
                    <!-- reason: loggedout -->
                    <a href="/web/start" class="btn btn-success" id="loginBtn">Logga in</a>
                </c:when>

                <c:when test="${param.reason eq \"denied\"}">
                    <h1>
                        <spring:message code="error.noauth.title"/>
                    </h1>

                    <div id="noAuth" class="alert alert-warning">
                        <spring:message code="error.noauth.text"/>
                    </div>
                    <!-- reason: denied -->
                    <a href="/web/start" class="btn btn-success" id="loginBtn">Logga in</a>
                </c:when>

                <c:when test="${param.reason eq \"medarbetaruppdrag\"}">
                    <h1>
                        <spring:message code="error.medarbetaruppdrag.title"/>
                    </h1>

                    <div id="noAuth" class="alert alert-danger">
                        <spring:message code="error.medarbetaruppdrag.text"/>
                    </div>

                </c:when>

                <c:when test="${param.reason eq \"notfound\"}">
                    <h1>
                        <spring:message code="error.notfound.title"/>
                    </h1>

                    <div id="notFound" class="alert alert-danger">
                        <spring:message code="error.notfound.text"/>
                    </div>
                </c:when>

                <c:otherwise>
                    <h1>
                        <spring:message code="error.generictechproblem.title"/>
                    </h1>

                    <div id="genericTechProblem" class="alert alert-danger">
                        <spring:message code="error.generictechproblem.text"/>
                    </div>

                    <!-- reason: generic -->
                </c:otherwise>
            </c:choose>

            <!--
            Error:
            <c:catch>
             <c:out value="${pageContext.errorData.throwable.message}" />,

             Stacktrace:
            <c:forEach items="${pageContext.errorData.throwable.stackTrace}" var="element">
              <c:out value="${element}" />,
              </c:forEach>
             </c:catch>
          -->

        </div>
    </div>
  </div> <!-- content-container -->
	<div class="content-footer">
	   <p>Webcert använder kakor. <a href="#" onclick="toggle(); return false;">Läs mer om Kakor (cookies)</a></p>
	    <div class="bluebox" id="cookiejar">
	     <h3>Om Kakor (cookies)</h3>
	      <p>Så kallade kakor (cookies) används för att underlätta för besökaren på webbplatsen. En kaka är en textfil som lagras på din dator och som innehåller information. Denna webbplats använder så kallade sessionskakor. Sessionskakor lagras temporärt i din dators minne under tiden du är inne på en webbsida. Sessionskakor försvinner när du stänger din webbläsare. Ingen personlig information om dig sparas vid användning av sessionskakor. Om du inte accepterar användandet av kakor kan du stänga av det via din webbläsares säkerhetsinställningar. Du kan även ställa in webbläsaren så att du får en varning varje gång webbplatsen försöker sätta en kaka på din dator. </p><p><strong>Observera!</strong> Om du stänger av kakor i din webbläsare kan du inte logga in i Mina Intyg.</p><p>Allmän information om kakor (cookies) och lagen om elektronisk kommunikation finns på Post- och telestyrelsens webbplats.</p>
	      <p>
	       <a href='http://www.pts.se/sv/Bransch/Regler/Lagar/Lag-om-elektronisk-kommunikation/Cookies-kakor/' target='_blank'>Mer om kakor (cookies) på Post- och telestyrelsens webbplats</a>
	      </p>
	    </div>
	 </div>
	</div>

  <script>
    var cookiejar = document.getElementById('cookiejar');
    cookiejar.style.visibility = 'hidden';

    function toggle() {
      if (cookiejar.style.visibility == 'hidden')
        cookiejar.style.visibility = 'visible';
      else
        cookiejar.style.visibility = 'hidden';
    }
  </script>

</div><!-- container-fluid -->

</body>
</html>
