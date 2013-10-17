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
     * Toggle vidarebefordrad state
     */
    function _setVidareBefordradState(id, isVidareBefordrad, callback) {
        $log.debug("_setVidareBefordradState");
        var restPath = '/moduleapi/fragasvar/' + id + "/setDispatchState";
        $http.put(restPath, isVidareBefordrad.toString()).success(function(data) {
            $log.debug("got data:" + data);
            callback(data);
        }).error(function(data, status, headers, config) {
            $log.error("error " + status);
            // Let calling code handle the error of no data response
            callback(null);
        });
    }
    // Return public API for the service
    return {
        getCertificates : _getCertificates,
        getQA : _getQA,
        setVidareBefordradState : _setVidareBefordradState
    }
} ]);
