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
 * Controller for logic related to listing unsigned certs
 */
angular.module('webcert').controller('webcert.EjSigneradeUtkastCtrl',
    [ '$log', '$scope', '$timeout', '$window', 'common.dialogService', 'webcert.UtkastFilterModel', 'webcert.UtkastProxy', 'common.User',
        function($log, $scope, $timeout, $window, dialogService, UtkastFilterModel, UtkastProxy, User) {
            'use strict';

            // Constant settings
            var PAGE_SIZE = 10;

            $scope.filter = UtkastFilterModel.build(PAGE_SIZE);


            // Exposed page state variables
            $scope.widgetState = {

                // User context
                valdVardenhet: User.getValdVardenhet(),

                // Loading states
                doneLoading: true,
                runningQuery: false,
                fetchingMoreInProgress: false,

                // Error state
                activeErrorMessageKey: null,

                // Search states
                filteredYet: false,
                initialQueryWasEmpty: true,

                //Active query
                currentFilterRequest :  $scope.filter.convertToPayload(),

                // List data
                totalCount: 0,
                currentList: undefined
            };

            /**
             *  Load initial data
             */
            $scope.widgetState.doneLoading = false;

            UtkastProxy.getUtkastList($scope.filter.convertToPayload(), function(data) {

                $scope.widgetState.doneLoading = true;
                $scope.widgetState.activeErrorMessageKey = null;
                $scope.widgetState.currentList = data.results;
                $scope.widgetState.totalCount = data.totalCount;
                $scope.widgetState.initialQueryWasEmpty = (data.totalCount === 0 && !$scope.widgetState.filteredYet);

            }, function() {
                $log.debug('Query Error');
                $scope.widgetState.doneLoading = true;
                $scope.widgetState.activeErrorMessageKey = 'info.query.error';
            });

            /**
             * Exposed scope functions
             **/
            $scope.filterDrafts = function() {
                $log.debug('filterDrafts');

                $scope.widgetState.activeErrorMessageKey = null;
                $scope.widgetState.filteredYet = true;
                $scope.widgetState.initialQueryWasEmpty = false;

                $scope.widgetState.currentFilterRequest.startFrom = 0;

                $scope.widgetState.currentFilterRequest = $scope.filter.convertToPayload();

                var spinnerWaiting = $timeout(function() {
                        $scope.widgetState.runningQuery = true;
                    }, 700);
                UtkastProxy.getUtkastFetchMore($scope.widgetState.currentFilterRequest, function(successData) {
                    $scope.widgetState.currentList = successData.results;
                    $scope.widgetState.totalCount = successData.totalCount;
                    if (spinnerWaiting) {
                        $timeout.cancel(spinnerWaiting);
                    }
                    $scope.widgetState.runningQuery = false;
                }, function() {
                    $log.debug('Query Error');
                    $scope.widgetState.activeErrorMessageKey = 'info.query.error';
                    if (spinnerWaiting) {
                        $timeout.cancel(spinnerWaiting);
                    }
                    $scope.widgetState.runningQuery = false;
                });
            };

            $scope.showFetchMore = function() {
                return ($scope.widgetState.currentFilterRequest.startFrom + PAGE_SIZE < $scope.widgetState.totalCount) || $scope.widgetState.fetchingMoreInProgress;
            };

            $scope.fetchMore = function() {
                $log.debug('fetchMore');
                $scope.widgetState.activeErrorMessageKey = null;
                $scope.widgetState.fetchingMoreInProgress = true;
                //Change nothing but
                $scope.widgetState.currentFilterRequest.startFrom += PAGE_SIZE;

                UtkastProxy.getUtkastFetchMore($scope.widgetState.currentFilterRequest, function(successData) {
                    $scope.widgetState.fetchingMoreInProgress = false;
                    for (var i = 0; i < successData.results.length; i++) {
                        $scope.widgetState.currentList.push(successData.results[i]);
                    }
                }, function() {
                    $scope.widgetState.fetchingMoreInProgress = false;
                    $log.debug('Query Error');
                    $scope.widgetState.activeErrorMessageKey = 'info.query.error';
                });
            };

            $scope.orderByProperty = function(property, ascending) {
                $log.debug('orderByProperty');
                $scope.filter.selection.orderBy = property;
                $scope.filter.selection.orderAscending = ascending;

                $scope.filterDrafts();

            };

        }]);
