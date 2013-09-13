'use strict';

/* Lakare Dashboard App Module */
angular.module('wcAdminApp', [ 'ui.bootstrap', 'modules.messages', 'wc.common.directives', 'admin.services', 'wc.utils' ]);
angular.module('wcAdminApp').config([ '$routeProvider', '$httpProvider', function($routeProvider, $httpProvider) {
    $routeProvider.when('/index', {
        templateUrl : '/views/adminview/index.html',
        controller : 'WebCertAdminCtrl'
    }).otherwise({
        redirectTo : '/index'
    });

    // Add cache buster interceptor
    $httpProvider.interceptors.push('httpRequestInterceptorCacheBuster');
} ]);

// Inject language resources
angular.module('wcAdminApp').run([ '$rootScope', 'messageService', function($rootScope, messageService) {
    $rootScope.lang = 'sv';
    $rootScope.DEFAULT_LANG = 'sv';
    $rootScope.MODULE_CONFIG = MODULE_CONFIG;
    messageService.addResources(webcertAdminResources);
    messageService.addResources(commonMessageResources);
} ]);
