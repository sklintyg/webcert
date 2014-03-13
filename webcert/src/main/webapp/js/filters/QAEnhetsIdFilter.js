define([
    'angular'
], function (angular) {
    'use strict';

    return function () {
        return function (enheter, enhetsId) {
            var result = [];
            angular.forEach(enheter, function (enhet) {
                if (enhet.vardperson.enhetsId === enhetsId) {
                    result.push(enhet);
                }
            });
            return result;
        };
    };
});
