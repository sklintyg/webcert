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
        'webcert.IntygProxy', 'webcert.UtkastProxy', 'common.IntygFornyaRequestModel', 'common.IntygFornyaRequestModel',
        'common.PatientModel',
        function($rootScope, $window, $filter, $location, $log, $scope, $stateParams, CommonIntygService,
            IntygProxy, UtkastProxy, IntygFornyaRequestModel, IntygCopyRequestModel, PatientModel) {
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
                unsigned: 'certlist-empty', // unsigned, unsigned-mixed,
                luseDescriptionLabel: 'DFR_3.1',
                lisuDescriptionLabel: 'XYZ123'
            };

            $scope.filterForm = {
                intygFilter: 'current' // possible values: current, revoked, all
            };

            $scope.personnummer = PatientModel.personnummer;
            $scope.sekretessmarkering = PatientModel.sekretessmarkering;
            $scope.fornamn = PatientModel.fornamn;
            $scope.mellannamn = PatientModel.mellannamn;
            $scope.efternamn = PatientModel.efternamn;

            $scope.intygType = 'default';
            $scope.certificateTypeText = '';

            // Format: { id: 'default', label: '' }
            $scope.certTypes = [];

            /*  --- PROTOTYPE CODE  START --- */
            $scope.prototypes = {
                selectedDefault: 0,
                isHighlighted: false,
                urlRoot: '/web/webjars/common/webcert/gui/sokskrivintyg/',
                fileTemplateName: 'intygValjare',
                templates: [
                    {
                        id: 0,
                        name: 'Default',
                        url: '/web/webjars/common/webcert/gui/sokskrivintyg/intygValjare.0.html'
                    },
                    {
                        id: 1,
                        name: 'variant-a',
                        url: '/web/webjars/common/webcert/gui/sokskrivintyg/intygValjare.1.html'
                    },
                    {
                        id: 2,
                        name: 'variant-b',
                        url: '/web/webjars/common/webcert/gui/sokskrivintyg/intygValjare.2.html'
                    },
                    {
                        id: 3,
                        name: 'variant-c',
                        url: '/web/webjars/common/webcert/gui/sokskrivintyg/intygValjare.3.html'
                    }
                ]
            };

            $scope.protoCertTypes = [{'sortValue': 0, 'id': 'default', 'type': 'default', 'label': 'Välj typ av intyg'},
                {'sortValue': 1, 'id': 'fk7263', 'type': 'fk', 'label': 'Läkarintyg FK 7263'},
                {'sortValue': 2, 'id': 'luse', 'type': 'fk', 'label': 'Läkarintyg, sjukersättning'},
                {'sortValue': 3, 'id': 'lisu', 'type': 'fk', 'label': 'Läkarintyg för sjukpenning utökat'},
                {'sortValue': 4, 'id': 'ts-bas', 'type': 'ts', 'label': 'Transportstyrelsens läkarintyg'},
                {
                    'sortValue': 5,
                    'id': 'ts-diabetes',
                    'type': 'ts',
                    'label': 'Transportstyrelsens läkarintyg, diabetes'
                },
                {'sortValue': 6, 'id': 'ss-dod', 'type': 'ss', 'label': 'Rigor mortis'},
                {'sortValue': 7, 'id': 'ss-dodextended', 'type': 'ss', 'label': 'Rigor mortis, uttökat'}];

            $scope.protoGroups = [{'id': 'default', 'type': 'default', 'label': 'Välj grupp för intyget'},
                {'id': 'fk', 'label': 'Försäkringskassans intyg'},
                {'id': 'ts', 'label': 'Transportstyrelsens intyg'},
                {'id': 'ss', 'label': 'Socialstyrelsens intyg'}
            ];

            $scope.current = {
                selected: 'default',
                group: 'default'
            };

            $scope.selectedTemplate = $scope.prototypes.templates[0];

            $scope.certReciever = null;

            $scope.resetPrototype = function() {
                $scope.current.selected = 'default';
                $scope.current.group = 'default';
            };

            if ( $rootScope.testModeActive === false) {
                $scope.current.selected = 'default';
                $scope.current.group = 'default';
                $scope.selectedTemplate = $scope.prototypes.templates[0];
            }

            /*  --- PROTOTYPE CODE  END --- */

            /**
             * Private functions
             * @private
             */


            function
            onPageLoad() {

                // Redirect to index if pnr and name isn't specified
                if (!PatientModel.personnummer || !PatientModel.fornamn || !PatientModel.efternamn) {
                    $location.url(changePatientUrl, true);
                }

                // Load cert types user can choose from
                UtkastProxy.getUtkastTypes(function(types) {
                    $scope.certTypes = types;
                    $scope.intygType = PatientModel.intygType;
                });

                // Load certs for person with specified pnr
                IntygProxy.getIntygForPatient($scope.personnummer, function(data) {
                    $scope.viewState.doneLoading = false;
                    $scope.viewState.certListUnhandled = data;
                    $scope.updateCertList();
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
                    $scope.viewState.unsigned = 'certlist-empty';
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
                $scope.updateCertList();
            });

            /**
             * Exposed to scope
             */

            $scope.updateCertList = function() {
                $scope.viewState.currentList =
                    $filter('TidigareIntygFilter')($scope.viewState.certListUnhandled, $scope.filterForm.intygFilter);
            };

            $scope.changePatient = function() {
                $location.path(changePatientUrl);
            };

            $scope.getDynamicText = function(key) {
                return DynamicLabelService.getProperty(key);
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

            $scope.openIntyg = function(cert) {
                if (cert.source === 'WC') {
                    $location.path('/' + cert.intygType + '/edit/' + cert.intygId);
                } else {
                    $location.path('/intyg/' + cert.intygType + '/' + cert.intygId);
                }
            };

            $scope.copyIntyg = function(cert) {
                $scope.viewState.createErrorMessageKey = null;

                // We don't have the required info about issuing unit in the supplied 'cert' object, always set to true.
                // It only affects a piece of text in the Kopiera-dialog anyway.
                var isOtherCareUnit = true;

                CommonIntygService.copy($scope.viewState,
                    IntygCopyRequestModel.build({
                        intygId: cert.intygId,
                        intygType: cert.intygType,
                        patientPersonnummer: $scope.personnummer,
                        nyttPatientPersonnummer: $stateParams.patientId
                    }),
                    isOtherCareUnit
                );
            };

            $scope.fornyaIntyg = function(cert) {
                $scope.viewState.createErrorMessageKey = null;

                // We don't have the required info about issuing unit in the supplied 'cert' object, always set to true.
                // It only affects a piece of text in the Kopiera-dialog anyway.
                var isOtherCareUnit = true;

                CommonIntygService.fornya($scope.viewState,
                    IntygFornyaRequestModel.build({
                        intygId: cert.intygId,
                        intygType: cert.intygType,
                        patientPersonnummer: $scope.personnummer,
                        nyttPatientPersonnummer: $stateParams.patientId
                    }),
                    isOtherCareUnit
                );
            };

            onPageLoad();
        }])
;
