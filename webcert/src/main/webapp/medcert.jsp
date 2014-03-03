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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"
         trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html lang="sv">
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge"/>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <meta name="ROBOTS" content="nofollow, noindex"/>

    <title>Medcert Legacy integration</title>

    <link rel="icon" href="<c:url value="/favicon.ico" />" type="image/vnd.microsoft.icon"/>

    <link rel="stylesheet" href="<c:url value="/css/bootstrap/2.3.2/bootstrap.css"/>">
    
    <script type="text/javascript" src="<c:url value="/js/vendor/angular/1.1.5/angular.js"/>"></script>
    
    <style type="text/css">
        textarea {
            font-family: Consolas, Lucida Console, monospace;
            font-size: 0.7em;
        }
    </style>

    <script type="text/javascript">
	
		var formApp = angular.module('formApp', []);

	
		function formController($scope, $window) {

			$scope.formData = {};
						
			$scope.changeLocation = function() {
				var loc = '/medcert/web/user/certificate/' + $scope.formData.intygId + '/questions';
				$window.location.href = loc;
			};
		};
	</script>
</head>
<body ng-app="formApp" ng-controller="formController">
<div class="container">
<div class="col-md-6 col-md-offset-3">

	<!-- PAGE TITLE -->
	<div class="page-header">
		<h1>Medcert integration</h1>
	</div>

	<!-- FORM -->
	<form ng-submit="changeLocation()">
		<!-- NAME -->
		<div id="name-group" class="form-group" ng-class="{ 'has-error' : errorName }">
			<label>Intygs id</label>
			<input type="text" name="intygId" class="form-control" ng-model="formData.intygId">
		</div>

		<!-- SUBMIT BUTTON -->
		<button type="submit" class="btn btn-success">K&ouml;r!</button>
	</form>

	<!-- SHOW DATA FROM INPUTS AS THEY ARE BEING TYPED -->
	<pre>
		{{ formData }}
	</pre>

</div>
</div>
</body>
</html>
