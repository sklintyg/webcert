angular.module('webcert').factory('webcert.UtkastProxy',
    [ '$q', '$http', '$stateParams', '$log', '$location', '$window', '$timeout',
        'common.User', 'common.dialogService', 'common.authorityService', 'common.featureService', 'common.messageService', 'common.statService',
        'common.UserModel',
        function($q, $http, $stateParams, $log, $location, $window, $timeout, User, dialogService,
            authorityService, featureService, messageService, statService, UserModel) {
            'use strict';

            /**
             * createUtkast
             * @param createDraftRequestPayload
             * @param onSuccess
             * @param onError
             * @private
             */
            function _createUtkast(createDraftRequestPayload, onSuccess, onError) {
                $log.debug('_createDraft');
                var restPath = '/api/utkast/' + createDraftRequestPayload.intygType;
                $http.post(restPath, createDraftRequestPayload).success(function(data) {
                    $log.debug('got callback data: ' + data);
                    onSuccess(data);
                    statService.refreshStat();
                }).error(function(data, status) {
                    $log.error('error ' + status);
                    onError(data);
                });
            }

            /**
             * Load list of all certificates types
             */
            function _getUtkastTypes(onSuccess, onError) {
                var restPath = '/api/modules/map';
                $http.get(restPath).success(function(data) {
                    $log.debug('got data:', data);
                    var sortValue = 0;
                    var types = [
                        { sortValue: sortValue++, id: 'default', label: messageService.getProperty('label.default-cert-type') }
                    ];
                    for (var i = 0; i < data.length; i++) {
                        var m = data[i];

                        var options = {
                            feature: featureService.features.HANTERA_INTYGSUTKAST,
                            authority: UserModel.privileges.SKRIVA_INTYG,
                            requestOrigin: UserModel.user.origin,
                            intygstyp: m.id};

                        // Only add type if feature is active and user has global intygTyp access through their role.
                        if (authorityService.isAuthorityActive(options)
                        ) {
                            types.push({sortValue: sortValue++, id: m.id, label: m.label, fragaSvarAvailable: m.fragaSvarAvailable});
                        }
/*
                        if (featureService.isFeatureActive(featureService.features.HANTERA_INTYGSUTKAST, m.id)
                            && UserModel.isAuthorizedForIntygstyp(UserModel.privileges.SKRIVA_INTYG, m.id)
                            && UserModel.isAuthorizedForOrigin(m.id)) {
                            types.push({sortValue: sortValue++, id: m.id, label: m.label, fragaSvarAvailable: m.fragaSvarAvailable});
                        }
*/
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
            function _getUtkastType(intygType, onSuccess) {

                _getUtkastTypes(function(types) {

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
             * Load unsigned certificate list for valdVardenhet
             */
            function _getUtkastList(onSuccess, onError) {
                $log.debug('_getUtkastList:');
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
            function _getUtkastFetchMore(query, onSuccess, onError) {
                $log.debug('_getUtkastFetchMore');
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

            function _getUtkastSavedByList(onSuccess, onError) {
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

            // Return public API for the service
            return {
                createUtkast: _createUtkast,
                getUtkastTypes: _getUtkastTypes,
                getUtkastType: _getUtkastType,
                getUtkastList: _getUtkastList,
                getUtkastFetchMore: _getUtkastFetchMore,
                getUtkastSavedByList: _getUtkastSavedByList
            };
        }]);
