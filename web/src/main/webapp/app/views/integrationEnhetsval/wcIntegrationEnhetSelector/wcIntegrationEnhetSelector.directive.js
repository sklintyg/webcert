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

angular.module('webcert').directive('wcIntegrationEnhetSelector', function() {
    'use strict';

    return {
        restrict: 'E',
        scope: {
            'user': '=',
            'onUnitSelection': '&',
            'expandVardgivare': '='
        },
        templateUrl: '/app/views/integrationEnhetsval/wcIntegrationEnhetSelector/wcIntegrationEnhetSelector.directive.html',
        link: function($scope) {

            //Create lo local copy with only required info
            var model = {};
            model.vardgivare = angular.copy($scope.user.vardgivare);
            model.valdVardenhet = angular.copy($scope.user.valdVardenhet);

            if (angular.isArray(model.vardgivare)) {
                angular.forEach(model.vardgivare, function(vg) {
                        vg.expanded = true;
                        angular.forEach(vg.vardenheter, function(ve) {
                            ve.expanded = true;
                        });
                });
            }

            //Expose our model copy to view
            $scope.model = model;

            //Report user selection back to user of directive
            $scope.itemSelected = function(unit) {
                $scope.onUnitSelection({
                    enhet: unit
                });
            };
        }

    };
});
