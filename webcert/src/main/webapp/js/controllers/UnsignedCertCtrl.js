define([
], function () {
    'use strict';

    /*
     * Controller for logic related to listing unsigned certs
     */
    return ['$scope', '$window', '$log', 'User', 'dashBoardService', function ($scope, $window, $log, User, dashBoardService) {
        // init state
        $scope.widgetState = {
            doneLoading : true,
            activeErrorMessageKey : null,
            queryFormCollapsed : true,
            searchedYet : false,
            queryStartFrom : 0,
            queryPageSize : 10,
            totalCount : 0,
            currentList : undefined
        };

        $scope.valdVardenhet = User.getValdVardenhet();

        $scope.widgetState.doneLoading = false;
        dashBoardService.getUnsignedCertificates(function(data){
            $scope.widgetState.doneLoading = true;
            $scope.widgetState.currentList = data;
            $scope.widgetState.totalCount = $scope.widgetState.currentList.length;
        });
    }];
});