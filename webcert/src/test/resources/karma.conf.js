'use strict';

/* global module */
module.exports = function (config) {
    config.set({

        autoWatch : true,

        basePath : '../../main/webapp/js',

        //browsers: ['Chrome', 'ChromeCanary', 'Firefox', 'PhantomJS'],
        browsers : ['PhantomJS'],

        files : [
            'vendor/angular/1.1.5/angular.js', 'vendor/angular/1.1.5/angular-mocks.js', 'app/dashboard/app.js',
            'app/**/*.js', 'common/*.js', '../../../test/js/**/*.js'
        ],

        frameworks : ['jasmine', 'ng-scenario'],

        junitReporter : {
            outputFile : '../../../../target/surefire-reports/karma-test-results.xml'
        },

        plugins : [
            'karma-ng-scenario','karma-jasmine', 'karma-junit-reporter', 'karma-chrome-launcher', 'karma-firefox-launcher',
            'karma-phantomjs-launcher','karma-coverage'
        ],

        reporters : ['progress', 'junit', 'coverage'],

        preprocessors: {
            // source files, that you wanna generate coverage for
            // do not include tests or libraries
            // (these files will be instrumented by Istanbul)
            '**/app/**/*.js': 'coverage',
            '**/common/*.js': 'coverage'
        },

        // optionally, configure the reporter
        coverageReporter: {
            type : 'html',
            dir : '../../../test/coverage/'
        }
    });
};
