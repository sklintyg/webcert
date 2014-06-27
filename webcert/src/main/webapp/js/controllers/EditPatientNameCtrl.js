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
                $scope.fornamn = CreateCertificateDraft.fornamn;
                $scope.efternamn = CreateCertificateDraft.efternamn;

                $scope.chooseCertType = function() {
                    CreateCertificateDraft.fornamn = $scope.fornamn;
                    CreateCertificateDraft.efternamn = $scope.efternamn;
                    $location.path('/create/choose-cert-type/index');
                };

                $scope.changePatient = function() {
                    $location.path('/create/index');
                };
            }
        ]);

    return moduleName;
});
