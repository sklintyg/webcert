define([
    'angular',
    'services/CreateCertificateDraft',
    'services/ManageCertificate',
    'webjars/common/webcert/js/services/dialogService',
    'webjars/common/webcert/js/services/User',
    'webjars/common/webcert/js/services/ManageCertView'
], function(angular, CreateCertificateDraft, ManageCertificate, dialogService, User, ManageCertView) {
    'use strict';

    var moduleName = 'wc.ChooseCertTypeCtrl';

    angular.module(moduleName, [ CreateCertificateDraft, dialogService, ManageCertificate, User, ManageCertView ]).
        controller(moduleName, [ '$filter', '$location', '$log', '$scope', '$cookieStore', CreateCertificateDraft, dialogService,
            ManageCertificate, User, ManageCertView,
            function($filter, $location, $log, $scope, $cookieStore, CreateCertificateDraft, dialogService, ManageCertificate, User, ManageCertView) {
                if (!CreateCertificateDraft.personnummer || !CreateCertificateDraft.firstname ||
                    !CreateCertificateDraft.lastname) {
                    $location.url('/create/index', true);
                }

                // Copy dialog setup
                var COPY_DIALOG_COOKIE = 'wc.dontShowCopyDialog';
                var copyDialog = {
                    isOpen: false
                };
                $scope.dialog = {
                    acceptprogressdone: true,
                    focus: false,
                    errormessageid: 'error.failedtocopyintyg',
                    showerror: false,
                    dontShowCopyInfo: $cookieStore.get(COPY_DIALOG_COOKIE)
                };

                // Page setup
                $scope.widgetState = {
                    doneLoading: true,
                    activeErrorMessageKey: null,
                    createErrorMessageKey: null,
                    currentList: undefined,
                };

                $scope.filterForm = {
                    intygFilter: 'current'
                };

                $scope.personnummer = CreateCertificateDraft.personnummer;
                $scope.firstname = CreateCertificateDraft.firstname;
                $scope.lastname = CreateCertificateDraft.lastname;

                $scope.intygType = 'default';
                $scope.certificateTypeText = '';

                $scope.certTypes = [
                    {
                        id: 'default',
                        label: ''
                    }
                ];

                ManageCertificate.getCertTypes(function(types) {
                    $scope.certTypes = types;
                    $scope.intygType = CreateCertificateDraft.intygType;
                });

                $scope.updateCertList = function() {
                    $scope.widgetState.currentList =
                        $filter('wc.TidigareIntygFilter')($scope.widgetState.certListUnhandled, $scope.filterForm.intygFilter);
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

                function _createDraft() {
                    var valdVardenhet = User.getValdVardenhet();
                    CreateCertificateDraft.vardGivareHsaId = valdVardenhet.id;
                    CreateCertificateDraft.vardGivareNamn = valdVardenhet.namn;
                    CreateCertificateDraft.vardEnhetHsaId = valdVardenhet.id;
                    CreateCertificateDraft.vardEnhetNamn = valdVardenhet.namn;
                    CreateCertificateDraft.createDraft(function(data) {
                        $scope.widgetState.createErrorMessageKey = undefined;
                        $location.url('/' + CreateCertificateDraft.intygType + '/edit/' + data, true);
                        CreateCertificateDraft.reset();
                    }, function(error) {
                        $log.debug('Create draft failed: ' + error.message);
                        $scope.widgetState.createErrorMessageKey = 'error.failedtocreateintyg';
                    });
                }

                /**
                 * Exposed to scope
                 */

                $scope.lookupAddress = function() {
                    CreateCertificateDraft.intygType = $scope.intygType;

                    // TODO: create a list with which intygTypes wants and address or not. FK7263 does not want an address,
                    // so hardcoded for now as it is the only one in the foreseeble future
                    if (CreateCertificateDraft.intygType !== 'fk7263' && CreateCertificateDraft.address) {
                        var bodyText = 'Patienten har tidigare intyg där adressuppgifter har angivits. Vill du ' +
                            'återanvända dessa i det nya intyget?<br><br>Adress: ' + CreateCertificateDraft.address;

                        dialogService.showDialog($scope, {
                            dialogId: 'confirm-address-dialog',
                            titleId: 'label.confirmaddress',
                            bodyText: bodyText,

                            button1click: function() {
                                $log.debug('confirm address yes');
                                _createDraft();
                            },
                            button2click: function() {
                                $log.debug('confirm address no');
                                CreateCertificateDraft.address = null;
                                _createDraft();
                            },

                            button1text: 'common.yes',
                            button2text: 'common.no',
                            button3text: 'common.cancel'
                        });
                    } else {
                        // Address is not important
                        CreateCertificateDraft.address = null;
                        _createDraft();
                    }
                };

                $scope.changePatient = function() {
                    $location.path('/create/index');
                };
                $scope.editPatientName = function() {
                    $location.path('/create/edit-patient-name/index');
                };

                $scope.$watch('filterForm.intygFilter', function() {
                    $scope.updateCertList();
                });

                $scope.$watch('widgetState.dontShowCopyInfo', function(newVal, oldVal) {
                    if (newVal) {
                        $cookieStore.put(COPY_DIALOG_COOKIE, newVal);
                    } else {
                        $cookieStore.remove(COPY_DIALOG_COOKIE);
                    }
                });

                $scope.openIntyg = function(cert) {
                    if (cert.source === 'WC') {
                        $location.path('/' + cert.intygType + '/edit/' + cert.intygId);
                        CreateCertificateDraft.reset();
                    } else {
                        $location.path('/intyg/' + cert.intygType + '/' + cert.intygId);
                    }
                };

                $scope.copyIntyg = function(cert) {
                    copyDialog = ManageCertificate.copy($scope, cert, copyDialog, COPY_DIALOG_COOKIE);
                };
            }
        ]);

    return moduleName;
});
