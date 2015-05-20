angular.module('webcert').factory('webcert.IntygProxy',
    [ '$http', '$stateParams', '$log',
        function($http, $stateParams, $log) {
            'use strict';

             /*
             * Load certificate list of all certificates for a person
             */
            function _getIntygForPatient(personId, onSuccess, onError) {
                $log.debug('getIntygForPatient type:' + personId);
                var restPath = '/api/intyg/person/' + personId;
                $http.get(restPath).success(function(data) {
                    $log.debug('got data:' + data);
                    onSuccess(data);
                }).error(function(data, status) {
                    $log.error('error ' + status);
                    // Let calling code handle the error of no data response
                    onError(status);
                });
            }

             // Return public API for the service
            return {
                getIntygForPatient: _getIntygForPatient
            };
        }]);
