define([ 'angular' ], function(angular) {
    'use strict';

    return function() {
        return function(certs, includeDeleted) {
            var result = [];

            if (includeDeleted) {
                return certs;
            }

            angular.forEach(certs, function(cert) {
                if (!cert.discarded) {
                    result.push(cert);
                }
            });
            return result;
        };
    };
});
