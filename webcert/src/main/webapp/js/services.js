define([
    'angular',
    'services/QuestionAnswer',
    'services/ManageCertificate',
    'services/CreateCertificateDraft'
], function (angular, QuestionAnswer, ManageCertificate, CreateCertificateDraft) {
    'use strict';

    var moduleName = 'wc.dashboard.services';

    angular.module(moduleName, [])
        .factory('QuestionAnswer', QuestionAnswer)
        .factory('ManageCertificate', ManageCertificate)
        .factory('CreateCertificateDraft', CreateCertificateDraft);

    return moduleName;
});
