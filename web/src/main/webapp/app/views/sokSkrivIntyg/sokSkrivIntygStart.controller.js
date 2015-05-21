angular.module('webcert').controller('webcert.InitCertCtrl',
    [ '$location', 'webcert.CreateCertificateDraft',
        function($location, CreateCertificateDraft) {
            'use strict';

            CreateCertificateDraft.reset();
            $location.replace(true);
            $location.path('/create/choose-patient/index');
        }]);
