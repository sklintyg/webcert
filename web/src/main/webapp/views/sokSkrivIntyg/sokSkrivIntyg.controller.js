angular.module('webcert').controller('webcert.ChoosePatientCtrl',
    ['$location', '$scope', 'webcert.PatientModel', 'webcert.PatientProxy',
        function($location, $scope, PatientModel, PatientProxy) {
            'use strict';

            $scope.widgetState = {
                waiting: false,
                errorid: undefined
            };

            $scope.focusPnr = true; // focus pnr input
            $scope.personnummer = PatientModel.personnummer;

            $scope.lookupPatient = function() {

                var onSuccess = function() {
                    $scope.widgetState.waiting = false;
                    $scope.widgetState.errorid = undefined;
                    $location.path('/create/choose-cert-type/index');
                };

                var onNotFound = function() {
                    $scope.widgetState.waiting = false;
                    $scope.widgetState.errorid = 'error.pu.namenotfound';
                };

                var onError = function() {
                    $scope.widgetState.waiting = false;
                    $scope.widgetState.errorid = undefined;
                    $location.path('/create/edit-patient-name/errorOccured');
                };

                $scope.widgetState.waiting = true;

                PatientProxy.getPatient($scope.personnummer,
                    onSuccess, onNotFound, onError);
            };
        }]);
