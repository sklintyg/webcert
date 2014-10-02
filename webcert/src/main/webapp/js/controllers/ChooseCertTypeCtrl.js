angular.module('webcert').controller('webcert.ChooseCertTypeCtrl',
    [ '$filter', '$location', '$log', '$scope', '$cookieStore', 'webcert.CreateCertificateDraft',
        'common.dialogService', 'webcert.ManageCertificate',
        function($filter, $location, $log, $scope, $cookieStore, CreateCertificateDraft, dialogService,
            ManageCertificate) {
            'use strict';

            if (!CreateCertificateDraft.personnummer || !CreateCertificateDraft.fornamn ||
                !CreateCertificateDraft.efternamn) {
                $location.url('/create/index', true);
            }

            // Page setup
            $scope.widgetState = {
                doneLoading: true,
                activeErrorMessageKey: null,
                createErrorMessageKey: null,
                currentList: undefined
            };

            $scope.filterForm = {
                intygFilter: 'current'
            };

            $scope.personnummer = CreateCertificateDraft.personnummer;
            $scope.fornamn = CreateCertificateDraft.fornamn;
            $scope.mellannamn = CreateCertificateDraft.mellannamn;
            $scope.efternamn = CreateCertificateDraft.efternamn;

            $scope.intygType = 'default';
            $scope.certificateTypeText = '';

            $scope.certTypes = [
/*                {
                    id: 'default',
                    label: ''
                }*/
            ];

            ManageCertificate.getCertTypes(function(types) {
                $scope.certTypes = types;
                $scope.intygType = CreateCertificateDraft.intygType;
            });

            $scope.updateCertList = function() {
                $scope.widgetState.currentList =
                    $filter('TidigareIntygFilter')($scope.widgetState.certListUnhandled, $scope.filterForm.intygFilter);
            };

            ManageCertificate.getCertificatesForPerson($scope.personnummer, function(data) {
                $scope.widgetState.doneLoading = false;
                $scope.widgetState.certListUnhandled = data;
                $scope.updateCertList();
            }, function(errorData) {
                $scope.widgetState.doneLoading = false;
                $log.debug('Query Error' + errorData);
                $scope.widgetState.activeErrorMessageKey = 'info.certload.error';
            });

            /**
             * Private functions
             * @private
             */

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

            /**
             * Exposed to scope
             */

            $scope.changePatient = function() {
                $location.path('/create/index');
            };

            $scope.$watch('filterForm.intygFilter', function() {
                $scope.updateCertList();
            });

            $scope.openIntyg = function(cert) {
                if (cert.source === 'WC') {
                    $location.path('/' + cert.intygType + '/edit/' + cert.intygId);
                } else {
                    $location.path('/intyg/' + cert.intygType + '/' + cert.intygId);
                }
            };

            ManageCertificate.initCopyDialog($scope);
            $scope.copyIntyg = function(cert) {
                ManageCertificate.copy($scope, cert);
            };
        }]);
