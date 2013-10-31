/*
 *  UnhandledQACtrl - Controller for logic related to listing questions and answers 
 * 
 */
angular
        .module('wcDashBoardApp')
        .controller(
                'UnhandledQACtrl',
                [
                        '$scope',
                        '$window',
                        '$log',
                        '$timeout',
                        '$filter',
                        'dashBoardService',
                        'fragaSvarCommonService',
                        'wcDialogService',
                        function UnhandledCertCtrl($scope, $window, $log, $timeout, $filter, dashBoardService, fragaSvarCommonService, wcDialogService) {
                            // init state
                            $scope.widgetState = {
                                doneLoading : false,
                                activeErrorMessageKey : null,
                                queryMode : false,
                                queryStartFrom : 0,
                                queryPageSize : 10,
                                totalCount : 0,
                                currentList : undefined,
                                queryFormCollapsed : true,
                                dpFromOpen : {
                                    open : false
                                },
                                dpToOpen : {
                                    open : false
                                },
                                dpAnswerOpen : {
                                    open : false
                                }
                            }

                            $scope.qaListUnhandled = {};
                            $scope.qaListQuery = {};
                            $scope.activeUnit = null;

                            $scope.statusList = [ {
                                label : 'Visa alla',
                                value : 'ALLA'
                            }, {
                                label : 'Alla som kräver åtgärd',
                                value : 'ALLA_OHANTERADE'
                            }, {
                                label : 'Markera som hanterad',
                                value : 'MARKERA_SOM_HANTERAD'
                            }, {
                                label : 'Komplettera',
                                value : 'KOMPLETTERING_FRAN_VARDEN'
                            }, {
                                label : 'Svara',
                                value : 'SVAR_FRAN_VARDEN'
                            }, {
                                label : 'Invänta svar från Försäkringskassan',
                                value : 'SVAR_FRAN_FK'
                            }, {
                                label : 'Ingen',
                                value : 'HANTERAD'
                            } ];

                            var defaultQuery = {
                                enhetsId : undefined, // set to chosen enhet
                                // before submitting query
                                questionFrom : "",
                                questionFromFK : false,
                                questionFromWC : false,
                                hsaId : "", // läkare
                                vidarebefordrad : undefined, // 3-state
                                // boolean
                                changedFrom : undefined,
                                changedTo : undefined,
                                vantarPaSelector : $scope.statusList[1],
                                replyLatest : undefined
                            }
                            
                            $scope.decorateList = function(list) {
                              angular.forEach(list, function(qa, key) {
                                fragaSvarCommonService.decorateSingleItemMeasure(qa);
                              });
                            }

                            $scope.doSearch = function() {
                                $log.debug("doSearch");
                                $scope.widgetState.queryMode = true;
                                $scope.widgetState.runningQuery = true;
                                $scope.widgetState.activeErrorMessageKey = null;
                                var toSend = $scope.prepareSearchFormForQuery($scope.qp, $scope.widgetState);
                                $scope.widgetState.lastQuery = toSend;
                                $timeout(function() {
                                    dashBoardService.getQAByQuery(toSend, function(successData) {
                                        $scope.widgetState.runningQuery = false;

                                        $scope.qaListQuery = successData.results;
                                        $scope.widgetState.currentList = $scope.qaListQuery;
                                        $scope.widgetState.totalCount = successData.totalCount;
                                        $scope.decorateList($scope.widgetState.currentList);

                                    }, function(errorData) {
                                        $scope.widgetState.runningQuery = false;
                                        $log.debug("Query Error");
                                        // TODO: real errorhandling
                                        $scope.widgetState.activeErrorMessageKey = "info.query.error";
                                    });

                                }, 1000);
                            }

                            $scope.fetchMore = function() {
                                $log.debug("fetchMore");
                                $scope.widgetState.queryMode = true;
                                $scope.widgetState.fetchingMoreInProgress = true;
                                $scope.widgetState.activeErrorMessageKey = null;
                                var queryInstance = $scope.widgetState.lastQuery;
                                queryInstance.startFrom = queryInstance.startFrom + queryInstance.pageSize;
                                $scope.widgetState.lastQuery = queryInstance;

                                $timeout(function() {
                                    dashBoardService.getQAByQueryFetchMore(queryInstance, function(successData) {
                                        $scope.widgetState.fetchingMoreInProgress = false;
                                        $scope.decorateList(successData.results);;
                                        for ( var i = 0; i < successData.results.length; i++) {
                                            $scope.qaListQuery.push(successData.results[i]);
                                        }

                                        $scope.widgetState.currentList = $scope.qaListQuery;
                                    }, function(errorData) {
                                        $scope.widgetState.fetchingMoreInProgress = false;
                                        $log.debug("Query Error");
                                        // TODO: real errorhandling
                                        $scope.widgetState.activeErrorMessageKey = "info.query.error";
                                    });

                                }, 1000);
                            }
                            $scope.resetSearchForm = function() {
                                $scope.qp = angular.copy(defaultQuery);
                                $scope.qp.vantarPaSelector = $scope.statusList[1];

                            }

                            $scope.prepareSearchFormForQuery = function(qp, ws) {

                                qp.enhetsId = $scope.activeUnit.id;
                                qp.vantarPa = qp.vantarPaSelector.value;
                                if (qp.changedFrom) {
                                    qp.changedFrom = $filter('date')(qp.changedFrom, 'yyyy-MM-dd');
                                }

                                if (qp.changedTo) {
                                    qp.changedTo = $filter('date')(qp.changedTo, 'yyyy-MM-dd');
                                }

                                if (qp.replyLatest) {
                                    qp.replyLatest = $filter('date')(qp.replyLatest, 'yyyy-MM-dd');
                                }

                                if (qp.questionFrom == "FK") {
                                    qp.questionFromFK = true;
                                    qp.questionFromWC = false;
                                } else if (qp.questionFrom == "WC") {
                                    qp.questionFromFK = false;
                                    qp.questionFromWC = true;
                                } else {
                                    qp.questionFromFK = false;
                                    qp.questionFromWC = false;
                                }

                                var queryInstance = {};
                                queryInstance.startFrom = ws.queryStartFrom;
                                queryInstance.pageSize = ws.queryPageSize;
                                queryInstance.filter = qp;
                                return queryInstance;
                            }

                            $scope.resetSearchForm();

                            // load all fragasvar for all units in usercontext

                            dashBoardService.getQA(function(data) {
                                $scope.widgetState.queryMode = false;
                                $scope.widgetState.doneLoading = true;
                                if (data != null) {
                                    $scope.widgetState.activeErrorMessageKey = null;
                                    $scope.qaListUnhandled = data;
                                    $scope.widgetState.currentList = $scope.qaListUnhandled;
                                    $scope.widgetState.totalCount = $scope.widgetState.currentList.length;
                                    $scope.decorateList($scope.widgetState.currentList);
                                    $scope.widgetState.queryMode = false;

                                } else {
                                    $scope.widgetState.activeErrorMessageKey = "error.unansweredcerts.couldnotbeloaded";
                                }
                            });

                            $scope.onVidareBefordradChange = function(qa) {
                                qa.updateInProgress = true;
                                fragaSvarCommonService
                                        .setVidareBefordradState(
                                                qa.internReferens,
                                                qa.vidarebefordrad,
                                                function(result) {
                                                    qa.updateInProgress = false;

                                                    if (result != null) {
                                                        qa.vidarebefordrad = result.vidarebefordrad;
                                                    } else {
                                                        qa.vidarebefordrad = !qa.vidarebefordrad;
                                                        wcDialogService
                                                                .showErrorMessageDialog("Kunde inte markera/avmarkera frågan som vidarebefordrad. Försök gärna igen för att se om felet är tillfälligt. Annars kan du kontakta supporten");
                                                    }
                                                });
                            }

                            $scope.setActiveUnit = function(unit) {
                                $log.debug("ActiveUnit is now:" + unit);
                                $scope.activeUnit = unit;
                                $scope.widgetState.queryMode = false;
                                $scope.widgetState.queryFormCollapsed = true;
                                $scope.widgetState.currentList = $filter('QAEnhetsIdFilter')($scope.qaListUnhandled, $scope.activeUnit.id);

                            }

                            // Calculate how many entities we have for a
                            // specific
                            // enhetsId
                            $scope.getItemCountForUnitId = function(unit) {
                                if (!$scope.widgetState.doneLoading) {
                                    return "?";
                                }
                                var count = $filter('QAEnhetsIdFilter')($scope.qaListUnhandled, unit.id).length;

                                return count;
                            }

                            $scope.openIntyg = function(intygsReferens) {
                                $log.debug("open intyg " + intygsReferens.intygsId);
                                $window.location.href = "/m/" + intygsReferens.intygsTyp.toLowerCase() + "/webcert/intyg/" + intygsReferens.intygsId;
                            }

                            // Handle vidarebefordra dialog
                            $scope.openMailDialog = function(qa) {
                                $timeout(function() {
                                    fragaSvarCommonService.handleVidareBefodradToggle(qa, $scope.onVidareBefordradChange);
                                }, 1000);
                                // Launch mail client
                                $window.location = fragaSvarCommonService.buildMailToLink(qa);
                            }


                            $scope.toggleDatePickerInstance = function(instance) {
                                $timeout(function() {
                                    instance.open = !instance.open;
                                });
                            }


                        } ]);