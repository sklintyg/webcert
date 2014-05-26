define([
    'angular',
    'filters/TidigareIntygFilter',
    'filters/QAEnhetsIdFilter',
    'webjars/common/webcert/js/filters/BoolToTextFilter'
], function(angular, TidigareIntygFilter, QAEnhetsIdFilter, BoolToTextFilter) {
    'use strict';

    var moduleName = 'wc.filters';

    angular.module(moduleName, [ TidigareIntygFilter, QAEnhetsIdFilter, BoolToTextFilter ]);

    return moduleName;
});
