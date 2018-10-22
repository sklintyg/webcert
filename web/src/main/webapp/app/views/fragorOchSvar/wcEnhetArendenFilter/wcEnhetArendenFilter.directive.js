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

angular.module('webcert').directive('wcEnhetArendenFilter', [
    '$rootScope', '$log', '$cookies',
    'webcert.enhetArendenProxy',
    'webcert.enhetArendenModel', 'webcert.enhetArendenFilterModel', 'webcert.vardenhetFilterModel', 'webcert.enhetArendenFilterService',
    function($rootScope, $log, $cookies,
        enhetArendenProxy,
        enhetArendenModel, enhetArendenFilterModel, vardenhetFilterModel, enhetArendenFilterService) {
        'use strict';

        return {
            restrict: 'E',
            transclude: false,
            replace: false,
            scope: {},
            templateUrl: '/app/views/fragorOchSvar/wcEnhetArendenFilter/wcEnhetArendenFilter.directive.html',
            controller: function($scope) {

                $scope.maxdate = moment().format('YYYY-MM-DD');

                $scope.pnrIsNotValid = false;
                $scope.pnrIsCorrect = false;

                $scope.pnrValidationCheck = function () {
                    if ($scope.pnrIsCorrect) {
                        $scope.pnrIsNotValid = false;
                    } else {
                        $scope.pnrIsNotValid = true;
                    }
                };

                this.$onInit = function(){

                    // Load filter form (first page load)
                    enhetArendenFilterModel.reset();
                    enhetArendenFilterService.initLakareList(enhetArendenModel.enhetId);

                    $scope.enhetArendenFilterModel = enhetArendenFilterModel;

                    $scope.hasUnhandledArenden = function(){
                        return vardenhetFilterModel.units ? vardenhetFilterModel.units[0].fragaSvar : false;
                    };

                    $scope.hasNoArenden = function(){
                        return vardenhetFilterModel.units ? vardenhetFilterModel.units[0].fragaSvar === 0 : true;
                    };

                    function updateArendenList(){
                        $rootScope.$broadcast('enhetArendenList.requestListUpdate', {startFrom: 0});
                    }

                    $scope.resetFilterForm = function() {
                        enhetArendenFilterModel.reset();
                        resetInvalidData();
                        updateArendenList();
                    };

                    $scope.filterList = function() {
                        updateArendenList();
                    };

                    // Broadcast by vardenhet filter directive on load and selection
                    $scope.$on('wcVardenhetFilter.unitSelected', function(event, unit) {

                        // If we change enhet then we probably don't want the same filter criterias
                        if (unit.id !== enhetArendenModel.enhetId) {
                            enhetArendenFilterModel.reset();
                            resetInvalidData();
                        }
                        enhetArendenModel.enhetId = unit.id;

                        enhetArendenFilterService.initLakareList(unit.id); // Update lakare list for filter form
                        updateArendenList();
                    });
                };

                $scope.$watch('enhetArendenFilterModel.filterForm.patientPersonId', function(value) {
                    if (value !== undefined) {
                        $scope.pnrIsCorrect = true;
                    } else {
                        $scope.pnrIsCorrect = false;
                    }
                });

                function resetInvalidData() {
                    // fiddle with the DOM to get rid of invalid data which isn't bind through the model
                    angular.element('#filter-changedate-from').val('');
                    angular.element('#filter-changedate-to').val('');
                    angular.element('#filter-person-id').val('');
                    $scope.pnrIsCorrect = false;
                    $scope.pnrIsNotValid = false;
                    $scope.filterForm['filter-changedate-from'].$setViewValue('');
                    $scope.filterForm['filter-changedate-to'].$setViewValue('');
                }

            }
        };
    }]);
