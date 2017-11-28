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

angular.module('webcert').factory('webcert.QuestionAnswer',
    function($http, $log) {
        'use strict';

        /*
         * Load questions and answers data
         */
        function _getQA(query, onSuccess, onError) {
            $log.debug('_getQA');
            var restPath = '/api/fragasvar/sok';
            $http.get(restPath, { params: query}).then(function(response) {
                $log.debug('got data:' + response.data);
                onSuccess(response.data);
            }, function(response) {
                $log.error('error ' + response.status);
                // Let calling code handle the error of no data response
                onError(response.data);
            });
        }

        /*
         * Get list of lakare for enhet
         */
        function _getQALakareList(enhetsId, onSuccess, onError) {
            $log.debug('_getQALakareList: ' + enhetsId);
            var restPath = '/api/fragasvar/lakare';
            $http.get(restPath, {params: { 'enhetsId': enhetsId}}).then(function(response) {
                $log.debug('_getQALakareList got data:' + response.data);
                onSuccess(response.data);
            }, function(response) {
                $log.error('_getQALakareList error ' + response.status);
                // Let calling code handle the error of no data response
                onError(response.data);
            });
        }

        // Return public API for the service
        return {
            getQA: _getQA,
            getQALakareList: _getQALakareList
        };
    });
