'use strict';

exports.config = {
    baseUrl: 'https://webcert.ip30.nordicmedtest.sjunet.org',
    allScriptsTimeout: 30000,
    seleniumAddress: 'http://127.0.0.1:4444/wd/hub',
    framework: 'cucumber',
    specs: [
        'features/*.feature'
    ],
    capabilities: {
        browserName: 'firefox',
        // 'phantomjs.binary.path': './node_modules/karma-phantomjs-launcher/node_modules/phantomjs/bin/phantomjs',
        // 'phantomjs.cli.args': '--debug=true --webdriver --webdriver-logfile=webdriver.log --webdriver-loglevel=DEBUG',
        version: '',
        platform: 'ANY'
    },
    cucumberOpts: {
        require: 'features/steps/*_steps.js',
        format: 'pretty',
        tags: ['@dev']
    },
    onPrepare: function() {
        // global.myVariable = 'test';

        //http://chaijs.com/
        global.chai = require('chai');

        //https://github.com/domenic/chai-as-promised/
        global.chaiAsPromised = require('chai-as-promised');
        global.chai.use(global.chaiAsPromised);

        global.expect = global.chai.expect;

        // Testdata lib
        global.testdata = require('../lib/testdata/testdata.js');


    }
};