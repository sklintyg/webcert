/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
    [ '$cookies', '$rootScope', '$timeout',
        'common.User', 'common.statService',
        'webcert.vardenhetFilterModel', 'webcert.enhetArendenModel',
        function($cookies, $rootScope, $timeout, User, statService, vardenhetFilterModel, enhetArendenModel) {
            'use strict';

            return {
                restrict: 'E',
                scope: {},
                templateUrl: '/app/views/fragorOchSvar/wcVardenhetFilter/wcVardenhetFilter.directive.html',
                controller: function($scope) {

                    this.$onInit = function(){
                        vardenhetFilterModel.initialize(User.getVardenhetFilterList(User.getValdVardenhet()));

                        $scope.vardenhetFilterModel = vardenhetFilterModel;

                        if (statService.getLatestData()) {
                            updateStats(null, statService.getLatestData());
                        }
                        $scope.$on('statService.stat-update', updateStats);

                        /**
                         * Toggles if the enheter without an active question should
                         * be shown
                         */
                        $scope.toggleShowInactive = function() {
                            vardenhetFilterModel.showInactive = !vardenhetFilterModel.showInactive;
                        };

                        $scope.selectUnit = function(unit) {
                            vardenhetFilterModel.selectedUnit = unit;
                            $rootScope.$broadcast('wcVardenhetFilter.unitSelected', vardenhetFilterModel.selectedUnit);
                        };
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
                                    'Totalt antal ej hanterade frågor och svar för den vårdenhet där du är inloggad. ' +
                                    'Här visas samtliga frågor och svar på vårdenhetsnivå och på mottagningsnivå.';
                            } else {
                                // Otherwise find the stats for the unit
                                angular.forEach(valdVardenheterStats, function(unitStat) {
                                    if (unit.id === unitStat.id) {
                                        unit.fragaSvar = unitStat.fragaSvar;
                                        unit.tooltip =
                                            'Det totala antalet ej hanterade frågor och svar som finns registrerade på ' +
                                            'vårdenheten. Det kan finnas frågor och svar som gäller denna vårdenhet men ' +
                                            'som inte visas här. För säkerhets skull bör du även kontrollera frågor ' +
                                            'och svar för övriga vårdenheter och mottagningar.';
                                    }
                                });
                            }
                        });
                    }

                }
            };
        }]);
