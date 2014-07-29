<%@ page contentType="text/html;charset=UTF-8" language="java" session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
    <head>
        <title>Webcert - health check</title>
    </head>
    <body>
        <div class='indicator'>
            <c:set var="status" value="${healthcheck.ping}"/>
            <span class="name">Ping</span>
            <span class="value">${status.measurement}</span>
            <span class="status">${status.ok ? "OK" : "FAIL"}</span>
        </div>
        <div class='indicator'>
            <c:set var="status" value="${healthcheck.dbStatus}"/>
            <span class="name">Database</span>
            <span class="value">${status.measurement}</span>
            <span class="status">${status.ok ? "OK" : "FAIL"}</span>
        </div>
        <div class='indicator'>
            <c:set var="status" value="${healthcheck.hsaStatus}"/>
            <span class="name">HSA</span>
            <span class="value">${status.measurement}</span>
            <span class="status">${status.ok ? "OK" : "FAIL"}</span>
        </div>
        <div class='indicator'>
            <c:set var="status" value="${healthcheck.signaturQueueSize}"/>
            <span class="name">Signatur</span>
            <span class="value">${status.measurement}</span>
            <span class="status">${status.ok ? "OK" : "FAIL"}</span>
        </div>
        <div class='indicator'>
            <c:set var="status" value="${healthcheck.uptime}"/>
            <span class="name">Uptime</span>
            <span class="value">${status.measurement / 1000}</span>
            <span class="status">${status.ok ? "OK" : "FAIL"}</span>
        </div>
    </body>
</html>
