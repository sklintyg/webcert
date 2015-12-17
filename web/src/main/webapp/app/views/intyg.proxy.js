/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

angular.module('webcert').factory('webcert.IntygProxy',
    [ '$http', '$stateParams', '$log', 'common.statService',
        function($http, $stateParams, $log, statService) {
            'use strict';

             /*
             * Load certificate list of all certificates for a person
             */
            function _getIntygForPatient(personId, onSuccess, onError) {
                $log.debug('getIntygForPatient type:' + personId);
                var restPath = '/api/intyg/person/' + personId;
                $http.get(restPath).success(function(data, statusCode, headers) {
                    $log.debug('got data:' + data);
                    if (typeof headers('offline_mode') !== 'undefined' && headers('offline_mode') === 'true') {
                        onError(statusCode, 'info.certload.offline');
                    }
                    onSuccess(data);
                }).error(function(data, status) {
                    $log.error('error ' + status);
                    // Let calling code handle the error of no data response
                    onError(status);
                });
            }

             // Return public API for the service
            return {
                getIntygForPatient: _getIntygForPatient
            };
        }]);
