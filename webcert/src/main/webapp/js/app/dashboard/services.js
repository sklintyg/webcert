'use strict';

/* 
 * Dashboard Services 
 */
angular.module('dashboard.services', []);
angular.module('dashboard.services').factory('dashBoardService', [ '$http', '$log', function($http, $log) {

    
    /* 
     * Load certificate list of specified type(unsigned, with unanswered questions and ready to mass sign)
     * TODO: Add careUnit and optionally careWard  +  
     */

    function _getCertificates(requestConfig, callback) {
        //var restPath = '/api/certificates/' + dataType;
        var restPath = '/jsonmocks/' + requestConfig.type;
        $http.get(restPath).success(function(data) {
            $log.debug("got data:" + data);
            callback(data);
        }).error(function(data, status, headers, config) {
            $log.error("error " + status);
            //Let calling code handle the error of no data response 
            callback(null);
        });
    }
    
    
    // Return public API for the service
    return {
        getCertificates : _getCertificates
    }
} ]);
