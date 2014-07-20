angular.module('webcert').filter('QAEnhetsIdFilter',
    function() {
        'use strict';

        return function(enheter, enhetsId) {
            var result = [];

            angular.forEach(enheter, function(enhet) {
                if (enhet.vardperson.enhetsId === enhetsId) {
                    result.push(enhet);
                }
            });
            return result;
        };
    });
