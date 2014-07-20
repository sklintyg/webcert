angular.module('webcert').filter('TidigareIntygFilter',
    function() {
        'use strict';

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
