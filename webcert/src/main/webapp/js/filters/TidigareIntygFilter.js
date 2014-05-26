define([
    'angular'
], function(angular) {
    'use strict';

    var moduleName = 'wc.TidigareIntygFilter';

    angular.module(moduleName, []).
        filter(moduleName, function() {
            return function(certs, intygToInclude) {
                var result = [];

                if (intygToInclude === 'all') {
                    return certs;
                }

                angular.forEach(certs, function(cert) {
                    if ((intygToInclude === 'current' && cert.status !== 'CANCELLED') ||
                        (intygToInclude === 'revoked' && cert.status === 'CANCELLED')) {
                        result.push(cert);
                    }
                });
                return result;
            };
        });

    return moduleName;
});
