/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
    var DEST_DIR = 'target/webapp/app/';

    var webcert = grunt.file.expand({cwd: SRC_DIR}, ['**/*.js', '!**/*.spec.js', '!**/*.test.js', '!**/app.js']).sort();
    grunt.file.write(DEST_DIR + 'app-deps.json', JSON.stringify(webcert.
        map(function(file) {
            return '/app/' + file;
        }).
        concat('/app/templates.js'), null, 4));
    webcert = [SRC_DIR + 'app.js', DEST_DIR + 'templates.js'].concat(webcert.map(function(file) {
        return SRC_DIR + file;
    }));

    var modules = {
        'common':      { base: 'common/web' },
        'fk7263':      { base: 'intygstyper/fk7263' },
        'ts-bas':      { base: 'intygstyper/ts-bas' },
        'ts-diabetes': { base: 'intygstyper/ts-diabetes' },
        'luse':        { base: 'intygstyper/fk/sjukersattning', angularModule:'luse' },
        'lisu':        { base: 'intygstyper/fk/sjukpenning-utokad', angularModule:'lisu' },
        'luae_na':        { base: 'intygstyper/fk/aktivitetsersattning-na', angularModule:'luae_na' }
    };
    Object.keys(modules).forEach(function(moduleName) {
        var module = modules[moduleName];
        module.name = moduleName;
        if (!module.angularModule) {
            module.angularModule = moduleName;
        }
        module.src =
            '/../../' + module.base + '/src/main/resources/META-INF/resources/webjars/' + moduleName + '/webcert';
        module.dest = '/../../' + module.base + '/target/classes/META-INF/resources/webjars/' + moduleName + '/webcert';
    });

    var CSS_COMMON_SRC_DIR = '/../../common/web/src/main/resources/META-INF/resources/webjars/common/css';
    var CSS_COMMON_DEST_DIR = '/../../common/web/target/classes/META-INF/resources/webjars/common/css';

    function buildListForAllModules(callback) {
        var list = [];
        Object.keys(modules).forEach(function(moduleName) {
            var module = modules[moduleName];
            list.push(callback(module));
        });
        return list;
    }

    function buildObjectForAllModules(callback) {
        var obj = {};
        Object.keys(modules).forEach(function(moduleName) {
            var module = modules[moduleName];
            obj[module.name] = callback(module);
        });
        return obj;
    }

    grunt.registerTask('generateModuleDeps', function() {
        // Generate webcert app-deps.json
        var files = grunt.file.expand({cwd: SRC_DIR},
            ['**/*.js', '!**/*.spec.js', '!**/*.test.js', '!**/app.js']).sort();
        grunt.file.write(DEST_DIR + 'app-deps.json', JSON.stringify(files.
            map(function(file) {
                return '/app/' + file;
            }).
            concat('/app/templates.js'), null, 4));

        // Generate all module-deps.json
        Object.keys(modules).forEach(function(moduleName) {
            var module = modules[moduleName];
            var files = grunt.file.expand({cwd: __dirname + module.src},
                ['**/*.js', '!**/*.spec.js', '!**/*.test.js', '!**/module.js']).sort();
            grunt.file.write(__dirname + module.dest + '/module-deps.json', JSON.stringify(files.
                map(function(file) {
                    return '/web/webjars/' + module.name + '/webcert/' + file;
                }).
                concat('/web/webjars/' + module.name + '/webcert/templates.js'), null, 4));
        });
    });

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
                dest: DEST_DIR + 'app.min.js'
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
            ci: {
                configFile: 'src/main/resources/karma.conf.ci.js',
                reporters: ['mocha']
            },
            watch: {
                configFile: 'src/main/resources/karma.conf.ci.js',
                reporters: ['mocha'],
                autoWatch: true,
                singleRun: false
            }
        },

        ngAnnotate: {
            options: {
                singleQuotes: true
            },
            webcert: {
                src: DEST_DIR + 'app.min.js',
                dest: DEST_DIR + 'app.min.js'
            }
        },

        uglify: {
            options: {
                mangle: false
            },
            webcert: {
                src: DEST_DIR + 'app.min.js',
                dest: DEST_DIR + 'app.min.js'
            }
        },

        //ngtemplates: config('ngtemplates'),

        // server ======================================================================================================


        watch: {
            //Watch for file changes in these scss-files that
            //we want to reprocess so that we are able to reload
            //them for the browser in dev mode,
            'css': {
                files: buildListForAllModules(function(module) {
                    return __dirname + module.src + '/css/*.scss';
                }).concat([__dirname + CSS_COMMON_SRC_DIR + '/*.scss']),
                tasks: ['sass:dev']
            },
            js: {
                files: buildListForAllModules(function(module) {
                    return module.src.substring(1) + '/**/*.js';
                }).concat(SRC_DIR + '/**/*.js'),
                tasks: ['generateModuleDeps'],
                options: {
                    event: ['added', 'deleted']
                }
            },
            html: {
                files: buildListForAllModules(function(module) {
                    return __dirname + module.src + '/**/*.html';
                }).concat([__dirname + '/src/main/webapp/**/*.html']),
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
                files: buildListForAllModules(function(module) {
                    return {
                        expand: true,
                        cwd: __dirname + module.src + '/css/',
                        src: ['*.scss'],
                        dest: __dirname + module.dest + '/css',
                        ext: '.css'
                    };
                }).concat([
                    {
                        expand: true,
                        cwd: __dirname + CSS_COMMON_SRC_DIR,
                        src: ['*.scss'],
                        dest: __dirname + CSS_COMMON_DEST_DIR,
                        ext: '.css'
                    }
                ])
            },
            dist: {
                //What we do when we build a distribution. Don't include intygstyper here or common.
                //This place is reserved for any scss files within this very project
            }
        },

        ngtemplates: grunt.util._.extend(buildObjectForAllModules(function(module) {
            return {
                cwd: __dirname + module.src,
                src: ['**/*.html'],
                dest: __dirname + module.dest + '/templates.js',
                options: {
                    module: module.angularModule,
                    url: function(url) {
                        return '/web/webjars/' + module.name + '/webcert/' + url;
                    }
                }
            };
        }), { webcert: {
            cwd: __dirname + '/src/main/webapp',
            src: ['app/views/**/**.html', 'app/partials/**/**.html'],
            dest: __dirname + '/target/webapp/app/templates.js',
            options: {
                module: 'webcert',
                url: function(url) {
                    return '/' + url.replace('../', '/');
                }
            }
        }}),

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
                                '/app/app-deps.js',
                                connect.static(__dirname + DEST_DIR + '/app-deps.js') // jshint ignore:line
                            ));
                        middlewares.push(
                            connect().use(
                                '/web/webjars/common/css',
                                connect.static(__dirname + CSS_COMMON_DEST_DIR) // jshint ignore:line
                            ));
                        Object.keys(modules).forEach(function(moduleName) {
                            var module = modules[moduleName];
                            middlewares.push(
                                connect().use(
                                        '/web/webjars/' + module.name + '/webcert',
                                    connect.static(__dirname + module.src) //jshint ignore:line
                                ));
                            middlewares.push(
                                connect().use(
                                        '/web/webjars/' + module.name + '/webcert/templates.js',
                                    connect.static(__dirname + module.dest + '/templates.js') //jshint ignore:line
                                ));
                            middlewares.push(
                                connect().use(
                                        '/web/webjars/' + module.name + '/webcert/module-deps.json',
                                    connect.static(__dirname + module.dest + '/module-deps.json') //jshint ignore:line
                                ));
                            middlewares.push(
                                connect().use(
                                        '/web/webjars/' + module.name + '/webcert/css',
                                    connect.static(__dirname + module.dest + '/css')//jshint ignore:line
                                ));
                        });
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
    grunt.registerTask('test', ['karma:ci']);
    grunt.registerTask('test:watch', ['karma:watch']);
    // frontend only dev ===============================================================================================
    grunt.registerTask('server', [ 'configureProxies:server', 'connect:server', 'watch' ]);
};
