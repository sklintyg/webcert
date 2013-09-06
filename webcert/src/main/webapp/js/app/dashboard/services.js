'use strict';

/* Dashboard Services */
/*
 */
angular.module('dashboard.service', []);
angular.module('dashboard.service').factory('dashBoardService', [ '$http', function($http) {

    function _getCertificates(callback) {
        callback({
            "data" : "123"
        });
    }

    // Return public API for the service
    return {
        getCertificates : _getCertificates
    }
} ]);
