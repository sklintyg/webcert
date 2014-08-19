angular.module('webcert').controller('webcert.EditPatientNameCtrl',
    [ '$location', '$routeParams' ,'$scope', 'webcert.CreateCertificateDraft',
        function($location, $routeParams, $scope, CreateCertificateDraft) {
            'use strict';

            if (!CreateCertificateDraft.personnummer) {
                $location.url('/create/choose-patient/index', true);
            }

            $scope.personnummer = CreateCertificateDraft.personnummer;
            $scope.fornamn = CreateCertificateDraft.fornamn;
            if (CreateCertificateDraft.mellannamn) {
                $scope.efternamn = CreateCertificateDraft.mellannamn + ' ' + CreateCertificateDraft.efternamn;
            } else {
                $scope.efternamn = CreateCertificateDraft.efternamn;
            }
            
            $scope.personNotFound = ($routeParams.mode === 'notFound');
            
            $scope.errorOccured = ($routeParams.mode === 'errorOccured');
            
            $scope.chooseCertType = function() {
                CreateCertificateDraft.fornamn = $scope.fornamn;
                CreateCertificateDraft.mellannamn = null;
                CreateCertificateDraft.efternamn = $scope.efternamn;
                $location.path('/create/choose-cert-type/index');
            };

            $scope.changePatient = function() {
                $location.path('/create/index');
            };
        }]);
