define([
], function () {
    'use strict';

    return ['$scope', '$location', 'CreateCertificateDraft',
        function ($scope, $location, CreateCertificateDraft) {
            $scope.personnummer = CreateCertificateDraft.personnummer;

            $scope.lookupPatient = function () {
                CreateCertificateDraft.getNameAndAddress($scope.personnummer, function () {
                    if (CreateCertificateDraft.firstname && CreateCertificateDraft.lastname) {
                        $location.path('/create/choose-cert-type/index');
                    } else {
                        $location.path('/create/edit-patient-name/index');
                    }
                });
            };
        }];
});
