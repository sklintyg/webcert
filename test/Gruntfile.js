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
                    args: {
                    }
                }
            }
        },

        protractor_webdriver: { // jshint ignore:line
            options: {
                // Task-specific options go here.
            }
        }

    });

    grunt.registerTask('default', ['env:dev', 'protractor_webdriver', 'protractor:dev']);


    // Run: 'grunt acc:ip20:tags'
    grunt.task.registerTask('acc', 'Task för att köra acceptanstest', function(environment, tags) {
        
        if(!environment){
            var defaultEnv = 'ip40';
            grunt.log.subhead('Ingen miljö vald, använder '+defaultEnv+'-miljön..');
            environment = defaultEnv;
        }
        if(tags){
            grunt.log.subhead('Kör tester taggade med: '+tags);
            grunt.config.set('protractor.acc.options.args.cucumberOpts.tags', tags);
        }
        else{
            grunt.config.set('protractor.acc.options.args.cucumberOpts.tags', ['~@notReady']);
        }


        grunt.task.run(['env:'+environment, 'protractor_webdriver', 'protractor:acc']);
        

    });
};