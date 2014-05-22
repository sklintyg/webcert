define([
    'angular'
], function(angular) {
    'use strict';

    var moduleName = 'wc.QuestionAnswer';

    angular.module(moduleName, []).
        factory(moduleName, [ '$http', '$log',
            function($http, $log) {

                /*
                 * Load questions and answers data
                 */
                function _getQA(query, onSuccess, onError) {
                    $log.debug('_getQA');
                    var restPath = '/api/fragasvar/query';
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
            }
        ]);

    return moduleName;
});
