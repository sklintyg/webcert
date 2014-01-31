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

angular.module('wcDashBoardApp').filter('CertDeletedFilter', function() {
  return function(certs, includeDeleted) {
    var result = [];

    if(includeDeleted) return certs;

    angular.forEach(certs, function (cert) {
      if (!cert.discarded) {
        result.push(cert);
      }
    });
    return result;
  }
});