/* global module */
module.exports = function(grunt) {
    'use strict';

    grunt.loadNpmTasks('grunt-contrib-csslint');
    grunt.loadNpmTasks('grunt-contrib-jshint');

    grunt.initConfig({

        csslint: {
            dev: {
                options: {
                    csslintrc: '../src/main/resources/.csslintrc',
                    force: true
                },
                src: [ 'src/main/webapp/**/*.css' ]
            }
        },

        jshint: {
            dev: {
                options: {
                    jshintrc: '../src/main/resources/.jshintrc',
                    force: true
                },
                src: [ 'Gruntfile.js', 'src/main/webapp/js/**/*.js', 'src/test/**/*.js' ]
            }
        }
    });

    grunt.registerTask('default', [ 'jshint', 'csslint' ]);
};
