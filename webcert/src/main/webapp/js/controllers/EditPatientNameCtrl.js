define([
    'angular',
    'services/CreateCertificateDraft'
], function(angular, CreateCertificateDraft) {
    'use strict';

    var moduleName = 'wc.EditPatientNameCtrl';

    angular.module(moduleName, [ CreateCertificateDraft ]).
        controller(moduleName, [ '$location', '$scope', CreateCertificateDraft,
            function($location, $scope, CreateCertificateDraft) {
                if (!CreateCertificateDraft.personnummer) {
                    $location.url('/create/choose-patient/index', true);
                }

                $scope.personnummer = CreateCertificateDraft.personnummer;
                $scope.firstname = CreateCertificateDraft.firstname;
                $scope.middlename = CreateCertificateDraft.middlename;
                $scope.lastname = CreateCertificateDraft.lastname;

                $scope.chooseCertType = function() {
                    CreateCertificateDraft.firstname = $scope.firstname;
                    CreateCertificateDraft.middlename = $scope.middlename;
                    CreateCertificateDraft.lastname = $scope.lastname;
                    $location.path('/create/choose-cert-type/index');
                };

                $scope.changePatient = function() {
                    $location.path('/create/index');
                };
            }
        ]);

    return moduleName;
});
