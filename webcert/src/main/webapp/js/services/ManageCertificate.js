define([
    'angular',
    'webjars/common/webcert/js/services/dialogService',
    'webjars/common/webcert/js/services/User',
    'services/CreateCertificateDraft'
], function(angular, dialogService, User, CreateCertificateDraft) {
    'use strict';

    var moduleName = 'wc.ManageCertificate';

    angular.module(moduleName, [User, CreateCertificateDraft, dialogService]).
        factory(moduleName, [ '$http', '$log', '$location', '$window', '$modal', '$cookieStore', CreateCertificateDraft, User, dialogService,
            function($http, $log, $location, $window, $modal, $cookieStore, CreateCertificateDraft, User, dialogService) {

                /**
                 * Load list of all certificates types
                 */
                function _getCertTypes(onSuccess, onError) {
                    var restPath = '/api/modules/map';
                    $http.get(restPath).success(function(data) {
                        $log.debug('got data:', data);
                        var sortValue = 0;
                        var types = [
                            { sortValue: sortValue++, id: 'default', label: 'Välj intygstyp' }
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
                            if (types[i].id === intygType){
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
                    //var restPath = '/jsonmocks/intyg_unsigned.json'; // mocked version
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

                function _handleForwardedToggle(qa, onYesCallback) {
                    // Only ask about toggle if not already set AND not skipFlag cookie is
                    // set
                    if (!qa.forwarded && !_isSkipForwardedCookieSet()) {
                        _showForwardedPreferenceDialog('markforward',
                            'Det verkar som att du har informerat den som ska hantera ärendet. Vill du markera ärendet som vidarebefordrat?',
                            function() { // yes
                                $log.debug('yes');
                                qa.forwarded = true;
                                if (onYesCallback) {
                                    // let calling scope handle yes answer
                                    onYesCallback(qa);
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
                            bodyText: 'När du kopierar detta intyg får du upp ett nytt intyg av samma typ och med ' +
                                'samma information som finns i det intyg som du kopierar. Du får möjlighet att redigera ' +
                                'informationen innan du signerar det nya intyget.',
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

                // Return public API for the service
                return {
                    getCertTypes: _getCertTypes,
                    getCertType: _getCertType,
                    getCertificatesForPerson: _getCertificatesForPerson,
                    getUnsignedCertificates: _getUnsignedCertificates,
                    getUnsignedCertificatesByQueryFetchMore: _getUnsignedCertificatesByQueryFetchMore,
                    getCertificateSavedByList: _getCertificateSavedByList,
                    setForwardedState: _setForwardedState,
                    handleForwardedToggle: _handleForwardedToggle,
                    buildMailToLink: _buildMailToLink,
                    copy: _copy
                };
            }
        ]);

    return moduleName;
});
