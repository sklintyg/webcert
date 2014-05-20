define([
    'angular',
    'filters/QAEnhetsIdFilter',
    'webjars/common/js/filters/BoolToTextFilter',
    'filters/CertDeletedFilter'
], function(angular, QAEnhetsIdFilter, BoolToTextFilter, CertDeletedFilter) {
    'use strict';

    var moduleName = 'wc.dashboard.filters';

    angular.module(moduleName, []).
        filter('QAEnhetsIdFilter', QAEnhetsIdFilter).
        filter('BoolToTextFilter', BoolToTextFilter).
        filter('CertDeletedFilter', CertDeletedFilter);

    return moduleName;
});
