// Generated on 2014-09-07 using generator-angular 0.9.7
'use strict';

// # Globbing
// for performance reasons we're only matching one level down:
// 'test/spec/{,*/}*.js'
// use this if you want to recursively match all subfolders:
// 'test/spec/**/*.js'

module.exports = function (grunt) {

  // Load grunt tasks automatically
  require('load-grunt-tasks')(grunt);

  // Time how long tasks take. Can help when optimizing build times
  require('time-grunt')(grunt);

  // // Define the configuration for all the tasks
  grunt.initConfig({
    // Protractor settings
    protractor: {
      options: {
        configFile: "node_modules/protractor/referenceConf.js", // Default config file
        keepAlive: true, // If false, the grunt process stops when the test fails.
        noColor: false, // If true, protractor will not use colors in its output.
        args: {
          // Arguments passed to the command
        }
      },
      your_target: {   // Grunt requires at least one target to run so you can simply put 'all: {}' here too.
        options: {
          configFile: "protractor-conf.js", // Target-specific config file
          args: {} // Target-specific arguments
        }
      },
    }
  });

  grunt.option('cucumberOpts', '@dev');

  grunt.registerTask('e2e', [
    'selenium_start',
    // 'clean:server',
    // 'concurrent:server',
    // 'connect:test',
    'protractor',
    'selenium_stop'
  ]);
};
