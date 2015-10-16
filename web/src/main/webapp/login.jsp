<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<div class="container" style="margin-bottom: 20px">
  <div class="row">
    <h4>Välj inloggning</h4>
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