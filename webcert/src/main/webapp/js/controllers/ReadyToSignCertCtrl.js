define([
], function () {
    'use strict';

    /*
     * Controller for logic related to displaying the list of certificates ready to mass-sign on the dashboard
     */
    return ['$scope', 'dashBoardService', '$log', '$timeout',
        function ($scope, dashBoardService, $log, $timeout) {
            $log.debug('ReadyToSignCertCtrl init()');
            // init state
            $scope.widgetState = {
                showMin : 2,
                showMax : 99,
                pageSize : 2,
                doneLoading : false,
                hasError : false
            };

            $scope.signCertList = [];

            // Load list
            var requestConfig = {
                'type' : 'dashboard_readytosign.json',
                'careUnit' : '',
                'clinic' : []
            };
            $timeout(function () { // wrap in timeout to simulate latency - remove soon
                dashBoardService.getCertificates(requestConfig, function (data) {
                    $scope.widgetState.doneLoading = true;
                    if (data !== null) {
                        $scope.signCertList = data;
                    } else {
                        $scope.widgetState.hasError = true;
                    }
                });
            }, 500);
        }];
});
