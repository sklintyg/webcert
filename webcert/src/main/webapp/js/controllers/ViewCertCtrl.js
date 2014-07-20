/*
 * Controller for logic related to viewing signed certs
 */
angular.module('webcert').controller('webcert.ViewCertCtrl',
    [ '$routeParams', '$scope', 'webcert.ManageCertificate',
        function($routeParams, $scope, ManageCertificate) {
            'use strict';

            $scope.widgetState = {
                certificateType: $routeParams.certificateType,
                fragaSvarAvailable: false
            };

            ManageCertificate.getCertType($routeParams.certificateType, function(intygType) {
                $scope.widgetState.fragaSvarAvailable = intygType.fragaSvarAvailable;
            });
        }]);
