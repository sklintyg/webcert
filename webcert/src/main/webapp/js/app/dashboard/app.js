'use strict';

/* Lakare Dashboard App Module */
angular.module('wcDashBoardApp', [ 'ui.bootstrap', 'modules.messages', 'wc.common.directives', 'dashboard.services', 'wc.utils', 'wc.common.fragasvarmodule' ]);
angular.module('wcDashBoardApp').config([ '$routeProvider', '$httpProvider', function($routeProvider, $httpProvider) {
    $routeProvider.when('/index', {
        templateUrl : '/views/dashboard/index.html',
        controller : 'WebCertCtrl'
    }).when('/create', {
    	templateUrl : '/views/dashboard/create-cert.html',
    	controller : 'CreateCertCtrl'
    }).when('/unhandled-qa', {
    	templateUrl : '/views/dashboard/unhandled-qa.html',
    	controller : 'UnhandledQACtrl'
    }).when('/view', {
    	templateUrl : '/views/dashboard/view-cert.html',
    	controller : 'ViewCertCtrl'
    }).when('/about.support', {
    	templateUrl : '/views/dashboard/about.support.html',
    	controller : 'AboutWebcertCtrl'
    }).when('/about.webcert', {
    	templateUrl : '/views/dashboard/about.webcert.html',
    	controller : 'AboutWebcertCtrl'
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
