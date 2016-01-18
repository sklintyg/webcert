/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Controller for logic related to listing questions and answers
 */
angular.module('webcert').controller('webcert.UnhandledQACtrl',
    ['$rootScope', '$cookies', '$filter', '$location', '$log', '$scope', '$timeout', '$window', 'common.dialogService',
        'common.fragaSvarCommonService', 'webcert.QuestionAnswer',
        function($rootScope, $cookies, $filter, $location, $log, $scope, $timeout, $window, dialogService,
            fragaSvarCommonService, QuestionAnswer) {
            'use strict';

            var PAGE_SIZE = 10;
            var enhetId = 'wc-all';

            $scope.widgetState = {
                doneLoading: true,
                runningQuery: false,
                activeErrorMessageKey: null,
                filteredYet: false,
                totalCount: 0,
                currentList: [],
                filterFormCollapsed: true
            };

            $scope.statusList = [
                {
                    label: 'Visa alla',
                    value: 'ALLA'
                },
                {
                    label: 'Visa alla ej hanterade',
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
                questionFrom: 'default',
                vidarebefordrad: 'default',
                vantarPaSelector: $scope.statusList[1],
                lakareSelector: $scope.lakareList[0]
            };

            var defaultQuery = {
                enhetId: undefined,
                startFrom: 0,
                pageSize: PAGE_SIZE,

                questionFromFK: false,
                questionFromWC: false,
                hsaId: undefined, // läkare
                vidarebefordrad: undefined, // 3-state

                changedFrom: undefined,
                changedTo: undefined,

                vantarPa: undefined
            };

            $scope.activeUnit = {};
            $scope.filterQuery = {};
            var unitStats = {};

            /**
             * Private functions
             */

            function decorateList(list) {
                angular.forEach(list, function(qa) {
                    fragaSvarCommonService.decorateSingleItemMeasure(qa);
                });
            }

            function prepareFilterQuery(enhetId, scopeFilterQuery) {

                // Converts view values and sets them on a copy of query object
                var filterQuery = angular.copy(scopeFilterQuery);

                if (enhetId === 'wc-all') {
                    filterQuery.enhetId = undefined;
                } else {
                    filterQuery.enhetId = enhetId;
                }

                filterQuery.vantarPa = $scope.filterForm.vantarPaSelector.value;

                if ($scope.filterForm.lakareSelector) {
                    filterQuery.hsaId = $scope.filterForm.lakareSelector.hsaId;
                }

                if (filterQuery.changedFrom) {
                    filterQuery.changedFrom = $filter('date')(filterQuery.changedFrom, 'yyyy-MM-dd');
                }

                if (filterQuery.changedTo) {
                    filterQuery.changedTo = $filter('date')(filterQuery.changedTo, 'yyyy-MM-dd');
                }

                if ($scope.filterForm.questionFrom === 'FK') {
                    filterQuery.questionFromFK = true;
                    filterQuery.questionFromWC = false;
                } else if ($scope.filterForm.questionFrom === 'WC') {
                    filterQuery.questionFromFK = false;
                    filterQuery.questionFromWC = true;
                } else {
                    filterQuery.questionFromFK = false;
                    filterQuery.questionFromWC = false;
                }
                if ($scope.filterForm.vidarebefordrad === 'default') {
                    filterQuery.vidarebefordrad = undefined;
                } else {
                    filterQuery.vidarebefordrad = $scope.filterForm.vidarebefordrad;
                }

                return filterQuery;
            }

            function getQA() {
                $scope.widgetState.activeErrorMessageKey = null;
                $cookies.putObject('enhetsId', enhetId);
                var preparedQuery = prepareFilterQuery(enhetId, $scope.filterQuery);
                $cookies.putObject('savedFilterQuery', preparedQuery);
                $scope.filterQuery = preparedQuery;

                QuestionAnswer.getQA(preparedQuery, function(successData) {

                    $log.debug('QuestionAnswer.getQA success +++++++++++++++');

                    $log.debug('--- preparedQuery : ' + JSON.stringify(preparedQuery));
                    $scope.widgetState.totalCount = successData.totalCount;

                    var qaListQuery = [];
                    if ($scope.filterQuery.startFrom === 0) {
                        // Get initial list
                        qaListQuery = successData.results;
                        $scope.widgetState.currentList = qaListQuery;
                    } else {
                        $scope.widgetState.fetchingMoreInProgress = false;
                        // Fetch more
                        qaListQuery = $scope.widgetState.currentList;
                        for (var i = 0; i < successData.results.length; i++) {
                            qaListQuery.push(successData.results[i]);
                        }
                    }

                    $scope.widgetState.runningQuery = false;

                    // If we temporarily pulled a bigger batch to set an initial state, reset page size to normal
                    if ($scope.filterQuery.pageSize > PAGE_SIZE) {
                        $scope.filterQuery.pageSize = PAGE_SIZE;
                        $scope.filterQuery.startFrom = $scope.filterQuery.savedStartFrom;
                        $scope.filterQuery.savedStartFrom = undefined;
                    }

                    decorateList($scope.widgetState.currentList);

                    $log.log('Running query : ' + $scope.widgetState.runningQuery);
                    $log.log('QuestionAnswer.getQA success -------------------');

                }, function(errorData) {
                    $log.debug('Query Error' + errorData);
                    $log.log('QuestionAnswer.getQA error ***************');
                    $scope.widgetState.runningQuery = false;
                    $scope.widgetState.activeErrorMessageKey = 'info.query.error';
                });
            }

            function selectVantarPaByValue(vantaValue) {
                for (var count = 0; count < $scope.statusList.length; count++) {
                    if ($scope.statusList[count].value === vantaValue) {
                        return $scope.statusList[count];
                    }
                }
                return $scope.statusList[0];
            }

            function resetFilterForm() {
                $cookies.remove('savedFilterQuery');
                $scope.filterQuery = angular.copy(defaultQuery);
                $scope.filterForm.vantarPaSelector = $scope.statusList[1];
                $scope.filterForm.lakareSelector = $scope.lakareList[0];
                $scope.filterForm.questionFrom = 'default';
                $scope.filterForm.vidarebefordrad = 'default';
            }

            function loadSearchForm() {

                // Check if cookie exists
                if ($cookies.getObject('savedFilterQuery') === undefined) {
                    resetFilterForm(); // Set default state for filter form
                } else {

                    // Load filter from cookie
                    $scope.filterQuery = $cookies.getObject('savedFilterQuery');

                    // If we saved an old query where we had fetched more load everything up to that page
                    if ($scope.filterQuery.startFrom > 0) {
                        $scope.filterQuery.pageSize = $scope.filterQuery.startFrom + $scope.filterQuery.pageSize;
                        $scope.filterQuery.savedStartFrom = $scope.filterQuery.startFrom;
                        $scope.filterQuery.startFrom = 0;
                    }

                    if ($scope.filterQuery.questionFromFK === false &&
                        $scope.filterQuery.questionFromWC === false) {
                        $scope.filterForm.questionFrom = 'default';
                    } else if ($scope.filterQuery.questionFromFK) {
                        $scope.filterForm.questionFrom = 'FK';
                    } else {
                        $scope.filterForm.questionFrom = 'WC';
                    }

                    if ($scope.filterQuery.vidarebefordrad === undefined) {
                        $scope.filterForm.vidarebefordrad = 'default';
                    } else {
                        $scope.filterForm.vidarebefordrad = $scope.filterQuery.vidarebefordrad;
                    }

                    if ($scope.filterForm.vantarPaSelector) {
                        $scope.filterForm.vantarPaSelector = selectVantarPaByValue($cookies
                            .getObject('savedFilterQuery').vantarPa);
                    } else {
                        $scope.filterForm.vantarPaSelector = $scope.statusList[1];
                    }
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
                QuestionAnswer.getQALakareList(unitId === 'wc-all' ? undefined : unitId, function(list) {

                    $scope.widgetState.loadingLakares = false;

                    $scope.lakareList = list;
                    if (list && (list.length > 0)) {
                        $scope.lakareList.unshift($scope.lakareListEmptyChoice);

                        if ($cookies.getObject('savedFilterQuery') &&
                            $cookies.getObject('savedFilterQuery').lakareSelector) {
                            $scope.filterQuery.lakareSelector = selectLakareByHsaId($cookies
                                .getObject('savedFilterQuery').lakareSelector.hsaId);
                        } else {
                            $scope.lakareSelector = $scope.lakareList[0];
                        }
                    }
                }, function() {
                    $scope.widgetState.loadingLakares = false;
                    $scope.lakareList = [];
                    $scope.lakareList.push({
                        hsaId: undefined,
                        name: '<Kunde inte hämta lista>'
                    });
                });
            }

            /**
             * Exposed view functions
             */

            $scope.resetFilterForm = function() {
                resetFilterForm();
                $scope.widgetState.runningQuery = true;
                getQA();
            };

            $scope.filterList = function() {
                $log.debug('filterList');
                $scope.filterQuery.startFrom = 0;
                $scope.widgetState.filteredYet = true;
                $scope.widgetState.runningQuery = true;
                getQA();
            };

            $scope.fetchMore = function() {
                $log.debug('fetchMore');
                $scope.filterQuery.startFrom += $scope.filterQuery.pageSize;
                $scope.widgetState.fetchingMoreInProgress = true;
                getQA();
            };

            $scope.isActiveUnitChosen = function() {
                // there is no better crossbrowser way to check activeunit == {} without using libraries
                // like JSON or jquery. The following is using a modified version of jquerys isEmptyObject
                // implementation.
                var name;
                for (name in $scope.activeUnit) {
                    if ($scope.activeUnit.hasOwnProperty(name)) {
                        return true;
                    }
                }
                return false;
            };

            $scope.onVidareBefordradChange = function(qa) {
                qa.updateInProgress = true;
                $log.debug('onVidareBefordradChange: fragaSvarId: ' + qa.internReferens + ' intysTyp: ' +
                qa.intygsReferens.intygsTyp);
                fragaSvarCommonService.setVidareBefordradState(qa.internReferens, qa.intygsReferens.intygsTyp,
                    qa.vidarebefordrad, function(result) {
                        qa.updateInProgress = false;

                        if (result !== null) {
                            qa.vidarebefordrad = result.vidarebefordrad;
                        } else {
                            qa.vidarebefordrad = !qa.vidarebefordrad;
                            dialogService
                                .showErrorMessageDialog('Kunde inte markera/avmarkera frågan som ' +
                                'vidarebefordrad. Försök gärna igen för att se om felet är tillfälligt. ' +
                                'Annars kan du kontakta supporten');
                        }
                    });
            };

            $scope.openIntyg = function(intygsReferens) {
                $log.debug('open intyg ' + intygsReferens.intygsId);
                $location.url('/fragasvar/' + intygsReferens.intygsTyp.toLowerCase() + '/' +
                intygsReferens.intygsId, true);
            };

            // Handle vidarebefordra dialog
            $scope.openMailDialog = function(qa) {
                $timeout(function() {
                    fragaSvarCommonService.handleVidareBefodradToggle(qa, $scope.onVidareBefordradChange);
                }, 1000);
                // Launch mail client
                $window.location = fragaSvarCommonService.buildMailToLink(qa);
            };

            // Broadcast by statService on poll
            $scope.$on('wc-stat-update', function(event, message) {
                unitStats = message;
                if (!$scope.widgetState.filteredYet && unitStats.fragaSvarValdEnhet === 0) {
                    $scope.widgetState.filterFormCollapsed = false;
                }
            });

            // Broadcast by wcCareUnitClinicSelector directive on load and selection
            $scope.$on('qa-filter-select-care-unit', function(event, unit) {

                $log.debug('on qa-filter-select-care-unit ++++++++++++++++');

                $log.debug('ActiveUnit is now:' + unit.id);
                $scope.activeUnit = unit;

                // If we change enhet then we probably don't want the same filter criterias
                if ($cookies.getObject('enhetsId') && $cookies.getObject('enhetsId') !== unit.id) {
                    resetFilterForm();
                }

                // Set unit id (reset search form resets it)
                $cookies.putObject('enhetsId', unit.id);
                enhetId = unit.id;

                $scope.widgetState.filteredYet = false; // so proper info message is displayed if no items are found
                $scope.widgetState.filterFormCollapsed = true; // collapse filter form so it isn't in the way

                initLakareList(unit.id); // Update lakare list for filter form
                $scope.widgetState.runningQuery = true;
                getQA();

                $log.debug('on qa-filter-select-care-unit ---------------');
            });

            var unbindLocationChange = $rootScope.$on('$locationChangeStart', function($event, newUrl, currentUrl) {
                fragaSvarCommonService.checkQAonlyDialog($scope, $event, newUrl, currentUrl, unbindLocationChange);
            });
            $scope.$on('$destroy', unbindLocationChange);

            // Load filter form from cookie if available (for first page load)
            loadSearchForm();
        }]);
