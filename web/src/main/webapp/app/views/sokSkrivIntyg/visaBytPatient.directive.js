angular.module('webcert').directive('wcVisaBytPatient',
    ['$state', 'common.PatientModel',
    function($state, PatientModel) {
        'use strict';

        return {
            restrict: 'E',
            templateUrl: '/app/views/sokSkrivIntyg/visaBytPatient.directive.html',
            scope: {},
            link: function(scope, element, attrs) {

                var choosePatientStateName = 'webcert.create-choosepatient-index';

                scope.patientModel = PatientModel;

                scope.changePatient = function() {
                    $state.go(choosePatientStateName);
                };
            }
        };
    }]
);
