<%@ page import="org.springframework.security.core.context.SecurityContextHolder"%><%@ page import="se.inera.intyg.webcert.web.service.user.dto.WebCertUser"%><%@ page language="java" contentType="text/javascript; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

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

<%
WebCertUser user = (WebCertUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
if ("urn:oasis:names:tc:SAML:2.0:ac:classes:TLSClient".equals(user.getAuthenticationScheme())) { %>

    (function () {

        var currentHsaId = '<sec:authentication property="principal.hsaId" htmlEscape="false"/>';

        function log(message) {
            try {
              if (window.console) {
                console.log(message);
              }
            } catch (e) {}

            logServer(message);
        }

        function logServer(message) {
            var http = new XMLHttpRequest();
            http.open('POST', '/api/jslog/debug');
            http.send(message);
        }

        function CheckNewEvent() {
            if (iid_GetProperty('EventPresent') === 'true') {
                OnNewEvent();
            } else {
                setTimeout(CheckNewEvent, 1000);
            }
        }

        function OnNewEvent() {
            if (!isCardPresent()) {
                log('No card present, logging out');
                window.location.href = "/saml/logout/";
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

        // Synced with SithsSpec.js to be able to test it.
        function getSerialFromSubject(subject) {

          // Find where the serialnumber starts and remove everything before
          var subjectSerial = subject.substring(subject.indexOf('2.5.4.5=') + 8);

          // Find where the serialnumber ends and remove everything after
          var subjectSerialEndIndex = subjectSerial.indexOf(',');
          if (subjectSerialEndIndex == -1) {
            // There are no more commas in the string, assume serial runs to the end of it
            subjectSerial = subjectSerial.substring(0);
          } else {
            // There are more commas, cut the serial from the '=' to the next ','
            subjectSerial = subjectSerial.substring(0, subjectSerialEndIndex);
          }

          return subjectSerial;
        }

        function getHcc(index) {
            try {
                var cert = iid_EnumProperty('Certificate', index);
                if (cert !== '') {
                    var certParts = cert.split(';');
                    var issuer = certParts[4];
                    var subject = certParts[5];
                    log('Issuer: ' + issuer);
                    log('Subject: ' + subject);

                    var subjectSerial = getSerialFromSubject(subject);

                    log('SubjectSerial: ' + subjectSerial);

                    return subjectSerial;
                }
            } catch (e) {
                log('Error: ' + e.message);
            }
            return '';
        }

        OnNewEvent();
    })();
<%
}
%>
