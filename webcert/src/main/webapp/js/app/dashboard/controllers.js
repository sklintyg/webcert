'use strict';

/* Controllers */
angular.module('wcDashBoardApp').controller('WebCertCtrl', [ '$scope', '$location', '$window', function WebCertCtrl($scope, $location, $window) {
    // Main controller
} ]);

angular.module('wcDashBoardApp').controller('ListUnsignedCertCtrl', [ '$scope', 'dashBoardService', '$timeout', function ListUnsignedCertCtrl($scope, dashBoardService, $timeout) {

    // init state
    $scope.widgetState = {
        doneLoading : false,
        error : false
    }

    $scope.wipCertList = [];

    // Load list
    var requestConfig = {
        "type" : "dashboard_unsigned.json",
        "careUnit" : "",
        "clinic" : []
    }
    $timeout(function() {
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
