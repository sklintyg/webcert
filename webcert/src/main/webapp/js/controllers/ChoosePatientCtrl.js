angular.module('webcert').controller('webcert.ChoosePatientCtrl',
    [ '$location', '$scope', 'webcert.CreateCertificateDraft',
        function($location, $scope, CreateCertificateDraft) {
            'use strict';

            $scope.personnummer = CreateCertificateDraft.personnummer;

            $scope.lookupPatient = function() {

                var onSuccess = function() {
                    $location.path('/create/choose-cert-type/index');
                };

                var onNotFound = function() {
                    $location.path('/create/edit-patient-name/notFound');
                };

                var onError = function() {
                    $location.path('/create/edit-patient-name/errorOccured');
                };

                CreateCertificateDraft.getNameAndAddress($scope.personnummer,
                    onSuccess, onNotFound, onError);
            };
        }]);
