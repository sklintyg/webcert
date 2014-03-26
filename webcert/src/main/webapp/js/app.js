define([
    'angular',
    'angularRoute',
    'angularSanitize',
    'controllers',
    'directives',
    'filters',
    'messages',
    'services',
    'webjars/common/js/wc-common',
    'webjars/common/js/wc-common-fragasvar-module',
    'webjars/common/js/wc-common-message-resources',
    'webjars/common/js/wc-message-module',
    'webjars/common/js/wc-utils'
], function (angular, angularRoute, angularSanitize, controllers, directives, filters, messages, services, wcCommon, wcCommonFragaSvarModule, commonMessages, wcMessageModule, wcUtils) {
    'use strict';

    var app = angular.module('wcDashBoardApp', ['ui.bootstrap', 'ngCookies', 'ngRoute', 'ngSanitize',
        controllers, directives, filters, services, wcCommon, wcCommonFragaSvarModule, wcMessageModule, wcUtils]);

    app.config(['$routeProvider', '$controllerProvider', '$compileProvider', '$filterProvider', '$provide', '$httpProvider', 'http403ResponseInterceptorProvider',
        function ($routeProvider, $controllerProvider, $compileProvider, $filterProvider, $provide, $httpProvider, http403ResponseInterceptorProvider) {

            app.register = {
                controller : $controllerProvider.register,
                directive : $compileProvider.directive,
                filter : $httpProvider.register,
                factory : $provide.factory,
                service : $provide.service,
                $routeProvider : $routeProvider
            };

            // Add cache buster interceptor
            $httpProvider.interceptors.push('httpRequestInterceptorCacheBuster');

            // Configure 403 interceptor provider
            http403ResponseInterceptorProvider.setRedirectUrl('/error.jsp?reason=denied');
            $httpProvider.responseInterceptors.push('http403ResponseInterceptor');
        }]);

    // Global config of default date picker config (individual attributes can be
    // overridden per directive usage)
    app.constant('datepickerPopupConfig', {
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
    app.run(['$rootScope', 'messageService', 'User',
        function ($rootScope, messageService, User) {
            $rootScope.lang = 'sv';
            $rootScope.DEFAULT_LANG = 'sv';
            User.setUserContext(MODULE_CONFIG.USERCONTEXT);
            messageService.addResources(messages);
            messageService.addResources(commonMessages);
        }]);

    require(['text!/api/modules/map'], function (modules) {

        var modulesMap = JSON.parse(modules);

        var modulesUrls = [];
        for (var artifactId in modulesMap) {
            modulesUrls.push('../web/webjars/' + modulesMap[artifactId].id + modulesMap[artifactId].scriptPath);
        }

        require(modulesUrls, function () {
            var modules = arguments;

            angular.element().ready(function () {
                angular.resumeBootstrap([app.name].concat(Array.prototype.slice.call(modules, 0)));
            });
        });
    });
    return app;
});
