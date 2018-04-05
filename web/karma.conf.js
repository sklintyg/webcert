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

    config.set({

        // base path, that will be used to resolve files and exclude
        basePath: '',

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
            // bower:js
            'src/main/webapp/bower_components/jquery/dist/jquery.js',
            'src/main/webapp/bower_components/angular/angular.js',
            'src/main/webapp/bower_components/angular-animate/angular-animate.js',
            'src/main/webapp/bower_components/angular-cookies/angular-cookies.js',
            'src/main/webapp/bower_components/angular-i18n/angular-locale_sv-se.js',
            'src/main/webapp/bower_components/angular-sanitize/angular-sanitize.js',
            'src/main/webapp/bower_components/angular-bootstrap/ui-bootstrap-tpls.js',
            'src/main/webapp/bower_components/angular-ui-router/release/angular-ui-router.js',
            'src/main/webapp/bower_components/bootstrap-sass/assets/javascripts/bootstrap.js',
            'src/main/webapp/bower_components/momentjs/moment.js',
            // endbower
            'src/main/webapp/bower_components/angular-mocks/angular-mocks.js',

            // Load these first
            SRC_DIR + 'app-test.js',
            SRC_DIR + 'utils/*.js',

            { pattern: SRC_DIR + '**/app.test.js' },
            { pattern: SRC_DIR + '**/views/**/*.js' },
            { pattern: SRC_DIR + '**/views/**/*.html' },
            { pattern: SRC_DIR + '**/views/**/*.spec.js' }
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

        // Run specs in semi-random order
        random: false,

        // Start these browsers, currently available:
        // - Chrome
        // - ChromeCanary
        // - Firefox
        // - Opera (has to be installed with `npm install karma-opera-launcher`)
        // - Safari (only Mac; has to be installed with `npm install karma-safari-launcher`)
        // - PhantomJS
        // - IE (only Windows; has to be installed with `npm install karma-ie-launcher`)
        browsers: [ 'PhantomJS' ],

        // If browser does not capture in given timeout [ms], kill it
        captureTimeout: 60000,

        // Continuous Integration mode if true, it capture browsers, run tests and exit
        singleRun: false,

        plugins: [
            'karma-jasmine',
            'karma-phantomjs-launcher',
            'karma-mocha-reporter',
            'karma-ng-html2js-preprocessor'
        ],

        reporters: [ 'progress' ]
    });
};
