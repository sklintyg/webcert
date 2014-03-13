define([
    'angular',
    'filters/QAEnhetsIdFilter',
    'filters/CertDeletedFilter'
], function (angular, QAEnhetsIdFilter, CertDeletedFilter) {
    'use strict';

    var moduleName = 'wc.dashboard.filters';

    angular.module(moduleName, [])
        .filter('QAEnhetsIdFilter', QAEnhetsIdFilter)
        .filter('CertDeletedFilter', CertDeletedFilter);

    return moduleName;
});
