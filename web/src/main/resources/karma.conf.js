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

/* global module */
module.exports = function(config) {
    'use strict';

    var SRC_DIR = 'src/main/webapp/app/';
    var TEST_DIR = SRC_DIR;
    var WEBJAR_DIR = 'target/webjardependencies/';

    config.set({

        // base path, that will be used to resolve files and exclude
        basePath: '../../../',

        // frameworks to use
        frameworks: [ 'jasmine' ],

        // generate js files from html templates to expose them during testing.
        preprocessors: {
            'src/main/webapp/app/**/*.html': ['ng-html2js']
        },

        ngHtml2JsPreprocessor: {
            // If your build process changes the path to your templates,
            // use stripPrefix and prependPrefix to adjust it.
            stripPrefix: 'src/main/webapp',

            // the name of the Angular module to create
            moduleName: 'htmlTemplates'
        },

        // list of files / patterns to load in the browser
        files: [

            // Dependencies
            WEBJAR_DIR + 'angularjs/angular.js',
            WEBJAR_DIR + 'angularjs/angular-mocks.js',
            WEBJAR_DIR + 'angularjs/1.4.10/angular-locale_sv-se.js',
            WEBJAR_DIR + 'angularjs/angular-cookies.js',
            WEBJAR_DIR + 'angular-ui-router/angular-ui-router.js',
            WEBJAR_DIR + 'angularjs/angular-sanitize.js',
            WEBJAR_DIR + 'angular-ui-bootstrap/ui-bootstrap-tpls.js',
            WEBJAR_DIR + 'jquery/jquery.js',
            WEBJAR_DIR + 'momentjs/moment.js',

            // Load these first
            SRC_DIR + 'messages.js',
            TEST_DIR + 'app-test.js',
            TEST_DIR + 'utils/*.js',

            { pattern: SRC_DIR + '**/app.test.js' },
            { pattern: SRC_DIR + '**/views/**/*.js' },
            { pattern: SRC_DIR + '**/views/**/*.html' },
            { pattern: TEST_DIR + '**/views/**/*.spec.js' }
        ],

        exclude: [ SRC_DIR + '**/*.page.js', SRC_DIR + 'app.js', SRC_DIR + 'app.min.js' ],

        // web server port
        port: 9876,

        // enable / disable colors in the output (reporters and logs)
        colors: true,

        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_DEBUG,

        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,

        // Start these browsers, currently available:
        // - Chrome
        // - ChromeCanary
        // - Firefox
        // - Opera (has to be installed with `npm install karma-opera-launcher`)
        // - Safari (only Mac; has to be installed with `npm install karma-safari-launcher`)
        // - PhantomJS
        // - IE (only Windows; has to be installed with `npm install karma-ie-launcher`)
        browsers: [ 'Chrome' ],

        // If browser does not capture in given timeout [ms], kill it
        captureTimeout: 60000,

        // Continuous Integration mode if true, it capture browsers, run tests and exit
        singleRun: false,

        plugins: [
            'karma-jasmine',
            'karma-chrome-launcher',
            'karma-firefox-launcher',
            'karma-phantomjs-launcher',
            'karma-mocha-reporter',
            'karma-ng-html2js-preprocessor'
        ],

        reporters: [ 'progress' ]
    });
};
