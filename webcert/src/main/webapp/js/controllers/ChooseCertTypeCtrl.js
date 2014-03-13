define([
], function () {
    'use strict';

    return ['$rootScope', '$scope', '$window', '$location', '$filter', '$log', '$timeout', 'wcDialogService', 'dashBoardService', 'CertificateDraft', 'User',
        function ($rootScope, $scope, $window, $location, $filter, $log, $timeout, wcDialogService, dashBoardService, CertificateDraft, User) {
            if (!CertificateDraft.personnummer || !CertificateDraft.firstname || !CertificateDraft.lastname) {
                $location.url('/create/index', true);
            }

            $scope.personnummer = CertificateDraft.personnummer;
            $scope.firstname = CertificateDraft.firstname;
            $scope.lastname = CertificateDraft.lastname;

            CertificateDraft.getCertTypes(function (types) {
                $scope.certTypes = types;
                $scope.intygType = CertificateDraft.intygType;
            });

            function _createDraft () {
                var valdVardenhet = User.getValdVardenhet();
                CertificateDraft.vardGivareHsaId = valdVardenhet.id;
                CertificateDraft.vardGivareNamn = valdVardenhet.namn;
                CertificateDraft.vardEnhetHsaId = valdVardenhet.id;
                CertificateDraft.vardEnhetNamn = valdVardenhet.namn;
                CertificateDraft.createDraft(function (data) {
                    $window.location.href = '/m/' + CertificateDraft.intygType + '/webcert/intyg/' + data + '/edit#/edit';
                    CertificateDraft.reset();
                }, function () {
                    // TODO: handle error visually for "failed to create cert"
                });
            }

            $scope.lookupAddress = function () {
                CertificateDraft.intygType = $scope.intygType;

                // TODO: create a list with which intygTypes wants and address or not. FK7263 does not want an address, so hardcoded for now as it is the only one in the foreseeble future
                if (CertificateDraft.intygType !== 'fk7263' && CertificateDraft.address) {
                    var bodyText = 'Patienten har tidigare intyg där adressuppgifter har angivits. Vill du återanvända dessa i det nya intyget?<br>' +
                        '<br>Adress: ' + CertificateDraft.address;

                    wcDialogService.showDialog($scope, {
                        dialogId : 'confirm-address-dialog',
                        titleId : 'label.confirmaddress',
                        bodyText : bodyText,

                        button1click : function () {
                            $log.debug('confirm address yes');
                            _createDraft();
                        },
                        button2click : function () {
                            $log.debug('confirm address no');
                            CertificateDraft.address = null;
                            _createDraft();
                        },

                        button1text : 'common.yes',
                        button2text : 'common.no',
                        button3text : 'common.cancel'
                    });
                } else {
                    // Address is not important
                    CertificateDraft.address = null;
                    _createDraft();
                }
            };

            $scope.changePatient = function () {
                $location.path('/create/index');
            };
            $scope.editPatientName = function () {
                $location.path('/create/edit-patient-name/index');
            };

            // List of old certificates.

            $scope.widgetState = {
                doneLoading : false,
                activeErrorMessageKey : null,
                currentList : undefined,
                queryFormCollapsed : true
            };

            $scope.updateCertList = function () {
                $scope.widgetState.currentList = $filter('CertDeletedFilter')($scope.widgetState.certListUnhandled, false); // TODO: Use search filter instead of "false"
            };

            $scope.widgetState.activeErrorMessageKey = null;
            $scope.widgetState.doneLoading = true;

            $timeout(function () {
                dashBoardService.getCertificatesForPerson($scope.personnummer, function (data) {
                    $scope.widgetState.doneLoading = false;
                    $scope.widgetState.certListUnhandled = data;
                    $scope.updateCertList();
                }, function (errorData) {
                    $scope.widgetState.doneLoading = false;
                    $log.debug('Query Error' + errorData);
                    $scope.widgetState.activeErrorMessageKey = 'info.certload.error';
                });
            }, 500);

            $scope.openIntyg = function (cert) {
                $window.location.href = '/m/' + cert.intygType + '/webcert/intyg/' + cert.intygId + '/edit#/edit';
                CertificateDraft.reset();
            };

            $scope.copyIntyg = function (cert) {
                //CertificateDraft.reset();
                wcDialogService.showDialog($scope, {
                    dialogId : 'copy-dialog',
                    titleId : 'label.copycert',
                    bodyText : '<p>När du kopierar detta intyg får du upp ett nytt intyg av samma typ och med samma information som finns i det intyg som du kopierar. Du får möjlighet att redigera informationen innan du signerar det nya intyget.</p><div class=\'form-inline\'><input id=\'dontShowAgain\' type=\'checkbox\' ng-model=\'dontShowCopyInfo\'> <label for=\'dontShowAgain\'>Visa inte denna information igen</label></div>',
                    button1click : function () {
                        $log.debug('copy cert');
                    },
                    button1text : 'common.copy',
                    button2text : 'common.cancel'
                });
            };
        }];
});
