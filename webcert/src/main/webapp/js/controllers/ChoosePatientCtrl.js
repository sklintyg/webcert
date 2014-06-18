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

                    var onSuccess = function() {
                        $location.path('/create/choose-cert-type/index');
                    };

                    var onNotFound = function() {
                        $location.path('/create/edit-patient-name/index');
                    };

                    var onError = onNotFound;

                    CreateCertificateDraft.getNameAndAddress($scope.personnummer,
                        onSuccess, onNotFound, onError);
                };
            }
        ]);

    return moduleName;
});
