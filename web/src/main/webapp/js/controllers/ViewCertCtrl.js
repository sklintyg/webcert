/*
 * Controller for logic related to viewing signed certs
 */
angular.module('webcert').controller('webcert.ViewCertCtrl',
    [ '$rootScope', '$routeParams', '$scope', '$window', 'common.dialogService', 'webcert.ManageCertificate',
        function($rootScope, $routeParams, $scope, $window, dialogService, ManageCertificate) {
            'use strict';

            // Check if the user used the special qa-link to get here.
            if ($routeParams.qaOnly) {
                var locationEvent = $rootScope.$on('$locationChangeStart', function(event, newUrl) {
                    event.preventDefault();
                    dialogService.showDialog($scope, {
                        dialogId: 'qa-only-warning-dialog',
                        titleId: 'label.qaonlywarning',
                        bodyTextId: 'label.qaonlywarning.body',
                        templateUrl: '/views/partials/qa-only-warning-dialog.html',
                        button1click: function() {
                            locationEvent();
                            $window.location = newUrl;
                        },
                        button1text: 'common.continue',
                        button1id: 'button1continue-dialog',
                        button2text: 'common.cancel',
                        autoClose: true
                    });
                });
            }

            $scope.widgetState = {
                certificateType: $routeParams.certificateType,
                fragaSvarAvailable: false
            };

            ManageCertificate.getCertType($routeParams.certificateType, function(intygType) {
                $scope.widgetState.fragaSvarAvailable = intygType.fragaSvarAvailable;
                $scope.widgetState.printStatus = intygType.printStatus;
            });
        }]);
