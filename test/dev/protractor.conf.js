/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

// conf.js
/*globals browser,global,exports*/
/**
 * Setup :
 * <webcert/test/> : npm install
 *
 * To run tests :
 * <webcert/test/> : grunt
 *
 **/
 'use strict';
var winston = require('winston');
var HtmlScreenshotReporter = require('protractor-jasmine2-screenshot-reporter');

var screenshotReporter = new HtmlScreenshotReporter({
    dest: 'reports',
    filename: 'index.html',
    ignoreSkippedSpecs: true,
    captureOnlyFailedSpecs: true
});

exports.config = {
    // Needed to start local seleniumserver with firefox
    localSeleniumStandaloneOpts: {
        jvmArgs: ["-Dwebdriver.gecko.driver=node_modules/protractor/node_modules/webdriver-manager/selenium/geckodriver-v0.19.0.exe"]
    },
    // Directconnect bypasses selenium and communicates directly with webdriver
    directConnect: true,
    seleniumAddress: require('./../webcertTestTools/environment.js').envConfig.SELENIUM_ADDRESS,
    baseUrl: require('./../webcertTestTools/environment.js').envConfig.WEBCERT_URL,

    // Timeouts
    getPageTimeout: 20000,
    allScriptsTimeout: 25000,

    specs: ['./spec/*.spec.js', './spec/intyg/**/*.spec.js'],

    suites: {
        testdata: './spec/generateTestData/**/*.spec.js',
        clean: './spec/cleanTestData/**/*.spec.js',
        app: ['./spec/*.spec.js', './spec/intyg/**/*.spec.js']
    },

    // If chromeOnly is true, we dont need to start the selenium server. (seems we don't need to anyway? can this be removed?)
    // If you want to test with firefox, then set this to false and change the browserName
    //chromeOnly: false,

    // Capabilities to be passed to the webdriver instance. (ignored if multiCapabilities is used)
    capabilities: {

        acceptInsecureCerts: true,

        loggingPrefs: {'driver': 'ALL', 'server': 'ALL', 'browser': 'ALL'},

        // IE11
        /*browserName: 'internet explorer',
        platform: 'ANY',
        version: '11',*/

        // Any other browser
        browserName: 'chrome', // possible values: firefox, chrome

        chromeOptions: {
            args: [
 		"--no-sandbox", "--headless", "--disable-gpu",
                "--window-size=1280x1024"
            ]
        },

        // Run parallell instances of same browser (combine with any browser above)
        shardTestFiles: false, // set to true to divide tests among instances
        maxInstances: 1 // change to >1 for parallell instances
    },

    // Run *different browsers* in parallell (optional, completely replaces 'capabilities' above)
/*    multiCapabilities: [{
        browserName: 'chrome',
        shardTestFiles: true,
        maxInstances: 1
    }, {
        browserName: 'firefox',
        shardTestFiles: true,
        maxInstances: 1
    }],*/
    framework: 'jasmine',
    jasmineNodeOpts: {
        // If true, print colors to the terminal.
        showColors: true,
        // Default time to wait in ms before a test fails.
        defaultTimeoutInterval: 120000
        // Function called to print jasmine results.
        //print: function() {},
        // If set, only execute specs whose names match the pattern, which is
        // internally compiled to a RegExp.
        //grep: 'pattern',
        // Inverts 'grep' matches
        //invertGrep: false
        //isVerbose: true, // jasmine 1.3 only
        //includeStackTrace: true // jasmine 1.3 only
    },

    // Setup the report before any tests start
    beforeLaunch: function() {
        return new Promise(function(resolve){
            screenshotReporter.beforeLaunch(resolve);
        });
    },

    onPrepare: function() {
        // implicit and page load timeouts
        //browser.manage().timeouts().pageLoadTimeout(40000);
        //browser.manage().timeouts().implicitlyWait(25000);

        // for non-angular page
        /**
         * This makes protractor not wait for Angular promises, such as those from $http or $timeout to resolve,
         * which you might want to do if you're testing behaviour during $http or $timeout (e.g., a 'loading' message),
         * or testing non-Angular sites or pages, such as a separate login page.
         */
        browser.ignoreSynchronization = false;

		/* Winston Logger Usage 
		
        logger.info("");
        logger.warn("");
        logger.error("");
        
		logger.log('silly', "");
        logger.log('debug', "");
        logger.log('verbose', "");
        logger.log('info', "");
        logger.log('warn', "");
        logger.log('error', "");
		
		*/
		
        global.logger = new(winston.Logger)({
            transports: [
                new(winston.transports.Console)({
                    colorize: true,
                    timestamp: formatLocalDate,
                    formatter: function(options) {
                        // Return string will be passed to logger.
                        return options.timestamp() + ' ' + options.level.toUpperCase() + ' ' + (undefined !== options.message ? options.message : '') +
                             (options.meta && Object.keys(options.meta).length ? '\n\t' + JSON.stringify(options.meta) : '');
                    }
                })
            ]
        });
		
		// Winston Logger level. Logging levels are prioritized from 0 to 5 (highest to lowest):
		// error: 0, warn: 1, info: 2, verbose: 3, debug: 4, silly: 5
		
		logger.transports.console.level = 'info';
		//logger.transports.console.level = 'error';

        var reporters = require('jasmine-reporters');
        jasmine.getEnv().addReporter(
            new reporters.JUnitXmlReporter({
                savePath:'reports/',
                filePrefix: 'junit',
                consolidateAll:true}));

        jasmine.getEnv().addReporter(screenshotReporter);

        var disableNgAnimate = function() {
            angular.module('disableNgAnimate', []).run(['$animate', function($animate) {
                $animate.enabled(false);
            }]);
        };

        var disableCssAnimate = function() {
            angular
                .module('disableCssAnimate', [])
                .run(function() {
                    var style = document.createElement('style');
                    style.type = 'text/css';
                    style.innerHTML = '* {' +
                        '-webkit-transition: none !important;' +
                        '-moz-transition: none !important' +
                        '-o-transition: none !important' +
                        '-ms-transition: none !important' +
                        'transition: none !important' +
                        '}';
                    document.getElementsByTagName('head')[0].appendChild(style);
                });
        };

        browser.addMockModule('disableNgAnimate', disableNgAnimate);
        browser.addMockModule('disableCssAnimate', disableCssAnimate);
    },

    // Close the report after all tests finish
    afterLaunch: function(exitCode) {
        return new Promise(function(resolve){
            screenshotReporter.afterLaunch(resolve.bind(this, exitCode));
        });
    }
};

function formatLocalDate() {
    var now = new Date(),
        tzo = -now.getTimezoneOffset(),
        dif = tzo >= 0 ? '+' : '-',
        pad = function(num) {
            var norm = Math.abs(Math.floor(num));
            return (norm < 10 ? '0' : '') + norm;
        };
    return now.getFullYear() + '-' + pad(now.getMonth() + 1) + '-' + pad(now.getDate()) + 'T' + pad(now.getHours()) + ':' + pad(now.getMinutes()) + ':' + pad(now.getSeconds()) + dif + pad(tzo / 60) +
        ':' + pad(tzo % 60);
}
