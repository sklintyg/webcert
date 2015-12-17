<%@ page contentType="text/html;charset=UTF-8" language="java"
	session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html lang="sv">
<head>
<title>Webcert - Health Check</title>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=Edge" />
<meta name="ROBOTS" content="nofollow, noindex" />

<link rel="stylesheet" href="/web/webjars/bootstrap/3.1.1/css/bootstrap.min.css" />
<link rel="stylesheet"href="/web/webjars/bootstrap/3.1.1/css/bootstrap-theme.min.css">
</head>
<body>
	<div class="container">
		<div class="page-header">
			<h1>Webcert HealthCheck</h1>
		</div>

		<c:set var="dbStatus" value="${healthcheck.checkDB()}" />
		<c:set var="jmsStatus" value="${healthcheck.checkJMS()}" />
		<c:set var="hsaAuthorizationmanagementStatus" value="${healthcheck.checkHsaAuthorizationmanagement()}" />
		<c:set var="hsaEmployeeStatus" value="${healthcheck.checkHsaEmployee()}" />
		<c:set var="hsaOrganizationStatus" value="${healthcheck.checkHsaOrganization()}" />
		<c:set var="intygstjanstStatus"
			value="${healthcheck.checkIntygstjanst()}" />
		<c:set var="signatureQueueStatus"
			value="${healthcheck.checkSignatureQueue()}" />
		<c:set var="uptime" value="${healthcheck.checkUptimeAsString()}" />

		<div class="table-responsive">
			<table class="table table-bordered table-striped">
				<thead>
					<tr>
						<th>Check</th>
						<th>Tid</th>
						<th>Status</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>Koppling databas</td>
						<td id="dbMeasurement">${dbStatus.measurement}ms</td>
						<td id="dbStatus" class="${dbStatus.ok ? "text-success" : "text-danger"}">${dbStatus.ok ? "OK" : "FAIL"}</td>
					</tr>
					<tr>
						<td>Koppling ActiveMQ</td>
						<td id="jmsMeasurement">${jmsStatus.measurement}ms</td>
						<td id="jmsStatus" class="${jmsStatus.ok ? "text-success" : "text-danger"}">${jmsStatus.ok ? "OK" : "FAIL"}</td>
					</tr>

					<!-- New HSA over NTjP -->
					<tr>
						<td>Koppling HSA authorizationmanagement</td>
						<td id="hsaAuthorizationmanagementMeasurement">${hsaAuthorizationmanagementStatus.measurement}ms</td>
						<td id="hsaAuthorizationmanagementStatus" class="${hsaAuthorizationmanagementStatus.ok ? "text-success" : "text-danger"}">${hsaAuthorizationmanagementStatus.ok ? "OK" : "FAIL"}</td>
					</tr>
					<tr>
						<td>Koppling HSA employee</td>
						<td id="hsaEmployeeMeasurement">${hsaEmployeeStatus.measurement}ms</td>
						<td id="hsaEmployeeStatus" class="${hsaEmployeeStatus.ok ? "text-success" : "text-danger"}">${hsaEmployeeStatus.ok ? "OK" : "FAIL"}</td>
					</tr>
					<tr>
						<td>Koppling HSA organization</td>
						<td id="hsaOrganizationMeasurement">${hsaOrganizationStatus.measurement}ms</td>
						<td id="hsaOrganizationStatus" class="${hsaOrganizationStatus.ok ? "text-success" : "text-danger"}">${hsaOrganizationStatus.ok ? "OK" : "FAIL"}</td>
					</tr>
					<!-- End new HSA over NTjP -->

					<tr>
						<td>Koppling till Intygstjänst</td>
						<td id="intygstjanstMeasurement">${intygstjanstStatus.measurement}ms</td>
						<td id="intygstjanstStatus" class="${intygstjanstStatus.ok ? "text-success" : "text-danger"}">${intygstjanstStatus.ok ? "OK" : "FAIL"}</td>
					</tr>
					<tr>
						<td>Antal signerade intyg som ska skickas till Intygstjänst</td>
						<td id="signatureQueueMeasurement" colspan="2">${signatureQueueStatus.measurement} st</td>
					</tr>
					<tr>
						<td>Applikationens upptid</td>
						<td id="uptime" colspan="2">${uptime}</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</body>
</html>
