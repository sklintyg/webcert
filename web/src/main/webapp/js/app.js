/* global MODULE_CONFIG, wcMessages */
window.name = 'NG_DEFER_BOOTSTRAP!'; // jshint ignore:line

var app = angular.module('webcert', ['ui.bootstrap', 'ui.router', 'ngCookies', 'ngSanitize', 'common', 'ngAnimate', 'smoothScroll']);

app.config(['$httpProvider', 'common.http403ResponseInterceptorProvider',
    function($httpProvider, http403ResponseInterceptorProvider) {
        'use strict';

        // Add cache buster interceptor
        $httpProvider.interceptors.push('common.httpRequestInterceptorCacheBuster');

        // Configure 403 interceptor provider
        http403ResponseInterceptorProvider.setRedirectUrl('/error.jsp');
        $httpProvider.responseInterceptors.push('common.http403ResponseInterceptor');
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
app.run(['$log', '$rootScope', '$window', 'common.messageService', 'common.UserModel',
    function($log, $rootScope, $window, messageService, UserModel) {
        'use strict';

        $rootScope.lang = 'sv';
        $rootScope.DEFAULT_LANG = 'sv';
        UserModel.setUserContext(MODULE_CONFIG.USERCONTEXT);
        messageService.addResources(wcMessages);

        $window.animations = 0;
        $window.doneLoading = false;
        $window.dialogDoneLoading = true;
        $window.rendered = true;

        $rootScope.$on('$stateChangeStart',
            function(event, toState, toParams, fromState, fromParams){
                $window.doneLoading = false;
            });

        $rootScope.$on('$stateNotFound',
            function(event, unfoundState, fromState, fromParams){
            })
        $rootScope.$on('$stateChangeSuccess',
            function(event, toState, toParams, fromState, fromParams){
                $window.doneLoading = true;
            })

        $rootScope.$on('$stateChangeError',
            function(event, toState, toParams, fromState, fromParams, error){
                $log.log("$stateChangeError");
                $log.log(toState);
            })
    }]);

// Get a list of all modules to find all files to load.
$.get('/api/modules/map').then(function(modules) {
    'use strict';

    var modulesIds = [];
    var modulePromises = [];

    if (MODULE_CONFIG.USE_MINIFIED_JAVASCRIPT === 'true') {
        modulePromises.push(loadScriptFromUrl('/web/webjars/common/webcert/js/module.min.js?' +
        MODULE_CONFIG.BUILD_NUMBER));
        // All dependencies in module-deps.json are included in module.min.js
        // All dependencies in app-deps.json are included in app.min.js

    } else {
        modulePromises.push(loadScriptFromUrl('/web/webjars/common/webcert/js/module.js'));
        modulePromises.push($.get('/web/webjars/common/webcert/js/module-deps.json'));
        modulePromises.push($.get('/js/app-deps.json'));

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
            angular.element().ready(function() {

                // Cant use common.featureService to check for this since it needs to be done before angular bootstrap.
                if (MODULE_CONFIG.USERCONTEXT &&
                    MODULE_CONFIG.USERCONTEXT.aktivaFunktioner &&
                    MODULE_CONFIG.USERCONTEXT.aktivaFunktioner.indexOf('jsLoggning') >= 0) {
                    addExceptionHandler();
                }

                // Everything is loaded, bootstrap the application with all dependencies.
                angular.resumeBootstrap([app.name, 'common'].concat(Array.prototype.slice.call(modulesIds, 0)));
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