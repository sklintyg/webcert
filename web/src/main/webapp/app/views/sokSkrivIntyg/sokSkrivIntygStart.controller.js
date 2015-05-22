angular.module('webcert').controller('webcert.InitCertCtrl',
    [ '$location', 'webcert.PatientModel',
        function($location, PatientModel) {
            'use strict';

            PatientModel.reset();
            $location.replace(true);
            $location.path('/create/choose-patient/index');
        }]);
