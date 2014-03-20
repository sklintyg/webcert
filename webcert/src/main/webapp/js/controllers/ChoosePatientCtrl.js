define([
], function () {
    'use strict';

    return ['$scope', '$location', 'CertificateDraft',
        function ($scope, $location, CertificateDraft) {
            $scope.personnummer = CertificateDraft.personnummer;

            $scope.lookupPatient = function () {
                CertificateDraft.getNameAndAddress($scope.personnummer, function () {
                    if (CertificateDraft.firstname && CertificateDraft.lastname) {
                        $location.path('/create/choose-cert-type/index');
                    } else {
                        $location.path('/create/edit-patient-name/index');
                    }
                });
            };
        }];
});
