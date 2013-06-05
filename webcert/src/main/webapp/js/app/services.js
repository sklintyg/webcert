'use strict';

/* Services */

/*
 * This service gets a list of all certificates. It also caches the list to
 * avoid having to fetch the list every time a controller requests it.
 */
angular.module('services.listCertService', []);
angular.module('services.listCertService').factory('listCertService', [ '$http', function($http) {


    // cached certificates response
    var cachedList = null;

    var _selectedCertificate = null;

    function _getCertificates(callback) {
        if (cachedList != null) {
            console.log("returning cached response");
            callback(cachedList);
            return;
        }
        $http.get('/api/certificates').success(function(data) {
            console.log("populating cache");
            cachedList = data;
            callback(cachedList);
        }).error(function(data, status, headers, config) {
            console.log("error " + status);
        });
    }

    function _archiveCertificate(item, callback) {
        console.log("Archiving " + item.id);
        $http.put('/api/certificates/' + item.id + "/archive").success(function(data) {
            callback(data, item);
        }).error(function(data, status, headers, config) {
            console.log("error " + status);
        });

    }

    function _restoreCertificate(item, callback) {
        console.log("restoring " + item.id);
        $http.put('/api/certificates/' + item.id + "/restore").success(function(data) {
            callback(data, item);
        }).error(function(data, status, headers, config) {
            console.log("error " + status);
        });

    }
    
    function _sendCertificate(item, callback) {
        console.log("service: sending " + item.id);
        $http.put('/api/certificates/' + item.id + "/send").success(function(data) {
            callback(data, item);
        }).error(function(data, status, headers, config) {
            console.log("error " + status);
        });

    }

    // Return public API for our service
    return {
        getCertificates : _getCertificates,
        archiveCertificate : _archiveCertificate,
        restoreCertificate : _restoreCertificate,
        sendCertificate : _sendCertificate,
        selectedCertificate : _selectedCertificate
    }
} ]);
