/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
angular.module('webcert').controller('webcert.VisaIntygFragasvarCtrl',
    ['$rootScope', '$state', '$stateParams', '$scope', '$location', '$q', '$uibModalStack', 'common.dialogService',
        'webcert.UtkastProxy', 'common.UserPreferencesService', 'common.enhetArendenCommonService',
        'common.featureService',
        function($rootScope, $state, $stateParams, $scope, $location, $q, $uibModalStack, dialogService, UtkastProxy,
            UserPreferencesService, enhetArendenCommonService, featureService) {
            'use strict';

            var certificateType = $state.current.data.intygType;
            if ($stateParams.certificateType) {
                certificateType = $stateParams.certificateType;
            }

            var unhandledDialogConfig = {
                skipShowUnhandledDialog: false,
                setSkipShowUnhandledDialog: function(unhandledDialogConfig) {
                    UserPreferencesService.setSkipShowUnhandledDialog(unhandledDialogConfig.skipShowUnhandledDialog);
                }
            };
            
            var unbindCheckHandledEvent = $rootScope.$on('$locationChangeStart',
                function($event, newUrl, currentUrl) {

                    // When disable flag is set, just bail.
                    // Needed to get wcCloseModals directive to work properly
                    if (typeof $uibModalStack.disableModals !== 'undefined' && $uibModalStack.disableModals) {
                        delete $uibModalStack.disableModals;
                        return;
                    }

                    // Show dialogs to mark all unhandled messages as handled and
                    // to inform uthopp/cosmic users that intyg should not be created in webcert by them

                    // Check if QA is an active feature for the current intyg.
                    if (featureService.isFeatureActive(featureService.features.HANTERA_FRAGOR,
                            certificateType)) {

                        // if we're changing url
                        if (newUrl !== currentUrl && !UserPreferencesService.isSkipShowUnhandledDialogSet()) {
                            unhandledDialogConfig.skipShowUnhandledDialog =
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
                                            var deferred = $q.defer();
                                            $scope.$broadcast('markAnsweredAsHandledEvent', deferred, unhandledQas);
                                            deferred.promise.then(function() {

                                                modal.close('hantera');
                                                enhetArendenCommonService.checkQAonlyDialog($scope, $event, newUrl,
                                                    currentUrl, unbindCheckHandledEvent);
                                            });
                                        },
                                        button2click: function() {
                                            modal.close('ejhantera');
                                            enhetArendenCommonService.checkQAonlyDialog($scope, $event, newUrl,
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
                                            widgetState: unhandledDialogConfig
                                        }
                                    });
                                } else {
                                    enhetArendenCommonService.checkQAonlyDialog($scope, $event, newUrl, currentUrl,
                                        unbindCheckHandledEvent);
                                }
                            });
                        } else {
                            enhetArendenCommonService.checkQAonlyDialog($scope, $event, newUrl, currentUrl,
                                unbindCheckHandledEvent);
                        }
                    }
                });
            $scope.$on('$destroy', unbindCheckHandledEvent);
        }]);
