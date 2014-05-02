define(['angular', 'angularMocks', 'angularScenario', 'directives'], function(angular, mocks) {
    'use strict';

    describe('wcVisited', function() {
        beforeEach(mocks.module('wc.dashboard.directives'));

        var $scope;
        var element;

        // Create a form to test the directive on.
        beforeEach(mocks.inject(function($compile, $rootScope) {
            $scope = $rootScope;
            $scope.model = { test: 'test' };

            element =
                angular.element('<form name="form"><input wc-visited type="text" ng-model="model.test" name="test"></form>');
            element = $compile(element)($scope);
            $scope.$digest();
        }));

        // Pass

        it('should pass if element is focused and marked visited', function() {
            element = element.find('input');
            browserTrigger(element, 'blur'); // jshint ignore:line
            expect(element.hasClass('wc-visited')).toBeTruthy();
            expect($scope.form.test.$visited).toBeTruthy();
        });
    });
});
