define([ 'angular' ], function(angular) {
    'use strict';

    return ['$http', '$log', '$window', '$modal', function($http, $log, $window, $modal) {

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
                    types.push({sortValue: sortValue++, id: m.id, label: m.label});
                }
                onSuccess(types);
            }).error(function(data, status) {
                $log.error('error ' + status);
                onError();
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

        function _showForwardedPreferenceDialog(title, bodyText, yesCallback, noCallback, noDontAskCallback, callback) {

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

        // Return public API for the service
        return {
            getCertTypes: _getCertTypes,
            getCertificatesForPerson: _getCertificatesForPerson,
            getUnsignedCertificates: _getUnsignedCertificates,
            getUnsignedCertificatesByQueryFetchMore: _getUnsignedCertificatesByQueryFetchMore,
            getCertificateSavedByList: _getCertificateSavedByList,
            setForwardedState: _setForwardedState,
            handleForwardedToggle: _handleForwardedToggle,
            buildMailToLink: _buildMailToLink
        };
    }];
});
