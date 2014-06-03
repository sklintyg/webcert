define([
    'angular',
    'services/CreateCertificateDraft',
    'services/ManageCertificate',
    'webjars/common/webcert/js/services/dialogService',
    'webjars/common/webcert/js/services/User'
], function(angular, CreateCertificateDraft, ManageCertificate, dialogService, User) {
    'use strict';

    var moduleName = 'wc.ChooseCertTypeCtrl';

    angular.module(moduleName, [ CreateCertificateDraft, dialogService, ManageCertificate, User ]).
        controller(moduleName, [ '$filter', '$location', '$log', '$scope', '$cookieStore', CreateCertificateDraft, dialogService,
            ManageCertificate, User,
            function($filter, $location, $log, $scope, $cookieStore, CreateCertificateDraft, dialogService, ManageCertificate, User) {
                if (!CreateCertificateDraft.personnummer || !CreateCertificateDraft.firstname ||
                    !CreateCertificateDraft.lastname) {
                    $location.url('/create/index', true);
                }

                var COPY_DIALOG_COOKIE = 'wc.dontShowCopyDialog';

                $scope.widgetState = {
                    doneLoading: true,
                    activeErrorMessageKey: null,
                    createErrorMessageKey: null,
                    currentList: undefined,
                    dontShowCopyInfo: $cookieStore.get(COPY_DIALOG_COOKIE)
                };

                var copyDialog = {
                    isOpen: false
                };

                $scope.dialog = {
                    acceptprogressdone: true,
                    focus: false,
                    errormessageid: 'error.failedtocopyintyg',
                    showerror: false
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

                function _copyIntyg(cert) {
                    var valdVardenhet = User.getValdVardenhet();
                    CreateCertificateDraft.vardGivareHsaId = valdVardenhet.id;
                    CreateCertificateDraft.vardGivareNamn = valdVardenhet.namn;
                    CreateCertificateDraft.vardEnhetHsaId = valdVardenhet.id;
                    CreateCertificateDraft.vardEnhetNamn = valdVardenhet.namn;
                    CreateCertificateDraft.intygType = cert.intygType;

                    $scope.dialog.showerror = false;
                    $scope.dialog.acceptprogressdone = false;
                    $scope.widgetState.activeErrorMessageKey = null;
                    CreateCertificateDraft.copyIntygToDraft(cert, function(data) {
                        $scope.dialog.acceptprogressdone = true;
                        $scope.widgetState.createErrorMessageKey = undefined;
                        copyDialog.close();
                        $location.url('/' + CreateCertificateDraft.intygType + '/edit/' + data, true);
                        CreateCertificateDraft.reset();
                    }, function(error) {
                        $log.debug('Create copy failed: ' + error.message);
                        $scope.dialog.acceptprogressdone = true;
                        $scope.dialog.showerror = true;
                        if (!copyDialog.isOpen && $cookieStore.get(COPY_DIALOG_COOKIE)) {
                            $scope.widgetState.activeErrorMessageKey = 'error.failedtocopyintyg';
                        }
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

                    if ($cookieStore.get(COPY_DIALOG_COOKIE)) {
                        $log.debug('copy cert without dialog' + cert);
                        _copyIntyg(cert);
                    } else {
                        copyDialog = dialogService.showDialog($scope, {
                            dialogId: 'copy-dialog',
                            titleId: 'label.copycert',
                            templateUrl: '/views/partials/check-dialog.html',
                            model: $scope.widgetState,
                            bodyText: 'När du kopierar detta intyg får du upp ett nytt intyg av samma typ och med ' +
                                'samma information som finns i det intyg som du kopierar. Du får möjlighet att redigera ' +
                                'informationen innan du signerar det nya intyget.',
                            button1click: function() {
                                $log.debug('copy cert from dialog' + cert);
                                _copyIntyg(cert);
                            },
                            button1text: 'common.copy',
                            button2text: 'common.cancel',
                            autoClose: false
                        });

                        copyDialog.opened.then(function() {
                            copyDialog.isOpen = true;
                        }, function() {
                            copyDialog.isOpen = false;
                        });
                    }

                };
            }
        ]);

    return moduleName;
});
