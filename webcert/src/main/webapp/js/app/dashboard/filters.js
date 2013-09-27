'use strict';

/* Filters */
angular.module('wcDashBoardApp').filter('QAEnhetsIdFilter', function() {
    return function(enheter, enhetsId) {
        var result = [];
        angular.forEach(enheter, function (enhet) {
            if (enhet.vardperson.enhetsId === enhetsId) {
                result.push(enhet);
            }
        });                
        return result;
    }
});