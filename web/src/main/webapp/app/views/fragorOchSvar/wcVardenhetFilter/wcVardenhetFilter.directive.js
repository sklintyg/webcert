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

angular.module('webcert').directive('wcVardenhetFilter',
    [ '$cookies', '$rootScope', '$timeout', 'common.User', 'common.statService', 'webcert.vardenhetFilterModel',
        function($cookies, $rootScope, $timeout, User, statService, vardenhetFilterModel) {
            'use strict';

            return {
                restrict: 'E',
                templateUrl: '/app/views/fragorOchSvar/wcVardenhetFilter/wcVardenhetFilter.directive.html',
                controller: function($scope) {

                    this.$onInit = function(){
                        vardenhetFilterModel.initialize();

                        $scope.vardenhetFilterModel = vardenhetFilterModel;

                        if (statService.getLatestData()) {
                            vardenhetFilterModel.updateStats(null, statService.getLatestData());
                        }
                        $scope.$on('wc-stat-update', vardenhetFilterModel.updateStats);
                    };

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

                    // we have to init it ourselves since this isn't a component controller yet
                    this.$onInit();
                }
            };
        }]);
