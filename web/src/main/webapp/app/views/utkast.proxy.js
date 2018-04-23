/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

angular.module('webcert').factory('webcert.UtkastProxy',
    [ '$q', '$http', '$stateParams', '$log', '$location', '$window', '$timeout',
        'common.User', 'common.dialogService', 'common.authorityService', 'common.featureService',
        'common.messageService', 'common.statService', 'common.UserModel',
        function($q, $http, $stateParams, $log, $location, $window, $timeout, User, dialogService,
            authorityService, featureService, messageService, statService, UserModel) {
            'use strict';

            var cachedIntygTypes = null;

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
                $http.post(restPath, createDraftRequestPayload).then(function(response) {
                    $log.debug('got callback data: ' + response.data);
                    onSuccess(response.data);
                    statService.refreshStat();
                }, function(response) {
                    $log.error('error ' + response.status);
                    onError(response.data);
                });
            }

            /**
             * Load list of all certificates types
             * Remove when INTYG-6009 is done
             */
            function _getUtkastTypesCachedUnfiltered(onSuccess, onError) {
                if (cachedIntygTypes !== null) {
                    $log.debug('returning cached response');
                    onSuccess(cachedIntygTypes);
                } else {
                    var restPath = '/api/modules/map';
                    $http.get(restPath).then(function(response) {
                        $log.debug('got data:', response.data);
                        cachedIntygTypes = response.data;
                        onSuccess(response.data);
                    }, function(response) {
                        $log.error('error ' + response.status);
                        if (onError) {
                            onError();
                        }
                    });
                }
            }

            /**
             * Load list of all certificates types
             */
            function _getUtkastTypes(onSuccess, onError) {
                var restPath = '/api/modules/map';
                $http.get(restPath).then(function(response) {
                    var data = response.data;
                    $log.debug('got data:', data);
                    var sortValue = 0;
                    var types = [
                        { sortValue: sortValue++, id: 'default', label: messageService.getProperty('label.default-intyg-type') }
                    ];
                    for (var i = 0; i < data.length; i++) {
                        var m = data[i];

                        var options = {
                            feature: featureService.features.HANTERA_INTYGSUTKAST,
                            authority: UserModel.privileges.SKRIVA_INTYG,
                            requestOrigin: UserModel.user.origin,
                            intygstyp: m.id};

                        // Only add type if feature is active and user has global intygTyp access through their role.
                        if (authorityService.isAuthorityActive(options)) {
                            types.push({sortValue: sortValue++, id: m.id, label: m.label, detailedDescription: m.detailedDescription, fragaSvarAvailable: m.fragaSvarAvailable});
                        }
                    }
                    onSuccess(types);
                }, function(response) {
                    $log.error('error ' + response.status);
                    if (onError) {
                        onError();
                    }
                });
            }

            /**
             * Load list of all certificates types
             */
            function _getUtkastTypesForPatient(patientId, onSuccess) {
                var restPath = '/api/modules/map/' + patientId;
                $http.get(restPath).then(function(response) {
                    $log.debug('got data:', response.data);
                    var sortValue = 0;
                    var types = [];
                    for (var i = 0; i < response.data.length; i++) {
                        var m = response.data[i];

                        var options = {
                            feature: featureService.features.HANTERA_INTYGSUTKAST,
                            authority: UserModel.privileges.SKRIVA_INTYG,
                            requestOrigin: UserModel.user.origin,
                            intygstyp: m.id};

                        // Only add type if feature is active and user has global intygTyp access through their role.
                        if (authorityService.isAuthorityActive(options)) {
                            types.push({sortValue: sortValue++, id: m.id, label: m.label, detailedDescription: m.detailedDescription, fragaSvarAvailable: m.fragaSvarAvailable});
                        }
                    }
                    onSuccess(types);
                }, function(response) {
                    $log.error('error ' + response.status);
                    // if (onError) {
                    //     onError();
                    // }
                });
            }

            /**
             * Get intyg type data cached
             */
            function _getUtkastType(intygType, onSuccess) {

                _getUtkastTypesCachedUnfiltered(function(types) {

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
                $http.get(restPath).then(function(response) {
                    $log.debug('got data:' + response.data);
                    onSuccess(response.data);
                }, function(response) {
                    $log.error('error ' + response.status);
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
                $http.get(restPath, { params: query }).then(function(response) {
                    $log.debug('_getUtkastFetchMore got data:' + response.data);
                    onSuccess(response.data);
                }, function(response) {
                    $log.error('_getUtkastFetchMore error ' + response.status);
                    // Let calling code handle the error of no data response
                    onError(response.data);
                });
            }

            function _getUtkastSavedByList(onSuccess, onError) {
                $log.debug('_getUtkastSavedByList');
                var restPath = '/api/utkast/lakare/';
                $http.get(restPath).then(function(response) {
                    $log.debug('_getUtkastSavedByList got data:' + response.data);
                    onSuccess(response.data);
                }, function(response) {
                    $log.error('_getUtkastSavedByList error ' + response.status);
                    // Let calling code handle the error of no data response
                    onError(response.data);
                });
            }

            // Return public API for the service
            return {
                createUtkast: _createUtkast,
                getUtkastTypesForPatient: _getUtkastTypesForPatient,
                getUtkastTypes: _getUtkastTypes,
                getUtkastType: _getUtkastType,
                getUtkastList: _getUtkastList,
                getUtkastFetchMore: _getUtkastFetchMore,
                getUtkastSavedByList: _getUtkastSavedByList
            };
        }]);
