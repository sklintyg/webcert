var tests = [];
for ( var file in window.__karma__.files) {
	if (window.__karma__.files.hasOwnProperty(file)) {
		if (/Spec\.js$/.test(file)) {
			tests.push(file);
		}
	}
}

var WEBJARS = '/base/target/webjardependencies';

require.config({

    baseUrl: '/base/src/main/webapp/js/',

	paths : {

        webjars : WEBJARS,

        angular : WEBJARS + '/angularjs/angular',
        angularCookies : WEBJARS + '/angularjs/angular-cookies',
        angularRoute : WEBJARS + '/angularjs/angular-route.min',
        angularSanitize : WEBJARS + '/angularjs/angular-sanitize.min',
        angularSwedish : WEBJARS + '/angularjs/1.2.14/angular-locale_sv-se',
        angularUiBootstrap : WEBJARS + '/angular-ui-bootstrap/ui-bootstrap-tpls',

        angularMocks : WEBJARS + '/angularjs/angular-mocks',
        angularScenario : WEBJARS + '/angularjs/angular-scenario',

        text : WEBJARS + '/requirejs-text/text'
    },
	deps : tests,

	shim : {
		'angular' : { 'exports' : 'angular'},
        'angularCookies' : [ 'angular' ],
        'angularRoute' : [ 'angular' ],
        'angularSanitize' : [ 'angular' ],
        'angularSwedish' : [ 'angular' ],
        'angularUiBootstrap' : [ 'angular' ],

		'angularScenario' : [ 'angular' ],
		'angularMocks' : {
			deps : [ 'angular' ],
			exports : 'angular.mock'
		}
	},
	priority : [ 'angular' ],
	callback : window.__karma__.start
});

require([ 'angular', 'angularMocks', 'angularScenario', 'angularRoute', 'angularSanitize', 'angularCookies',
    'angularSwedish', 'angularUiBootstrap' ], function(angular, mocks) {
    'use strict';
});
