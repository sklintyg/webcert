/* global module */
module.exports = function(grunt) {
	'use strict';

	grunt.loadNpmTasks('grunt-contrib-csslint');
	grunt.loadNpmTasks('grunt-contrib-jshint');

	grunt.initConfig({

		csslint : {
			dev : {
				options : {
					csslintrc : '../src/main/resources/.csslintrc',
					force : true
				},
				src : [ 'src/main/webapp/**/*.css' ]
			},
			build : {
				options : {
					csslintrc : '../src/main/resources/.csslintrc',
					force : true,
					formatters : [ {
						id : 'checkstyle-xml',
						dest : 'target/checkstyle-csslint-result.xml'
					} ],
					absoluteFilePathsForFormatters : true
				},
				src : [ 'src/main/webapp/**/*.css' ]
			}
		},

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
					reporterOutput : 'target/checkstyle-jshint-result.xml'
				},
				src : [ 'Gruntfile.js', 'src/main/webapp/js/**/*.js', 'src/test/js/**/*.js' ]
			}
		}
	});

	grunt.registerTask('dev', [ 'csslint:dev', 'jshint:dev' ]);
	grunt.registerTask('default', [ 'csslint:build', 'jshint:build' ]);
};
