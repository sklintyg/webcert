define([
    'angular',
    'directives/wcCareUnitClinicSelector',
    'directives/wcAbout',
    'directives/wcVisited',
    'directives/wcInsertCertificate',
    'directives/wcInsertQa',
    'directives/wcPersonNumber'
], function(angular, wcCareUnitClinicSelector, wcAbout, wcVisited, wcInsertCertificate, wcInsertQa, wcPersonNumber) {
    'use strict';

    var moduleName = 'wc.dashboard.directives';

    angular.module(moduleName, []).
        directive('wcCareUnitClinicSelector', wcCareUnitClinicSelector).
        directive('wcAbout', wcAbout).
        directive('wcVisited', wcVisited).
        directive('wcInsertCertificate', wcInsertCertificate).
        directive('wcInsertQa', wcInsertQa).
        directive('wcPersonNumber', wcPersonNumber);

    return moduleName;
});
