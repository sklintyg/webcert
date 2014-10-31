angular.module('webcert').controller('webcert.ChoosePatientCtrl',
    [ '$location', '$scope', 'webcert.CreateCertificateDraft', '$timeout',
        function($location, $scope, CreateCertificateDraft, $timeout) {
            'use strict';

            $scope.widgetState = {
                waiting: false
            };

            $scope.focusPnr = true; // focus pnr input
            $scope.personnummer = CreateCertificateDraft.personnummer;

            $scope.lookupPatient = function() {

                var onSuccess = function() {
                    $scope.widgetState.waiting = false;
                    $location.path('/create/choose-cert-type/index');
                };

                var onNotFound = function() {
                    $scope.widgetState.waiting = false;
                    $location.path('/create/edit-patient-name/notFound');
                };

                var onError = function() {
                    $scope.widgetState.waiting = false;
                    $location.path('/create/edit-patient-name/errorOccured');
                };

                $scope.widgetState.waiting = true;

                CreateCertificateDraft.getNameAndAddress($scope.personnummer,
                    onSuccess, onNotFound, onError);
            };
        }]);
