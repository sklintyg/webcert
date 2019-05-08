/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/* global console */

(function() {
    'use strict';

    // --- define test hooks
    window.doneLoading = false;
    window.rendered = true;
    window.saving = false;
    window.digest = 0;
    window.autoSave = true;

    window.setAutoSave = function(val) {
        window.autoSave = val;
    };

    window.setSaving = function(val) {
        window.saving = val;
    };
    // --- end test hooks

    // Globally configure jquery not to cache ajax requests.
    // Our other angular $http service driven requests have their own solution (using an interceptor)

    $.ajaxSetup({ cache: false });

    // Cancel any logout requests if one exists
    window.onload = function() {
        $.get('/api/anvandare/logout/cancel');
    };

    // before we do anything.. we need to get the user and moduleConfig
    var moduleArray = [];
    var moduleConfig;
    var user;
    var _links;

    var app = angular.module('webcert',
        ['ui.bootstrap', 'ui.router', 'ui.router.history', 'ui.router.state.events', 'ngCookies', 'ngSanitize', 'common',
            'ngAnimate', 'smoothScroll', 'ng.shims.placeholder', 'oc.lazyLoad']);

    app.value('networkConfig', {
        defaultTimeout: 30000 // test: 1000
    });

    function getDynamicLinks() {
        return $.get('/api/config/links').then(function(data) {
            return data;
        });
    }

    function getModuleConfig() {
        return $.get('/api/config').then(function(data) {
            moduleConfig = data;
            app.constant('moduleConfig', moduleConfig);
            return data;
        });
    }

    function getModules() {
        return $.get('/api/modules/map').then(function(data) {
            return data;
        });
    }

    function setupIntygModuleFutureStates($stateProvider) {

        function loadModule($transition$, module) {
            console.log('Lazy loading module ', module.id);

            var filesToLoad = [];
            if(module.cssPath && module.cssPath !== ''){
                filesToLoad.push(module.cssPath + '?' + moduleConfig.BUILD_NUMBER);
            }

            if (moduleConfig.JS_MINIFIED) {
                if (window.console) {
                    console.log('use mini is true! loading compressed modules');
                }
                filesToLoad.push(module.scriptPath + '.min.js?' + moduleConfig.BUILD_NUMBER);
                return $transition$.injector().get('$ocLazyLoad').load(filesToLoad);
            }
            else {
                filesToLoad.push(module.scriptPath + '.js');
                return $.get(module.dependencyDefinitionPath).then(function(deps) {
                    return $transition$.injector().get('$ocLazyLoad').load({
                        serie: true,
                        files: filesToLoad.concat(deps)
                    });
                });
            }
        }

        angular.forEach(moduleArray, function(module) {
            // Add future state for lazy loading, substates needs to be added by the lazyloaded module
            $stateProvider.state({
                name: module.id + '.**',
                url: '/' + module.id,
                lazyLoad: function($transition$) {
                    return loadModule($transition$, module);
                }
            });
            $stateProvider.state({
                name: 'webcert.intyg.' + module.id + '.**',
                url: '/intyg/' + module.id,
                lazyLoad: function($transition$) {
                    return loadModule($transition$, module);
                }
            });
            $stateProvider.state({
                name: 'webcert.fragasvar.' + module.id + '.**',
                url: '/fragasvar/' + module.id,
                lazyLoad: function($transition$) {
                    return loadModule($transition$, module);
                }
            });
            $stateProvider.state({
                name: module.id + '-readonly.**',
                url: '/intyg-read-only/' + module.id,
                lazyLoad: function($transition$) {
                    return loadModule($transition$, module);
                }
            });
        });
    }

    function getUser() {
        var restPath = '/api/anvandare';
        return $.get(restPath).then(function(data) {
            user = data;
            return data;
        }, function() {
            return null;
        });
    }

    app.config(['$httpProvider', 'common.http403ResponseInterceptorProvider', '$logProvider', '$compileProvider', '$locationProvider', '$animateProvider', '$uibTooltipProvider', '$stateProvider',
        function($httpProvider, http403ResponseInterceptorProvider, $logProvider, $compileProvider, $locationProvider, $animateProvider, $uibTooltipProvider, $stateProvider) {

            // Set in boot-app.jsp
            var debugMode = angular.isDefined(WEBCERT_DEBUG_MODE) ? WEBCERT_DEBUG_MODE : true; //jshint ignore:line

            // START TEMP 1.6 migration compatibility flags
            $compileProvider.preAssignBindingsEnabled(true);
            $locationProvider.hashPrefix('');
            // END

            // Add cache buster interceptor
            $httpProvider.interceptors.push('common.httpRequestInterceptorCacheBuster');

            // Configure 403 interceptor provider
            http403ResponseInterceptorProvider.setRedirectUrl('/error.jsp');
            $httpProvider.interceptors.push('common.http403ResponseInterceptor');

            // Enable debug logging
            $logProvider.debugEnabled(debugMode);

            // Disable angular debug info.
            $compileProvider.debugInfoEnabled(debugMode);

            // Disable comment and css directives
            $compileProvider.commentDirectivesEnabled(false);
            $compileProvider.cssClassDirectivesEnabled(false);

            $animateProvider.classNameFilter(/^(?:(?!ng-animate-disabled).)*$/);

            //set default popover trigger config.
            $uibTooltipProvider.options({
                trigger: 'mouseenter'
            });

            setupIntygModuleFutureStates($stateProvider);
        }]);

/*
 // Workaround for bug #1404
  // https://github.com/angular/angular.js/issues/1404
  // Source: http://plnkr.co/edit/hSMzWC?p=preview

    // Decorators that update form input names and interpolates them. Needed for datepicker directives templates dynamic name attributes
    app.config(function($provide) {
        $provide.decorator('ngModelDirective', function($delegate) {
            var ngModel = $delegate[0], controller = ngModel.controller;
            ngModel.controller =
                ['$scope', '$element', '$attrs', '$injector', function(scope, element, attrs, $injector) {
                    var $interpolate = $injector.get('$interpolate');
                    attrs.$set('name', $interpolate(attrs.name || '')(scope));
                    $injector.invoke(controller, this, {
                        '$scope': scope,
                        '$element': element,
                        '$attrs': attrs
                    });
                }];
            return $delegate;
        });
        $provide.decorator('formDirective', function($delegate) {
            var form = $delegate[0], controller = form.controller;
            form.controller = ['$scope', '$element', '$attrs', '$injector', function(scope, element, attrs, $injector) {
                var $interpolate = $injector.get('$interpolate');
                attrs.$set('name', $interpolate(attrs.name || attrs.ngForm || '')(scope));
                $injector.invoke(controller, this, {
                    '$scope': scope,
                    '$element': element,
                    '$attrs': attrs
                });
            }];
            return $delegate;
        });
    });
*/
    // Global config of default date picker config (individual attributes can be
    // overridden per directive usage)
    app.constant('uibDatepickerPopupConfig', {
        altInputFormats: [],
        appendToBody: true,
        clearText: 'Rensa',
        closeOnDateSelection: true,
        closeText: 'OK',
        currentText: 'Idag',
        datepickerPopup: 'yyyy-MM-dd',
        datepickerPopupTemplateUrl: 'uib/template/datepickerPopup/popup.html',
        datepickerTemplateUrl: 'uib/template/datepicker/datepicker.html',
        html5Types: {
            date: 'yyyy-MM-dd',
            'datetime-local': 'yyyy-MM-ddTHH:mm:ss.sss',
            'month': 'yyyy-MM'
        },
        onOpenFocus: true,
        showButtonBar: true,
        placement: 'auto bottom-left'
    });

    // Inject language resources
    app.run(['$log', '$rootScope', '$window', '$location', '$state', '$q', '$uibModalStack', 'common.messageService', 'common.moduleService',
             'common.UserModel', 'webcert.messages', 'common.MonitoringLogService', 'common.dynamicLinkService',
        function($log, $rootScope, $window, $location, $state, $q, $uibModalStack, messageService, moduleService, UserModel, wcMessages, MonitoringLogService, dynamicLinkService) {

            $rootScope.lang = 'sv';
            $rootScope.DEFAULT_LANG = 'sv';
            $rootScope.testModeActive = false;

            UserModel.setUser(user);
            UserModel.termsAccepted = user && user.privatLakareAvtalGodkand;

            messageService.addResources(wcMessages);
            dynamicLinkService.addLinks(_links);
            moduleService.setModules(moduleArray);

            $rootScope.$on('$stateChangeStart',
                function(event, toState, toParams, fromState, fromParams) {
                    // Check if state transition is triggered by link
                    var triggeredByLink = $location.search().forcelink;
                    $location.search('forcelink', null);

                    var redirectToUnitSelection = function() {
                        if (toState.name!=='normal-origin-enhetsval' && UserModel.isNormalOrigin() && !UserModel.user.valdVardenhet) {
                            event.preventDefault();
                            $state.go('normal-origin-enhetsval', { destinationState: toState }, { location: false });
                            return true;
                        }
                        return false;
                    };

                    var termsCheck = function() {
                        // check terms if not accepted then always redirect
                        if (toState.name !== 'webcert.terms') {
                            UserModel.transitioning = false;
                        }
                        if (UserModel.isPrivatLakare() && !UserModel.termsAccepted && !UserModel.transitioning) {
                            event.preventDefault();
                            UserModel.transitioning = true;
                            $state.transitionTo('webcert.terms');
                        }
                    };

                    // if we dont have a user
                    if (!UserModel.user) {
                        // Make sure we send user to login state
                        if (toState.name !== 'webcert.index') {
                            event.preventDefault();
                            $state.go('webcert.index');
                        }
                    } else {
                        if (!redirectToUnitSelection()) {
                            termsCheck();

                            if (fromState.name !== 'webcert.terms' || !UserModel.transitioning) {
                                // INTYG-4465, INTYG-7789: prevent state change when user press 'backwards' if modal is
                                // open, but close modal.
                                if ($uibModalStack.getTop()) {
                                    $uibModalStack.dismissAll();
                                    //If modal closed, and we did not navigate because of click on a modal link..
                                    if (!$uibModalStack.getTop() && !triggeredByLink) {
                                        // Abort current transition that apparently happended without explicitly first
                                        // closing the active modal. Most likely because of click on Back button in browser.
                                        event.preventDefault();
                                        $state.go(fromState, fromParams);
                                    }
                                }
                            }
                        }
                    }
                });

            $rootScope.$on('$stateNotFound',
                function(event, unfoundState, fromState, fromParams) {
                    $log.debug('$stateNotFound '+unfoundState.to + '  - fired when a state cannot be found by its name.');
                    $log.debug(unfoundState, fromState, fromParams);
                });

            $rootScope.$on('$stateChangeSuccess',
                function(event, toState/*, toParams, fromState, fromParams*/) {
                    $log.debug('$stateChangeSuccess to ' + toState.name + '- fired once the state transition is complete.');
                    if (!UserModel.termsAccepted && UserModel.transitioning && toState.name === 'webcert.terms') {
                        UserModel.transitioning = false;
                    }
                });

            $rootScope.$on('$stateChangeError',
                function(event, toState, toParams, fromState, fromParams, error) {
                    $log.debug('$stateChangeError');
                    $log.debug(toState);
                    $log.debug(error);
                });

            $rootScope.$on('$viewContentLoaded', function(event) {
                $log.debug('$viewContentLoaded - fired after dom rendered',event);
            });

            // INTYG-3069
            // Once per session we want to log relevant information about the users environment.
            // As of now this is limited to screen resolution.
            if (user) {
                MonitoringLogService.screenResolution($window.innerWidth, $window.innerHeight);
            }

            $window.onbeforeunload = function() {
                if (user && user.origin === 'DJUPINTEGRATION') {
                    $.get('/api/anvandare/logout');
                }
            };
        }]);


    // We need to have the moduleConfig available before loading modules
    getModuleConfig().then(function(data) {

        // Get a list of all modules to find all files to load.
        getUser().then(function(data) {
            user = data;

            getDynamicLinks().then(function(links) {
                _links = links;

                // Ugly hack loading these in sequence but before we have rewritten dynamiclink/message service content loading this is how it needs to be
                // There is a task to improve this whole loading flow
                getModules().then(function(modules) {
                    moduleArray = modules;

                    var modulePromises = [];

                    if (moduleConfig.JS_MINIFIED) {
                        modulePromises.push(loadScriptFromUrl('/web/webjars/common/webcert/module.min.js?' +
                            moduleConfig.BUILD_NUMBER));
                        // All dependencies in module-deps.json are included in module.min.js
                        // All dependencies in app-deps.json are included in app.min.js

                    } else {
                        modulePromises.push(loadScriptFromUrl('/web/webjars/common/webcert/module.js'));
                        modulePromises.push($.get('/web/webjars/common/webcert/module-deps.json'));
                        modulePromises.push($.get('/app/app-deps.json'));

                        // Prevent jQuery from appending cache buster to the url to make it easier to debug.
                        $.ajaxSetup({
                            cache: true
                        });
                    }

                    // Wait for all modules and module dependency definitions to load.
                    $.when.apply(this, modulePromises).then(function() {
                        var dependencyPromises = [];

                        // Only needed for development since all dependencies are included in other files.
                        if (!moduleConfig.JS_MINIFIED) {
                            if (window.console) {
                                console.log('use mini is false! loading modules');
                            }
                            angular.forEach(arguments, function(data) {
                                if (data !== undefined && data[0] instanceof Array) {
                                    angular.forEach(data[0], function(depdendency) {
                                        dependencyPromises.push(loadScriptFromUrl(depdendency));
                                    });
                                }
                            });
                        }

                        // Wait for all dependencies to load (for production dependencies are empty which is resolved immediately)
                        $.when.apply(this, dependencyPromises).then(function() {
                            angular.element(document).ready(function() {

                                var allModules = ['webcert', 'common'];

                                // Cant use common.featureService to check for this since it needs to be done before angular bootstrap.
                                if (user && user.jsLoggning) {
                                    addExceptionHandler();
                                }

                                // Everything is loaded, bootstrap the application with all dependencies.
                                document.documentElement.setAttribute('ng-app', 'webcert');
                                angular.bootstrap(document, allModules);

                            });
                        }).fail(function(error) {
                            if (window.console) {
                                console.log(error);
                            }
                        });
                    }).fail(function(error) {
                        if (window.console) {
                            console.log(error);
                        }
                    });
                });
            });
        });
    });

    function loadScriptFromUrl(url) {
        var result = $.Deferred();
        var script = document.createElement('script');
        script.async = 'async';
        script.type = 'text/javascript';
        script.src = url;
        script.onload = script.onreadystatechange = function(_, isAbort) {
            if (!script.readyState || /loaded|complete/.test(script.readyState)) {
                if (isAbort) {
                    result.reject();
                } else {
                    result.resolve();
                }
            }
        };
        script.onerror = function() {
            result.reject();
        };
        document.getElementsByTagName('head')[0].appendChild(script);
        return result.promise();
    }

    function addExceptionHandler() {

        // By default, AngularJS will catch errors and log them to
        // the Console. We want to keep that behavior; however, we
        // want to intercept it so that we can also log the errors
        // to the server for later analysis.
        app.provider('$exceptionHandler', function() { // $exceptionHandlerProvider
            this.$get = ['common.errorLogService', function(errorLogService) {
                return errorLogService;
            }];
        });
    }

}());
