'use strict';

angular.module('wcDashBoardApp', ['ui.bootstrap', 'ngCookies', 'modules.messages', 'wc.common', 'wc.dashboard.controllers', 'wc.dashboard.services',
    'wc.dashboard.directives', 'wc.utils', 'wc.common.fragasvarmodule']);

angular.module('wcDashBoardApp').config(['$routeProvider', '$httpProvider', 'http403ResponseInterceptorProvider',
    function ($routeProvider, $httpProvider, http403ResponseInterceptorProvider) {
        $routeProvider.

            when('/create/index', {
                // Route to initialize the create flow, template will be ignored.
                templateUrl : '/views/dashboard/create.choose-patient.html',
                controller : 'InitCertCtrl'}).
            when('/create/choose-patient/index', {
                templateUrl : '/views/dashboard/create.choose-patient.html',
                controller : 'ChoosePatientCtrl'}).
            when('/create/edit-patient-name/index', {
                templateUrl : '/views/dashboard/create.edit-patient-name.html',
                controller : 'EditPatientNameCtrl'}).
            when('/create/choose-cert-type/index', {
                templateUrl : '/views/dashboard/create.choose-cert-type.html',
                controller : 'ChooseCertTypeCtrl'}).

            when('/unhandled-qa', {
                templateUrl : '/views/dashboard/unhandled-qa.html',
                controller : 'UnhandledQACtrl'}).
            when('/unsigned', {
                templateUrl : '/views/dashboard/unsigned.html',
                controller : 'UnsignedCertCtrl'}).
            when('/view', {
                templateUrl : '/views/dashboard/view-cert.html',
                controller : 'ViewCertCtrl'}).
            when('/support/about', {
                templateUrl : '/views/dashboard/about.support.html',
                controller : 'AboutWebcertCtrl'}).
            when('/webcert/about', {
                templateUrl : '/views/dashboard/about.webcert.html',
                controller : 'AboutWebcertCtrl'}).

            otherwise({
                redirectTo : '/create/index'});

        // Add cache buster interceptor
        $httpProvider.interceptors.push('httpRequestInterceptorCacheBuster');

        // Configure 403 interceptor provider
        http403ResponseInterceptorProvider.setRedirectUrl("/error.jsp?reason=denied");
        $httpProvider.responseInterceptors.push('http403ResponseInterceptor');
    }]);

// Global config of default date picker config (individual attributes can be
// overridden per directive usage)
angular.module('wcDashBoardApp').constant('datepickerPopupConfig', {
    dateFormat : 'yyyy-MM-dd',
    closeOnDateSelection : true,
    appendToBody : false,
    showWeeks : true,
    closeText : 'OK',
    currentText : 'Idag',
    toggleWeeksText : 'Visa Veckor',
    clearText : 'Rensa'
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
