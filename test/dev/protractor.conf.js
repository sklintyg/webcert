// conf.js

/**
 * Setup :
 * <webcert/test/> : npm install
 *
 * To run tests :
 * <webcert/test/> : grunt
 *
 **/
var HtmlScreenshotReporter = require('protractor-jasmine2-screenshot-reporter');

exports.config = {
    //seleniumAddress: 'http://localhost:4444/wd/hub',
    specs: ['./spec/*.spec.js'],
    baseUrl: 'http://localhost:9088/',
    //rootElement:'html',
    // If chromeOnly is true, we dont need to stand the selenium server.
    // If you want to test with firefox, then set this to false and change the browserName
    chromeOnly: false,

    // Capabilities to be passed to the webdriver instance.
    capabilities: {
        'browserName': 'chrome' // possible values: phantomjs, firefox, chrome

        // IE11
        /*'browserName': 'internet explorer',
        'platform': 'ANY',
        'version': '11'*/
    },
    rootElement:'html',
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

        global.testdata = require('../lib/testdata/testdata.js');
        global.intygTemplates = require('./../lib/testdata/intygTemplates.js');
        global.pages = require('./../lib/pages.js');

        jasmine.getEnv().addReporter(
            new HtmlScreenshotReporter({
                dest: 'screenshots',
                filename: 'dev-report.html'
            })
        );
    }
};