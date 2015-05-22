angular.module('webcert').controller('webcert.EditPatientNameCtrl',
    [ '$location', '$stateParams' ,'$scope', 'webcert.PatientModel',
        function($location, $stateParams, $scope, PatientModel) {
            'use strict';

            if (!PatientModel.personnummer) {
                $location.url('/create/choose-patient/index', true);
            }

            $scope.personnummer = PatientModel.personnummer;
            $scope.fornamn = PatientModel.fornamn;
            if (PatientModel.mellannamn) {
                $scope.efternamn = PatientModel.mellannamn + ' ' + PatientModel.efternamn;
            } else {
                $scope.efternamn = PatientModel.efternamn;
            }
            
            $scope.personNotFound = ($stateParams.mode === 'notFound');
            
            $scope.errorOccured = ($stateParams.mode === 'errorOccured');
            
            $scope.chooseCertType = function() {
                PatientModel.fornamn = $scope.fornamn;
                PatientModel.mellannamn = null;
                PatientModel.efternamn = $scope.efternamn;
                $location.path('/create/choose-cert-type/index');
            };

            $scope.changePatient = function() {
                $location.path('/create/index');
            };
        }]);
