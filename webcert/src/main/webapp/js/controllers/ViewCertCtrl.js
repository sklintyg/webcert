define([
    'angular'
], function(angular) {
    'use strict';

    var moduleName = 'wc.ViewCertCtrl';

    /*
     * Controller for logic related to viewing signed certs
     */
    angular.module(moduleName, []).
        controller(moduleName, [ '$routeParams', '$scope', 'ManageCertificate',
            function($routeParams, $scope, ManageCertificate) {

                $scope.widgetState = {
                    certificateType: $routeParams.certificateType,
                    fragaSvarAvailable: ManageCertificate.getCertType($routeParams.certificateType)
                };
            }
        ]);

    return moduleName;
});
