'use strict';

/* Controllers */
angular.module('wcDashBoardApp').controller('WebCertCtrl', [ '$scope', '$location', '$window', function WebCertCtrl($scope, $location, $window) {
    // Main controller
} ]);


/*
 *  ListUnsignedCertCtrl - Controller for logic related to displaying the list of unsigned certificates 
 * 
 */
angular.module('wcDashBoardApp').controller('ListUnsignedCertCtrl', [ '$scope', 'dashBoardService', '$timeout', function ListUnsignedCertCtrl($scope, dashBoardService, $timeout) {

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
angular.module('wcDashBoardApp').controller('UnansweredCertCtrl', [ '$scope', 'dashBoardService', '$timeout', function UnansweredCertCtrl($scope, dashBoardService, $timeout) {

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
    }, 2500);
} ]);

/*
 *  ReadyToSignCertCtrl - Controller for logic related to displaying the list of certificates ready to mass-sign on the dashboard 
 *  
 * 
 */
angular.module('wcDashBoardApp').controller('ReadyToSignCertCtrl', [ '$scope', 'dashBoardService', '$timeout', function ReadyToSignCertCtrl($scope, dashBoardService, $timeout) {

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
    }, 1500);
} ]);