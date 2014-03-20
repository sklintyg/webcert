define([
], function () {
    'use strict';

    /*
     *  Controller for logic related to displaying the list of unsigned certificates
     */
    return ['$scope', 'dashBoardService', '$log', '$timeout',
        function ($scope, dashBoardService, $log, $timeout) {

            $log.debug('ListUnsignedCertCtrl init()');
            // init state
            $scope.widgetState = {
                showMin : 2,
                showMax : 99,
                pageSize : 2,
                doneLoading : false,
                hasError : false
            };

            $scope.wipCertList = [];

            $scope.$on('vardenhet', function (event, vardenhet) {
                // Make new call with careUnit
            });
            $scope.$on('mottagning', function (event, mottagning) {
                // Make new call with clinic
            });

            // Load list
            var requestConfig = {
                'type' : 'dashboard_unsigned.json',
                'careUnit' : '',
                'clinic' : []
            };

            $timeout(function () { // wrap in timeout to simulate latency - remove soon
                dashBoardService.getCertificates(requestConfig, function (data) {
                    $scope.widgetState.doneLoading = true;
                    if (data !== null) {
                        $scope.wipCertList = data;
                    } else {
                        $scope.widgetState.hasError = true;
                    }
                });
            }, 1000);
        }];
});
