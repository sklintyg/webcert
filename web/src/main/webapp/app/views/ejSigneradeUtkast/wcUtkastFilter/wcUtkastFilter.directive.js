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

angular.module('webcert').directive('wcUtkastFilter', ['$timeout', '$rootScope', 'webcert.UtkastProxy',
  'common.UserModel', 'common.authorityService',
      function($timeout, $rootScope, UtkastProxy, UserModel, authorityService) {
        'use strict';

        return {
          restrict: 'E',
          templateUrl: '/app/views/ejSigneradeUtkast/wcUtkastFilter/wcUtkastFilter.directive.html',
          scope: {
            onSearch: '&',
            onReset: '&',
            filter: '='
          },
          controller: function($scope) {
            $scope.showDateFromErrors = false;
            $scope.showDateToErrors = false;

            $scope.showVidarebefordra = function() {
              var options = {
                authority: 'VIDAREBEFORDRA_UTKAST',
                intygstyp: ''
              };
              return authorityService.isAuthorityActive(options);
            };

            $scope.showDateError = function () {
              return ($scope.showDateFromErrors || $scope.showDateToErrors) && $scope.filterForm.$invalid && !$scope.filterForm.$pristine;
            };

            $scope.getAlwaysHighlightedSparatAv = function () {
              return !UserModel.isVardAdministrator();
            };

            $scope.setShowDateFromVisible = function() {
              $scope.showDateFromErrors = !!$scope.filterForm['filter-changedate-from'].$viewValue;
            };

            $scope.setShowDateToVisible = function() {
              $scope.showDateToErrors = !!$scope.filterForm['filter-changedate-to'].$viewValue;
            };

            $scope.maxdate = moment().format('YYYY-MM-DD');

            $scope.widgetState = {
              loadingSavedByList: undefined,
              savedByList: [],
              searched: false,
              activeErrorMessageKey: null
            };

            //Clicked Search
            $scope.search = function() {
              $scope.widgetState.searched = true;
              $scope.onSearch();
            };

            //Clicked Återställ
            $scope.resetFilter = function() {
              resetFilterState();
              $scope.widgetState.searched = false;
              $scope.onReset();
            };

            function resetFilterState() {
              $scope.filter.reset();
              $scope.setDefaultSavedBy();
              // fiddle with the DOM to get rid of invalid data which isn't bind through the model
              angular.element('#filter-changedate-from').val('');
              angular.element('#filter-changedate-to').val('');
              if ($scope.filterForm['filter-changedate-from'] && $scope.filterForm['filter-changedate-to']) {
                $scope.filterForm['filter-changedate-from'].$setViewValue('');
                $scope.filterForm['filter-changedate-to'].$setViewValue('');
              }

              angular.element('#filter-person-id').val('');
              if ($scope.filterForm.pnr) {
                $scope.filterForm.pnr.$setViewValue('');
              }

              $scope.showDateFromErrors = false;
              $scope.showDateToErrors = false;
            }

            $scope.setDefaultSavedBy = function() {
              if (!UserModel.isVardAdministrator()) {
                $scope.filter.selection.savedBy = UserModel.user.hsaId;
              } else {
                $scope.filter.selection.savedBy = undefined;
              }
            };

            $scope.addCurrentLakare = function() {
              var inList = false;
              if(!UserModel.isVardAdministrator()) {
                $scope.widgetState.savedByList.forEach(function(lakare) {
                  if (UserModel.user && lakare.id === UserModel.user.hsaId) {
                    inList = true;
                  }
                });

                if(!inList) {
                  var userLakare = {
                    id: UserModel.user.hsaId, label: UserModel.user.namn
                  };
                  $scope.widgetState.savedByList.push(userLakare);
                }
              }
            };

            function loadSavedByList() {
              $scope.widgetState.loadingSavedByList = true;

              UtkastProxy.getUtkastSavedByList(function(list) {
                $scope.widgetState.loadingSavedByList = false;
                $scope.widgetState.savedByList = list || [];
                $scope.widgetState.savedByList.unshift({
                  label: 'Visa alla',
                  id: undefined
                });

                $scope.filter.savedByOptions = $scope.widgetState.savedByList;
                //if only 1 option avaiable it must be 'Visa alla'
                if ($scope.filter.savedByOptions.length === 1) {
                  $scope.filter.selection.savedBy = undefined;
                }
                $scope.addCurrentLakare();
              }, function() {
                $scope.widgetState.loadingSavedByList = false;
                $scope.widgetState.savedByList = [{
                  id: undefined,
                  label: '<Kunde inte hämta lista>'
                }];
                $scope.filter.savedByOptions = $scope.widgetState.savedByList;
              });
            }

            /**
             *  Load initial data
             */
            loadSavedByList();
          }
        };
      }
    ]
);
