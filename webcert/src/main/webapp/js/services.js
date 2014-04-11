define([
    'angular',
    'services/QuestionAnswer',
    'services/WebcertCertificate',
    'services/CertificateDraft'
], function (angular, QuestionAnswer, WebcertCertificate, CertificateDraft) {
    'use strict';

    var moduleName = 'wc.dashboard.services';

    angular.module(moduleName, [])
        .factory('QuestionAnswer', QuestionAnswer)
        .factory('WebcertCertificate', WebcertCertificate)
        .factory('CertificateDraft', CertificateDraft);

    return moduleName;
});
