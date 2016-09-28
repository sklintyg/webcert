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

angular.module('webcert').controller('webcert.ChoosePatientCtrl',
    [ '$scope', '$state', 'webcert.SokSkrivValjUtkastService',
        function($scope, $state, Service) {
            'use strict';
            var viewState = {
                loading: false,
                errorid: null
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
                $scope.viewState.loading = true;
                Service.lookupPatient($scope.personnummer).then(function(patientResult) {
                    $scope.viewState.loading = false;
                    $state.go('webcert.create-choose-certtype-index', { 'patientId': patientResult.personnummer});
                }, function(errorId) {
                    $scope.viewState.loading = false;
                    $scope.viewState.errorid = errorId;
                    if(errorId === null){
                        // If the pu-service isn't available the doctor can write any name they want.
                        // redirect to edit patient name
                        $state.go('webcert.create-edit-patientname', {mode:'errorOccured'});
                    }
                });
                
            };

        }]);
