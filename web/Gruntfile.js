/* global module */
module.exports = function(grunt) {
    'use strict';

    grunt.loadNpmTasks('grunt-contrib-csslint');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-karma');
    grunt.loadNpmTasks('grunt-ng-annotate');

    var SRC_DIR = 'src/main/webapp/js/';
    var TEST_DIR = 'src/test/js/';

    var webcert = grunt.file.readJSON(SRC_DIR + 'app-deps.json').map(function(file) {
        return file.replace(/\/js\//g, SRC_DIR);
    });

    webcert = [SRC_DIR + 'app.js'].concat(webcert);

    grunt.initConfig({

        csslint: {
            webcert: {
                options: {
                    csslintrc: '../target/build-tools/csslint/.csslintrc',
                    force: true
                },
                src: [ SRC_DIR + '../**/*.css' ]
            }
        },

        concat: {
            webcert: {
                src: webcert,
                dest: SRC_DIR + 'app.min.js'
            }
        },

        jshint: {
            webcert: {
                options: {
                    jshintrc: '../target/build-tools/jshint/.jshintrc',
                    force: true
                },
                src: [ 'Gruntfile.js', SRC_DIR + '**/*.js', TEST_DIR + '**/*.js', '!' + SRC_DIR + '/app.min.js' ]
            }
        },

        karma: {
            webcert: {
                configFile: 'src/test/resources/karma.conf.ci.js',
                reporters: [ 'mocha' ]
            }
        },

        ngAnnotate: {
            options: {
                singleQuotes: true
            },
            webcert: {
                src: SRC_DIR + 'app.min.js',
                dest: SRC_DIR + 'app.min.js'
            }
        },

        uglify: {
            options: {
                mangle: false
            },
            webcert: {
                src: SRC_DIR + 'app.min.js',
                dest: SRC_DIR + 'app.min.js'
            }
        }
    });

    grunt.registerTask('default', [ 'concat', 'ngAnnotate', 'uglify' ]);
    grunt.registerTask('lint', [ 'jshint', 'csslint' ]);
    grunt.registerTask('test', [ 'karma' ]);
};
