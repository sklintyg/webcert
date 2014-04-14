define(
    [],
    function () {
        'use strict';

        /*
         * Controller for logic related to listing questions and answers
         */
        return [
            '$scope', '$window', '$location', '$log', '$timeout', '$filter', '$cookieStore',
            'ManageCertificate', 'fragaSvarCommonService', 'QuestionAnswer', 'wcDialogService', 'User',
            function ($scope, $window, $location, $log, $timeout, $filter, $cookieStore,
                      ManageCertificate, fragaSvarCommonService, QuestionAnswer, wcDialogService, User) {

                // init state
                $scope.widgetState = {
                    doneLoading: false,
                    runningQuery: false,
                    activeErrorMessageKey: null,
                    searchedYet: false,
                    totalCount: 0,
                    currentList: undefined,
                    queryFormCollapsed: true,
                    dpFromOpen: {
                        open: false
                    },
                    dpToOpen: {
                        open: false
                    },
                    dpAnswerOpen: {
                        open: false
                    }
                };

                $scope.statusList = [
                    {
                        label: 'Visa alla',
                        value: 'ALLA'
                    },
                    {
                        label: 'Alla som kräver åtgärd',
                        value: 'ALLA_OHANTERADE'
                    },
                    {
                        label: 'Markera som hanterad',
                        value: 'MARKERA_SOM_HANTERAD'
                    },
                    {
                        label: 'Komplettera',
                        value: 'KOMPLETTERING_FRAN_VARDEN'
                    },
                    {
                        label: 'Svara',
                        value: 'SVAR_FRAN_VARDEN'
                    },
                    {
                        label: 'Invänta svar från Försäkringskassan',
                        value: 'SVAR_FRAN_FK'
                    },
                    {
                        label: 'Ingen',
                        value: 'HANTERAD'
                    }
                ];

                $scope.lakareListEmptyChoice = {
                    hsaId: undefined,
                    name: 'Alla'
                };
                $scope.lakareList = [];
                $scope.lakareList.push($scope.lakareListEmptyChoice);

                $scope.filterForm = {
                    questionFrom: "default",
                    vidarebefordrad: "default",
                    vantarPaSelector: $scope.statusList[1],
                    lakareSelector: $scope.lakareList[0]
                };

                var defaultQuery = {
                    enhetId: undefined, // set to chosen enhet
                    startFrom: 0,
                    pageSize: undefined,

                    questionFromFK: false,
                    questionFromWC: false,
                    hsaId: undefined, // läkare
                    vidarebefordrad: undefined, // 3-state

                    changedFrom: undefined,
                    changedTo: undefined,

                    vantarPa: undefined,
                    replyLatest: undefined
                };

                $scope.qaListUnhandled = {};
                $scope.qaListQuery = {};
                $scope.activeUnit = {};
                $scope.lastQuery = {};
                var unitStats = {};

                /**
                 * Private functions
                 */

                function decorateList(list) {
                    angular.forEach(list, function (qa) {
                        fragaSvarCommonService.decorateSingleItemMeasure(qa);
                    });
                }

                function prepareSearchFormForQuery(filterQuery) {

                    filterQuery.enhetId = $scope.activeUnit.id;
                    if (filterQuery.enhetId === "wc-all") {
                        filterQuery.enhetId = undefined;
                    }
                    $cookieStore.put('enhetId', filterQuery.enhetId);
                    filterQuery.vantarPa = $scope.filterForm.vantarPaSelector.value;

                    if (filterQuery.lakareSelector) {
                        filterQuery.hsaId = $scope.filterForm.lakareSelector.hsaId;
                    }

                    if (filterQuery.changedFrom) {
                        filterQuery.changedFrom = $filter('date')(filterQuery.changedFrom, 'yyyy-MM-dd');
                    }

                    if (filterQuery.changedTo) {
                        filterQuery.changedTo = $filter('date')(filterQuery.changedTo, 'yyyy-MM-dd');
                    }

                    if (filterQuery.replyLatest) {
                        filterQuery.replyLatest = $filter('date')(filterQuery.replyLatest, 'yyyy-MM-dd');
                    }

                    if ($scope.filterForm.questionFrom === "FK") {
                        filterQuery.questionFromFK = true;
                        filterQuery.questionFromWC = false;
                    } else if ($scope.filterForm.questionFrom === "WC") {
                        filterQuery.questionFromFK = false;
                        filterQuery.questionFromWC = true;
                    } else {
                        filterQuery.questionFromFK = false;
                        filterQuery.questionFromWC = false;
                    }
                    if ($scope.filterForm.vidarebefordrad === "default") {
                        filterQuery.vidarebefordrad = undefined;
                    } else {
                        filterQuery.vidarebefordrad = $scope.filterForm.vidarebefordrad;
                    }

                    $cookieStore.put('savedFilterQuery', filterQuery);
                    return filterQuery;
                }

                function getQA() {

                    $scope.widgetState.runningQuery = true;
                    $scope.widgetState.activeErrorMessageKey = null;
                    var toSend = prepareSearchFormForQuery($scope.filterQuery);
                    $scope.lastQuery = toSend;

                    QuestionAnswer.getQA(toSend, function (successData) {

                        $scope.widgetState.runningQuery = false;
                        $scope.qaListQuery = successData.results;
                        $scope.qaListUnhandled = $scope.qaListQuery;
                        $scope.widgetState.currentList = $scope.qaListQuery;
                        $scope.widgetState.totalCount = successData.totalCount;
                        decorateList($scope.widgetState.currentList);

                    }, function (errorData) {

                        $log.debug('Query Error' + errorData);
                        $scope.widgetState.runningQuery = false;
                        $scope.widgetState.activeErrorMessageKey = 'info.query.error';

                    });
                }

                function filterCurrentList(unit) {
                    if (unit.id === "wc-all") {
                        $scope.widgetState.currentList = angular.copy($scope.qaListUnhandled);
                    } else {
                        $scope.widgetState.currentList = $filter('QAEnhetsIdFilter')($scope.qaListUnhandled, unit.id);
                    }
                }

                function selectVantarPaByValue(vantaValue) {
                    for (var count = 0; count < $scope.statusList.length; count++) {
                        if ($scope.statusList[count].value === vantaValue) {
                            return $scope.statusList[count];
                        }
                    }
                    return $scope.statusList[0];
                }

                function resetSearchForm() {
                    $cookieStore.remove('savedFilterQuery');
                    $scope.filterQuery = angular.copy(defaultQuery);
                    $scope.filterForm.vantarPaSelector = $scope.statusList[1];
                    $scope.filterForm.lakareSelector = $scope.lakareList[0];
                    $scope.filterForm.questionFrom = "default";
                    $scope.filterForm.vidarebefordrad = "default";
                    getQA();
                }

                function loadSearchForm() {
                    if ($cookieStore.get('savedFilterQuery')) {
                        $scope.filterQuery = $cookieStore.get('savedFilterQuery');

                        if ($scope.filterQuery.questionFromFK === false && $scope.filterQuery.questionFromWC === false) {
                            $scope.filterForm.questionFrom = "default";
                        } else if ($scope.filterQuery.questionFromFK) {
                            $scope.filterForm.questionFrom = "FK";
                        } else {
                            $scope.filterForm.questionFrom = "WC";
                        }

                        if ($scope.filterQuery.vidarebefordrad === undefined) {
                            $scope.filterForm.vidarebefordrad = "default";
                        } else {
                            $scope.filterForm.vidarebefordrad = $scope.filterQuery.vidarebefordrad;
                        }

                        if ($scope.filterForm.vantarPaSelector) {
                            $scope.filterForm.vantarPaSelector = selectVantarPaByValue($cookieStore
                                .get('savedFilterQuery').vantarPa);
                        } else {
                            $scope.filterForm.vantarPaSelector = $scope.statusList[1];
                        }

                        getQA();

                    } else {
                        resetSearchForm();
                    }
                }

                function selectLakareByHsaId(hsaId) {
                    for (var count = 0; count < $scope.lakareList.length; count++) {
                        if ($scope.lakareList[count].hsaId === hsaId) {
                            return $scope.lakareList[count];
                        }
                    }
                    return $scope.lakareList[0];
                }

                function initLakareList(unitId) {
                    $scope.widgetState.loadingLakares = true;
                    QuestionAnswer.getQALakareList(unitId === "wc-all" ? undefined : unitId, function (list) {

                        $scope.widgetState.loadingLakares = false;

                        $scope.lakareList = list;
                        if (list && (list.length > 0)) {
                            $scope.lakareList.unshift($scope.lakareListEmptyChoice);

                            if ($cookieStore.get('savedFilterQuery') && $cookieStore.get('savedFilterQuery').lakareSelector) {
                                $scope.filterQuery.lakareSelector = selectLakareByHsaId($cookieStore
                                    .get('savedFilterQuery').lakareSelector.hsaId);
                            } else {
                                $scope.lakareSelector = $scope.lakareList[0];
                            }
                        }
                    }, function () {
                        $scope.widgetState.loadingLakares = false;
                        $scope.lakareList = [];
                        $scope.lakareList.push({
                            hsaId: undefined,
                            name: '<Kunde inte hämta lista>'
                        });
                    });
                }

                // load initial state
                loadSearchForm();
                $scope.widgetState.doneLoading = true;

                /**
                 * Exposed view functions
                 */

                $scope.resetSearchForm = function () {
                    resetSearchForm();
                };

                $scope.doSearch = function () {
                    $log.debug('doSearch');
                    $scope.filterQuery.startFrom = 0;
                    $scope.filterQuery.pageSize = undefined;
                    $scope.widgetState.searchedYet = true;
                    getQA();
                };

                $scope.fetchMore = function () {
                    $log.debug('fetchMore');
                    $scope.filterQuery.startFrom += $scope.filterQuery.pageSize;
                    $scope.filterQuery.pageSize = 10;
                    getQA();
                };

                $scope.isActiveUnitChosen = function () {
                    // there is no better crossbrowser way to check activeunit == {} without using libraries
                    // like JSON or jquery. The following is using a modified version of jquerys isEmptyObject
                    // implementation.
                    var name;
                    for (name in $scope.activeUnit) {
                        return true;
                    }
                    return false;
                };

                // Calculate how many entities we have for a specific enhetsId
                $scope.getItemCountForUnitId = function(unit) {
                    if (!$scope.widgetState.doneLoading) {
                        return '?';
                    }
                    var count = 0;

                    if (unit.id === "wc-all") {
                        count = $scope.qaListUnhandled.length;
                    }
                    else {
                        count = $filter('QAEnhetsIdFilter')($scope.qaListUnhandled, unit.id).length;
                    }

                    return count;
                };

                $scope.$on("wc-stat-update", function (event, message) {
                    unitStats = message;
                });

                $scope.$on('qa-filter-select-care-unit', function (event, unit) {
                    $log.debug('ActiveUnit is now:' + unit);
                    $scope.activeUnit = unit;
                    $scope.widgetState.queryFormCollapsed = true;

                    // If we change enhet then we probably don't want the same filter criterias
                    if ($cookieStore.get('enhetId') && $cookieStore.get('enhetId') !== unit.id) {
                        resetSearchForm();
                    }
                    $cookieStore.put('enhetId', unit.id);

                    initLakareList(unit.id);
                    filterCurrentList(unit);

                    // If we have a query stored, open the advanced filter
                    if ($cookieStore.get('savedFilterQuery')) {
                        //$scope.widgetState.queryFormCollapsed = false;
                        //getQA();
                    }
                });

                $scope.onVidareBefordradChange = function (qa) {
                    qa.updateInProgress = true;
                    fragaSvarCommonService
                        .setVidareBefordradState(
                        qa.internReferens,
                        qa.vidarebefordrad,
                        function (result) {
                            qa.updateInProgress = false;

                            if (result !== null) {
                                qa.vidarebefordrad = result.vidarebefordrad;
                            } else {
                                qa.vidarebefordrad = !qa.vidarebefordrad;
                                wcDialogService
                                    .showErrorMessageDialog('Kunde inte markera/avmarkera frågan som vidarebefordrad. Försök gärna igen för att se om felet är tillfälligt. Annars kan du kontakta supporten');
                            }
                        });
                };

                $scope.openIntyg = function (intygsReferens) {
                    $log.debug('open intyg ' + intygsReferens.intygsId);
                    $location.url('/' + intygsReferens.intygsTyp.toLowerCase() + "/view/" + intygsReferens.intygsId, true);
                };

                // Handle vidarebefordra dialog
                $scope.openMailDialog = function (qa) {
                    $timeout(function () {
                        fragaSvarCommonService.handleVidareBefodradToggle(qa, $scope.onVidareBefordradChange);
                    }, 1000);
                    // Launch mail client
                    $window.location = fragaSvarCommonService.buildMailToLink(qa);
                };

                $scope.toggleDatePickerInstance = function (instance) {
                    $timeout(function () {
                        instance.open = !instance.open;
                    });
                };
            } ];
    });


//getQA();

/*              QuestionAnswer.getQA(function (data) {
 $scope.widgetState.queryMode = false;
 $scope.widgetState.doneLoading = true;
 if (data !== null) {
 $scope.widgetState.activeErrorMessageKey = null;
 $scope.qaListUnhandled = data;
 $scope.widgetState.currentList = $scope.qaListUnhandled;
 $scope.widgetState.totalCount = $scope.widgetState.currentList.length;
 $scope.decorateList($scope.widgetState.currentList);
 $scope.widgetState.queryMode = false;

 // If active unit is already set then do the
 // filtering
 if ($scope.activeUnit) {
 filterCurrentList($scope.activeUnit);
 }
 } else {
 $scope.widgetState.activeErrorMessageKey = 'error.unansweredcerts.couldnotbeloaded';
 }
 });
 */


/*                $scope.fetchMore = function () {
 $log.debug('fetchMore');
 var queryInstance = $scope.lastQuery;
 queryInstance.startFrom += queryInstance.pageSize;
 $scope.lastQuery= queryInstance;
 $scope.widgetState.queryMode = true;
 $scope.widgetState.fetchingMoreInProgress = true;
 $scope.widgetState.activeErrorMessageKey = null;

 QuestionAnswer.getQA(queryInstance, function (successData) {
 $scope.widgetState.fetchingMoreInProgress = false;
 $scope.decorateList(successData.results);

 for (var i = 0; i < successData.results.length; i++) {
 $scope.qaListQuery.push(successData.results[i]);
 }

 $scope.widgetState.currentList = $scope.qaListQuery;
 }, function () {
 $scope.widgetState.fetchingMoreInProgress = false;
 $log.debug('Query Error');
 $scope.widgetState.activeErrorMessageKey = 'info.query.error';
 });
 };
 */