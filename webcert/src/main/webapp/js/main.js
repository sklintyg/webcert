require.config({
    paths : {
        angular : '../web/webjars/angularjs/1.1.5/angular',
        angularCookies : '../web/webjars/angularjs/1.1.5/angular-cookies',
        angularSwedish : '../web/webjars/angularjs/1.1.5/i18n/angular-locale_sv-se',
        angularUiBootstrap : '../web/webjars/angular-ui-bootstrap/0.7.0/ui-bootstrap-tpls',

        text : '../web/webjars/requirejs-text/2.0.10/text',

        'ts-bas' : '../web/webjars/ts-bas',
       	'ts-diabetes' : '../web/webjars/ts-diabetes'
        	
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
    'routes',
    'angularCookies',
    'angularSwedish',
    'angularUiBootstrap'
], function (app, angular) {
    'use strict';

});
