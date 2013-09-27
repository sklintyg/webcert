/*
 *  UnhandledQACtrl - Controller for logic related to listing questions and answers 
 * 
 */
angular.module('wcDashBoardApp').controller('UnhandledQACtrl', [ '$scope', '$window', '$log', 'dashBoardService', function UnhandledCertCtrl($scope, $window, $log, dashBoardService) {

    $scope.qaList = {};
    $scope.activeUnit = "";

    // load all fragasvar for all units in usercontext
    dashBoardService.getQA(function(data) {
        // $scope.widgetState.doneLoading = true;
        if (data != null) {
            $scope.qaList = data;
            $log.debug("got Data!"); 
        } else {
            // $scope.widgetState.hasError = true;
        }
    });

    $scope.setActiveUnit = function(unit) {
        $log.debug("ActiveUnit is now:" + unit); 
        $scope.activeUnit = unit;
    }

} ]);