'use strict';

/* Lakare Dashboard App Module */
angular.module('wcDashBoardApp', [ 'ui.bootstrap', 'ngCookies', 'modules.messages', 'wc.common.directives', 'dashboard.services', 'wc.utils', 'wc.common.fragasvarmodule' ]);
angular.module('wcDashBoardApp').config([ '$routeProvider', '$httpProvider', 'http403ResponseInterceptorProvider', function($routeProvider, $httpProvider, http403ResponseInterceptorProvider) {
    $routeProvider.when('/index', {
        templateUrl : '/views/dashboard/index.step1.html',
        controller : 'CreateCertCtrl'
    }).when('/edit-patient/index', {
        templateUrl : '/views/dashboard/index.editpatient.html',
        controller : 'CreateCertCtrl'
    }).when('/choose-cert/index', {
        templateUrl : '/views/dashboard/index.step2.html',
        controller : 'CreateCertCtrl'
    }).when('/choose-unit/index', {
        templateUrl : '/views/dashboard/index.step3.html',
        controller : 'CreateCertCtrl'
    }).when('/mycert', {
        templateUrl : '/views/dashboard/mycert.html',
        controller : 'WebCertCtrl'
    }).when('/unhandled-qa', {
        templateUrl : '/views/dashboard/unhandled-qa.html',
        controller : 'UnhandledQACtrl'
    }).when('/unsigned', {
        templateUrl : '/views/dashboard/unsigned.html',
        controller : 'UnsignedCertCtrl'
    }).when('/view', {
        templateUrl : '/views/dashboard/view-cert.html',
        controller : 'ViewCertCtrl'
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
angular.module('wcDashBoardApp').run([ '$rootScope', 'messageService', function($rootScope, messageService) {
    $rootScope.lang = 'sv';
    $rootScope.DEFAULT_LANG = 'sv';
    $rootScope.MODULE_CONFIG = MODULE_CONFIG;
    messageService.addResources(webcertResources);
    messageService.addResources(commonMessageResources);
} ]);
