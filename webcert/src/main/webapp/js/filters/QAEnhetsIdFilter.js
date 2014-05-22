define([
    'angular'
], function(angular) {
    'use strict';

    var moduleName = 'wc.QAEnhetsIdFilter';

    angular.module(moduleName, []).
        filter(moduleName, function() {
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

    return moduleName;
});
