angular.module('webcert').controller('webcert.ChooseCertTypeCtrl',
    [ '$filter', '$location', '$log', '$scope', '$cookieStore', 'webcert.CreateCertificateDraft', 'webcert.ManageCertificate',
        function($filter, $location, $log, $scope, $cookieStore, CreateCertificateDraft,
            ManageCertificate) {
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
                currentList: undefined
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
                }, function(errorData) {
                    $scope.widgetState.doneLoading = false;
                    $log.debug('Query Error' + errorData);
                    $scope.widgetState.activeErrorMessageKey = 'info.certload.error';
                });

                // Prepare copy dialog
                ManageCertificate.initCopyDialog($scope);
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
                CreateCertificateDraft.intygType = $scope.intygType;
                CreateCertificateDraft.createDraft(function(data) {
                    $scope.widgetState.createErrorMessageKey = undefined;
                    $location.url('/' + CreateCertificateDraft.intygType + '/edit/' + data, true);
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
                ManageCertificate.copy($scope, cert);
            };

            onPageLoad();
        }]);
