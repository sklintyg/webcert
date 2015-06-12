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
                    $location.path('/create/choose-cert-type/index');
                };

                var onNotFound = function() {
                    $scope.widgetState.waiting = false;

                    if(personIdValidator.validResult(personIdValidator.validateSamordningsnummer($scope.personnummer))) {
                        $scope.widgetState.errorid = 'error.pu.samordningsnummernotfound';
                    } else {
                        $scope.widgetState.errorid = 'error.pu.namenotfound';
                    }
                };

                var onError = function() {
                    $scope.widgetState.waiting = false;
                    $scope.widgetState.errorid = undefined;
                    $location.path('/create/edit-patient-name/errorOccured');
                };

                $scope.widgetState.waiting = true;

                PatientProxy.getPatient($scope.personnummer, onSuccess, onNotFound, onError);
            };
        }]);
