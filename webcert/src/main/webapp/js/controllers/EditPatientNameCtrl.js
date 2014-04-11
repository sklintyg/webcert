define([
], function () {
    'use strict';

    return ['$scope', '$location', 'CreateCertificateDraft',
        function ($scope, $location, CreateCertificateDraft) {
            if (!CreateCertificateDraft.personnummer) {
                $location.url('/create/choose-patient/index', true);
            }

            $scope.personnummer = CreateCertificateDraft.personnummer;
            $scope.firstname = CreateCertificateDraft.firstname;
            $scope.lastname = CreateCertificateDraft.lastname;

            $scope.chooseCertType = function () {
                CreateCertificateDraft.firstname = $scope.firstname;
                CreateCertificateDraft.lastname = $scope.lastname;
                $location.path('/create/choose-cert-type/index');
            };

            $scope.changePatient = function () {
                $location.path('/create/index');
            };
        }];
});
