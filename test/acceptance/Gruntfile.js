/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

/* global module, Promise*/
require('path');
var request = require('request');

module.exports = function(grunt) {
  'use strict';

  grunt.loadNpmTasks('grunt-protractor-runner');
  grunt.loadNpmTasks('grunt-protractor-webdriver');
  grunt.loadNpmTasks('grunt-env');
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks("grunt-jsbeautifier");
  grunt.loadNpmTasks('grunt-force-task');
  grunt.loadNpmTasks('grunt-contrib-clean');

  grunt.initConfig({
    clean: {
      options: {
        force: true
      },
      json: ['node_modules/common-testtools/cucumber-html-report/*.json'],
      png: ['node_modules/common-testtools/cucumber-html-report/*.png']
    },
    jshint: {
      acc: [
        'features/steps/*.js',
        'features/steps/**/*.js',
        'jshint --exclude ../webcertTestTools/node_modules',
        '../webcertTestTools/**/*.js',
        '../webcertTestTools/*.js'
      ],
      options: {
        force: false,
        jshintrc: '.jshintrc'
      }
    },
    jsbeautifier: {
      verify: {
        src: [
          'features/steps/*.js',
          'features/steps/**/*.js',
          '../webcertTestTools/**/*.js',
          '../webcertTestTools/*.js'
        ],
        options: {
          mode: 'VERIFY_ONLY',
          config: '../.jsbeautifyrc'
        }
      },
      modify: {
        src: [
          'features/steps/*.js',
          'features/steps/**/*.js',
          '../webcertTestTools/**/*.js',
          '../webcertTestTools/*.js'
        ],
        options: {
          mode: 'VERIFY_AND_WRITE',
          config: '../.jsbeautifyrc'
        }
      }
    },
    env: grunt.file.readJSON('../webcertTestTools/envConfig.json'),
    protractor: {
      options: {
        //configFile: './protractor.cli.conf.js', // Target-specific config file
        keepAlive: true, // If false, the grunt process stops when the test fails.
        noColor: false, // If true, protractor will not use colors in its output.
        args: {
          // Arguments passed to the command
        }
      },
      acc: {
        options: {
          configFile: 'protractor-conf.js',
          keepAlive: true,
          args: {
            params: {
              // En fil där cucumber fyller i alla externa länkar som hittas.
              externalLinksFile: 'external_links.txt'
            }
          }
        },
        partialReportPattern: 'node_modules/common-testtools/cucumber-html-report/acc_results.*.json',
        reportFile: 'node_modules/common-testtools/cucumber-html-report/acc_results.json'
      }
    },

    protractor_webdriver: { // jshint ignore:line
      options: {
        keepAlive: true,
        // Task-specific options go here.
      }
    }

  });

  grunt.registerTask('genReport', 'Genererar rapport från testkörningen', function() {
    var files = grunt.file.expand(grunt.config.get('protractor.acc.partialReportPattern'));
    var firstWrite = true;
    var combinedReport = '[';
    if (files.length >= 1) {
      files.forEach(function(item, index) {
        var fileText = grunt.file.read(item);

        // Ibland är delrapporter tomma eller innehaller endast en []. 
        // Hoppa over dessa.
        if (fileText !== '[]' && fileText !== '') {
          if (firstWrite) {
            firstWrite = false;
          } else {
            combinedReport += ',';
          }

          combinedReport += fileText.substring(1, (fileText.length - 2));
        } else {
          grunt.log.subhead(fileText);
        }

        // Ta bort fil efter att den processats
        grunt.file.delete(item);
      });
      combinedReport += ']';
      grunt.file.write(grunt.config.get('protractor.acc.reportFile'), combinedReport);

    }

    // Gör en kontroll om vi fick något fel i tidigare task. Denna hantering finns pga att vi tvingades köra med 'force' i 
    // task 'protractor:acc' då ett eventuellt fail i testfall i protractor-steget hindrar den här tasken från att köra 
    // och vi vill ha en rapport oavsett om ett testfall har gått fel eller inte. 
    if (grunt.fail.errorcount > 0) {
      grunt.log.subhead('Tidigare eller nuvarande task innehöll ett felmeddelande (errorcount =' + grunt.fail.errorcount + ')');
      grunt.fail.warn('Hittade ett fel i task force:protractor:acc');
    }
  });

  // Run: 'grunt acc:ip20'
  grunt.task.registerTask('acc', 'Task för att köra acceptanstest', function(environment) {

    //Miljö
    if (!environment) {
      var defaultEnv = 'ip30';
      grunt.log.subhead('Ingen miljö vald, använder ' + defaultEnv + '-miljön..');
      environment = defaultEnv;
    }

    process.env.environmentName = environment.toUpperCase();

    if (grunt.option('gridnodeinstances') > 1) {
      grunt.config.set('protractor.acc.options.args.capabilities.shardTestFiles', true);
      grunt.config.set('protractor.acc.options.args.capabilities.maxInstances', grunt.option('gridnodeinstances'));
    }

    if (!grunt.option('local-selenium')) {
      grunt.config.set('protractor.acc.options.args.seleniumAddress', 'http://selenium1.nordicmedtest.se:4444/wd/hub');
    } else {
      grunt.config.set('protractor.acc.options.args.directConnect', true);
    }

    //Använder tag expressions
    //https://github.com/cucumber/cucumber/tree/master/tag-expressions

    let tags = 'not @waitingForFix and not @notReady and not @NOTREADY and not @WAITINGFORFIX and not @BACKOFFICE and not @LegacyFK7263';

    if (grunt.option('tags')) {
      tags = grunt.option('tags');
    }

    grunt.log.subhead('Taggar:' + tags);
    grunt.config.set('protractor.acc.options.args.cucumberOpts.tags', tags);

    //Tasks
    var tasks = [];
    if (!grunt.option('CI')) {
      tasks = ['jshint:acc', 'jsbeautifier:modify'];
    }

    if (grunt.file.exists(grunt.config.get('protractor.acc.options.args.params.externalLinksFile'))) {
      grunt.file.delete(grunt.config.get('protractor.acc.options.args.params.externalLinksFile'));
    }
    tasks.push('clean');
    tasks.push('env:' + environment);
    tasks.push('protractor_webdriver');
    tasks.push('force:protractor:acc');
    tasks.push('genReport');
    grunt.task.run(tasks);
  });
};
