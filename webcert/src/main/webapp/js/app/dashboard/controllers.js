'use strict';

/* Controllers */
angular.module('wcDashBoardApp').controller('WebCertCtrl', [ '$scope', '$window','$log','$location', function WebCertCtrl($scope, $window, $log, $location) {
    // Main controller
	
	$scope.createCert = function() {
		$location.path("/create");
	}
	
	$scope.viewCert = function(item) {
    $log.debug("open " + item.id);
    //listCertService.selectedCertificate = item;
    var path = "/m/" + item.typ.toLowerCase() + "/webcert/intyg/" + item.id + "#/view"
    $window.location.href = path;
	}
	
} ]);


/*
 *  CreateCertCtrl - Controller for logic related to creating a new certificate 
 * 
 */
angular.module('wcDashBoardApp').controller('CreateCertCtrl', [ '$scope', '$window', '$log', function CreateCertCtrl($scope, $window, $log) {

	$scope.editCert = function() {
    $log.debug("edit cert");
    var path = "/m/fk7263/webcert/intyg/new/edit#/edit";
    $window.location.href = path;
	}
	
} ]);


/*
 *  ListUnsignedCertCtrl - Controller for logic related to displaying the list of unsigned certificates 
 * 
 */
angular.module('wcDashBoardApp').controller('ListUnsignedCertCtrl', [ '$scope', 'dashBoardService', '$log', '$timeout', function ListUnsignedCertCtrl($scope, dashBoardService, $log, $timeout) {
    $log.debug("ListUnsignedCertCtrl init()");
    // init state
    $scope.widgetState = {
        showMin : 2,
        showMax : 99,
        pageSize : 2,
        doneLoading : false,
        hasError : false
    }

    $scope.wipCertList = [];

    $scope.$on("vardenhet", function(event, vardenhet) {
        // Make new call with careUnit
    });
    $scope.$on("mottagning", function(event, mottagning) {
        // Make new call with clinic
    });

    // Load list
    var requestConfig = {
        "type" : "dashboard_unsigned.json",
        "careUnit" : "",
        "clinic" : []
    }
    $timeout(function() { // wrap in timeout to simulate latency - remove soon
        dashBoardService.getCertificates(requestConfig, function(data) {
            $scope.widgetState.doneLoading = true;
            if (data != null) {
                $scope.wipCertList = data;
            } else {
                $scope.widgetState.hasError = true;
            }
        });
    }, 1000);
} ]);


/*
 *  UnansweredCertCtrl - Controller for logic related to displaying the list of unanswered questions 
 *  for a certificate on the dashboard.
 * 
 */
angular.module('wcDashBoardApp').controller('UnansweredCertCtrl', [ '$scope', 'dashBoardService', '$log', '$timeout', function UnansweredCertCtrl($scope, dashBoardService, $log, $timeout) {
    $log.debug("UnansweredCertCtrl init()");
    // init state
    $scope.widgetState = {
        showMin : 2,
        showMax : 99,
        pageSize : 2,
        doneLoading : false,
        hasError : false
    }

    $scope.qaCertList = [];

    // Load list
    var requestConfig = {
        "type" : "dashboard_unanswered.json",
        "careUnit" : "",
        "clinic" : []
    }
    $timeout(function() { // wrap in timeout to simulate latency - remove soon
        dashBoardService.getCertificates(requestConfig, function(data) {
            $scope.widgetState.doneLoading = true;
            if (data != null) {
                $scope.qaCertList = data;
            } else {
                $scope.widgetState.hasError = true;
            }
        });
    }, 500);
    
} ]);

/*
 *  ReadyToSignCertCtrl - Controller for logic related to displaying the list of certificates ready to mass-sign on the dashboard 
 *  
 * 
 */
angular.module('wcDashBoardApp').controller('ReadyToSignCertCtrl', [ '$scope', 'dashBoardService', '$log', '$timeout', function ReadyToSignCertCtrl($scope, dashBoardService, $log, $timeout) {
    $log.debug("ReadyToSignCertCtrl init()");
    // init state
    $scope.widgetState = {
        showMin : 2,
        showMax : 99,
        pageSize : 2,
        doneLoading : false,
        hasError : false
    }

    $scope.signCertList = [];

    // Load list
    var requestConfig = {
        "type" : "dashboard_readytosign.json",
        "careUnit" : "",
        "clinic" : []
    }
    $timeout(function() { // wrap in timeout to simulate latency - remove soon
        dashBoardService.getCertificates(requestConfig, function(data) {
            $scope.widgetState.doneLoading = true;
            if (data != null) {
                $scope.signCertList = data;
            } else {
                $scope.widgetState.hasError = true;
            }
        });
    }, 500);
} ]);

/*
 *  AboutWebcertCtrl - Controller for logic related to creating a new certificate 
 * 
 */
angular.module('wcDashBoardApp').controller('AboutWebcertCtrl', [ '$scope', '$window', function AboutWebcertCtrl($scope, $window) {

} ]);
