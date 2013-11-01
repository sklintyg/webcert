/*
 *  UnsignedCertCtrl - Controller for logic related to listing unsigned certs 
 * 
 */
angular
        .module('wcDashBoardApp')
        .controller(
                'UnsignedCertCtrl',
                [
                        '$scope',
                        '$window',
                        '$log',
                        '$timeout',
                        '$filter',
                        'dashBoardService',
                        'fragaSvarCommonService',
                        'wcDialogService',
                        function UnsignedCertCtrl($scope, $window, $log, $timeout, $filter, dashBoardService, fragaSvarCommonService, wcDialogService) {
                            // init state
                            $scope.widgetState = {
                                doneLoading : false,
                                activeErrorMessageKey : null,
                            }

                            $scope.unsignedList = {};
                            $scope.activeUnit = null;

                        } ]);