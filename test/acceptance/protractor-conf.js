/*global
browser
*/
'use strict';

exports.config = {
    baseUrl: process.env.WEBCERT_URL,
    allScriptsTimeout: 30000,
    // seleniumAddress: 'http://127.0.0.1:4444/wd/hub',
    framework: 'custom',
    timeout : 100000,
    defaultTimeoutInterval: 30000,
    
    // path relative to the current config file
    frameworkPath: require.resolve('protractor-cucumber-framework'),
    specs: [
        'features/*.feature'
    ],
    capabilities: {
        browserName: 'firefox',
        // 'phantomjs.binary.path': './node_modules/karma-phantomjs-launcher/node_modules/phantomjs/bin/phantomjs',
        //'phantomjs.cli.args': '--debug=true --webdriver --webdriver-logfile=webdriver.log --webdriver-loglevel=DEBUG',
        version: '',
        platform: 'ANY'
    },
    cucumberOpts: {
        format: ['json:./acceptance/report/acc_results.json', 'pretty'],
        require: ['features/steps/**/*.js', 'features/support/**/*.js']
    },
    onPrepare: function () {
        //http://chaijs.com/
        global.chai = require('chai');
        
        //https://github.com/domenic/chai-as-promised/
        global.chaiAsPromised = require('chai-as-promised');
        global.chai.use(global.chaiAsPromised);
        
        global.expect = global.chai.expect;
        
        // Testdata lib
        global.testdata = require('../lib/testdata/testdata.js');
        global.pages = require('./../lib/pages.js');
        global.intyg = {};
        
        browser.ignoreSynchronization = false;
        browser.baseUrl = process.env.WEBCERT_URL;
    }
};
