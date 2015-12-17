/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Controller for logic related to viewing signed certs
 */
angular.module('webcert').controller('webcert.ViewCertCtrl',
    ['$rootScope', '$stateParams', '$scope', '$window', '$location', '$q', 'common.dialogService',
        'webcert.UtkastProxy', 'common.UserPreferencesService', 'common.fragaSvarCommonService',
        'common.featureService',
        function($rootScope, $stateParams, $scope, $window, $location, $q, dialogService, UtkastProxy,
            UserPreferencesService, fragaSvarCommonService, featureService) {
            'use strict';

            $scope.widgetState = {
                certificateType: $stateParams.certificateType,
                fragaSvarAvailable: false,
                skipShowUnhandledDialog: false,
                setSkipShowUnhandledDialog: function(widgetState) {
                    UserPreferencesService.setSkipShowUnhandledDialog(widgetState.skipShowUnhandledDialog);
                }
            };


            var unbindCheckHandledEvent = $rootScope.$on('$locationChangeStart',
                function($event, newUrl, currentUrl) {

                    // Show dialogs to mark all unhandled messages as handled and
                    // to inform uthopp/cosmic users that intyg should not be created in webcert by them

                    // Check if QA is an active feature for the current intyg.
                    if (featureService.isFeatureActive(featureService.features.HANTERA_FRAGOR,
                            $scope.widgetState.certificateType)) {

                        // if we're changing url
                        if (newUrl !== currentUrl && !UserPreferencesService.isSkipShowUnhandledDialogSet()) {
                            $scope.widgetState.skipShowUnhandledDialog =
                                UserPreferencesService.isSkipShowUnhandledDialogSet();
                            $event.preventDefault();
                            var deferred = $q.defer();

                            // Depends on QACtrl listener. If there is no listener the page will hang here.
                            $scope.$broadcast('hasUnhandledQasEvent', deferred);

                            // When deferred is resolved in listener above the dialog is shown
                            deferred.promise.then(function(unhandledQas) {
                                if (unhandledQas && unhandledQas.length > 0) {
                                    var modal = dialogService.showDialog({
                                        dialogId: 'qa-check-hanterad-dialog',
                                        titleId: 'label.qacheckhanterad.title',
                                        bodyTextId: 'label.qacheckhanterad.body',
                                        templateUrl: '/app/partials/qa-check-hanterad-dialog.html',
                                        button1click: function() {
                                            $window.doneLoading = false; // should be resolved in the ajax callback in QACtrl
                                            var deferred = $q.defer();
                                            $scope.$broadcast('markAnsweredAsHandledEvent', deferred, unhandledQas);
                                            deferred.promise.then(function() {

                                                modal.close('hantera');
                                                fragaSvarCommonService.checkQAonlyDialog($scope, $event, newUrl,
                                                    currentUrl, unbindCheckHandledEvent);
                                            });
                                        },
                                        button2click: function() {
                                            modal.close('ejhantera');
                                            fragaSvarCommonService.checkQAonlyDialog($scope, $event, newUrl,
                                                currentUrl, unbindCheckHandledEvent);
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
                                        autoClose: true,
                                        model: {
                                            widgetState: $scope.widgetState
                                        }
                                    });
                                } else {
                                    fragaSvarCommonService.checkQAonlyDialog($scope, $event, newUrl, currentUrl,
                                        unbindCheckHandledEvent);
                                }
                            });
                        } else {
                            fragaSvarCommonService.checkQAonlyDialog($scope, $event, newUrl, currentUrl,
                                unbindCheckHandledEvent);
                        }
                    }
                });
            $scope.$on('$destroy', unbindCheckHandledEvent);


            UtkastProxy.getUtkastType($stateParams.certificateType, function(intygType) {
                $scope.widgetState.fragaSvarAvailable = intygType.fragaSvarAvailable;
                $scope.widgetState.printStatus = intygType.printStatus;
            });

        }]);
