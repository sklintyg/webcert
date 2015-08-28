describe('wcVisited', function() {
    'use strict';


    var $scope;
    var element;

    beforeEach(angular.mock.module('webcert', ['$provide', function($provide) {
        $provide.value('webcert.TermsState', {termsAccepted:true, transitioning:false, reset: function(){}});
    }]));

    // Create a form to test the directive on.
    beforeEach(angular.mock.inject(function($compile, $rootScope) {
        $scope = $rootScope;
        $scope.model = { test: 'test' };
        element = angular.element('<form name="form"><input wc-visited type="text" ng-model="model.test" name="test"></form>');
        element = $compile(element)($scope);
        $scope.$digest();
    }));

    // Pass

    it('should pass if element is focused and marked visited', function() {
        element = element.find('input');
        element.triggerHandler('blur');
        expect(element.hasClass('wc-visited')).toBeTruthy();
        expect($scope.form.test.$visited).toBeTruthy();
    });
});
