/* global MODULE_CONFIG, wcMessages */
window.name = 'NG_DEFER_BOOTSTRAP!'; // jshint ignore:line

var app = angular.module('webcert', [ 'ui.bootstrap', 'ngCookies', 'ngRoute', 'ngSanitize', 'common' ]);

app.config(function($routeProvider) {
    'use strict';

    $routeProvider.
        when('/create/index', {
            // Route to initialize the create flow, template will be ignored.
            templateUrl: '/views/dashboard/create.choose-patient.html',
            controller: 'webcert.InitCertCtrl'
        }).
        when('/create/choose-patient/index', {
            templateUrl: '/views/dashboard/create.choose-patient.html',
            controller: 'webcert.ChoosePatientCtrl'
        }).
        when('/create/edit-patient-name/:mode', {
            templateUrl: '/views/dashboard/create.edit-patient-name.html',
            controller: 'webcert.EditPatientNameCtrl'
        }).
        when('/create/choose-cert-type/index', {
            templateUrl: '/views/dashboard/create.choose-cert-type.html',
            controller: 'webcert.ChooseCertTypeCtrl'
        }).
        when('/unhandled-qa', {
            templateUrl: '/views/dashboard/unhandled-qa.html',
            controller: 'webcert.UnhandledQACtrl'
        }).
        when('/unsigned', {
            templateUrl: '/views/dashboard/unsigned.html',
            controller: 'webcert.UnsignedCertCtrl'
        }).
        when('/intyg/:certificateType/:certificateId', {
            templateUrl: '/views/dashboard/view.certificate.html',
            controller: 'webcert.ViewCertCtrl'
        }).
        when('/fragasvar/:certificateType/:certificateId/:qaOnly?', {
            templateUrl: '/views/dashboard/view.qa.html',
            controller: 'webcert.ViewCertCtrl'
        }).
        when('/webcert/about', {
            templateUrl: '/views/dashboard/about.webcert.html',
            controller: 'webcert.AboutWebcertCtrl'
        }).
        when('/support/about', {
            templateUrl: '/views/dashboard/about.support.html',
            controller: 'webcert.AboutWebcertCtrl'
        }).
        when('/certificates/about', {
            templateUrl: '/views/dashboard/about.certificates.html',
            controller: 'webcert.AboutWebcertCtrl'
        }).
        when('/faq/about', {
            templateUrl: '/views/dashboard/about.faq.html',
            controller: 'webcert.AboutWebcertCtrl'
        }).
        when('/cookies/about', {
            templateUrl: '/views/dashboard/about.cookies.html',
            controller: 'webcert.AboutWebcertCtrl'
        }).
        otherwise({
            redirectTo: '/create/index'
        });
});

app.config([ '$httpProvider', 'common.http403ResponseInterceptorProvider',
    function($httpProvider, http403ResponseInterceptorProvider) {
        'use strict';

        // Add cache buster interceptor
        $httpProvider.interceptors.push('common.httpRequestInterceptorCacheBuster');

        // Configure 403 interceptor provider
        http403ResponseInterceptorProvider.setRedirectUrl('/error.jsp?reason=denied');
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
    if (!Array.prototype.indexOf)
    {
        Array.prototype.indexOf = function(elt /*, from*/)
        {
            var len = this.length >>> 0;

            var from = Number(arguments[1]) || 0;
            from = (from < 0)
                ? Math.ceil(from)
                : Math.floor(from);
            if (from < 0)
                from += len;

            for (; from < len; from++)
            {
                if (from in this &&
                    this[from] === elt)
                    return from;
            }
            return -1;
        };
    }
}

// Inject language resources
app.run([ '$rootScope', 'common.messageService', 'common.User',
    function($rootScope, messageService, User) {
        'use strict';

        // add IndexOf to IE8
        checkAddIndexOf();

        $rootScope.lang = 'sv';
        $rootScope.DEFAULT_LANG = 'sv';
        User.setUserContext(MODULE_CONFIG.USERCONTEXT);
        messageService.addResources(wcMessages);

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

                // Everything is loaded, bootstrap the application with all dependencies.
                angular.resumeBootstrap([app.name, 'common'].concat(Array.prototype.slice.call(modulesIds, 0)));
            });
        }).fail(function(error) {
            console.log(error);
        });
    }).fail(function(error) {
        console.log(error);
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
