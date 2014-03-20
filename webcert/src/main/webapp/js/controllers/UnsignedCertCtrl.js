define([
], function () {
    'use strict';

    /*
     * Controller for logic related to listing unsigned certs
     */
    return ['$scope', '$window', '$log', function ($scope, $window, $log) {
        // init state
        $scope.widgetState = {
            doneLoading : true,
            activeErrorMessageKey : null,
            queryFormCollapsed : true,
            queryMode : false,
            queryStartFrom : 0,
            queryPageSize : 10,
            totalCount : 0,
            currentList : undefined
        };

        $scope.unsignedList = {};
        $scope.activeUnit = {};

        $scope.$on('select-care-unit', function (event, unit) {
            $log.debug('ActiveUnit is now:' + unit);
            $scope.activeUnit = unit;
            $scope.widgetState.queryMode = false;
            $scope.widgetState.queryFormCollapsed = true;
        });
    }];
});