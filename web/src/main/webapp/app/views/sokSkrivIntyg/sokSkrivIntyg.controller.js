angular.module('webcert').controller('webcert.ChoosePatientCtrl',
    ['$location', '$scope', 'common.PatientModel', 'common.PatientProxy', 'common.PersonIdValidatorService',
        function($location, $scope, PatientModel, PatientProxy, personIdValidator) {
            'use strict';

            $scope.widgetState = {
                waiting: false,
                errorid: undefined
            };

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
                        $scope.widgetState.errorid = 'error.pu.samordningsnummernotfound';
                    } else {
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
