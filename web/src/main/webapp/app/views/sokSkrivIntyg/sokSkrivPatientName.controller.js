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

angular.module('webcert').controller('webcert.EditPatientNameCtrl',
    [ '$state', '$location', '$stateParams' ,'$scope', 'common.PatientModel',
        function($state, $location, $stateParams, $scope, PatientModel) {
            'use strict';

            if (!PatientModel.personnummer) {
                $state.go('webcert.create-choosepatient-index');
            }

            $scope.personnummer = PatientModel.personnummer;
            $scope.fornamn = PatientModel.fornamn;
            if (PatientModel.mellannamn) {
                $scope.efternamn = PatientModel.mellannamn + ' ' + PatientModel.efternamn;
            } else {
                $scope.efternamn = PatientModel.efternamn;
            }

            $scope.personNotFound = ($stateParams.mode === 'notFound');

            $scope.errorOccured = ($stateParams.mode === 'errorOccured');

            $scope.chooseCertType = function() {
                PatientModel.fornamn = $scope.fornamn;
                PatientModel.mellannamn = null;
                PatientModel.efternamn = $scope.efternamn;
                $state.go('webcert.create-choose-certtype-index', { 'patientId': PatientModel.personnummer});
            };

            $scope.changePatient = function() {
                $state.go('webcert.create-choosepatient-index');
            };
        }]);
