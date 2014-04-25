/* global module */
module.exports = function(grunt) {
	'use strict';

	grunt.loadNpmTasks('grunt-contrib-jshint');

	grunt.initConfig({

		jshint : {
			dev : {
				options : {
					jshintrc : '../src/main/resources/.jshintrc',
					force : true
				},
				src : [ 'Gruntfile.js', 'src/main/webapp/js/**/*.js', 'src/test/js/**/*.js' ]
			},
			build : {
				options : {
					jshintrc : '../src/main/resources/.jshintrc',
					force : true,
					reporter : 'checkstyle',
					reporterOutput : 'target/jshint/checkstyle-result.xml'
				},
				src : [ 'Gruntfile.js', 'src/main/webapp/js/**/*.js', 'src/test/js/**/*.js' ]
			}
		}
	});

	grunt.registerTask('dev', [ 'jshint:dev' ]);
	grunt.registerTask('default', [ 'jshint:build' ]);
};
