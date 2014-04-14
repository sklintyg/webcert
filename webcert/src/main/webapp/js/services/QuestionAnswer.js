define([
], function () {
    'use strict';

    return ['$http', '$log', function ($http, $log) {

        /*
         * Load questions and answers data
         */
        function _getQA(query, onSuccess, onError) {
            $log.debug('_getQA');
            var restPath = '/api/fragasvar/query';
            $http.get(restPath, { params: query}).success(function (data) {
                $log.debug('got data:' + data);
                onSuccess(data);
            }).error(function (data, status) {
                $log.error('error ' + status);
                // Let calling code handle the error of no data response
                onError(data);
            });
        }

        /*
         * Load questions and answers data for search
         */
/*        function _getQAByQuery(qp, onSuccess, onError) {
            $log.debug('_getQAByQuery');
            var restPath = '/api/fragasvar/query';
            $http.put(restPath, qp).success(function (data) {
                $log.debug('_getQAByQuery got data:' + data);
                onSuccess(data);
            }).error(function (data, status) {
                $log.error('_getQAByQuery error ' + status);
                // Let calling code handle the error of no data response
                onError(data);
            });
        }
*/
        /*
         * Load more questions and answers data
         */
/*        function _getQAByQueryFetchMore(qp, onSuccess, onError) {
            $log.debug('_getQAByQueryFetchMore');
            var restPath = '/api/fragasvar/query/paging';
            $http.put(restPath, qp).success(function (data) {
                $log.debug('_getQAByQueryFetchMore got data:' + data);
                onSuccess(data);
            }).error(function (data, status) {
                $log.error('_getQAByQueryFetchMore error ' + status);
                // Let calling code handle the error of no data response
                onError(data);
            });
        }
*/
        /*
         * Get list of lakare for enhet
         */
        function _getQALakareList(enhetsId, onSuccess, onError) {
            $log.debug('_getDoctorList: ' + enhetsId);
            var restPath = '/api/fragasvar/lakare';
            $http.get(restPath, {params: { 'enhetsId' : enhetsId}}).success(function (data) {
                $log.debug('_getDoctorList got data:' + data);
                onSuccess(data);
            }).error(function (data, status) {
                $log.error('_getDoctorList error ' + status);
                // Let calling code handle the error of no data response
                onError(data);
            });
        }

        // Return public API for the service
        return {
            getQA: _getQA,
/*            getQAByQuery: _getQAByQuery,
            getQAByQueryFetchMore: _getQAByQueryFetchMore,*/
            getQALakareList: _getQALakareList
        };
    }];
});
