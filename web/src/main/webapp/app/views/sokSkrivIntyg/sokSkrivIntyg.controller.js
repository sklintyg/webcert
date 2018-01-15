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
angular.module('webcert').controller('webcert.SokSkrivIntygCtrl',
    [ '$scope', '$state', '$filter', 'webcert.SokSkrivValjUtkastService',
        function($scope, $state, $filter, Service) {
            'use strict';
            var viewState = {
                loading: false,
                errorid: null
            };

            $scope.model = {
                test: false
            };

            $scope.viewState = angular.copy(viewState);

            // Clear errormessage when user starts typing a new personnummer
            $scope.$watch('personnummer', function personnummerWatch () {
                if ($scope.viewState.errorid) {
                    $scope.viewState = angular.copy(viewState);
                }
            });

            $scope.focusPnr = true; // focus pnr input
            $scope.personnummer = '';

            $scope.loadPatient = function() {

                if($scope.pnrForm && $scope.pnrForm.pnr) {
                    $scope.pnrForm.pnr.$setTouched();
                    if($scope.pnrForm.pnr.$invalid){
                        return;
                    }
                }

                $scope.viewState.loading = true;
                Service.lookupPatient($scope.personnummer).then(function(patientResult) {
                    $scope.viewState.loading = false;
                    $state.go('webcert.create-choose-certtype-index', {
                        'patientId': $filter('PersonIdFormatter')(patientResult.personnummer)
                    });
                }, function(errorId) {
                    $scope.viewState.loading = false;
                    $scope.viewState.errorid = errorId;
                });
                
            };

        }]);
