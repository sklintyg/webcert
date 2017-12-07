/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

angular.module('webcert').directive('wcFragorOchSvarFilter', [
    '$log', '$cookies', 'common.ArendeProxy', 'webcert.enhetArendenService', 'webcert.enhetArendenModel', 'webcert.wcFragorOchSvarFilterModel',
    function($log, $cookies, enhetArendenProxy, enhetArendenService, enhetArendenModel, filterModel) {
        'use strict';

        return {
            restrict: 'E',
            transclude: false,
            replace: false,
            scope: {

            },
            templateUrl: '/app/views/fragorOchSvar/wcFragorOchSvarFilter/wcFragorOchSvarFilter.directive.html',
            controller: function($scope) {

                $scope.filterModel = filterModel;

                $scope.lakareListEmptyChoice = {
                    hsaId: undefined,
                    name: 'Alla'
                };
                $scope.lakareList = [];
                $scope.lakareList.push($scope.lakareListEmptyChoice);

                $scope.filterForm = {
                    questionFrom: 'default',
                    vidarebefordrad: 'default',
                    vantarPaSelector: filterModel.statusList[1],
                    lakareSelector: $scope.lakareList[0]
                };

                var defaultQuery = {
                    enhetId: undefined,
                    startFrom: 0,
                    pageSize: enhetArendenModel.PAGE_SIZE,

                    questionFromFK: false,
                    questionFromWC: false,
                    hsaId: undefined, // läkare
                    vidarebefordrad: undefined, // 3-state

                    changedFrom: undefined,
                    changedTo: undefined,

                    vantarPa: undefined
                };
                $scope.filterQuery = {};

                function resetFilterForm() {
                    $scope.filterQuery = angular.copy(defaultQuery);
                    $scope.filterForm.vantarPaSelector = filterModel.statusList[1];
                    $scope.filterForm.lakareSelector = $scope.lakareList[0];
                    $scope.filterForm.questionFrom = 'default';
                    $scope.filterForm.vidarebefordrad = 'default';
                    $scope.filterForm.changedFrom = undefined;
                    $scope.filterForm.changedTo = undefined;
                }

                function loadSearchForm() {
                    resetFilterForm(); // Set default state for filter form

                    // If we saved an old query where we had fetched more load everything up to that page
                    if ($scope.filterQuery.startFrom > 0) {
                        $scope.filterQuery.pageSize = $scope.filterQuery.startFrom + $scope.filterQuery.pageSize;
                        $scope.filterQuery.savedStartFrom = $scope.filterQuery.startFrom;
                        $scope.filterQuery.startFrom = 0;
                    }

                    if ($scope.filterQuery.questionFromFK === false && $scope.filterQuery.questionFromWC === false) {
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

                    if ($scope.filterQuery.changedFrom === undefined) {
                        $scope.filterForm.changedFrom = undefined;
                    } else {
                        $scope.filterForm.changedFrom = $scope.filterQuery.changedFrom;
                    }
                    if ($scope.filterQuery.changedTo === undefined) {
                        $scope.filterForm.changedTo = undefined;
                    } else {
                        $scope.filterForm.changedTo = $scope.filterQuery.changedTo;
                    }
                }

                function initLakareList(unitId) {
                    $scope.widgetState.loadingLakares = true;
                    enhetArendenProxy.getArendenLakareList(unitId === 'wc-all' ? undefined : unitId, function(list) {

                        $scope.widgetState.loadingLakares = false;

                        $scope.lakareList = list;
                        if (list && (list.length > 0)) {
                            $scope.lakareList.unshift($scope.lakareListEmptyChoice);
                            $scope.lakareSelector = $scope.lakareList[0];
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

                // Broadcast by wcCareUnitClinicSelector directive on load and selection
                $scope.$on('wcVardenhetFilter.unitSelected', function(event, unit) {

                    $log.debug('on wcVardenhetFilter.unitSelected ++++++++++++++++');

                    $log.debug('ActiveUnit is now:' + unit.id);
                    $scope.activeUnit = unit;

                    // If we change enhet then we probably don't want the same filter criterias
                    if ($cookies.getObject('enhetsId') && $cookies.getObject('enhetsId') !== unit.id) {
                        resetFilterForm();
                    }

                    // Set unit id (reset search form resets it)
                    $cookies.putObject('enhetsId', unit.id);
                    enhetArendenModel.enhetId = unit.id;

                    filterModel.filteredYet = false; // so proper info message is displayed if no items are found
                    filterModel.filterFormCollapsed = true; // collapse filter form so it isn't in the way

                    initLakareList(unit.id); // Update lakare list for filter form
                    $scope.widgetState.runningQuery = true;
                    enhetArendenService.getArenden($scope);

                    $log.debug('on wcVardenhetFilter.unitSelected ---------------');
                });

                // Load filter form (first page load)
                loadSearchForm();

                $scope.resetFilterForm = function() {
                    resetFilterForm();
                    $scope.widgetState.runningQuery = true;
                    enhetArendenService.getArenden($scope);
                };

                $scope.filterList = function() {
                    $log.debug('filterList');
                    $scope.filterQuery.startFrom = 0;
                    filterModel.filteredYet = true;
                    $scope.widgetState.runningQuery = true;
                    enhetArendenService.getArenden($scope);
                };

            }
        };
    }]);
