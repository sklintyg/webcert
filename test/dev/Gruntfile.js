/* global module */
require('path');

module.exports = function(grunt) {
    'use strict';

    grunt.loadNpmTasks('grunt-protractor-runner');
    grunt.loadNpmTasks('grunt-protractor-webdriver');

    grunt.initConfig({

        protractor: {
            options: {
                configFile: './protractor.cli.conf.js', // Target-specific config file
                keepAlive: true, // If false, the grunt process stops when the test fails.
                noColor: false, // If true, protractor will not use colors in its output.
                args: {
                    // Arguments passed to the command
                }
            },

            // Grunt requires at least one target to run so you can simply put 'all: {}' here too.
            your_target: { // jshint ignore:line
/*                options: {
                    configFile: 'src/test/resources/protractor.cli.conf.js', // Target-specific config file
                    args: {} // Target-specific arguments
                }*/
            }
        },

        protractor_webdriver: { // jshint ignore:line
            options: {
                // Task-specific options go here.
            },
            your_target: { // jshint ignore:line
                // Target-specific file lists and/or options go here.
            }
        }

    });

    /*When we build the distribution we don't want to run sass:dev since that would rebuild the sass of projects
    * that webcert depends on*/
    grunt.registerTask('default', ['protractor_webdriver','protractor']);
};
