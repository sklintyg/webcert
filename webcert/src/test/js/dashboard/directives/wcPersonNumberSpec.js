define([ 'angular', 'angularMocks', 'directives' ], function(angular, mocks) {
    'use strict';

	describe('wcPersonNumber', function() {
		beforeEach(mocks.module('wc.dashboard.directives'));

		var $scope;

		// Create a form to test the validation directive on.
		beforeEach(mocks.inject(function($compile, $rootScope) {
			$scope = $rootScope;
			$scope.model = {
				test : null
			};

			var el = angular
					.element('<form name="form"><input ng-model="model.test" name="test" wc-person-number></form>');
			$compile(el)($scope);
			$scope.$digest();
		}));

		// Pass

		it('should pass with a valid "personnummer" with format "yyyyMMdd-nnnn"', function() {
			$scope.form.test.$setViewValue('19121212-1212');

			expect($scope.model.test).toEqual('19121212-1212');
			expect($scope.form.$valid).toBeTruthy();
		});

		it('should pass with a valid "personnummer" with format "yyyyMMddnnnn"', function() {
			$scope.form.test.$setViewValue('191212121212');

			expect($scope.model.test).toEqual('19121212-1212');
			expect($scope.form.$valid).toBeTruthy();
		});

		it('should pass with a valid "personnummer" with format yyMMdd-nnnn', function() {
			$scope.form.test.$setViewValue('121212-1212');

			expect($scope.model.test).toEqual('20121212-1212');
			expect($scope.form.$valid).toBeTruthy();
		});

		it('should pass with a valid "personnummer" with format yyMMdd+nnnn', function() {
			$scope.form.test.$setViewValue('121212+1212');

			expect($scope.model.test).toEqual('19121212-1212');
			expect($scope.form.$valid).toBeTruthy();
		});

		it('should pass with a valid "personnummer" with format yyMMddnnnn', function() {
			$scope.form.test.$setViewValue('1212121212');

			expect($scope.model.test).toEqual('20121212-1212');
			expect($scope.form.$valid).toBeTruthy();
		});

		it('should pass with a valid "samordningsnummer" with format "yyyyMMnn-nnnn"', function() {
			$scope.form.test.$setViewValue('19121272-1219');

			expect($scope.model.test).toEqual('19121272-1219');
			expect($scope.form.$valid).toBeTruthy();
		});

		it('should pass with a valid "samordningsnummer" with format "yyyyMMnnnnnn"', function() {
			$scope.form.test.$setViewValue('191212721219');

			expect($scope.model.test).toEqual('19121272-1219');
			expect($scope.form.$valid).toBeTruthy();
		});

		it('should pass with a valid "samordningsnummer" with format "yyMMnn-nnnn"', function() {
			$scope.form.test.$setViewValue('121272-1219');

			expect($scope.model.test).toEqual('20121272-1219');
			expect($scope.form.$valid).toBeTruthy();
		});

		it('should pass with a valid "samordningsnummer" with format "yyMMnnnnnn"', function() {
			$scope.form.test.$setViewValue('1212721219');

			expect($scope.model.test).toEqual('20121272-1219');
			expect($scope.form.$valid).toBeTruthy();
		});

		// Fail

		it('should fail if "personnummer" has invalid check digit', function() {
			$scope.form.test.$setViewValue('121212-1213');

			expect($scope.model.test).toBeUndefined();
			expect($scope.form.$valid).toBeFalsy();
		});

		it('should fail if "personnummer" has invalid date', function() {
			$scope.form.test.$setViewValue('121212-1213');

			expect($scope.model.test).toBeUndefined();
			expect($scope.form.$valid).toBeFalsy();
		});

		it('should fail with if "personnummer" has invalid characters', function() {
			$scope.form.test.$setViewValue('121212.1213');

			expect($scope.model.test).toBeUndefined();
			expect($scope.form.$valid).toBeFalsy();
		});

		it('should fail if "samordningsnummer" has invalid check digit', function() {
			$scope.form.test.$setViewValue('121272-1213');

			expect($scope.model.test).toBeUndefined();
			expect($scope.form.$valid).toBeFalsy();
		});

		it('should fail with if "samordningsnummer" has invalid date', function() {
			$scope.form.test.$setViewValue('121292-1215');

			expect($scope.model.test).toBeUndefined();
			expect($scope.form.$valid).toBeFalsy();
		});

		it('should fail with if "samordningsnummer" has invalid characters', function() {
			$scope.form.test.$setViewValue('121272.1219');

			expect($scope.model.test).toBeUndefined();
			expect($scope.form.$valid).toBeFalsy();
		});
	});
});
