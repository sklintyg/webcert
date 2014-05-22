define([
    'angular',
    'services/CreateCertificateDraft'
], function(angular, CreateCertificateDraft) {
    'use strict';

    var moduleName = 'wc.InitCertCtrl';

    angular.module(moduleName, [ CreateCertificateDraft ]).
        controller(moduleName, [ '$location', CreateCertificateDraft,
            function($location, CreateCertificateDraft) {
                CreateCertificateDraft.reset();
                $location.replace(true);
                $location.path('/create/choose-patient/index');
            }
        ]);

    return moduleName;
});
