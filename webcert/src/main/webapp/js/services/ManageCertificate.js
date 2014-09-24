angular.module('webcert').factory('webcert.ManageCertificate',
    [ '$http', '$log', '$location', '$window', '$modal', '$cookieStore', 'webcert.CreateCertificateDraft',
        'common.User', 'common.dialogService', 'common.messageService', 'common.CertificateService',
        function($http, $log, $location, $window, $modal, $cookieStore, CreateCertificateDraft, User, dialogService,
            messageService, CertificateService) {
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
                        types.push({sortValue: sortValue++, id: m.id, label: m.label, fragaSvarAvailable: m.fragaSvarAvailable});
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
                var restPath = '/api/intyg/list/' + requestConfig;
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
                var restPath = '/api/intyg/unsigned/'; // release version
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
                var restPath = '/api/intyg/unsigned';
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
                var restPath = '/api/intyg/unsigned/lakare/';
                $http.get(restPath).success(function(data) {
                    $log.debug('_getCertificateSavedByList got data:' + data);
                    onSuccess(data);
                }).error(function(data, status) {
                    $log.error('_getCertificateSavedByList error ' + status);
                    // Let calling code handle the error of no data response
                    onError(data);
                });
            }

            /*
             * Toggle Forwarded state of a fragasvar entity with given id
             */
            function _setForwardedState(id, isForwarded, callback) {
                $log.debug('_setForwardedState');
                var restPath = '/api/intyg/forward/' + id;
                $http.put(restPath, isForwarded.toString()).success(function(data) {
                    $log.debug('_setForwardedState data:' + data);
                    callback(data);
                }).error(function(data, status) {
                    $log.error('error ' + status);
                    // Let calling code handle the error of no data response
                    callback(null);
                });
            }

            function _buildMailToLink(cert) {
                var baseURL = $window.location.protocol + '//' + $window.location.hostname +
                    ($window.location.port ? ':' + $window.location.port : '');
                var url = baseURL + '/web/dashboard#/' + cert.intygType + '/edit/' + cert.intygId;
                var recipient = '';
                var subject = 'Du har blivit tilldelad ett ej signerat intyg i Webcert';
                var body = 'Klicka länken för att gå till intyget:\n' + url;
                var link = 'mailto:' + recipient + '?subject=' + encodeURIComponent(subject) + '&body=' +
                    encodeURIComponent(body);
                $log.debug(link);
                return link;
            }

            function _setSkipForwardedCookie() {
                var secsDays = 12 * 30 * 24 * 3600 * 1000; // 1 year
                var now = new Date();
                var expires = new Date(now.getTime() + secsDays);
                document.cookie = 'WCDontAskForForwardedToggle=1; expires=' + expires.toUTCString();
            }

            function _isSkipForwardedCookieSet() {
                return document.cookie && document.cookie.indexOf('WCDontAskForForwardedToggle=1') !== -1;
            }

            // This handles forwarding of Ej signerade utkast only
            function _handleForwardedToggle(draft, onYesCallback) {
                // Only ask about toggle if not already set AND not skipFlag cookie is
                // set
                if (!draft.forwarded && !_isSkipForwardedCookieSet()) {
                    _showForwardedPreferenceDialog('markforward',
                        'Det verkar som att du har informerat den som ska signera utkastet. Vill du markera utkastet som vidarebefordrad?',
                        function() { // yes
                            $log.debug('yes');
                            draft.forwarded = true;
                            if (onYesCallback) {
                                // let calling scope handle yes answer
                                onYesCallback(draft);
                            }
                        },
                        function() { // no
                            $log.debug('no');
                            // Do nothing
                        },
                        function() {
                            $log.debug('no and dont ask');
                            // How can user reset this?
                            _setSkipForwardedCookie();
                        }
                    );
                }
            }

            function _showForwardedPreferenceDialog(title, bodyText, yesCallback, noCallback, noDontAskCallback,
                callback) {

                var DialogInstanceCtrl = function($scope, $modalInstance, title, bodyText, yesCallback, noCallback,
                    noDontAskCallback) {
                    $scope.title = title;
                    $scope.bodyText = bodyText;
                    $scope.noDontAskVisible = noDontAskCallback !== undefined;
                    $scope.yes = function(result) {
                        yesCallback();
                        $modalInstance.close(result);
                    };
                    $scope.no = function() {
                        noCallback();
                        $modalInstance.close('cancel');
                    };
                    $scope.noDontAsk = function() {
                        noDontAskCallback();
                        $modalInstance.close('cancel_dont_ask_again');
                    };
                };

                var msgbox = $modal.open({
                    templateUrl: '/views/partials/preference-dialog.html',
                    controller: DialogInstanceCtrl,
                    resolve: {
                        title: function() {
                            return angular.copy(title);
                        },
                        bodyText: function() {
                            return angular.copy(bodyText);
                        },
                        yesCallback: function() {
                            return yesCallback;
                        },
                        noCallback: function() {
                            return noCallback;
                        },
                        noDontAskCallback: function() {
                            return noDontAskCallback;
                        }
                    }
                });

                msgbox.result.then(function(result) {
                    if (callback) {
                        callback(result);
                    }
                }, function() {
                });
            }

            function _createCopyDraft($scope, copyDialog, COPY_DIALOG_COOKIE, cert) {
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

            function _copy($scope, cert, copyDialog, COPY_DIALOG_COOKIE) {

                if ($cookieStore.get(COPY_DIALOG_COOKIE)) {
                    $log.debug('copy cert without dialog' + cert);
                    _createCopyDraft($scope, copyDialog, COPY_DIALOG_COOKIE, cert);
                } else {
                    copyDialog = dialogService.showDialog($scope, {
                        dialogId: 'copy-dialog',
                        titleId: 'label.copycert',
                        templateUrl: '/views/partials/check-dialog.html',
                        model: $scope.dialog,
                        bodyText: '<p>Kopiera intyg innebär att en kopia skapas av det befintliga intyget och med samma ' +
                            'information. I de fall patienten har ändrat namn eller adress så uppdateras den informationen.</p>' +
                            '<p>Uppgifterna i intygsutkastet går att ändra innan det signeras.</p>' +
                            'Kopiera intyg kan användas exempelvis vid förlängning av en sjukskrivning.',
                        button1click: function() {
                            $log.debug('copy cert from dialog' + cert);
                            _createCopyDraft($scope, copyDialog, COPY_DIALOG_COOKIE, cert);
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

            function _sendSigneratIntyg(cert, recipientId, patientConsent, dialogModel, sendDialog, onSuccess) {
                dialogModel.showerror = false;
                dialogModel.acceptprogressdone = false;
                CertificateService.sendSigneratIntyg(cert, recipientId, patientConsent, function() {
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
                        _sendSigneratIntyg(cert, recipientId, $scope.dialogSend.patientConsent, $scope.dialogSend,
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
                CertificateService.revokeSigneratIntyg(cert.id, function() {
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
                setForwardedState: _setForwardedState,
                handleForwardedToggle: _handleForwardedToggle,
                buildMailToLink: _buildMailToLink,
                initSend: _initSend,
                send: _send,
                copy: _copy
            };
        }]);
