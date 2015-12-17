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

angular.module('webcert').factory('webcert.QuestionAnswer',
    function($http, $log) {
        'use strict';

        /*
         * Load questions and answers data
         */
        function _getQA(query, onSuccess, onError) {
            $log.debug('_getQA');
            var restPath = '/api/fragasvar/sok';
            $http.get(restPath, { params: query}).success(function(data) {
                $log.debug('got data:' + data);
                onSuccess(data);
            }).error(function(data, status) {
                $log.error('error ' + status);
                // Let calling code handle the error of no data response
                onError(data);
            });
        }

        /*
         * Get list of lakare for enhet
         */
        function _getQALakareList(enhetsId, onSuccess, onError) {
            $log.debug('_getQALakareList: ' + enhetsId);
            var restPath = '/api/fragasvar/lakare';
            $http.get(restPath, {params: { 'enhetsId': enhetsId}}).success(function(data) {
                $log.debug('_getQALakareList got data:' + data);
                onSuccess(data);
            }).error(function(data, status) {
                $log.error('_getQALakareList error ' + status);
                // Let calling code handle the error of no data response
                onError(data);
            });
        }

        // Return public API for the service
        return {
            getQA: _getQA,
            getQALakareList: _getQALakareList
        };
    });
