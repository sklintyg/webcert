'use strict';

/* Lakare Dashboard App Module */
angular.module('wcDashBoardApp', [ 'ui.bootstrap', 'modules.messages', 'wc.common.directives', 'dashboard.services', 'wc.utils' ]);
angular.module('wcDashBoardApp').config([ '$routeProvider', '$httpProvider', function($routeProvider, $httpProvider) {
    $routeProvider.when('/index', {
        templateUrl : '/views/dashboard/index.html',
        controller : 'WebCertCtrl'
    }).when('/create', {
    	templateUrl : '/views/dashboard/create-cert.html',
    	controller : 'CreateCertCtrl',
    }).when('/view', {
    	templateUrl : '/views/dashboard/view.html',
    	controller : 'ViewCertCtrl',
    }).otherwise({
        redirectTo : '/index'
    });

    // Add cache buster interceptor
    $httpProvider.interceptors.push('httpRequestInterceptorCacheBuster');
} ]);

// Inject language resources
angular.module('wcDashBoardApp').run([ '$rootScope', 'messageService', function($rootScope, messageService) {
    $rootScope.lang = 'sv';
    $rootScope.DEFAULT_LANG = 'sv';
    $rootScope.MODULE_CONFIG = MODULE_CONFIG;
    messageService.addResources(webcertResources);
    messageService.addResources(commonMessageResources);
} ]);
