<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
<div class="container-fluid" style="margin-bottom: 8px">
  <div class="row">
    <h4>Välj inloggning</h4>
    <div class="alert alert-danger" ng-show="showELegWarning">
      <h5>Har du Telia e-legitimation?</h5>
      <p>Webbläsarna Chrome och Edge stödjer inte den metod som Telia e-legitimation använder för att skapa signaturer. För att kunna signera intyg i Webcert måste du därför välja en annan webbläsare.</p>
      <p>Rekommenderad webbläsare är Internet Explorer 11 eller efterföljande versioner.</p>
    </div>
  </div>
  <div class="row">

    <div class="buttonbar">
      <a href="/saml/login/alias/defaultAlias?idp=<spring:eval expression="@webcertProps.getProperty('sakerhetstjanst.saml.idp.metadata.url')" />"
         class="btn btn-success" id="loginBtn">SITHS-kort</a>

      <a href="/saml/login/alias/eleg?idp=<spring:eval expression="@webcertProps.getProperty('cgi.funktionstjanster.saml.idp.metadata.url')" />"
         class="btn btn-success" id="loginBtn2" style="margin-left: 20px;">E-legitimation</a>
    </div>
  </div>
</div>

<p class="text-expander" ><a href="#" ng-click="toggleLoginDesc($event)" ng-bind-html="loginDescText"> </a></p>

<div uib-collapse="collapseLoginDesc" class="well">
  <h4>SITHS-kort</h4>

  <p>Du som arbetar i en organisation som är ansluten till HSA-katalogen samt har medarbetaruppdrag ”Vård och behandling” loggar in med SITHS-kort.</p>

  <p>Problem med inloggning med SITHS-kort?</p>
  <p><a href="#" ng-click="open('/app/views/index/loginMetoder/loginMetoderSithsHelp.html')">Läs mer om hur du kan felsöka</a></p>

  <h4>E-legitimation</h4>

  <p>Du som är läkare enligt Socialstyrelsens register över legitimerad hälso- och sjukvårdspersonal (HOSP)
    men inte arbetar inom en organisation som är ansluten till HSA-katalogen loggar in med e-legitimation.</p>

  <p>Problem med inloggning med e-legitimation?</p>
  <p><a href="#" ng-click="open('/app/views/index/loginMetoder/loginMetoderElegHelp.html')">Läs mer om hur du kan felsöka</a></p>

</div>
