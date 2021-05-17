/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

angular.module('webcert').factory('webcert.SubscriptionProxy', ['$http', '$log',
    function($http, $log) {
        'use strict';

        function _updateAcknowledgedWarnings(hsaId, onSuccess, onError) {
            var restPath = '/api/subscription/acknowledge/' + hsaId;
            $http.get(restPath).then(function(response) {
                $log.debug(restPath + ' response:' + response.data);
                onSuccess(response.data);
            }, function(response) {
                $log.error('error ' + response.status);
                onError(null);
            });
        }

        return {
            updateAcknowledgedWarnings: _updateAcknowledgedWarnings
        };
    }]);