define([
    'angular',
    'directives/wcCareUnitClinicSelector',
    'directives/wcAbout',
    'directives/wcVisited',
    'directives/wcPersonNumber'
], function(angular, wcCareUnitClinicSelector, wcAbout, wcVisited, wcPersonNumber) {
    'use strict';

    var moduleName = 'wc.dashboard.directives';

    angular.module(moduleName, []).
        directive('wcCareUnitClinicSelector', wcCareUnitClinicSelector).
        directive('wcAbout', wcAbout).
        directive('wcVisited', wcVisited).
        directive('wcPersonNumber', wcPersonNumber);

    return moduleName;
});
