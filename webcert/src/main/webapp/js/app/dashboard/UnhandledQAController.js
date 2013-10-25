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
                                queryPageSize: 10,
                                totalCount : 0,
                                currentList : undefined
                            }

                            $scope.isCollapsed = true;

                            $scope.qaListUnhandled = {};
                            $scope.qaListQuery = {};
                            $scope.activeUnit = null;

                            $scope.statusList = [ {
                                label : 'Visa Alla',
                                value : 'ALLA'
                            }, {
                                label : 'Alla ohanterade',
                                value : 'ALLA_OHANTERADE'
                            }, {
                                label : 'Att markeras som hanterad',
                                value : 'MARKERA_SOM_HANTERAD'
                            }, {
                                label : 'Komplettering från vårdenheten',
                                value : 'KOMPLETTERING_FRAN_VARDEN'
                            }, {
                                label : 'Svar från vårdenheten',
                                value : 'SVAR_FRAN_VARDEN'
                            }, {
                                label : 'Svar från Försäkringskassan',
                                value : 'SVAR_FRAN_FK'
                            }, {
                                label : 'Inget - frågan är hanterad',
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
                                vantarPaSelector : $scope.statusList[0],
                                replyLatest : undefined
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
                                        $scope.widgetState.activeErrorMessageKey = "error.query.error";
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
                                        $scope.decorateList(successData.results);
                                        for(var i = 0; i < successData.results.length; i++) {
                                            $scope.qaListQuery.push(successData.results[i]);
                                        }
                                       
                                        $scope.widgetState.currentList = $scope.qaListQuery;
                                    }, function(errorData) {
                                        $scope.widgetState.fetchingMoreInProgress = false;
                                        $log.debug("Query Error");
                                        // TODO: real errorhandling
                                        $scope.widgetState.activeErrorMessageKey = "error.query.error";
                                    });

                                }, 1000);
                            }
                            $scope.resetSearchForm = function() {
                                $scope.qp = angular.copy(defaultQuery);
                                $scope.qp.vantarPaSelector = $scope.statusList[0];

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

                            $scope.decorateList = function(list) {

                                angular.forEach(list, function(qa, key) {

                                    if (qa.status == "ANSWERED" || qa.amne == "MAKULERING" || qa.amne == "PAMINNELSE") {
                                        qa.vantarpaResKey = "markhandled";
                                    } else if (qa.status == "CLOSED") {
                                        qa.vantarpaResKey = "handled";
                                    } else if (qa.amne == "KOMPLETTERING_AV_LAKARINTYG") {
                                        qa.vantarpaResKey = "komplettering";
                                    } else {

                                        if (qa.status == "PENDING_INTERNAL_ACTION") {
                                            qa.vantarpaResKey = "svarfranvarden";
                                        } else if (qa.status == "PENDING_EXTERNAL_ACTION") {
                                            qa.vantarpaResKey = "svarfranfk";
                                        } else {
                                            qa.vantarpaResKey = "";
                                            $log.debug("warning: undefined status");
                                        }
                                    }
                                });
                            }

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
                                $scope.isCollapsed = true;
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

                            // Search filter date controls
                            $scope.today = function() {
                                $scope.dtF = new Date();
                                $scope.dtT = new Date();
                                $scope.dtA = new Date();
                            };
                            $scope.today();

                            $scope.showWeeks = false;
                            $scope.minDate = null;

                            $scope.dpFromOpen = false;
                            $scope.openDPFrom = function() {
                                $timeout(function() {
                                    $scope.dpFromOpen = !$scope.dpFromOpen;
                                });
                            };

                            $scope.dpToOpen = false;
                            $scope.openDPTo = function() {
                                $timeout(function() {
                                    $scope.dpToOpen = !$scope.dpToOpen;
                                });
                            };

                            $scope.dpAnswerOpen = false;
                            $scope.openDPAnswer = function() {
                                $timeout(function() {
                                    $scope.dpAnswerOpen = !$scope.dpAnswerOpen;
                                });
                            };

                            $scope.dateOptions = {
                                'year-format' : "'yy'",
                                'starting-day' : 1
                            };

                        } ]);