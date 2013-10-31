'use strict';

/*
 * Dashboard Services
 */
angular.module('dashboard.services', []);
angular.module('dashboard.services').factory('dashBoardService', [ '$http', '$log', function($http, $log) {

    /*
     * Load certificate list of specified type(unsigned, with unanswered
     * questions and ready to mass sign) TODO: Add careUnit and optionally
     * careWard +
     */

    function _getCertificates(requestConfig, callback) {
        $log.debug("_getCertificates type:" + requestConfig.type);
        // var restPath = '/api/certificates/' + dataType;
        var restPath = '/jsonmocks/' + requestConfig.type;
        $http.get(restPath).success(function(data) {
            $log.debug("got data:" + data);
            callback(data);
        }).error(function(data, status, headers, config) {
            $log.error("error " + status);
            // Let calling code handle the error of no data response
            callback(null);
        });
    }

    /*
     * Load questions and answers data for
     */
    function _getQA(callback) {
        $log.debug("_getQA");
        var restPath = '/api/fragasvar';
        $http.get(restPath).success(function(data) {
            $log.debug("got data:" + data);
            callback(data);
        }).error(function(data, status, headers, config) {
            $log.error("error " + status);
            // Let calling code handle the error of no data response
            callback(null);
        });

    }
    
    /*
     * Load questions and answers data for
     */
    function _getQAByQuery(qp, onSuccess, onError) {
        $log.debug("_getQAByQuery");
        var restPath = '/api/fragasvar/query';
        $http.put(restPath, qp).success(function(data) {
            $log.debug("_getQAByQuery got data:" + data);
            onSuccess(data);
        }).error(function(data, status, headers, config) {
            $log.error("_getQAByQuery error " + status);
            // Let calling code handle the error of no data response
            onError(data);
        });

    }

    /*
     * Load questions and answers data for
     */
    function _getQAByQueryFetchMore(qp, onSuccess, onError) {
        $log.debug("_getQAByQueryFetchMore");
        var restPath = '/api/fragasvar/query/paging';
        $http.put(restPath, qp).success(function(data) {
            $log.debug("_getQAByQueryFetchMore got data:" + data);
            onSuccess(data);
        }).error(function(data, status, headers, config) {
            $log.error("_getQAByQueryFetchMore error " + status);
            // Let calling code handle the error of no data response
            onError(data);
        });

    }
    
    function _getDoctorList(enhetsId, onSuccess, onError) {
        $log.debug("_getDoctorList: "+ enhetsId);
        var restPath = '/api/fragasvar/mdlist/' + enhetsId;
        $http.get(restPath).success(function(data) {
            $log.debug("_getDoctorList got data:" + data);
            onSuccess(data);
        }).error(function(data, status, headers, config) {
            $log.error("_getDoctorList error " + status);
            // Let calling code handle the error of no data response
            onError(data);
        });

    }
    
    
    // Return public API for the service
    return {
        getCertificates : _getCertificates,
        getQA : _getQA,
        getQAByQuery : _getQAByQuery,
        getQAByQueryFetchMore : _getQAByQueryFetchMore,
        getDoctorList : _getDoctorList
    }
} ]);


