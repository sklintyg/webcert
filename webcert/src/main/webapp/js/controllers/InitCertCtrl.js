define([
], function () {
    'use strict';

return ['$scope', '$location', 'CreateCertificateDraft',
        function ($scope, $location, CreateCertificateDraft) {
            CreateCertificateDraft.reset();
            $location.replace(true);
            $location.path('/create/choose-patient/index');
        }];
});
