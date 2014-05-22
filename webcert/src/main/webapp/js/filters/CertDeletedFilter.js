define([
    'angular'
], function(angular) {
    'use strict';

    var moduleName = 'wc.CertDeletedFilter';

    angular.module(moduleName, []).
        filter(moduleName, function() {
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
        });

    return moduleName;
});
