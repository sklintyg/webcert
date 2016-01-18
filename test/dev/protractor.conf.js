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

// conf.js
/*globals browser,global,exports,process*/
/**
 * Setup :
 * <webcert/test/> : npm install
 *
 * To run tests :
 * <webcert/test/> : grunt
 *
 **/
 'use strict';
var HtmlScreenshotReporter = require('protractor-jasmine2-screenshot-reporter');

exports.config = {
    //seleniumAddress: 'http://localhost:4444/wd/hub',
    baseUrl: require('./../webcertTestTools/environment.js').envConfig.WEBCERT_URL,

    specs: ['./spec/*.spec.js'],

    suites: {
        testdata: './spec/generateTestData/**/*.spec.js',
        clean: './spec/cleanTestData/**/*.spec.js',
        app: ['./spec/*.spec.js']
    },

    // If chromeOnly is true, we dont need to start the selenium server. (seems we don't need to anyway? can this be removed?)
    // If you want to test with firefox, then set this to false and change the browserName
    //chromeOnly: false,

    // Capabilities to be passed to the webdriver instance. (ignored if multiCapabilities is used)
    capabilities: {

        // IE11
        /*browserName: 'internet explorer',
        platform: 'ANY',
        version: '11',*/

        // Any other browser
        browserName: 'firefox', // possible values: phantomjs, firefox, chrome

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
        defaultTimeoutInterval: 30000
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

        global.wcTestTools = require('webcert-testtools');

        global.logg = function(text){
            console.log(text);
        };

        var reporters = require('jasmine-reporters');
        jasmine.getEnv().addReporter(
            new reporters.JUnitXmlReporter({
                savePath:'dev/report/',
                filePrefix: 'junit',
                consolidateAll:true}));

        jasmine.getEnv().addReporter(
            new HtmlScreenshotReporter({
                dest: 'dev/report',
                filename: 'index.html'
            })
        );
    }
};
