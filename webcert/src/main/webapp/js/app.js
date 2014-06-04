define([
    'angular',
    'angularCookies',
    'angularRoute',
    'angularSanitize',
    'angularSwedish',
    'angularUiBootstrap',
    'directives',
    'filters',
    'messages',
    'controllers/AboutWebcertCtrl',
    'controllers/ChooseCertTypeCtrl',
    'controllers/ChoosePatientCtrl',
    'controllers/EditPatientNameCtrl',
    'controllers/InitCertCtrl',
    'controllers/UnhandledQACtrl',
    'controllers/UnsignedCertCtrl',
    'controllers/ViewCertCtrl',
    'webjars/common/webcert/js/messages',
    'webjars/common/webcert/js/directives',
    'webjars/common/webcert/js/services/http403ResponseInterceptor',
    'webjars/common/webcert/js/services/httpRequestInterceptorCacheBuster',
    'webjars/common/webcert/js/services/messageService',
    'webjars/common/webcert/js/services/User'
], function(angular, angularCookies, angularRoute, angularSanitize, angularSwedish, angularUiBootstrap, directives,
    filters, messages, AboutWebcertCtrl, ChooseCertTypeCtrl, ChoosePatientCtrl, EditPatientNameCtrl, InitCertCtrl,
    UnhandledQACtrl, UnsignedCertCtrl, ViewCertCtrl, commonMessages, commonDirectives, http403ResponseInterceptor,
    httpRequestInterceptorCacheBuster, messageService, User) {
    'use strict';

    var app = angular.module('webcert', [ 'ui.bootstrap', 'ngCookies', 'ngRoute', 'ngSanitize',
        directives, commonDirectives, filters, AboutWebcertCtrl, ChooseCertTypeCtrl, ChoosePatientCtrl,
        EditPatientNameCtrl, InitCertCtrl, UnhandledQACtrl, UnsignedCertCtrl, ViewCertCtrl, http403ResponseInterceptor,
        httpRequestInterceptorCacheBuster, messageService, User ]);

    app.config(['$routeProvider', function($routeProvider) {
        $routeProvider.
            when('/create/index', {
                // Route to initialize the create flow, template will be ignored.
                templateUrl: '/views/dashboard/create.choose-patient.html',
                controller: InitCertCtrl
            }).
            when('/create/choose-patient/index', {
                templateUrl: '/views/dashboard/create.choose-patient.html',
                controller: ChoosePatientCtrl
            }).
            when('/create/edit-patient-name/index', {
                templateUrl: '/views/dashboard/create.edit-patient-name.html',
                controller: EditPatientNameCtrl
            }).
            when('/create/choose-cert-type/index', {
                templateUrl: '/views/dashboard/create.choose-cert-type.html',
                controller: ChooseCertTypeCtrl
            }).
            when('/unhandled-qa', {
                templateUrl: '/views/dashboard/unhandled-qa.html',
                controller: UnhandledQACtrl
            }).
            when('/unsigned', {
                templateUrl: '/views/dashboard/unsigned.html',
                controller: UnsignedCertCtrl
            }).
            when('/intyg/:certificateType/:certificateId', {
                templateUrl: '/views/dashboard/view.certificate.html',
                controller: ViewCertCtrl
            }).
            when('/fragasvar/:certificateType/:certificateId', {
                templateUrl: '/views/dashboard/view.qa.html',
                controller: ViewCertCtrl
            }).
            when('/support/about', {
                templateUrl: '/views/dashboard/about.support.html',
                controller: AboutWebcertCtrl
            }).
            when('/certificates/about', {
                templateUrl: '/views/dashboard/about.certificates.html',
                controller: AboutWebcertCtrl
            }).
            when('/faq/about', {
                templateUrl: '/views/dashboard/about.faq.html',
                controller: AboutWebcertCtrl
            }).
            when('/cookies/about', {
                templateUrl: '/views/dashboard/about.cookies.html',
                controller: AboutWebcertCtrl
            }).
            otherwise({
                redirectTo: '/create/index'
            });
    }]);

    app.config([ '$httpProvider', http403ResponseInterceptor + 'Provider',
        function($httpProvider, http403ResponseInterceptorProvider) {

            // Add cache buster interceptor
            $httpProvider.interceptors.push(httpRequestInterceptorCacheBuster);

            // Configure 403 interceptor provider
            http403ResponseInterceptorProvider.setRedirectUrl('/error.jsp?reason=denied');
            $httpProvider.responseInterceptors.push(http403ResponseInterceptor);
        }]);

    // Global config of default date picker config (individual attributes can be
    // overridden per directive usage)
    app.constant('datepickerPopupConfig', {
        dateFormat: 'yyyy-MM-dd',
        closeOnDateSelection: true,
        appendToBody: false,
        showWeeks: true,
        closeText: 'OK',
        currentText: 'Idag',
        toggleWeeksText: 'Visa Veckor',
        clearText: 'Rensa'
    });

    // Inject language resources
    app.run([ '$rootScope', messageService, User,
        function($rootScope, messageService, User) {
            $rootScope.lang = 'sv';
            $rootScope.DEFAULT_LANG = 'sv';
            User.setUserContext(MODULE_CONFIG.USERCONTEXT);
            messageService.addResources(messages);
            messageService.addResources(commonMessages);
        }]);

    require([ 'text!/api/modules/map' ], function(modules) {

        var modulesMap = JSON.parse(modules);

        var modulesIds = [];
        var modulesNames = [];
        var modulesMinUrls = [];
        var modulesUrls = [];

        for (var artifactId in modulesMap) {
            modulesIds.push(modulesMap[artifactId].id);
            modulesNames.push('webjars/' + modulesMap[artifactId].id + modulesMap[artifactId].scriptPath + '.js');
            modulesMinUrls.push(modulesMap[artifactId].id + modulesMap[artifactId].scriptPath + '.min');
            modulesUrls.push(modulesMap[artifactId].id + modulesMap[artifactId].scriptPath);
            loadCssFromUrl('/web' + '/webjars/' + modulesMap[artifactId].id + modulesMap[artifactId].cssPath);
        }

        if (MODULE_CONFIG.REQUIRE_DEV_MODE === 'true') {
            require({ baseUrl: '/web/webjars/' }, modulesUrls, function() {
                var modules = arguments;
                angular.element().ready(function() {
                    angular.resumeBootstrap([app.name].concat(Array.prototype.slice.call(modules, 0)));
                });
            });
        } else {
            require({ baseUrl: '/web/webjars/' }, modulesMinUrls, function() {
                require(modulesNames, function() {
                    var modules = arguments;
                    angular.element().ready(function() {
                        angular.resumeBootstrap([app.name].concat(Array.prototype.slice.call(modules, 0)));
                    });
                });
            });
        }
    });

    function loadCssFromUrl(url) {
        var link = createLinkElement(url);
        document.getElementsByTagName('head')[0].appendChild(link);
    }

    function createLinkElement(url) {
        var link = document.createElement('link');
        link.type = 'text/css';
        link.rel = 'stylesheet';
        link.href = url;
        return link;
    }

    return app;
});
