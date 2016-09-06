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
angular.module('webcert').controller('webcert.ChooseCertTypeCtrl',
    ['$rootScope', '$window', '$filter', '$location', '$log', '$scope', '$stateParams', 'common.IntygService',
        'webcert.IntygProxy', 'webcert.UtkastProxy', 'common.IntygFornyaRequestModel', 'common.IntygCopyRequestModel',
        'common.PatientModel', 'common.messageService',
        function($rootScope, $window, $filter, $location, $log, $scope, $stateParams, CommonIntygService,
            IntygProxy, UtkastProxy, IntygFornyaRequestModel, IntygCopyRequestModel, PatientModel, messageService) {
            'use strict';

            /**
             * Page state
             */
            var changePatientUrl = '/create/index';

            // Page setup
            $scope.focusFirstInput = true;
            $scope.viewState = {
                doneLoading: true,
                activeErrorMessageKey: null,
                createErrorMessageKey: null,
                inlineErrorMessageKey: null,
                currentList: undefined,
                unsigned: 'intyglist-empty' // unsigned, unsigned-mixed,
            };

            $scope.filterForm = {
                intygFilter: 'current' // possible values: current, revoked, all
            };

            $scope.personnummer = PatientModel.personnummer;
            $scope.sekretessmarkering = PatientModel.sekretessmarkering;
            $scope.fornamn = PatientModel.fornamn;
            $scope.mellannamn = PatientModel.mellannamn;
            $scope.efternamn = PatientModel.efternamn;
            $scope.fornyaTitleText = messageService.getProperty('fk7263.label.fornya.text');

            $scope.intygType = 'default';
            $scope.certificateTypeText = '';

            // Format: { id: 'default', label: '' }
            $scope.intygTypes = [];


            /**
             * Private functions
             * @private
             */

            function onPageLoad() {
                // Redirect to index if pnr and name isn't specified
                if (!PatientModel.personnummer || !PatientModel.fornamn || !PatientModel.efternamn) {
                    $location.url(changePatientUrl, true);
                    return;
                }

                // Load intyg types user can choose from
                UtkastProxy.getUtkastTypes(function(types) {
                    $scope.intygTypes = types;
                    if (PatientModel.intygType) {
                        $scope.intygType = PatientModel.intygType;
                    }
                });

                // Load intyg for person with specified pnr
                IntygProxy.getIntygForPatient($scope.personnummer, function(data) {
                    $scope.viewState.doneLoading = false;
                    $scope.viewState.intygListUnhandled = data;
                    $scope.updateIntygList();
                    hasUnsigned($scope.viewState.currentList);
                    $window.doneLoading = true;
                }, function(errorData, errorCode) {
                    $scope.viewState.doneLoading = false;
                    $log.debug('Query Error' + errorData);
                    $scope.viewState.activeErrorMessageKey = errorCode;
                });

                $scope.$watch('current.selected', function(newValue, oldValue) {
                    if (newValue !== oldValue) {
                        $scope.intygType = newValue;
                    }
                });
            }

            function hasUnsigned(list) {
                if (!list) {
                    return;
                }
                if (list.length === 0) {
                    $scope.viewState.unsigned = 'intyglist-empty';
                    return;
                }
                var unsigned = true;
                for (var i = 0; i < list.length; i++) {
                    var item = list[i];
                    if (item.status === 'DRAFT_COMPLETE') {
                        unsigned = false;
                        break;
                    }
                }
                if (unsigned) {
                    $scope.viewState.unsigned = 'unsigned';
                } else {
                    $scope.viewState.unsigned = 'signed';
                }
            }


            /**
             * Watches
             */

            $scope.$watch('filterForm.intygFilter', function() {
                $scope.updateIntygList();
            });

            /**
             * Exposed to scope
             */

            $scope.updateIntygList = function() {
                $scope.viewState.currentList =
                    $filter('TidigareIntygFilter')($scope.viewState.intygListUnhandled, $scope.filterForm.intygFilter);
            };

            $scope.changePatient = function() {
                $location.path(changePatientUrl);
            };

            //Use loaded module metadata to look up detailed description for a intygsType
            $scope.getDetailedDescription = function(intygsType) {
                var intygTypes = $scope.intygTypes.filter(function(intygType) {
                    return (intygType.id === intygsType);
                });
                if (intygTypes && intygTypes.length>0) {
                    return intygTypes[0].detailedDescription;
                }
            };


            $scope.createDraft = function() {

                var createDraftRequestPayload = {
                    intygType: $scope.intygType,
                    patientPersonnummer: PatientModel.personnummer,
                    patientFornamn: PatientModel.fornamn,
                    patientMellannamn: PatientModel.mellannamn,
                    patientEfternamn: PatientModel.efternamn,
                    patientPostadress: PatientModel.postadress,
                    patientPostnummer: PatientModel.postnummer,
                    patientPostort: PatientModel.postort
                };
                UtkastProxy.createUtkast(createDraftRequestPayload, function(data) {
                    $scope.viewState.createErrorMessageKey = undefined;
                    $location.url('/' + createDraftRequestPayload.intygType + '/edit/' + data.intygsId, true);
                }, function(error) {
                    $log.debug('Create draft failed: ' + error.message);
                    $scope.viewState.createErrorMessageKey = 'error.failedtocreateintyg';
                });
            };

            $scope.openIntyg = function(intyg) {
                if (intyg.status === 'DRAFT_INCOMPLETE' || intyg.status === 'DRAFT_COMPLETE') {
                    $location.path('/' + intyg.intygType + '/edit/' + intyg.intygId);
                } else {
                    $location.path('/intyg/' + intyg.intygType + '/' + intyg.intygId);
                }
            };

            $scope.copyIntyg = function(intyg) {
                $scope.viewState.createErrorMessageKey = null;

                // We don't have the required info about issuing unit in the supplied 'intyg' object, always set to true.
                // It only affects a piece of text in the Kopiera-dialog anyway.
                var isOtherCareUnit = true;

                CommonIntygService.copy($scope.viewState,
                    IntygCopyRequestModel.build({
                        intygId: intyg.intygId,
                        intygType: intyg.intygType,
                        patientPersonnummer: $scope.personnummer,
                        nyttPatientPersonnummer: $stateParams.patientId
                    }),
                    isOtherCareUnit
                );
            };

            $scope.fornyaIntyg = function(intyg) {
                $scope.viewState.createErrorMessageKey = null;

                // We don't have the required info about issuing unit in the supplied 'intyg' object, always set to true.
                // It only affects a piece of text in the Kopiera-dialog anyway.
                var isOtherCareUnit = true;

                CommonIntygService.fornya($scope.viewState,
                    IntygFornyaRequestModel.build({
                        intygId: intyg.intygId,
                        intygType: intyg.intygType,
                        patientPersonnummer: $scope.personnummer,
                        nyttPatientPersonnummer: $stateParams.patientId
                    }),
                    isOtherCareUnit
                );
            };

            onPageLoad();
        }])
;
