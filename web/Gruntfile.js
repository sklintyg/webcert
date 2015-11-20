/*global __dirname:false*/
/* global module */
/*function config(name) {
    return require('./tasks/' + name);
}*/

require('path');


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
    grunt.loadNpmTasks('grunt-sass');
    grunt.loadNpmTasks('grunt-sass-lint');

    var SRC_DIR = 'src/main/webapp/app/';
    var TEST_DIR = 'src/test/js/';

    var webcert = grunt.file.readJSON(SRC_DIR + 'app-deps.json').map(function(file) {
        return file.replace(/\/app\//g, SRC_DIR);
    });

    webcert = [SRC_DIR + 'app.js'].concat(webcert);

    var COMMON_SRC_DIR = '/../../common/web/src/main/resources/META-INF/resources/webjars/common/webcert';
    var COMMON_DEST_DIR = '/../../common/web/target/classes/META-INF/resources/webjars/common/webcert';
    var CSS_COMMON_SRC_DIR = '/../../common/web/src/main/resources/META-INF/resources/webjars/common/css';
    var CSS_COMMON_DEST_DIR = '/../../common/web/target/classes/META-INF/resources/webjars/common/css';
    var TSBAS_SRC_DIR = '/../../intygstyper/ts-bas/src/main/resources/META-INF/resources/webjars/ts-bas/webcert';
    var TSBAS_DEST_DIR = '/../../intygstyper/ts-bas/target/classes/META-INF/resources/webjars/ts-bas/webcert'; 
    var TSDIABETES_SRC_DIR = '/../../intygstyper/ts-diabetes/src/main/resources/META-INF/resources/webjars/ts-diabetes/webcert';
    var TSDIABETES_DEST_DIR = '/../../intygstyper/ts-diabetes/target/classes/META-INF/resources/webjars/ts-diabetes/webcert'; 
    var FK7263_SRC_DIR = '/../../intygstyper/fk7263/src/main/resources/META-INF/resources/webjars/fk7263/webcert';
    var FK7263_DEST_DIR = '/../../intygstyper/fk7263/target/classes/META-INF/resources/webjars/fk7263/webcert'; 

    grunt.initConfig({

        sasslint: {
            options: {
                //configFile: 'config/.sass-lint.yml' //For now we use the .sass-lint.yml that is packaged with sass-lint
            },
            target: [SRC_DIR + '**/*.scss']
        },

        concat: {
            webcert: {
                src: webcert,
                dest: SRC_DIR + 'app.min.js'
            }
        },

        jshint: {
			options: {
				jshintrc: 'target/build-tools/jshint/.jshintrc',
				force: false,
				ignores: ['**/templates.js', '**/*.min.js', '**/vendor/*.js']
			},
            webcert: {
                src: ['Gruntfile.js', SRC_DIR + '**/*.js', TEST_DIR + '**/*.js']
            }
        },

        karma: {
            webcert: {
                configFile: 'src/main/resources/karma.conf.ci.js',
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
            //Watch for file changes in these scss-files that
            //we want to reprocess so that we are able to reload
            //them for the browser in dev mode,
            'css': {
                files: [
                    __dirname + FK7263_SRC_DIR + '/css/*.scss',
                    __dirname + TSBAS_SRC_DIR + '/css/*.scss',
                    __dirname + TSDIABETES_SRC_DIR + '/css/*.scss',
                    __dirname + CSS_COMMON_SRC_DIR + '/*.scss',
                    __dirname + COMMON_SRC_DIR + '/css/*.scss'
                ],
                tasks: ['sass:dev']
            },
            //js: {
            //    files: ['public/src/js/**/*.js'],
            //    tasks: ['jshint', 'uglify', 'injector', 'wiredep']
            //},
            html: {
                files: [
                        __dirname + '/src/main/webapp/**/*.html',
                        __dirname + COMMON_SRC_DIR + '/**/*.html',
                        __dirname + FK7263_SRC_DIR + '/**/*.html',
                        __dirname + TSBAS_SRC_DIR + '/**/*.html',
                        __dirname + TSDIABETES_SRC_DIR + '/**/*.html'
                ],
                tasks: ['ngtemplates']
            }
        },

        // Compiles Sass to CSS
        sass: {
            options: {
                update: true
            },
            dev: {
                //Compile all
                files: [{
                    expand: true,
                    cwd: __dirname + FK7263_SRC_DIR + '/css/',
                    src: ['*.scss'],
                    dest: __dirname + FK7263_DEST_DIR + '/css',
                    ext: '.css'
                },
                {
                    expand: true,
                    cwd: __dirname + TSBAS_SRC_DIR + '/css/',
                    src: ['*.scss'],
                    dest: __dirname + TSBAS_DEST_DIR + '/css',
                    ext: '.css'
                },
                {
                    expand: true,
                    cwd: __dirname + TSDIABETES_SRC_DIR + '/css/',
                    src: ['*.scss'],
                    dest: __dirname + TSDIABETES_DEST_DIR + '/css',
                    ext: '.css'
                },
                {
                    expand: true,
                    cwd: __dirname + COMMON_SRC_DIR + '/css/',
                    src: ['*.scss'],
                    dest: __dirname + COMMON_DEST_DIR + '/css',
                    ext: '.css'
                },
                {
                    expand: true,
                    cwd: __dirname + CSS_COMMON_SRC_DIR,
                    src: ['*.scss'],
                    dest: __dirname + CSS_COMMON_DEST_DIR,
                    ext: '.css'
                }]
            }, 
            dist: {
                //What we do when we build a distribution. Don't include intygstyper here or common.
                //This place is reserved for any scss files within this very project
            }
        },

        ngtemplates : {
            webcert: {
                cwd: __dirname + '/src/main/webapp',
                src: ['app/views/**/**.html', 'app/partials/**/**.html'],
                dest: __dirname + '/src/main/webapp/app/templates.js',
                options: {
                    module: 'webcert',
                    url: function(url) {
                        return '/' + url.replace('../', '/');
                    }
                }
            },
            common: {
                cwd: __dirname + COMMON_SRC_DIR,
                src: ['**/*.html'],
                dest: __dirname + COMMON_SRC_DIR + '/templates.js',
                options:{
                    module: 'common',
                    url: function(url) {
                        return '/web/webjars/common/webcert/' + url;
                    }
                }
            },
            tsdiabetes: {
                cwd: __dirname + TSDIABETES_SRC_DIR,
                src: ['**/*.html'],
                dest: __dirname + TSDIABETES_SRC_DIR + '/templates.js',
                options:{
                    module: 'ts-diabetes',
                    url: function(url) {
                        return '/web/webjars/ts-diabetes/webcert/' + url;
                    }
                }
            },
            tsbas: {
                cwd: __dirname + TSBAS_SRC_DIR,
                src: ['**/*.html'],
                dest: __dirname + TSBAS_SRC_DIR + '/templates.js',
                options:{
                    module: 'ts-bas',
                    url: function(url) {
                        return '/web/webjars/ts-bas/webcert/' + url;
                    }
                }
            },
            fk7263: {
                cwd: __dirname + FK7263_SRC_DIR,
                src: ['**/*.html'],
                dest: __dirname + FK7263_SRC_DIR + '/templates.js',
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
                    middleware: function(connect/*, options*/) {
                        var proxy = require('grunt-connect-proxy/lib/utils').proxyRequest;
                        var middlewares = [];
                        middlewares.push(
                            connect().use(
                                '/web',
                                connect.static(__dirname + '/src/main/webapp') // jshint ignore:line
                            ));
                        middlewares.push(
                            connect().use(
                                '/app',
                                connect.static(__dirname + '/src/main/webapp/app') // jshint ignore:line
                            ));
                        middlewares.push(
                            connect().use(
                                '/web/webjars/common/webcert',
                                connect.static(__dirname + COMMON_SRC_DIR) // jshint ignore:line
                            ));
                        middlewares.push(
                            connect().use(
                                '/web/webjars/common/webcert/css',
                                connect.static(__dirname + COMMON_DEST_DIR + '/css') // jshint ignore:line
                            ));
                        middlewares.push(
                            connect().use(
                                '/web/webjars/common/css',
                                connect.static(__dirname + CSS_COMMON_DEST_DIR) // jshint ignore:line
                            ));
                        middlewares.push(
                            connect().use(
                                '/web/webjars/fk7263/webcert',
                                connect.static(__dirname + FK7263_SRC_DIR) // jshint ignore:line
                            ));
                        middlewares.push(
                            connect().use(
                                '/web/webjars/fk7263/webcert/css',
                                connect.static(__dirname + FK7263_DEST_DIR + '/css') //jshint ignore:line
                            ));
                        middlewares.push(
                            connect().use(
                                '/web/webjars/ts-bas/webcert',
                                connect.static(__dirname + TSBAS_SRC_DIR) // jshint ignore:line
                            ));
                        middlewares.push(
                            connect().use(
                                '/web/webjars/ts-bas/webcert/css',
                                connect.static(__dirname + TSBAS_DEST_DIR + '/css') //jshint ignore:line
                            ));
                        middlewares.push(
                            connect().use(
                                '/web/webjars/ts-diabetes/webcert',
                                connect.static(__dirname + TSDIABETES_SRC_DIR) // jshint ignore:line
                            ));
                        middlewares.push(
                            connect().use(
                                '/web/webjars/ts-diabetes/webcert/css',
                                connect.static(__dirname + TSDIABETES_DEST_DIR + '/css') //jshint ignore:line
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

    /*When we build the distribution we don't want to run sass:dev since that would rebuild the sass of projects
    * that webcert depends on*/
    grunt.registerTask('default', ['ngtemplates:webcert', 'concat', 'ngAnnotate', 'uglify', 'sass:dist']);
    grunt.registerTask('lint', ['jshint', 'csslint']);
    grunt.registerTask('test', ['karma']);
    // frontend only dev ===============================================================================================
    grunt.registerTask('server', [ 'configureProxies:server', 'connect:server', 'watch' ]);
};
