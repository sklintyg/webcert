/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

describe('wcVisited', function() {
    'use strict';


    var $scope;
    var element;

    beforeEach(angular.mock.module('webcert', ['$provide', function($provide) {
        //$provide.value('webcert.TermsState', {termsAccepted:true, transitioning:false, reset: function(){}});
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
