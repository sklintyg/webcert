<%@ page import="org.springframework.security.core.context.SecurityContextHolder"%><%@ page import="se.inera.webcert.hsa.model.WebCertUser"%><%@ page language="java" contentType="text/javascript; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<%
WebCertUser user = (WebCertUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
if ("urn:oasis:names:tc:SAML:2.0:ac:classes:TLSClient".equals(user.getAuthenticationScheme())) { %>

   var siths = (function () {

       var classInstance = {};

       var currentHsaId = '<sec:authentication property="principal.hsaId" htmlEscape="false"/>';

       var issuers = ['2.5.4.6=SE, 2.5.4.10=Carelink, 2.5.4.3=SITHS CA v3',
                      '2.5.4.6=SE, 2.5.4.10=SITHS CA, 2.5.4.3=SITHS CA TEST v3',
                      '2.5.4.6=SE, 2.5.4.10=SITHS CA, 2.5.4.3=SITHS CA TEST v4',
                      '2.5.4.6=SE, 2.5.4.10=Inera AB, 2.5.4.3=SITHS Type 1 CA v1 PP',
                      '2.5.4.6=SE, 2.5.4.10=Inera AB, 2.5.4.3=SITHS Type 1 CA v1'];

       function CheckNewEvent() {
           if (document.iID.GetProperty('EventPresent') == "true") {
               OnNewEvent();
           } else {
               setTimeout(CheckNewEvent, 1000);
           }
       }

       function OnNewEvent() {
           if (!isCardPresent()) {
               window.location.href = "/saml/logout";
           } else {
               CheckNewEvent();
           }
       }

       function isCardPresent() {
           for (var index = 0; index < 10; index++) {
               var hsaId = getHcc(index);
               if (hsaId === currentHsaId) {
                   return true;
               }
           }
           return false;
       }

       function getHcc(index) {
           try {
               var cert = document.iID.EnumProperty('Certificate', index);
               if (cert != '') {
                   var certParts = cert.split(';');
                   var issuer = certParts[4];
                   var subject = certParts[5];
                   if (issuers.indexOf(issuer) !== -1 && issuer !== subject) {
                       // Remember to remove the CA certificate, check if subject and issuer is the same

                       // Find where the serialnumber starts and remove everything before
                       var subjectSerial = subject.substring(subject.indexOf("2.5.4.5=") + 8);

                       // Find where the serialnumber ends and remove everything after
                       subjectSerial = subjectSerial.substring(0, subjectSerial.indexOf(","));
                       return subjectSerial;
                   }
               }
           } catch (e) {
           }
           return '';
       }

       function isIE() {
         return ((navigator.appName == 'Microsoft Internet Explorer') ||
             ((navigator.appName == 'Netscape') && (new RegExp("Trident/.*rv:([0-9]{1,}[\.0-9]{0,})").exec(navigator.userAgent) != null)));
       }

       classInstance.startup = function() {
         if (isIE()) {
             explorer = true;
             plugin = ControlExists("IID.iIDCtl");
           } else {
             explorer = false;
             plugin = navigator.mimeTypes["application/x-iid"];
           }
           if (plugin) {
               if (explorer) {
                   document.writeln("<OBJECT NAME='iID' CLASSID='CLSID:5BF56AD2-E297-416E-BC49-00B327C4426E' WIDTH=0 HEIGHT=0></OBJECT>");
               } else {
                   document.writeln("<OBJECT NAME='iID' TYPE='application/x-iid' WIDTH=0 HEIGHT=0></OBJECT>");
               }
           }
           OnNewEvent();
         };

       return classInstance;
   })();

   siths.startup();


<% } else { %>

<% } %>
