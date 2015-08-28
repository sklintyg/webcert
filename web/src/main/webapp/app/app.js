/* global MODULE_CONFIG, wcMessages */
//window.name = 'NG_DEFER_BOOTSTRAP!'; // jshint ignore:line

var app = angular.module('webcert', ['ui.bootstrap', 'ui.router', 'ngCookies', 'ngSanitize', 'common', 'ngAnimate', 'smoothScroll']);

app.config(['$httpProvider', 'common.http403ResponseInterceptorProvider', '$logProvider',
    function($httpProvider, http403ResponseInterceptorProvider, $logProvider) {
        'use strict';

        // Add cache buster interceptor
        $httpProvider.interceptors.push('common.httpRequestInterceptorCacheBuster');

        // Configure 403 interceptor provider
        http403ResponseInterceptorProvider.setRedirectUrl('/error.jsp');
        $httpProvider.responseInterceptors.push('common.http403ResponseInterceptor');

        // Enable debug logging
        $logProvider.debugEnabled(false);
    }]);

// Decorators that update form input names and interpolates them. Needed for datepicker directives templates dynamic name attributes
app.config(function($provide) {
    'use strict';
    $provide.decorator('ngModelDirective', function($delegate) {
        var ngModel = $delegate[0], controller = ngModel.controller;
        ngModel.controller = ['$scope', '$element', '$attrs', '$injector', function(scope, element, attrs, $injector) {
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

    // This decorator will add build number to all html requests
    // The purpose is to allow the browser to cache templates but only for this build.
    $provide.decorator('$http', function($delegate, $templateCache) {
        var $http = $delegate;

        var wrapper = function () {
            // Apply global changes to arguments, or perform other
            // nefarious acts.
            return $http.apply($http, arguments);
        };

        // $http has convenience methods such as $http.get() that we have
        // to pass through as well.
        Object.keys($http).filter(function (key) {
            return (typeof $http[key] === 'function');
        }).forEach(function (key) {
            wrapper[key] = function () {

                // Apply global changes to arguments, or perform other
                // nefarious acts.

                if (key === 'get') {
                    // Add build number to all html get requests.
                    // Ignore templates already provided in templateCache (angular ui uses these)
                    if (MODULE_CONFIG.BUILD_NUMBER &&
                        arguments.length > 0 &&
                        !$templateCache.get(arguments[0]) &&
                        arguments[0].indexOf('.html', arguments[0].length - 5) !== -1) {
                        // Add build number
                        if (arguments[0].indexOf('?') >= 0) {
                            arguments[0] += '&';
                        }
                        else {
                            arguments[0] += '?';
                        }
                        arguments[0] += MODULE_CONFIG.BUILD_NUMBER;
                    }
                }

                return $http[key].apply($http, arguments);
            };
        });

        return wrapper;
    });
});

// Global config of default date picker config (individual attributes can be
// overridden per directive usage)
app.constant('datepickerConfig', {
    formatDay: 'dd',
    formatMonth: 'MMMM',
    formatYear: 'yyyy',
    formatDayHeader: 'EEE',
    formatDayTitle: 'MMMM yyyy',
    formatMonthTitle: 'yyyy',
    datepickerMode: 'day',
    minMode: 'day',
    maxMode: 'year',
    showWeeks: true,
    startingDay: 1,
    yearRange: 20,
    minDate: null,
    maxDate: null
});

app.constant('datepickerPopupConfig', {
    datepickerPopup: 'yyyy-MM-dd',
    currentText: 'Idag',
    clearText: 'Rensa',
    closeText: 'OK',
    closeOnDateSelection: true,
    appendToBody: false,
    showButtonBar: true
});

// IE8 doesn't have Array.indexOf function, check and add it as early as possible
function checkAddIndexOf() {
    'use strict';

    if (!Array.prototype.indexOf) {
        Array.prototype.indexOf = function(elt /*, from*/) {
            var len = this.length >>> 0;

            var from = Number(arguments[1]) || 0;
            from = (from < 0) ? Math.ceil(from) : Math.floor(from);
            if (from < 0) {
                from += len;
            }

            for (; from < len; from++) {
                if (from in this &&
                    this[from] === elt) {
                    return from;
                }
            }
            return -1;
        };
    }
}

// Inject language resources
app.run(['$log', '$rootScope', '$window', '$location','$state', '$q', 'common.messageService', 'common.UserModel', 'common.User', 'webcert.TermsState',
    function($log, $rootScope, $window, $location, $state, $q, messageService, UserModel, UserService, TermsState ) {
        'use strict';

        $rootScope.lang = 'sv';
        $rootScope.DEFAULT_LANG = 'sv';
        UserModel.setUserContext(MODULE_CONFIG.USERCONTEXT);
        messageService.addResources(wcMessages);

        // define test hooks
        $window.doneLoading = false;
        $window.dialogDoneLoading = true;
        $window.rendered = true;
        $window.saving = false;
        $window.digest = 0;
        $window.autoSave = true;

        $window.setAutoSave = function(val){
            $window.autoSave = val;
        }

        var userDef = $q.defer();

        // get the current user from the backend
        UserService.initUser(function(data){
            TermsState.termsAccepted = data.privatLakareAvtalGodkand;
            TermsState.transitioning = false;
            userDef.resolve();
        });

        // watch the digest cycle
        //$rootScope.$watch(function() {
        //    $window.digest ++;
        //    $log.log('---- inc digest : ' + $window.digest);
        //    // Note that we're using a private Angular method here (for now)
        //    $rootScope.$$postDigest(function() {
        //        $window.digest --;
        //        $log.log('---- dec digest : ' + $window.digest);
        //    });
        //});



        $rootScope.$on('$stateChangeStart',
            function(event, toState, toParams, fromState, fromParams){
                // if we dont have a user then we need to defer until we do ..
                var termsCheck = function(){
                    // check terms if not accepted then always redirect
                    if(toState.name !== 'webcert.terms'){
                        TermsState.transitioning = false;
                    }
                    if(!TermsState.termsAccepted && !TermsState.transitioning){
                        event.preventDefault();
                        TermsState.transitioning = true;
                        $state.transitionTo('webcert.terms');
                    }
                }
                if(!UserModel.user){
                    event.preventDefault();
                    // gets resolved when a user is loaded
                    userDef.promise.then(function(){
                        $state.transitionTo(toState.name, toParams);
                    });
                } else {
                    termsCheck();
                    $window.doneLoading = false;
                }
            });

        $rootScope.$on('$stateNotFound',
            function(event, unfoundState, fromState, fromParams){
                //$log.log('$stateNotFound '+unfoundState.to+'  - fired when a state cannot be found by its name.');
                //$log.log(unfoundState, fromState, fromParams);

            })
        $rootScope.$on('$stateChangeSuccess',
            function(event, toState, toParams, fromState, fromParams){
                //$log.log('$stateChangeSuccess to '+toState.name+'- fired once the state transition is complete.');
                if(!TermsState.termsAccepted && TermsState.transitioning && toState.name === 'webcert.terms'){
                    TermsState.transitioning = false;
                }
                $window.doneLoading = true;
            })

        $rootScope.$on('$stateChangeError',
            function(event, toState, toParams, fromState, fromParams, error){
                //$log.log("$stateChangeError");
                //$log.log(toState);
            });


        $rootScope.$on('$viewContentLoading',function(event, viewConfig){
            // runs on individual scopes, so putting it in "run" doesn't work.
            $window.rendered = false;
            //$log.log('+++ $viewContentLoading, rendered : ' + $window.rendered);
        });
        $rootScope.$on('$viewContentLoaded',function(event){
            //$log.log('$viewContentLoaded - fired after dom rendered',event);
            $window.rendered = true;
            //$log.log('--- $viewContentLoaded, rendered : ' + $window.rendered);
        });

    }]);

// Get a list of all modules to find all files to load.
$.get('/api/modules/map').then(function(modules) {
    'use strict';

    var modulesIds = [];
    var modulePromises = [];

    if (MODULE_CONFIG.USE_MINIFIED_JAVASCRIPT === 'true') {
        modulePromises.push(loadScriptFromUrl('/web/webjars/common/webcert/module.min.js?' +
        MODULE_CONFIG.BUILD_NUMBER));
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

    angular.forEach(modules, function(module) {
        modulesIds.push(module.id);
        loadCssFromUrl(module.cssPath + '?' + MODULE_CONFIG.BUILD_NUMBER);

        if (MODULE_CONFIG.USE_MINIFIED_JAVASCRIPT === 'true') {
            modulePromises.push(loadScriptFromUrl(module.scriptPath + '.min.js?' + MODULE_CONFIG.BUILD_NUMBER));
            // All dependencies for the modules are included in module.min.js
        } else {
            modulePromises.push(loadScriptFromUrl(module.scriptPath + '.js'));
            modulePromises.push($.get(module.dependencyDefinitionPath));
        }
    });

    // Wait for all modules and module dependency definitions to load.
    $.when.apply(this, modulePromises).then(function() {
        var dependencyPromises = [];

        // Only needed for development since all dependencies are included in other files.
        if (MODULE_CONFIG.USE_MINIFIED_JAVASCRIPT === 'false') {
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

                var allModules = [app.name, 'common'].concat(Array.prototype.slice.call(modulesIds, 0));

                // Cant use common.featureService to check for this since it needs to be done before angular bootstrap.
                if (MODULE_CONFIG.USERCONTEXT &&
                    MODULE_CONFIG.USERCONTEXT.aktivaFunktioner &&
                    MODULE_CONFIG.USERCONTEXT.aktivaFunktioner.indexOf('jsLoggning') >= 0) {
                    addExceptionHandler();
                }

                // Everything is loaded, bootstrap the application with all dependencies.
                //angular.resumeBootstrap([app.name, 'common'].concat(Array.prototype.slice.call(modulesIds, 0)));
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

function loadCssFromUrl(url) {
    'use strict';

    var link = document.createElement('link');
    link.type = 'text/css';
    link.rel = 'stylesheet';
    link.href = url;
    document.getElementsByTagName('head')[0].appendChild(link);
}

function loadScriptFromUrl(url) {
    'use strict';

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
    'use strict';

    // By default, AngularJS will catch errors and log them to
    // the Console. We want to keep that behavior; however, we
    // want to intercept it so that we can also log the errors
    // to the server for later analysis.
    app.provider('$exceptionHandler', function $exceptionHandlerProvider() {
        this.$get = ['common.errorLogService', function(errorLogService) {
            return errorLogService;
        }];
    });
}