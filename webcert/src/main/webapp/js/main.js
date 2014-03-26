require.config({
    paths : {

        webjars : '../web/webjars',

        angular : '../web/webjars/angularjs/1.2.14/angular',
        angularCookies : '../web/webjars/angularjs/1.2.14/angular-cookies',
        angularRoute : '../web/webjars/angularjs/1.2.14/angular-route.min',
        angularSanitize : '../web/webjars/angularjs/1.2.14/angular-sanitize.min',
        angularSwedish : '../web/webjars/angularjs/1.2.14/i18n/angular-locale_sv-se',
        angularUiBootstrap : '../web/webjars/angular-ui-bootstrap/0.8.0/ui-bootstrap-tpls',

        text : '../web/webjars/requirejs-text/2.0.10/text',

        'ts-bas' : '../web/webjars/ts-bas',
       	'ts-diabetes' : '../web/webjars/ts-diabetes'
        	
    },
    shim : {
        'angular' : {'exports' : 'angular'},
        'angularCookies' : ['angular'],
        'angularRoute' : ['angular'],
        'angularSanitize' : ['angular'],
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
    'angularRoute',
    'angularSanitize',
    'angular',
    'routes',
    'angularCookies',
    'angularSwedish',
    'angularUiBootstrap'
], function (app, angular) {
    'use strict';

});
