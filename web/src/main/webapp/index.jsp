<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html lang="sv" id="ng-app" ng-app="webcertIndex">
<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=Edge" />
<meta name="viewport" content="width=device-width, initial-scale=1">

<title><spring:message code="application.name" /></title>

<link rel="stylesheet" href="/web/webjars/bootstrap/3.1.1/css/bootstrap.min.css" />
<link rel="stylesheet" href="/web/webjars/bootstrap/3.1.1/css/bootstrap-theme.min.css" />
<link rel="stylesheet" href="/web/webjars/common/webcert/css/inera-webcert.css">
<link rel="stylesheet" href="/web/webjars/common/css/inera-certificate.css">

<c:choose>
  <c:when test="${useMinifiedJavaScript == 'true'}">
    <script type="text/javascript" src="/web/webjars/angularjs/1.2.27/angular.min.js"></script>
    <script type="text/javascript" src="/web/webjars/angularjs/1.2.27/i18n/angular-locale_sv-se.js"></script>
    <script type="text/javascript" src="/web/webjars/angular-ui-bootstrap/0.11.2/ui-bootstrap-tpls.min.js"></script>
    <script type="text/javascript" src="/web/webjars/angular-ui-router/0.2.13/angular-ui-router.min.js"></script>
    <script type="text/javascript" src="/pubapp/index.controller.js"></script>
  </c:when>
  <c:otherwise>
    <script type="text/javascript" src="/web/webjars/angularjs/1.2.27/angular.js"></script>
    <script type="text/javascript" src="/web/webjars/angularjs/1.2.27/i18n/angular-locale_sv-se.js"></script>
    <script type="text/javascript" src="/web/webjars/angular-ui-bootstrap/0.11.2/ui-bootstrap-tpls.js"></script>
    <script type="text/javascript" src="/web/webjars/angular-ui-router/0.2.13/angular-ui-router.js"></script>
    <script type="text/javascript" src="/pubapp/index.controller.js"></script>
  </c:otherwise>
</c:choose>

</head>
<body class="start jsp" id="indexPage" ng-controller="IndexController">
  <div class="container-fluid">
    <div class="content-container">
      <div class="row">
        <div class="col-xs-6">
          <img class="pull-right" src="/img/webcert_big.png" />
        </div>
        <div class="col-xs-6">
          <h1>Välkommen till Webcert</h1>
          <p>Webcert är en tjänst för att utfärda elektroniska läkarintyg. I Webcert kan du skriva läkarintyg och
            kommunicera med Försäkringskassan om läkarintyg FK 7263. För närvarande stödjer tjänsten följande intyg:
          <ul>
            <li>Läkarintyg FK 7263</li>
            <li>Transportstyrelsens läkarintyg</li>
            <li>Transportstyrelsens läkarintyg, diabetes</li>
          </ul>
          </p>

          <div class="container">
            <div class="row">
              <h4>Välj inloggning</h4>
            </div>
            <div class="row">

              <div class="col-xs-2">
                <a href="/saml/login/alias/defaultAlias?idp=<spring:eval expression="@webcertProps.getProperty('sakerhetstjanst.saml.idp.metadata.url')" />"
                   class="btn btn-success" id="loginBtn">SITHS-kort</a>
              </div>
              <div class="col-xs-10">
                <a href="/saml/login/alias/eleg?idp=<spring:eval expression="@webcertProps.getProperty('cgi.funktionstjanster.saml.idp.metadata.url')" />"
                   class="btn btn-success" id="loginBtn2">E-legitimation</a>
              </div>
            </div>
          </div>

          <p class="text-expander" ><a href="#" ng-click="toggleLoginDesc()" ng-bind-html="loginDescText"> </a></p>

          <div collapse="collapseLoginDesc" class="collapse well">
            <h4>SITHS-kort</h4>

            <p>Du som arbetar i en organisation som är ansluten till HSA-katalogen (Hälso- och sjukvårdens
              adressregister) samt har medarbetaruppdrag ”Vård och behandling” loggar in med SITHS-kort.</p>

            <p>Problem med inloggning med SITHS-kort?</p>
            <p><a href="#" ng-click="open('pubapp/siths.help.html')">Läs mer om hur du kan felsöka</a></p>

            <h4>E-legitimation</h4>

            <p>Du som är läkare enligt Socialstyrelsens register över legitimerad hälso- och sjukvårdspersonal (HOSP)
              men inte arbetar inom en organisation som är ansluten till HSA-katalogen loggar in med e-legitimation.</p>

            <p>Problem med inloggning med e-legitimation?</p>
            <p><a href="#" ng-click="open('pubapp/e-leg.help.html')">Läs mer om hur du kan felsöka</a></p>

          </div>

          <h4>Ny användare</h4>
          <p>Är du privatläkare och vill utfärda intyg i Webcert?</p>

          <p><a href="<spring:eval expression="@webcertProps.getProperty('private.practitioner.portal.registration.url')" />" >
            Registrea dig som användare</a>
          </p>
        </div>
      </div>
    </div>
  </div>
  <div class="content-footer">
    <p>Webcert använder kakor. <a href="#" ng-click="showCookieText = !showCookieText">Läs mer om Kakor (cookies)</a>
    </p>
    <div collapse="!showCookieText" class="bluebox">
      <h4>Om Kakor (cookies)</h4>
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
</body>
</html>
