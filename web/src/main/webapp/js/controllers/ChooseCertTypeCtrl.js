angular.module('webcert').controller('webcert.ChooseCertTypeCtrl',
    [ '$window', '$filter', '$location', '$log', '$scope', '$cookieStore', '$stateParams', 'webcert.CreateCertificateDraft',
        'webcert.ManageCertificate', 'common.IntygCopyRequestModel',
        function($window, $filter, $location, $log, $scope, $cookieStore, $stateParams, CreateCertificateDraft,
            ManageCertificate, IntygCopyRequestModel) {
            'use strict';

            /**
             * Page state
             */
            var changePatientUrl = '/create/index';

            // Page setup
            $scope.focusFirstInput = true;
            $scope.widgetState = {
                doneLoading: true,
                activeErrorMessageKey: null,
                createErrorMessageKey: null,
                inlineErrorMessageKey: null,
                currentList: undefined,
                unsigned : 'certlist-empty' // unsigned, unsigned-mixed,
            };

            $scope.filterForm = {
                intygFilter: 'current' // possible values: current, revoked, all
            };

            $scope.personnummer = CreateCertificateDraft.personnummer;
            $scope.fornamn = CreateCertificateDraft.fornamn;
            $scope.mellannamn = CreateCertificateDraft.mellannamn;
            $scope.efternamn = CreateCertificateDraft.efternamn;

            $scope.intygType = 'default';
            $scope.certificateTypeText = '';

            // Format: { id: 'default', label: '' }
            $scope.certTypes = [];

            /**
             * Private functions
             * @private
             */

            function onPageLoad() {

                // Redirect to index if pnr and name isn't specified
                if (!CreateCertificateDraft.personnummer || !CreateCertificateDraft.fornamn ||
                    !CreateCertificateDraft.efternamn) {
                    $location.url(changePatientUrl, true);
                }

                // Load cert types user can choose from
                ManageCertificate.getCertTypes(function(types) {

                    $scope.certTypes = types;
                    $scope.intygType = CreateCertificateDraft.intygType;
                });

                // Load certs for person with specified pnr
                ManageCertificate.getCertificatesForPerson($scope.personnummer, function(data) {
                    $scope.widgetState.doneLoading = false;
                    $scope.widgetState.certListUnhandled = data;
                    $scope.updateCertList();
                    hasUnsigned($scope.widgetState.currentList);
                    $window.doneLoading = true;
                }, function(errorData) {
                    $scope.widgetState.doneLoading = false;
                    $log.debug('Query Error' + errorData);
                    $scope.widgetState.activeErrorMessageKey = 'info.certload.error';
                });
            }

            function hasUnsigned(list){
                if(!list){
                    return;
                }
                if(list.length === 0){
                    $scope.widgetState.unsigned = 'certlist-empty';
                    return;
                }
                var unsigned = true;
                for(var i=0; i< list.length; i++){
                    var item = list[i];
                    if(item.status === 'DRAFT_COMPLETE' ){
                        unsigned = false;
                        break;
                    }
                }
                if(unsigned){
                    $scope.widgetState.unsigned = 'unsigned';
                } else {
                    $scope.widgetState.unsigned = 'signed';
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
                $scope.widgetState.currentList =
                    $filter('TidigareIntygFilter')($scope.widgetState.certListUnhandled, $scope.filterForm.intygFilter);
            };

            $scope.changePatient = function() {
                $location.path(changePatientUrl);
            };

            $scope.createDraft = function() {

                var createDraftRequestPayload = {
                    intygType: $scope.intygType,
                    patientPersonnummer: CreateCertificateDraft.personnummer,
                    patientFornamn: CreateCertificateDraft.fornamn,
                    patientMellannamn: CreateCertificateDraft.mellannamn,
                    patientEfternamn: CreateCertificateDraft.efternamn,
                    patientPostadress: CreateCertificateDraft.postadress,
                    patientPostnummer: CreateCertificateDraft.postnummer,
                    patientPostort: CreateCertificateDraft.postort
                };
                CreateCertificateDraft.createDraft(createDraftRequestPayload, function(data) {
                    $scope.widgetState.createErrorMessageKey = undefined;
                    $location.url('/' + createDraftRequestPayload.intygType + '/edit/' + data, true);
                }, function(error) {
                    $log.debug('Create draft failed: ' + error.message);
                    $scope.widgetState.createErrorMessageKey = 'error.failedtocreateintyg';
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
                ManageCertificate.copy($scope,
                    IntygCopyRequestModel.build({
                        intygId: cert.intygId,
                        intygType: cert.intygType,
                        patientPersonnummer: $scope.personnummer,
                        nyttPatientPersonnummer: $stateParams.patientId
                    }),
                    false
                );
            };

            onPageLoad();
        }]);
