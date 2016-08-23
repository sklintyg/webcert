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
    ['$location', '$scope', 'common.PatientModel', 'common.PatientProxy', 'common.PersonIdValidatorService',
        function($location, $scope, PatientModel, PatientProxy, personIdValidator) {
            'use strict';
            var widgetState = {
                waiting: false,
                errorid: undefined
            };

            $scope.widgetState = angular.copy(widgetState);

            // Clear errormeesage when user starts typing a new personnummer
            $scope.$watch('personnummer', function personnummerWatch () {
                if ($scope.widgetState.errorid) {
                    $scope.widgetState = angular.copy(widgetState);
                }
            });

            $scope.focusPnr = true; // focus pnr input
            $scope.personnummer = PatientModel.personnummer;

            $scope.lookupPatient = function() {

                var onSuccess = function(patientResult) {
                    PatientModel = patientResult;
                    $scope.widgetState.waiting = false;
                    $scope.widgetState.errorid = undefined;

                    if (!PatientModel.personnummer) {
                        // This shouldn't ever happen but in case we don't receive a personnummer we should tell the user.
                        $scope.widgetState.errorid = 'error.pu.nopersonnummer';
                        return;
                    }

                    // If the successful result does not contain mandatory name information, present an error (should never happen in production)
                    if (!PatientModel.fornamn || !PatientModel.efternamn) {
                        $scope.widgetState.errorid = 'error.pu.noname';
                        return;
                    }
                    $location.path('/create/choose-cert-type/index');
                };

                var onNotFound = function() {
                    $scope.widgetState.waiting = false;

                    // If the pu-service says noone exists with this pnr we just show an error message.
                    if(personIdValidator.validResult(personIdValidator.validateSamordningsnummer($scope.personnummer))) {
                        // This is a samordningsnummer that does not exist
                        $scope.widgetState.errorid = 'error.pu.samordningsnummernotfound';
                    } else {
                        // This is a personnummer that does not exist
                        $scope.widgetState.errorid = 'error.pu.namenotfound';
                    }
                };

                var onError = function() {
                    // If the service isn't available the doctor can write any name they want. redirect to edit patient name
                    $scope.widgetState.waiting = false;
                    $scope.widgetState.errorid = undefined;
                    $location.path('/create/edit-patient-name/errorOccured');
                };

                $scope.widgetState.waiting = true;

                PatientProxy.getPatient($scope.personnummer, onSuccess, onNotFound, onError);
            };
        }]);
