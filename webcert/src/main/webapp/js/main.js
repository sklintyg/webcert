require.config({
    paths : {
        angular : '../webjars/angularjs/1.1.5/angular',
        angularCookies : '../webjars/angularjs/1.1.5/angular-cookies',
        angularSwedish : '../webjars/angularjs/1.1.5/i18n/angular-locale_sv-se',
        angularUiBootstrap : '../webjars/angular-ui-bootstrap/0.7.0/ui-bootstrap-tpls',

        text : '../webjars/requirejs-text/2.0.10/text'
    },
    shim : {
        'angular' : {'exports' : 'angular'},
        'angularCookies' : ['angular'],
        'angularSwedish' : ['angular'],
        'angularUiBootstrap' : ['angular']
    },
    priority : [
        'angular'
    ]
});

//http://code.angularjs.org/1.2.1/docs/guide/bootstrap#overview_deferred-bootstrap
window.name = 'NG_DEFER_BOOTSTRAP!';

require([
    'app',
    'angular',
    'angularCookies',
    'angularSwedish',
    'angularUiBootstrap'
], function (app, angular) {
    'use strict';

    angular.element().ready(function () {
        angular.resumeBootstrap([app.name]);
    });
});
