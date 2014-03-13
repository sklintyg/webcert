define([
], function () {
    'use strict';

    /*
     * Controller for logic related to displaying the list of unanswered questions
     * for a certificate on the dashboard.
     */
    return ['$scope', 'dashBoardService', '$log', '$timeout',
        function ($scope, dashBoardService, $log, $timeout) {
            $log.debug('UnansweredCertCtrl init()');
            // init state
            $scope.widgetState = {
                showMin : 2,
                showMax : 99,
                pageSize : 2,
                doneLoading : false,
                hasError : false
            };

            $scope.qaCertList = [];

            // Load list
            var requestConfig = {
                'type' : 'dashboard_unanswered.json',
                'careUnit' : '',
                'clinic' : []
            };
            $timeout(function () { // wrap in timeout to simulate latency - remove soon
                dashBoardService.getCertificates(requestConfig, function (data) {
                    $scope.widgetState.doneLoading = true;
                    if (data !== null) {
                        $scope.qaCertList = data;
                    } else {
                        $scope.widgetState.hasError = true;
                    }
                });
            }, 500);
        }];
});
