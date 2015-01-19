/*
 * Controller for logic related to viewing signed certs
 */
angular.module('webcert').controller('webcert.ViewCertCtrl',
    [ '$rootScope', '$routeParams', '$scope', '$window', '$location', '$q', 'common.dialogService',
        'webcert.ManageCertificate', 'common.UserPreferencesService',
        function($rootScope, $routeParams, $scope, $window, $location, $q, dialogService, ManageCertificate, UserPreferencesService) {
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
                    console.debug("def 1");
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
                                    console.debug("def 2");
                                    var deferred = $q.defer();
                                    $scope.$broadcast('markAllAsHandledEvent', deferred);
                                    deferred.promise.then(function(){

                                        console.debug("def 2 then");
                                        modal.close('hantera');
                                        // unbind the location change listener
                                        unbindCheckHandledEvent();
                                        $window.location.href = newUrl;
                                    });
                                },
                                button2click: function() {
                                    modal.close('ejhantera');
                                    // unbind the location change listener
                                    unbindCheckHandledEvent();
                                    $window.location.href = newUrl;
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
                            // unbind the location change listener
                            unbindCheckHandledEvent();
                            $window.location.href = newUrl;
                        }
                    });
                } else {
                    //$event.preventDefault();
                    unbindCheckHandledEvent();
                    //$window.location.href = newUrl;
                }
            });
            $scope.$on('$destroy', unbindCheckHandledEvent);

            ManageCertificate.getCertType($routeParams.certificateType, function(intygType) {
                $scope.widgetState.fragaSvarAvailable = intygType.fragaSvarAvailable;
                $scope.widgetState.printStatus = intygType.printStatus;
            });

        }]);
