'use strict';

/* Lakare Dashboard App Module */
angular.module('wcDashBoardApp', [ 'ui.bootstrap', 'ngCookies', 'ngRoute', 'ngSanitize', 'modules.messages', 'wc.common', 'dashboard.services', 'wc.utils', 'wc.common.fragasvarmodule' ]);
angular.module('wcDashBoardApp').config([ '$routeProvider', '$httpProvider', 'http403ResponseInterceptorProvider', function($routeProvider, $httpProvider, http403ResponseInterceptorProvider) {
    $routeProvider.when('/index', {
      templateUrl : '/views/dashboard/unhandled-qa.html',
      controller : 'UnhandledQACtrl'
    }).when('/unhandled-qa', {
        templateUrl : '/views/dashboard/unhandled-qa.html',
        controller : 'UnhandledQACtrl'
    }).when('/support/about', {
        templateUrl : '/views/dashboard/about.support.html',
        controller : 'AboutWebcertCtrl'
    }).when('/webcert/about', {
        templateUrl : '/views/dashboard/about.webcert.html',
        controller : 'AboutWebcertCtrl'
    }).otherwise({
        redirectTo : '/index'
    });

    // Add cache buster interceptor
    $httpProvider.interceptors.push('httpRequestInterceptorCacheBuster');

    // Configure 403 interceptor provider
    http403ResponseInterceptorProvider.setRedirectUrl("/error.jsp?reason=denied");
    $httpProvider.responseInterceptors.push('http403ResponseInterceptor');
} ]);

// Global config of default date picker config (individual attributes can be
// overridden per directive usage)
angular.module('wcDashBoardApp').constant('datepickerPopupConfig', {
    dateFormat : "yyyy-MM- dd",
    closeOnDateSelection : true,
    appendToBody : false,
    showWeeks : true,
    closeText : 'OK',
    currentText : "Idag",
    toggleWeeksText : "Visa Veckor",
    clearText : "Rensa"
});

// Inject language resources
angular.module('wcDashBoardApp').run(['$rootScope', 'messageService','User',
    function ($rootScope, messageService, User) {
        $rootScope.lang = 'sv';
        $rootScope.DEFAULT_LANG = 'sv';
        User.setUserContext(MODULE_CONFIG.USERCONTEXT);
        messageService.addResources(webcertResources);
        messageService.addResources(commonMessageResources);
    }]);
