define([
    'angular',
    'services/CreateCertificateDraft'
], function(angular, CreateCertificateDraft) {
    'use strict';

    var moduleName = 'wc.ChoosePatientCtrl';

    angular.module(moduleName, [ CreateCertificateDraft ]).
        controller(moduleName, [ '$location', '$scope', CreateCertificateDraft,
            function($location, $scope, CreateCertificateDraft) {
                $scope.personnummer = CreateCertificateDraft.personnummer;

                $scope.lookupPatient = function() {
                    CreateCertificateDraft.getNameAndAddress($scope.personnummer, function() {
                        if (CreateCertificateDraft.firstname && CreateCertificateDraft.lastname) {
                            $location.path('/create/choose-cert-type/index');
                        } else {
                            $location.path('/create/edit-patient-name/index');
                        }
                    });
                };
            }
        ]);

    return moduleName;
});
