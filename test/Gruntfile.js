/* global module */
require('path');

module.exports = function(grunt) {
    'use strict';
 
    grunt.loadNpmTasks('grunt-protractor-runner');
    grunt.loadNpmTasks('grunt-protractor-webdriver');
    grunt.loadNpmTasks('grunt-env');

    var devSuite = grunt.option('suite') || 'app';
    grunt.initConfig({
        env: grunt.file.readJSON('./lib/envConfig.json'),
        protractor: {
            options: {
                //configFile: './protractor.cli.conf.js', // Target-specific config file
                keepAlive: false, // If false, the grunt process stops when the test fails.
                noColor: false, // If true, protractor will not use colors in its output.
                args: {
                    // Arguments passed to the command
                }
            },

            // Grunt requires at least one target to run so you can simply put 'all: {}' here too.
            dev: {
                options: {
                    configFile: './dev/protractor.conf.js',
                    args: {
                        'suite': devSuite
                    } // Target-specific arguments
                }
            },
            acc: {
                options: {
                    configFile: './acceptance/protractor-conf.js',
                    args: {} // Target-specific arguments
                }
            }
        },

        protractor_webdriver: { // jshint ignore:line
            options: {
                // Task-specific options go here.
            }
        }

    });

    grunt.registerTask('acc', ['env:ip40','protractor_webdriver','protractor:acc']);
    grunt.registerTask('acc-ci', ['env:build-server','protractor_webdriver','protractor:acc']);
    grunt.registerTask('default', ['env:dev', 'protractor_webdriver','protractor:dev']);
};
