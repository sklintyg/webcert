define([
    'angular',
    'services/dashBoardService',
    'services/unsignedCertificateService',
    'services/CertificateDraft'
], function (angular, dashBoardService, unsignedCertificateService, CertificateDraft) {
    'use strict';

    var moduleName = 'wc.dashboard.services';

    angular.module(moduleName, [])
        .factory('dashBoardService', dashBoardService)
        .factory('unsignedCertificateService', unsignedCertificateService)
        .factory('CertificateDraft', CertificateDraft);

    return moduleName;
});
