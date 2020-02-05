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

angular.module('webcert').directive('wcEnhetArendenFilter', [
  '$rootScope', '$log', '$cookies',
  'webcert.enhetArendenProxy',
  'webcert.enhetArendenModel', 'webcert.enhetArendenFilterModel', 'webcert.vardenhetFilterModel', 'webcert.enhetArendenFilterService', 'common.UserModel',
  function($rootScope, $log, $cookies,
      enhetArendenProxy,
      enhetArendenModel, enhetArendenFilterModel, vardenhetFilterModel, enhetArendenFilterService, UserModel) {
    'use strict';

    return {
      restrict: 'E',
      transclude: false,
      replace: false,
      scope: {},
      templateUrl: '/app/views/fragorOchSvar/wcEnhetArendenFilter/wcEnhetArendenFilter.directive.html',
      controller: function($scope) {

        $scope.getAlwaysHighlightedSigneratAv = function() {
            return UserModel.isLakare();
        };

        $scope.maxdate = moment().format('YYYY-MM-DD');

        this.$onInit = function() {

          // Load filter form (first page load)
          enhetArendenFilterModel.reset();
          enhetArendenFilterService.initLakareList(enhetArendenModel.enhetId);

          $scope.enhetArendenFilterModel = enhetArendenFilterModel;
          $scope.vardenhetFilterModel = vardenhetFilterModel;
          $scope.showDateFromErrors = false;
          $scope.showDateToErrors = false;

          $scope.showDateError = function () {
            return ($scope.showDateFromErrors || $scope.showDateToErrors) && $scope.filterForm.$invalid && !$scope.filterForm.$pristine;
          };

          $scope.setShowDateFromVisible = function() {
            $scope.showDateFromErrors = !!$scope.filterForm['filter-changedate-from'].$viewValue;
          };

          $scope.setShowDateToVisible = function() {
            $scope.showDateToErrors = !!$scope.filterForm['filter-changedate-to'].$viewValue;
          };

          $scope.hasUnhandledArenden = function() {
            return vardenhetFilterModel.units ? vardenhetFilterModel.units[0].fragaSvar : false;
          };

          $scope.hasNoArenden = function() {
            return vardenhetFilterModel.units ? vardenhetFilterModel.units[0].fragaSvar === 0 : true;
          };

          function updateArendenList(reset) {
            enhetArendenModel.enhetId = vardenhetFilterModel.selectedUnit;
            $rootScope.$broadcast('enhetArendenList.requestListUpdate', {startFrom: 0, reset: reset});
          }

          function resetFrom() {
            enhetArendenFilterModel.reset();
            vardenhetFilterModel.reset();
            resetInvalidData();
            $scope.showDateFromErrors = false;
            $scope.showDateToErrors = false;
          }

          $scope.resetFilterForm = function() {
            resetFrom();
            updateArendenList(true);
          };

          $scope.filterList = function() {
            updateArendenList(false);
          };

          // Broadcast by vardenhet filter directive on load and selection
          $scope.$on('wcVardenhetFilter.unitSelected', function(event, unit) {
            if(unit !== undefined) {
              enhetArendenModel.enhetId = unit.id;
              vardenhetFilterModel.selectedUnitName = unit.namn;
              if(unit.id === vardenhetFilterModel.ALL_ARENDEN){
                vardenhetFilterModel.selectedUnitName = vardenhetFilterModel.selectedUnitName.toLowerCase();
              }
              enhetArendenFilterService.initLakareList(unit.id);
            }
          });
        };

        function resetInvalidData() {
          // fiddle with the DOM to get rid of invalid data which isn't bind through the model
          angular.element('#filter-changedate-from').val('');
          angular.element('#filter-changedate-to').val('');
          angular.element('#filter-person-id').val('');
          if ($scope.filterForm['filter-changedate-from'] && $scope.filterForm['filter-changedate-to']) {
            $scope.filterForm['filter-changedate-from'].$setViewValue('');
            $scope.filterForm['filter-changedate-to'].$setViewValue('');
          }
        }

      }
    };
  }]);
