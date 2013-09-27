/*
 *  UnhandledQACtrl - Controller for logic related to listing questions and answers 
 * 
 */
angular.module('wcDashBoardApp').controller('UnhandledQACtrl', [ '$scope', '$window', '$log', '$timeout','$filter', 'dashBoardService', function UnhandledCertCtrl($scope, $window, $log, $timeout, $filter, dashBoardService) {
 // init state
    $scope.widgetState = {
        doneLoading : false,
        hasError : false
    }
    
    $scope.qaList = {};
    $scope.activeUnit = "";

    // load all fragasvar for all units in usercontext
    $timeout(function() { // wrap in timeout to simulate latency - remove soon
    dashBoardService.getQA(function(data) {
        $scope.widgetState.doneLoading = true;
        if (data != null) {
            $scope.qaList = data;
            $log.debug("got Data!"); 
        } else {
            $scope.widgetState.hasError = true;
        }
    });
    }, 1000);
    
    $scope.setActiveUnit = function(unit) {
        $log.debug("ActiveUnit is now:" + unit); 
        $scope.activeUnit = unit;
    }

    $scope.getItemCountForUnitId = function(unit) {
        
        var count = $filter('QAEnhetsIdFilter')($scope.qaList, unit.id).length;
        $log.debug("Count for " + unit.namn + " is " + count); 
        return count;
    }
} ]);