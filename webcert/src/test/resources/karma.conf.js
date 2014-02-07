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
            'app/**/*.js', '../../../test/js/**/*.js'
        ],

        frameworks : ['jasmine'],

        junitReporter : {
            outputFile : '../../../../target/surefire-reports/karma-test-results.xml'
        },

        plugins : [
            'karma-jasmine', 'karma-junit-reporter', 'karma-chrome-launcher', 'karma-firefox-launcher',
            'karma-phantomjs-launcher'
        ],

        reporters : ['progress', 'junit']
    });
};
