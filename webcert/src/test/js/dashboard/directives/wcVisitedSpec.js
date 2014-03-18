'use strict';

describe('wcVisited', function () {
    beforeEach(module('wc.dashboard.directives'));

    var $scope;
    var element;

    // Create a form to test the directive on.
    beforeEach(inject(function ($compile, $rootScope) {
        $scope = $rootScope;
        $scope.model = { test : "test" };

        element = angular.element('<form name="form"><input wc-visited type="text" ng-model="model.test" name="test"></form>');
        element = $compile(element)($scope);
        $scope.$digest();
    }));

    // Pass

    it('should pass if element is focused and marked visited', function () {
        element = element.find('input');
        browserTrigger(element, 'blur');
        expect(element.hasClass('wc-visited')).toBeTruthy();
        expect($scope.form.test.$visited).toBeTruthy();
    });

});
