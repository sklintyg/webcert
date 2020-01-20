/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
    ['$log', '$scope', '$timeout', '$rootScope', '$window', 'common.dialogService', 'webcert.UtkastFilterModel', 'webcert.UtkastProxy', 'common.User',
      function($log, $scope, $timeout, $rootScope, $window, dialogService, UtkastFilterModel, UtkastProxy, User) {
        'use strict';

        $scope.filter = UtkastFilterModel.build();

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
          currentFilterRequest: $scope.filter.convertToPayload(),

          // List data
          totalCount: 0,
          currentList: undefined,

          DEFAULT_PAGE: 1,
          DEFAULT_PAGE_SIZE: 10,
          DEFAULT_NUMBER_PAGES: 10,
          LIST_NAME: 'utkastList'
        };

        /**
         *  Load initial data
         */
        $scope.widgetState.doneLoading = false;

        UtkastProxy.getUtkastList($scope.filter.convertToPayload(), function(data) {

          $scope.widgetState.doneLoading = true;
          $scope.widgetState.activeErrorMessageKey = null;
          $scope.updateView(data);
          $scope.widgetState.initialQueryWasEmpty = (data.totalCount === 0 && !$scope.widgetState.filteredYet);

        }, function() {
          $log.debug('Query Error');
          $scope.widgetState.doneLoading = true;
          $scope.widgetState.activeErrorMessageKey = 'info.query.error';
        });

        /**
         * Exposed scope functions
         **/
        $scope.updateView = function(data) {
          $scope.widgetState.currentList = data.results;
          $scope.widgetState.totalCount = data.totalCount;
          data.startFrom = data.startFrom >= 0 ? data.startFrom : 0;
          $scope.widgetState.startPoint = data.startFrom + 1;
          $scope.widgetState.endPoint = data.startFrom + $scope.widgetState.currentList.length;

          if(data.totalCount === 0) {
            $scope.listInit();
          }
          $scope.updateLists();
        };

        $scope.listInit = function() {
          $scope.widgetState.limit = $scope.widgetState.DEFAULT_PAGE_SIZE;
          $scope.filter.pageSize = $scope.widgetState.DEFAULT_PAGE_SIZE;
          $scope.widgetState.chosenPage = $scope.widgetState.DEFAULT_PAGE;
          $scope.widgetState.chosenPageList = $scope.widgetState.DEFAULT_PAGE;
        };

        $scope.updateLists = function() {
          $rootScope.$broadcast('wcListDropdown.getLimits');
          $rootScope.$broadcast('wcListPageNumbers.getPages');
        };

        $scope.onSearch = function () {
          filterDrafts(null, { startFrom: -1 });
        };

        $scope.onReset = function () {
          $scope.listInit();
          $scope.onSearch();
        };

        function filterDrafts(event, data) {
          $log.debug('filterDrafts');

          $scope.widgetState.activeErrorMessageKey = null;
          $scope.widgetState.filteredYet = true;
          $scope.widgetState.initialQueryWasEmpty = false;

          if (data.startFrom >= 0) {
            $scope.filter.startFrom = data.startFrom;
          }

          $scope.widgetState.currentFilterRequest = $scope.filter.convertToPayload();

          var spinnerWaiting = $timeout(function() {
            $scope.widgetState.runningQuery = true;
          }, 700);
          UtkastProxy.getUtkastFetchMore($scope.widgetState.currentFilterRequest, function(successData) {
             data.results = successData.results;
             data.totalCount = successData.totalCount;
            $scope.updateView(data);
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
        }

        $scope.orderByProperty = function(property, ascending) {
          $log.debug('orderByProperty');
          $scope.filter.selection.orderBy = property;
          $scope.filter.selection.orderAscending = ascending;

          filterDrafts(null,{startFrom: $scope.widgetState.startPoint - 1});

        };

        $scope.listInit();
        $scope.$on($scope.widgetState.LIST_NAME + '.requestListUpdate', filterDrafts);
      }]);
