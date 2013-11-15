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
                        '$cookieStore',
                        'dashBoardService',
                        'fragaSvarCommonService',
                        'wcDialogService',
                        function UnsignedCertCtrl($scope, $window, $log, $timeout, $filter, $cookieStore, dashBoardService, fragaSvarCommonService, wcDialogService) {
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
                            }

                            $scope.unsignedList = {};
                            $scope.activeUnit = {}
                            
                            dashBoardService.setActiveCareUnitViewCallback(function (unit) {
	                              $log.debug("ActiveUnit is now:" + unit);
	                              $scope.activeUnit = unit;
	                              $scope.widgetState.queryMode = false;
	                              $scope.widgetState.queryFormCollapsed = true;
	                          });


                        } ]);