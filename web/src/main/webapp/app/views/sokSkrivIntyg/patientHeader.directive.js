angular.module('webcert').directive('wcPatientHeader',
    ['$state', 'common.PatientModel',
    function($state, PatientModel) {
        'use strict';

        return {
            restrict: 'E',
            templateUrl: '/app/views/sokSkrivIntyg/patientHeader.directive.html',
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
