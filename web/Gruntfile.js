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
    grunt.loadNpmTasks('grunt-concurrent');
    grunt.loadNpmTasks('grunt-connect-proxy');
    grunt.loadNpmTasks('grunt-contrib-connect');
    grunt.loadNpmTasks('grunt-angular-templates');
    grunt.loadNpmTasks('grunt-contrib-watch');

    var SRC_DIR = 'src/main/webapp/app/';
    var TEST_DIR = 'src/test/js/';

    var webcert = grunt.file.readJSON(SRC_DIR + 'app-deps.json').map(function(file) {
        return file.replace(/\/app\//g, SRC_DIR);
    });

    webcert = [SRC_DIR + 'app.js'].concat(webcert);

//    grunt.log.write(JSON.stringify(webcert));

    var COMMON_DIR = '/../../common/web/src/main/resources/META-INF/resources/webjars/common/webcert';
    var TSBAS_DIR = '/../../intygstyper/ts-bas/src/main/resources/META-INF/resources/webjars/ts-bas/webcert';
    var TSDIABETES_DIR = '/../../intygstyper/ts-diabetes/src/main/resources/META-INF/resources/webjars/ts-diabetes/webcert';
    var FK7263_DIR = '/../../intygstyper/fk7263/src/main/resources/META-INF/resources/webjars/fk7263/webcert';

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
                src: ['Gruntfile.js', !SRC_DIR + 'vendor/*.js', SRC_DIR + '**/*.js', TEST_DIR + '**/*.js', '!' + SRC_DIR + '/app.min.js']
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

        //ngtemplates: config('ngtemplates'),

        // server ======================================================================================================


        watch: {
            //css: {
            //    files: ['public/src/css/**/*.less'],
            //    tasks: ['less', 'cssmin']
            //},
            //js: {
            //    files: ['public/src/js/**/*.js'],
            //    tasks: ['jshint', 'uglify', 'injector', 'wiredep']
            //},
            html: {
                files: [
                        __dirname + '/src/main/webapp/**/*.html',
                        __dirname + COMMON_DIR + '/**/*.html',
                        __dirname + FK7263_DIR + '/**/*.html',
                        __dirname + TSBAS_DIR + '/**/*.html',
                        __dirname + TSDIABETES_DIR + '/**/*.html'
                ],
                tasks: ['ngtemplates']
            }
        },

        ngtemplates : {
            webcert: {
                cwd: __dirname + '/src/main/webapp',
                src: ['app/views/**/*.html', 'app/partials/**/*.html'],
                dest: __dirname + '/src/main/webapp/app/templates.js',
                options: {
                    module: 'webcert',
                    url: function(url) {
                        return '/' + url.replace('../', '/');
                    }
                }
            },
            common: {
                cwd: __dirname + COMMON_DIR,
                src: ['**/*.html'],
                dest: __dirname + COMMON_DIR + '/templates.js',
                options:{
                    module: 'common',
                    url: function(url) {
                        return '/web/webjars/common/webcert/' + url;
                    }
                }
            },
            tsdiabetes: {
                cwd: __dirname + TSDIABETES_DIR,
                src: ['**/*.html'],
                dest: __dirname + TSDIABETES_DIR + '/templates.js',
                options:{
                    module: 'ts-diabetes',
                    url: function(url) {
                        return '/web/webjars/ts-diabetes/webcert/' + url;
                    }
                }
            },
            tsbas: {
                cwd: __dirname + TSBAS_DIR,
                src: ['**/*.html'],
                dest: __dirname + TSBAS_DIR + '/templates.js',
                options:{
                    module: 'ts-bas',
                    url: function(url) {
                        return '/web/webjars/ts-bas/webcert/' + url;
                    }
                }
            },
            fk7263: {
                cwd: __dirname + FK7263_DIR,
                src: ['**/*.html'],
                dest: __dirname + FK7263_DIR + '/templates.js',
                options:{
                    module: 'fk7263',
                    url: function(url) {
                        return '/web/webjars/fk7263/webcert/' + url;
                    }
                }
            }
        },

        connect: {
            server: {
                options: {
                    port: 9089,
                    base: 'src/main/webapp',
                    hostname: '*',
                    middleware: function(connect, options) {
                        var proxy = require('grunt-connect-proxy/lib/utils').proxyRequest;
                        var middlewares = [];
                        middlewares.push(
                            connect().use(
                                '/web',
                                connect.static(__dirname + '/src/main/webapp')
                            ));
                        middlewares.push(
                            connect().use(
                                '/app',
                                connect.static(__dirname + '/src/main/webapp/app')
                            ));
                        middlewares.push(
                            connect().use(
                                '/web/webjars/common',
                                connect.static(__dirname + COMMON_DIR)
                            ));
                        middlewares.push(
                            connect().use(
                                '/web/webjars/fk7263/webcert',
                                connect.static(__dirname + FK7263_DIR)
                            ));
                        middlewares.push(
                            connect().use(
                                '/web/webjars/ts-bas/webcert',
                                connect.static(__dirname + TSBAS_DIR)
                            ));
                        middlewares.push(
                            connect().use(
                                '/web/webjars/ts-diabetes/webcert',
                                connect.static(__dirname + TSDIABETES_DIR)
                            ));
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

        },

        concurrent: {
            options: {
                logConcurrentOutput: true
            },
            tasks: ['connect:server', 'watch']
        }
    });

    grunt.registerTask('default', ['ngtemplates:webcert', 'concat', 'ngAnnotate', 'uglify']);
    grunt.registerTask('lint', ['jshint', 'csslint']);
    grunt.registerTask('test', ['karma']);

    // frontend only dev ===============================================================================================
    grunt.registerTask('server', [ 'configureProxies:server', 'connect:server', 'watch' ]);
};
