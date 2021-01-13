/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

angular.module('webcert').directive('wcVardenhetFilter',
    ['$cookies', '$rootScope', '$timeout',
      'common.User', 'common.statService',
      'webcert.vardenhetFilterModel', 'webcert.enhetArendenModel',
      function($cookies, $rootScope, $timeout, User, statService, vardenhetFilterModel, enhetArendenModel) {
        'use strict';

        return {
          restrict: 'E',
          scope: {},
          templateUrl: '/app/views/fragorOchSvar/wcVardenhetFilter/wcVardenhetFilter.directive.html',
          controller: function($scope) {
            $scope.initUnitList = function() {
              vardenhetFilterModel.loadingUnit = true;
              $scope.unitsList = angular.copy(vardenhetFilterModel.units);
              for(var i = 0; i < $scope.unitsList.length; i++) {
                $scope.unitsList[i].label = $scope.unitsList[i].namn;
                $scope.unitsList[i].number = $scope.unitsList[i].fragaSvar ? $scope.unitsList[i].fragaSvar : 0;
                $scope.unitsList[i].isSubcategory = $scope.isUnderenhet(i);
              }
              vardenhetFilterModel.selectedUnit = 'wc-all';
              vardenhetFilterModel.loadingUnit = false;
            };

            $scope.isUnderenhet = function(index) {
              return index >= 2;
            };

            this.$onInit = function() {
              vardenhetFilterModel.initialize(User.getVardenhetFilterList(User.getValdVardenhet()));

              $scope.vardenhetFilterModel = vardenhetFilterModel;

              if (statService.getLatestData()) {
                updateStats(null, statService.getLatestData());
              }
              $scope.$on('statService.stat-update', updateStats);

              $scope.$watch('vardenhetFilterModel.selectedUnit', function(){
                $rootScope.$broadcast('wcVardenhetFilter.unitSelected', vardenhetFilterModel.selectUnitById(vardenhetFilterModel.units, vardenhetFilterModel.selectedUnit));
              });

              $scope.$watch('vardenhetFilterModel.units', function() {
                vardenhetFilterModel.showSelectUnit =
                    vardenhetFilterModel.units && vardenhetFilterModel.units.length > 2 && vardenhetFilterModel.units[0].fragaSvar > 0;
              });
            };

            function updateStats(event, message) {
              // Get the latest stats
              var unitStats = message;

              // Get the chosen vardgivare
              var valdVardgivare = User.getValdVardgivare();

              // Find stats for the chosen vardenhets units below the chosen vardgivare
              var valdVardenheterStats = {};
              angular.forEach(unitStats.vardgivare, function(vardgivareStats) {
                if (vardgivareStats.id === valdVardgivare.id) {
                  valdVardenheterStats = vardgivareStats.vardenheter;
                }
              });

              // Set stats for each unit available for the filter
              angular.forEach(vardenhetFilterModel.units, function(unit) {

                // If it's the all choice, we know we want the total of everything
                if (unit.id === enhetArendenModel.ALL_UNITS) {
                  unit.fragaSvar = unitStats.fragaSvarValdEnhet;
                  unit.tooltip =
                      'Totalt antal ej hanterade ärenden för den vårdenhet där du är inloggad. ' +
                      'Här visas samtliga ärenden på vårdenhetsnivå och på mottagningsnivå.';
                } else {
                  // Otherwise find the stats for the unit
                  angular.forEach(valdVardenheterStats, function(unitStat) {
                    if (unit.id === unitStat.id) {
                      unit.fragaSvar = unitStat.fragaSvar;
                      unit.tooltip =
                          'Det totala antalet ej hanterade ärenden som finns registrerade på ' +
                          'vårdenheten. Det kan finnas ärenden som gäller denna vårdenhet men ' +
                          'som inte visas här. För säkerhets skull bör du även kontrollera ärenden ' +
                          'för övriga vårdenheter och mottagningar.';
                    }
                  });
                }
              });
              vardenhetFilterModel.showSelectUnit =
                  vardenhetFilterModel.units && vardenhetFilterModel.units.length > 2 && vardenhetFilterModel.units[0].fragaSvar > 0;
              $scope.initUnitList();
            }

          }
        };
      }]);
