<%@ page language="java" contentType="text/javascript; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

var siths = (function () {

    var classInstance = {};

    var currentHsaId = '<sec:authentication property="principal.hsaId" htmlEscape="false"/>';

    function CheckNewEvent() {
        console.log("check new event");
        if (document.iID.GetProperty('EventPresent') == "true") {
            OnNewEvent();

        }
        else {
            setTimeout(CheckNewEvent, 333);
        }
    }

    function OnNewEvent() {

        if (!isCardPresent()) {
            window.location.href = "/saml/logout";
        }
        else {
            CheckNewEvent();
        }
    }

    function isCardPresent() {
        var index = 0;

        while (index < 10) {

            console.log("checking token at " + index);

            var hsaId = getHcc(index);
            console.log("HSA ID: " + hsaId);

            if (hsaId == currentHsaId) {
                return true;
            }

            index++;
        }

        return false;
    }

    function getHcc(index) {
        var cert = document.iID.EnumProperty('Certificate', index);
        if (cert != '') {
            var certParts = cert.split(';');
            var issuer = certParts[4];
            var subject = certParts[5];
            if (issuer == '2.5.4.6=SE, 2.5.4.10=Carelink, 2.5.4.3=SITHS CA v3' && issuer != subject) {
                // Remember to remove the CA certificate, check if subject and issuer is the same

                // Find where the serialnumber starts and remove everything before
                var subjectSerial = subject.substring(subject.indexOf("2.5.4.5=") + 8);

                // Find where the serialnumber ends and remove everything after
                subjectSerial = subjectSerial.substring(0, subjectSerial.indexOf(","));
                return subjectSerial;
            }

            else if (issuer == '2.5.4.6=SE, 2.5.4.10=SITHS CA, 2.5.4.3=SITHS CA TEST v3' && issuer != subject) {
                // Remember to remove the CA certificate, check if subject and issuer is the same

                // Find where the serialnumber starts and remove everything before
                var subjectSerial = subject.substring(subject.indexOf("2.5.4.5=") + 8);

                // Find where the serialnumber ends and remove everything after
                subjectSerial = subjectSerial.substring(0, subjectSerial.indexOf(","));
                return subjectSerial;
            }

            else if (issuer == '2.5.4.6=SE, 2.5.4.10=SITHS CA, 2.5.4.3=SITHS CA TEST v4' && issuer != subject) {
                // Remember to remove the CA certificate, check if subject and issuer is the same

                // Find where the serialnumber starts and remove everything before
                var subjectSerial = subject.substring(subject.indexOf("2.5.4.5=") + 8);

                // Find where the serialnumber ends and remove everything after
                subjectSerial = subjectSerial.substring(0, subjectSerial.indexOf(","));
                return subjectSerial;
            }
        }

        return '';
    }


    classInstance.startup = function() {
        if (navigator.appName.indexOf("Explorer") == -1) {
            explorer = false;
            plugin = navigator.mimeTypes["application/x-iid"];
        }
        else {
            explorer = true;
            plugin = ControlExists("IID.iIDCtl");
        }
        if (plugin) {
            if (explorer)
                document.writeln("<OBJECT NAME='iID' CLASSID='CLSID:5BF56AD2-E297-416E-BC49-00B327C4426E' WIDTH=0 HEIGHT=0></OBJECT>");
            else
                document.writeln("<OBJECT NAME='iID' TYPE='application/x-iid' WIDTH=0 HEIGHT=0></OBJECT>");
        }
        else {
            document.writeln("Software not installed");
        }
        OnNewEvent();
      };


    return classInstance;
})();

siths.startup();