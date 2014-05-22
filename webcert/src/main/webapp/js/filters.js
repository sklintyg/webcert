define([
    'angular',
    'filters/CertDeletedFilter',
    'filters/QAEnhetsIdFilter',
    'webjars/common/webcert/js/filters/BoolToTextFilter'
], function(angular, CertDeletedFilter, QAEnhetsIdFilter, BoolToTextFilter) {
    'use strict';

    var moduleName = 'wc.filters';

    angular.module(moduleName, [ CertDeletedFilter, QAEnhetsIdFilter, BoolToTextFilter ]);

    return moduleName;
});
