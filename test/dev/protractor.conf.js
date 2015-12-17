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

        global.wcTestTools = require('./../webcertTestTools/webcertTestTools.js');

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