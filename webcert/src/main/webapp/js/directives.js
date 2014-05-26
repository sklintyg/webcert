define([
    'angular',
    'directives/wcAbout',
    'directives/wcCareUnitClinicSelector',
    'directives/wcInsertCertificate',
    'directives/wcInsertQa',
    'directives/wcPersonNumber',
    'directives/wcVisited'
], function(angular, wcAbout, wcCareUnitClinicSelector, wcInsertCertificate, wcInsertQa, wcPersonNumber, wcVisited) {
    'use strict';

    var moduleName = 'wc.directives';

    angular.module(moduleName, [ wcAbout, wcCareUnitClinicSelector, wcInsertCertificate, wcInsertQa,
        wcPersonNumber, wcVisited ]);

    return moduleName;
});
