/*
 * Controller for logic related to viewing signed certs
 */
angular.module('webcert').controller('webcert.ViewCertCtrl',
    [ '$rootScope', '$routeParams', '$scope', '$window', '$location', '$q', 'common.dialogService',
        'webcert.ManageCertificate', 'common.UserPreferencesService', 'common.fragaSvarCommonService',
        function($rootScope, $routeParams, $scope, $window, $location, $q, dialogService, ManageCertificate,
                 UserPreferencesService, fragaSvarCommonService) {
            'use strict';

            $scope.widgetState = {
                certificateType: $routeParams.certificateType,
                fragaSvarAvailable: false,
                skipShowUnhandledDialog : false,
                setSkipShowUnhandledDialog : function(widgetState){
                    UserPreferencesService.setSkipShowUnhandledDialog(widgetState.skipShowUnhandledDialog);
                }
            };

            var unbindCheckHandledEvent = $rootScope.$on('$locationChangeStart', function($event, newUrl, currentUrl){
                if(newUrl !== currentUrl && !UserPreferencesService.isSkipShowUnhandledDialogSet()){  // if we're changing url
                    $scope.widgetState.skipShowUnhandledDialog = UserPreferencesService.isSkipShowUnhandledDialogSet();
                    $event.preventDefault();
                    var deferred = $q.defer();
                    $scope.$broadcast('hasUnhandledQasEvent', deferred);
                    deferred.promise.then(function(hasUnhandledQas) {
                        if (hasUnhandledQas) {
                            var modal = dialogService.showDialog($scope, {
                                dialogId: 'qa-check-hanterad-dialog',
                                titleId: 'label.qacheckhanterad.title',
                                bodyTextId: 'label.qacheckhanterad.body',
                                templateUrl: '/views/partials/qa-check-hanterad-dialog.html',
                                button1click: function() {
                                    $window.doneLoading = false; // should be resolved in the ajax callback in QACtrl
                                    var deferred = $q.defer();
                                    $scope.$broadcast('markAllAsHandledEvent', deferred);
                                    deferred.promise.then(function(){

                                        modal.close('hantera');
                                        fragaSvarCommonService.checkQAonlyDialog($scope, $event, newUrl, currentUrl, unbindCheckHandledEvent);
                                    });
                                },
                                button2click: function() {
                                    modal.close('ejhantera');
                                    fragaSvarCommonService.checkQAonlyDialog($scope, $event, newUrl, currentUrl, unbindCheckHandledEvent);
                                },
                                button3click: function() {
                                    // bara stänga modal fönstret
                                    modal.close('button3 close');
                                },
                                button1text: 'label.qacheckhanterad.hanterad',
                                button1id: 'button1checkhanterad-dialog-hantera',
                                button2text: 'label.qacheckhanterad.ejhanterad',
                                button2id: 'button1checkhanterad-dialog-ejhantera',
                                button3text: 'label.qacheckhanterad.tillbaka',
                                button3id: 'button1checkhanterad-dialog-tillbaka',
                                autoClose: true
                            });
                        } else {
                            fragaSvarCommonService.checkQAonlyDialog($scope, $event, newUrl, currentUrl, unbindCheckHandledEvent);
                        }
                    });
                } else {
                    fragaSvarCommonService.checkQAonlyDialog($scope, $event, newUrl, currentUrl, unbindCheckHandledEvent);
                }
            });
            $scope.$on('$destroy', unbindCheckHandledEvent);

            ManageCertificate.getCertType($routeParams.certificateType, function(intygType) {
                $scope.widgetState.fragaSvarAvailable = intygType.fragaSvarAvailable;
                $scope.widgetState.printStatus = intygType.printStatus;
            });

        }]);
