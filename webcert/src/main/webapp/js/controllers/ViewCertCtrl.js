define([
    'angular',
    'services/ManageCertificate'
], function(angular, ManageCertificate) {
    'use strict';

    var moduleName = 'wc.ViewCertCtrl';

    /*
     * Controller for logic related to viewing signed certs
     */
    angular.module(moduleName, [ManageCertificate]).
        controller(moduleName, [ '$routeParams', '$scope', ManageCertificate,
            function($routeParams, $scope, ManageCertificate) {

                $scope.widgetState = {
                    certificateType: $routeParams.certificateType,
                    fragaSvarAvailable: false
                };

                ManageCertificate.getCertType($routeParams.certificateType, function(intygType) {
                    $scope.widgetState.fragaSvarAvailable = intygType.fragaSvarAvailable;
                });
            }
        ]);

    return moduleName;
});
