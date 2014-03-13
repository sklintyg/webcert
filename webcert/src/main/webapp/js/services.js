define([
    'angular',
    'services/dashBoardService',
    'services/CertificateDraft'
], function (angular, dashBoardService, CertificateDraft) {
    'use strict';

    var moduleName = 'wc.dashboard.services';

    angular.module(moduleName, [])
        .factory('dashBoardService', dashBoardService)
        .factory('CertificateDraft', CertificateDraft);

    return moduleName;
});
