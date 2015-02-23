/* global module */
function config(name) {
    return require('./tasks/' + name);
}

var path = require('path');


module.exports = function(grunt) {
    'use strict';

    grunt.loadNpmTasks('grunt-contrib-csslint');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-karma');
    grunt.loadNpmTasks('grunt-ng-annotate');

    grunt.loadNpmTasks('grunt-connect-proxy');
    grunt.loadNpmTasks('grunt-contrib-connect');


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
                src: [SRC_DIR + '../**/*.css']
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
                src: ['Gruntfile.js', SRC_DIR + '**/*.js', TEST_DIR + '**/*.js', '!' + SRC_DIR + '/app.min.js']
            }
        },

        karma: {
            webcert: {
                configFile: 'src/test/resources/karma.conf.ci.js',
                reporters: ['mocha']
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
        },


        // server ======================================================================================================
        connect : {
            server: {
                options: {
                    port: 9089,
                    base: 'src/main/webapp',
                    keepalive: true,
                    hostname: '*',
                    middleware: function(connect, options) {
                        var proxy = require('grunt-connect-proxy/lib/utils').proxyRequest;
                        var middlewares = [];
                        middlewares.push(
                            connect().use(
                                '/web',
                            connect.static(__dirname + '/src/main/webapp')
                        ) );
                        middlewares.push(
                            connect().use(
                                '/web/webjars/common',
                                connect.static(__dirname + '/../../common/web/src/main/resources/META-INF/resources/webjars/common')
                            ) );
                        middlewares.push(
                            connect().use(
                                '/web/webjars/fk7263/webcert',
                                connect.static(__dirname + '/../../intygstyper/fk7263/src/main/resources/META-INF/resources/webjars/fk7263/webcert')
                            ) );

                        middlewares.push(proxy);
                        return middlewares;
                    }
                },
                proxies: [
                    {
                        context: '/',
                        host: 'localhost',
                        port: 9088
                    }
                ]


            }
        }
    });

    grunt.registerTask('default', ['concat', 'ngAnnotate', 'uglify']);
    grunt.registerTask('lint', ['jshint', 'csslint']);
    grunt.registerTask('test', ['karma']);

    // frontend only dev ===============================================================================================
    grunt.registerTask('server', ['configureProxies:server','connect:server']);
};
