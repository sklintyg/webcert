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

angular.module('webcert').factory('webcert.enhetArendenService',
    [ '$log', '$filter', '$cookies', 'common.enhetArendenCommonService', 'webcert.enhetArendenProxy', 'webcert.enhetArendenModel',
    function($log, $filter, $cookies, enhetArendenProxy, enhetArendenCommonService, enhetArendenModel) {
        'use strict';

        function decorateList(list) {
            angular.forEach(list, function(qa) {
                enhetArendenCommonService.decorateSingleItemMeasure(qa);
            });
        }

        //$scope.filterForm
        function prepareFilterQuery(filterForm, enhetId, scopeFilterQuery) {

            // Converts view values and sets them on a copy of query object
            var filterQuery = angular.copy(scopeFilterQuery);

            if (enhetId === 'wc-all') {
                filterQuery.enhetId = undefined;
            } else {
                filterQuery.enhetId = enhetId;
            }

            filterQuery.vantarPa = filterForm.vantarPaSelector.value;

            if (filterForm.lakareSelector) {
                filterQuery.hsaId = filterForm.lakareSelector.hsaId;
            }

            if (filterForm.changedFrom) {
                filterQuery.changedFrom = $filter('date')(filterForm.changedFrom, 'yyyy-MM-dd');
            }
            else {
                filterQuery.changedFrom = undefined;
            }

            if (filterForm.changedTo) {
                filterQuery.changedTo = $filter('date')(filterForm.changedTo, 'yyyy-MM-dd');
            }
            else {
                filterQuery.changedTo = undefined;
            }

            if (filterForm.questionFrom === 'FK') {
                filterQuery.questionFromFK = true;
                filterQuery.questionFromWC = false;
            } else if (filterForm.questionFrom === 'WC') {
                filterQuery.questionFromFK = false;
                filterQuery.questionFromWC = true;
            } else {
                filterQuery.questionFromFK = false;
                filterQuery.questionFromWC = false;
            }
            if (filterForm.vidarebefordrad === 'default') {
                filterQuery.vidarebefordrad = undefined;
            } else {
                filterQuery.vidarebefordrad = filterForm.vidarebefordrad;
            }

            return filterQuery;
        }

        // TODO: remove scope
        function _getArenden($scope) {
            $scope.widgetState.activeErrorMessageKey = null;

            $cookies.putObject('enhetsId', enhetArendenModel.enhetId);

            var preparedQuery = prepareFilterQuery($scope.filterForm, enhetArendenModel.enhetId, $scope.filterQuery);
            $scope.filterQuery = preparedQuery;

            enhetArendenProxy.getArenden(preparedQuery, function(successData) {

                $log.debug('QuestionAnswer.getArenden success +++++++++++++++');

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
                if ($scope.filterQuery.pageSize > enhetArendenModel.PAGE_SIZE) {
                    $scope.filterQuery.pageSize = enhetArendenModel.PAGE_SIZE;
                    $scope.filterQuery.startFrom = $scope.filterQuery.savedStartFrom;
                    $scope.filterQuery.savedStartFrom = undefined;
                }

                decorateList($scope.widgetState.currentList);

                $log.log('Running query : ' + $scope.widgetState.runningQuery);
                $log.log('QuestionAnswer.getArenden success -------------------');

            }, function(errorData) {
                $log.debug('Query Error' + errorData);
                $log.log('QuestionAnswer.getArenden error ***************');
                $scope.widgetState.runningQuery = false;
                $scope.widgetState.activeErrorMessageKey = 'info.query.error';
            });
        }

        // Return public API for the service
        return {
            getArenden: _getArenden
        };
    }]);
