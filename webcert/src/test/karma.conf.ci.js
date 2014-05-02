/* global module */
var baseConfig = require('./karma.conf.js');

module.exports = function(config) {
    'use strict';

    // Load base config
    baseConfig(config);

    // Override base config
    config.set({
        singleRun: true,
        autoWatch: false,
        plugins: [
            'karma-jasmine',
            'karma-junit-reporter',
            'karma-phantomjs-launcher',
            'karma-requirejs'
        ],
        browsers: ['PhantomJS']
    });
};
