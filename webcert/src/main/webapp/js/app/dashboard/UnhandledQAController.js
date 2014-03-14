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
                        '$cookieStore',
                        'dashBoardService',
                        'fragaSvarCommonService',
                        'wcDialogService',
                        function UnhandledCertCtrl($scope, $window, $log, $timeout, $filter, $cookieStore, dashBoardService, fragaSvarCommonService, wcDialogService) {

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
                                form : {
                                  questionFrom : "default",
                                  vidarebefordrad : "default"
                                },
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
                            $scope.activeUnit = {};
                            
                            $scope.isActiveUnitChosen = function() {
                            	// there is no better crossbrowser way to check activeunit == {} without using libraries like JSON or jquery. The following is using a modified version of jquerys isEmptyObject implementation.
                              var name;
                              for(name in $scope.activeUnit) {
                                  return true;
                              }
                              return false;
                            }                            
                            
                            $scope.$on('qa-filter-select-care-unit', function (event, unit) {
	                              $log.debug("ActiveUnit is now:" + unit);
	                              $scope.activeUnit = unit;
	                              $scope.widgetState.queryMode = false;
	                              $scope.widgetState.queryFormCollapsed = true;
	
	                              //If we change enhet then we probably don't want the same filter criterias
	                              if($cookieStore.get("enhetsId") && $cookieStore.get("enhetsId")!=unit.id){
	                                  $scope.resetSearchForm();
	                              }
	                              $cookieStore.put("enhetsId" ,unit.id);
	
	                              $scope.initDoctorList(unit.id);
	                              $scope.widgetState.currentList = $filter('QAEnhetsIdFilter')($scope.qaListUnhandled, unit.id);
	
	                              //If we have a query stored, open the advanced filter
	                              if($cookieStore.get("query_instance")){
	                                  $scope.widgetState.queryFormCollapsed = false;
	                                  $scope.doSearch();
	                              }
	                          });

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

                            $scope.doctorListEmptyChoice = {
                                hsaId : undefined,
                                name : "Alla"
                            }
                            $scope.doctorList = [];
                            $scope.doctorList.push($scope.doctorListEmptyChoice);

                            var defaultQuery = {
                                enhetsId : undefined, // set to chosen enhet
                                // before submitting query
                                questionFromFK : false,
                                questionFromWC : false,
                                hsaId : undefined, // läkare
                                vidarebefordrad : undefined, // 3-state
                                // boolean
                                changedFrom : undefined,
                                changedTo : undefined,
                                vantarPaSelector : $scope.statusList[1],
                                doctorSelector : $scope.doctorList[0],
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

                            }

                            $scope.fetchMore = function() {
                                $log.debug("fetchMore");
                                $scope.widgetState.queryMode = true;
                                $scope.widgetState.fetchingMoreInProgress = true;
                                $scope.widgetState.activeErrorMessageKey = null;
                                var queryInstance = $scope.widgetState.lastQuery;
                                queryInstance.startFrom = queryInstance.startFrom + queryInstance.pageSize;
                                $scope.widgetState.lastQuery = queryInstance;

                                // Hack to make sure "default" value isn't sent to the API, it wants undefined.
                                // But angular can't mark radio buttons with "" or undefined values in IE8.
                                // So we set this to undefined here and the watch above will set it back to reflect the correct radio button choice in UI
                                if (queryInstance.filter.vidarebefordrad == "default") {
                                  queryInstance.filter.vidarebefordrad = undefined;
                                }

                                dashBoardService.getQAByQueryFetchMore(queryInstance, function(successData) {
                                      $scope.widgetState.fetchingMoreInProgress = false;
                                      $scope.decorateList(successData.results);

                                      for ( var i = 0; i < successData.results.length; i++) {
                                          $scope.qaListQuery.push(successData.results[i]);
                                      }

                                      $scope.widgetState.currentList = $scope.qaListQuery;
                                  }, function(errorData) {
                                      $scope.widgetState.fetchingMoreInProgress = false;
                                      $log.debug("Query Error");
                                      $scope.widgetState.activeErrorMessageKey = "info.query.error";
                                  }
                                );
                            }
                            $scope.resetSearchForm = function() {
                                $cookieStore.remove("query_instance");

                                $scope.qp = angular.copy(defaultQuery);
                                $scope.qp.vantarPaSelector = $scope.statusList[1];
                                $scope.qp.doctorSelector = $scope.doctorList[0];
                                $scope.widgetState.form.questionFrom = "default";
                                $scope.widgetState.form.vidarebefordrad = "default";
                            }

                            $scope.reloadSearchForm = function() {
                                if ($cookieStore.get("query_instance")) {
                                    $scope.qp = $cookieStore.get("query_instance").filter

                                    if($scope.qp.questionFromFK == false && $scope.qp.questionFromWC == false) {
                                      $scope.widgetState.form.questionFrom = "default";
                                    }
                                    else if($scope.qp.questionFromFK) {
                                        $scope.widgetState.form.questionFrom = "FK";
                                    }
                                    else {
                                        $scope.widgetState.form.questionFrom = "WC";
                                    }

                                    if($scope.qp.vidarebefordrad == undefined) {
                                      $scope.widgetState.form.vidarebefordrad = "default";
                                    }
                                    else{
                                      $scope.widgetState.form.vidarebefordrad = $scope.qp.vidarebefordrad;
                                    }

                                    if ($scope.qp.vantarPaSelector) {
                                        $scope.qp.vantarPaSelector = selectVantarPaByValue($cookieStore.get("query_instance").filter.vantarPaSelector.value);
                                    } else {
                                        $scope.qp.vantarPaSelector = $scope.statusList[1];
                                    }

                                } else {
                                    $scope.resetSearchForm();
                                }
                            }

                            $scope.prepareSearchFormForQuery = function(qp, ws) {

                                qp.enhetsId = $scope.activeUnit.id;
                                $cookieStore.put("enhetsId", qp.enhetsId);
                                qp.vantarPa = qp.vantarPaSelector.value;
                                
                                if (qp.doctorSelector) {
                                    qp.hsaId = qp.doctorSelector.hsaId;
                                }

                                if (qp.changedFrom) {
                                    qp.changedFrom = $filter('date')(qp.changedFrom, 'yyyy-MM-dd');
                                }

                                if (qp.changedTo) {
                                    qp.changedTo = $filter('date')(qp.changedTo, 'yyyy-MM-dd');
                                }

                                if (qp.replyLatest) {
                                    qp.replyLatest = $filter('date')(qp.replyLatest, 'yyyy-MM-dd');
                                }

                                if ($scope.widgetState.form.questionFrom == "FK") {
                                    qp.questionFromFK = true;
                                    qp.questionFromWC = false;
                                } else if ($scope.widgetState.form.questionFrom == "WC") {
                                    qp.questionFromFK = false;
                                    qp.questionFromWC = true;
                                } else {
                                    qp.questionFromFK = false;
                                    qp.questionFromWC = false;
                                }
                                if($scope.widgetState.form.vidarebefordrad == "default") {
                                  qp.vidarebefordrad = undefined;
                                }
                                else{
                                  qp.vidarebefordrad = $scope.widgetState.form.vidarebefordrad;
                                }

                                var queryInstance = {};
                                queryInstance.startFrom = ws.queryStartFrom;
                                queryInstance.pageSize = ws.queryPageSize;
                                queryInstance.filter = qp;
                                $cookieStore.put("query_instance", queryInstance);
                                return queryInstance;
                            }

                            $scope.reloadSearchForm();

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

                                    // If active unit is already set then do the
                                    // filtering
                                    if ($scope.activeUnit) {
                                        $scope.widgetState.currentList = $filter('QAEnhetsIdFilter')($scope.qaListUnhandled, $scope.activeUnit.id);
                                    }
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

                            $scope.initDoctorList = function(unitId) {
                                $scope.widgetState.loadingDoctors = true;
                                dashBoardService.getDoctorList(unitId, function(list) {
                                    $scope.widgetState.loadingDoctors = false;

                                    $scope.doctorList = list;
                                    if (list && (list.length > 0)) {
                                        $scope.doctorList.unshift($scope.doctorListEmptyChoice);

                                        if ($cookieStore.get("query_instance") && $cookieStore.get("query_instance").filter.doctorSelector) {
                                            $scope.qp.doctorSelector = selectDoctorByHsaId($cookieStore.get("query_instance").filter.doctorSelector.hsaId);
                                        } else {
                                            $scope.doctorSelector = $scope.doctorList[0];
                                        }
                                    }
                                }, function(errorData) {
                                    $scope.widgetState.loadingDoctors = false;
                                    $scope.doctorList = [];
                                    $scope.doctorList.push({
                                        hsaId : undefined,
                                        name : "<Kunde inte hämta lista>"
                                    });
                                });
                            }

                            function selectDoctorByHsaId(hsaId) {
                                for ( var count = 0; count < $scope.doctorList.length; count++) {
                                    if ($scope.doctorList[count].hsaId == hsaId) {
                                        return $scope.doctorList[count];
                                    }
                                }
                                return $scope.doctorList[0];
                            }
                            function selectVantarPaByValue(vantaValue) {
                                for ( var count = 0; count < $scope.statusList.length; count++) {
                                    if ($scope.statusList[count].value == vantaValue) {
                                        return $scope.statusList[count];
                                    }
                                }
                                return $scope.statusList[0];
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