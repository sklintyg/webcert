/* global module */
module.exports = function(config) {
    'use strict';

    var WEBJAR_DEPENDENCIES = 'target/webjardependencies/';
    config.set({

        // base path, that will be used to resolve files and exclude
        basePath: '../../',

        // frameworks to use
        frameworks: ['jasmine', 'requirejs'],

        // list of files / patterns to load in the browser
        files: [
            { pattern: WEBJAR_DEPENDENCIES + '**/*.*', included: false },
            { pattern: 'src/main/webapp/js/**/*', included: false },
            { pattern: 'src/test/js/**/*Spec.js', included: false },
            'src/test/resources/test-main.js'
        ],

        // list of files to exclude
        exclude: [ WEBJAR_DEPENDENCIES + '**/*require*.*' ],

        // web server port
        port: 9876,

        // enable / disable colors in the output (reporters and logs)
        colors: true,

        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,

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

        junitReporter: {
            outputFile: 'target/surefire-reports/TEST-karma-test-results.xml'
        },

        plugins: [
            'karma-jasmine',
            'karma-junit-reporter',
            'karma-chrome-launcher',
            'karma-firefox-launcher',
            'karma-phantomjs-launcher',
            'karma-requirejs'
        ],

        reporters: [ 'progress', 'junit' ]
    });
};
