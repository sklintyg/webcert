define([
], function () {
    'use strict';

    return ['$scope', '$location', 'CertificateDraft',
        function ($scope, $location, CertificateDraft) {
            if (!CertificateDraft.personnummer) {
                $location.url('/create/choose-patient/index', true);
            }

            $scope.personnummer = CertificateDraft.personnummer;
            $scope.firstname = CertificateDraft.firstname;
            $scope.lastname = CertificateDraft.lastname;

            $scope.chooseCertType = function () {
                CertificateDraft.firstname = $scope.firstname;
                CertificateDraft.lastname = $scope.lastname;
                $location.path('/create/choose-cert-type/index');
            };

            $scope.changePatient = function () {
                $location.path('/create/index');
            };
        }];
});
