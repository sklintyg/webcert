/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

/* global module, require */
var baseConfig = require('./karma.conf.js');

var runCoverage = false;
process.argv.forEach(function(a) {
    'use strict';
    if (a.indexOf('--skip-coverage') === 0) {
        var s = a.split('=');
        if (s.length === 2) {
            var value = s[1].trim();
            if (value === 'false') {
                runCoverage = true;
            }
        }
    }
});

module.exports = function(config) {
    'use strict';

    // Load base config
    baseConfig(config);

    // Override base config
    config.set({
        autoWatch: false,
        logLevel: config.LOG_ERROR,
        singleRun: true,

        browsers: [ 'PhantomJS' ],

        plugins: (function() {
            var plugins = [
                'karma-jasmine',
                'karma-junit-reporter',
                'karma-phantomjs-launcher',
                'karma-mocha-reporter'
            ];
            if (runCoverage) {
                plugins.push('karma-coverage');
            }
            return plugins;
        })(),

        reporters: [ 'dots', 'junit', 'coverage' ],

        preprocessors: {
            'src/main/webapp/app/**/*.js': ['coverage']
        },

        coverageReporter: {
            type : 'lcovonly',
            dir : 'target/karma_coverage/',
            subdir: '.'
        },

        junitReporter: {
            outputFile: 'target/surefire-reports/TEST-karma-test-results.xml'
        }
    });
};
