angular.module('webcert').factory('webcert.ManageCertificate',
    [ '$http', '$routeParams', '$log', '$location', '$window', '$modal', '$cookieStore', 'webcert.CreateCertificateDraft',
        'common.User', 'common.dialogService', 'common.featureService', 'common.messageService', 'common.CertificateService',
        function($http, $routeParams, $log, $location, $window, $modal, $cookieStore, CreateCertificateDraft, User, dialogService,
            featureService, messageService, CertificateService) {
            'use strict';

            /**
             * Load list of all certificates types
             */
            function _getCertTypes(onSuccess, onError) {
                var restPath = '/api/modules/map';
                $http.get(restPath).success(function(data) {
                    $log.debug('got data:', data);
                    var sortValue = 0;
                    var types = [
                        { sortValue: sortValue++, id: 'default', label: messageService.getProperty('label.default-cert-type') }
                    ];
                    for (var i = 0; i < data.length; i++) {
                        var m = data[i];
                        if (featureService.isFeatureActive(featureService.features.HANTERA_INTYGSUTKAST, m.id)) {
                            types.push({sortValue: sortValue++, id: m.id, label: m.label, fragaSvarAvailable: m.fragaSvarAvailable});
                        }
                    }
                    onSuccess(types);
                }).error(function(data, status) {
                    $log.error('error ' + status);
                    if (onError) {
                        onError();
                    }
                });
            }

            /**
             * Get intyg type data
             */
            function _getCertType(intygType, onSuccess) {

                _getCertTypes(function(types) {

                    var intygTypeMeta = {};
                    for (var i = 0; i < types.length; i++) {
                        if (types[i].id === intygType) {
                            intygTypeMeta = types[i];
                            break;
                        }
                    }

                    onSuccess(intygTypeMeta);
                });
            }

            /*
             * Load certificate list of all certificates for a person
             */
            function _getCertificatesForPerson(requestConfig, onSuccess, onError) {
                $log.debug('_getCertificatesForPerson type:' + requestConfig);
                var restPath = '/api/intyg/person/' + requestConfig;
                $http.get(restPath).success(function(data) {
                    $log.debug('got data:' + data);
                    onSuccess(data);
                }).error(function(data, status) {
                    $log.error('error ' + status);
                    // Let calling code handle the error of no data response
                    onError(status);
                });
            }

            /*
             * Load unsigned certificate list for valdVardenhet
             */
            function _getUnsignedCertificates(onSuccess, onError) {
                $log.debug('_getUnsignedCertificates:');
                var restPath = '/api/utkast/'; // release version
                $http.get(restPath).success(function(data) {
                    $log.debug('got data:' + data);
                    onSuccess(data);
                }).error(function(data, status) {
                    $log.error('error ' + status);
                    // Let calling code handle the error of no data response
                    onError(null);
                });
            }

            /*
             * Load more unsigned certificates by query
             */
            function _getUnsignedCertificatesByQueryFetchMore(query, onSuccess, onError) {
                $log.debug('_getUnsignedCertificatesByQueryFetchMore');
                var restPath = '/api/utkast';
                $http.get(restPath, { params: query }).success(function(data) {
                    $log.debug('_getUnsignedCertificatesByQueryFetchMore got data:' + data);
                    onSuccess(data);
                }).error(function(data, status) {
                    $log.error('_getUnsignedCertificatesByQueryFetchMore error ' + status);
                    // Let calling code handle the error of no data response
                    onError(data);
                });
            }

            function _getCertificateSavedByList(onSuccess, onError) {
                $log.debug('_getCertificateSavedByList');
                var restPath = '/api/utkast/lakare/';
                $http.get(restPath).success(function(data) {
                    $log.debug('_getCertificateSavedByList got data:' + data);
                    onSuccess(data);
                }).error(function(data, status) {
                    $log.error('_getCertificateSavedByList error ' + status);
                    // Let calling code handle the error of no data response
                    onError(data);
                });
            }

            var COPY_DIALOG_COOKIE = 'wc.dontShowCopyDialog';
            var copyDialog = {
                isOpen: false
            };

            function _initCopyDialog($scope) {
                copyDialog.isOpen = false;

                if($scope) {
                    $scope.dialog = {
                        errormessageid: 'error.failedtocopyintyg'
                    };
                }
            }

            function _createCopyDraft($scope, dialogModel, cert, onSuccess, onError) {
                CreateCertificateDraft.nyttPatientPersonnummer = $routeParams.patientId;
                CreateCertificateDraft.copyIntygToDraft(cert, function(data) {
                    $log.debug('Successfully requested copy draft');
                    if(onSuccess) {
                        onSuccess(data);
                    }
                }, function(error) {
                    $log.debug('Create copy failed: ' + error.message);
                    if (onError) {
                        onError();
                    }
                });
            }

            function _copy($scope, cert) {

                function goToDraft(type, intygId) {
                    $location.url('/' + type + '/edit/' + intygId, true);
                }

                // Create cookie and model representative
                var dialogModel = {
                    dontShowCopyInfo: false
                };
                if($cookieStore.get(COPY_DIALOG_COOKIE) === undefined) {
                    $cookieStore.put(COPY_DIALOG_COOKIE, dialogModel.dontShowCopyInfo);
                }

                if ($cookieStore.get(COPY_DIALOG_COOKIE)) {
                    $log.debug('copy cert without dialog' + cert);
                    $scope.widgetState.activeErrorMessageKey = null;
                    _createCopyDraft($scope, dialogModel, cert, function(draftResponse) {
                        goToDraft(draftResponse.intygsTyp, draftResponse.intygsUtkastId);
                    }, function() {
                        $scope.widgetState.activeErrorMessageKey = 'error.failedtocopyintyg';
                    });
                } else {

                    copyDialog = dialogService.showDialog($scope, {
                        dialogId: 'copy-dialog',
                        titleId: 'label.copycert',
                        templateUrl: '/views/partials/check-dialog.html',
                        model: dialogModel,
                        bodyText: '<p>Kopiera intyg innebär att en kopia skapas av det befintliga intyget och med samma information. '+
                            ($routeParams.patientId ?
                                'För denna patient finns nytt person-id och informationen kommer därför uppdateras i det nya intyget.'
                            :
                                'I de fall patienten har ändrat namn eller adress så uppdateras den informationen.') +
                            '</p><p>Uppgifterna i intygsutkastet går att ändra innan det signeras.</p>' +
                            'Kopiera intyg kan användas exempelvis vid förlängning av en sjukskrivning.',
                        button1click: function() {
                            $log.debug('copy cert from dialog' + cert);
                            if (dialogModel.dontShowCopyInfo) {
                                $cookieStore.put(COPY_DIALOG_COOKIE, dialogModel.dontShowCopyInfo);
                            }

                            $scope.dialog.showerror = false;
                            $scope.dialog.acceptprogressdone = false;
                            _createCopyDraft($scope, dialogModel, cert, function(draftResponse) {
                                $scope.dialog.acceptprogressdone = true;
                                $scope.widgetState.createErrorMessageKey = undefined;
                                copyDialog.close();
                                goToDraft(draftResponse.intygsTyp, draftResponse.intygsUtkastId);
                            }, function() {
                                $scope.dialog.acceptprogressdone = true;
                                $scope.dialog.showerror = true;
                            });
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

                return copyDialog;
            }


            // Send dialog setup
            var sendDialog = {
                isOpen: false
            };

            function _initSend($scope) {
                $scope.dialogSend = {
                    acceptprogressdone: true,
                    focus: false,
                    errormessageid: 'error.failedtosendintyg',
                    showerror: false,
                    patientConsent: false
                };
            }

            function _sendSigneratIntyg(intygsId, intygsTyp, recipientId, patientConsent, dialogModel, sendDialog, onSuccess) {
                dialogModel.showerror = false;
                dialogModel.acceptprogressdone = false;
                CertificateService.sendSigneratIntyg(intygsId, intygsTyp, recipientId, patientConsent, function() {
                    dialogModel.acceptprogressdone = true;
                    sendDialog.close();
                    onSuccess();
                }, function(error) {
                    $log.debug('Send cert failed: ' + error);
                    dialogModel.acceptprogressdone = true;
                    dialogModel.showerror = true;
                });
            }

            function _send($scope, cert, recipientId, titleId, onSuccess) {
                sendDialog = dialogService.showDialog($scope, {
                    dialogId: 'send-dialog',
                    titleId: titleId,
                    templateUrl: '/views/partials/send-dialog.html',
                    model: $scope.dialogSend,
                    button1click: function() {
                        $log.debug('send cert from dialog' + cert);
                        _sendSigneratIntyg(cert.id, cert.intygType, recipientId, $scope.dialogSend.patientConsent, $scope.dialogSend,
                            sendDialog, onSuccess);
                    },
                    button1text: 'common.send',
                    button1id: 'button1send-dialog',
                    button2text: 'common.cancel',
                    autoClose: false
                });

                sendDialog.opened.then(function() {
                    sendDialog.isOpen = true;
                }, function() {
                    sendDialog.isOpen = false;
                });

                return sendDialog;
            }

            // Makulera dialog setup
            var makuleraDialog = {
                isOpen: false
            };

            function _initMakulera($scope) {
                $scope.dialogMakulera = {
                    acceptprogressdone: true,
                    focus: false,
                    errormessageid: 'error.failedtomakuleraintyg',
                    showerror: false
                };
            }

            function _revokeSigneratIntyg(cert, dialogModel, makuleraDialog, onSuccess) {
                dialogModel.showerror = false;
                dialogModel.acceptprogressdone = false;
                CertificateService.revokeSigneratIntyg(cert.id, cert.intygType, function() {
                    dialogModel.acceptprogressdone = true;
                    makuleraDialog.close();
                    onSuccess();
                }, function(error) {
                    $log.debug('Revoke failed: ' + error);
                    dialogModel.acceptprogressdone = true;
                    dialogModel.showerror = true;
                });
            }

            function _makulera($scope, cert, confirmationMessage, onSuccess) {

                var successCallback = function() {
                    dialogService.showMessageDialog('label.makulera.confirmation', confirmationMessage,
                        function() {
                            onSuccess();
                        });
                };

                makuleraDialog = dialogService.showDialog($scope, {
                    dialogId: 'makulera-dialog',
                    titleId: 'label.makulera',
                    templateUrl: '/views/partials/makulera-dialog.html',
                    model: $scope.dialogMakulera,
                    button1click: function() {
                        $log.debug('revoking cert from dialog' + cert);
                        _revokeSigneratIntyg(cert, $scope.dialogMakulera, makuleraDialog, successCallback);
                    },

                    button1text: 'common.revoke',
                    button1id: 'button1makulera-dialog',
                    button2text: 'common.cancel',
                    bodyTextId: 'label.makulera.body',
                    autoClose: false
                });

                return makuleraDialog;
            }


            // Return public API for the service
            return {
                getCertTypes: _getCertTypes,
                getCertType: _getCertType,
                getCertificatesForPerson: _getCertificatesForPerson,
                getUnsignedCertificates: _getUnsignedCertificates,
                getUnsignedCertificatesByQueryFetchMore: _getUnsignedCertificatesByQueryFetchMore,
                getCertificateSavedByList: _getCertificateSavedByList,
                initMakulera: _initMakulera,
                makulera: _makulera,
                initSend: _initSend,
                send: _send,
                initCopyDialog: _initCopyDialog,
                copy: _copy
            };
        }]);
