define([
], function () {
    'use strict';

    return ['$scope', '$location', 'CertificateDraft',
        function ($scope, $location, CertificateDraft) {
            CertificateDraft.reset();
            $location.replace(true);
            $location.path('/create/choose-patient/index');
        }];
});
