
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

describe('wcUtkastFilterSpec', function() {
    'use strict';

    var $scope;
    var $httpBackend;
    var $cookies;
    var mockFactory;
    var element;
    var utkastFilterModel;
    var emptyFilter;

    beforeEach(angular.mock.module('htmlTemplates'));
    beforeEach(function() {
        module('webcertTest');
        module('webcert', ['$provide', '$stateProvider', function($provide) {

            var webcertTest = angular.injector(['webcertTest', 'ng']);
            mockFactory = webcertTest.get('mockFactory');

            var User = mockFactory.buildUserMinimal();
            $provide.value('common.User', User);
            $provide.value('common.UserModel', { userContext: { authenticationScheme: null }, getActiveFeatures: function() {},
                hasIntygsTyp: function() {return true;} });
            $provide.value('common.dialogService', mockFactory.buildDialogService());
            $provide.value('common.featureService', jasmine.createSpyObj('common.featureService', [ 'isFeatureActive' ]));
            $provide.value('common.authorityService', {});
            $provide.value('common.messageService', {});
            $provide.value('common.statService', jasmine.createSpyObj('common.statService', [ 'refreshStat' ]));
        }]);

        inject(['$rootScope', '$compile', '$cookies', '$httpBackend', 'webcert.UtkastFilterModel',
            function($rootScope, $compile, _$cookies_, _$httpBackend_, _utkastFilterModel_) {
                $cookies = _$cookies_;
                $httpBackend = _$httpBackend_;
                utkastFilterModel = _utkastFilterModel_;
                emptyFilter = _utkastFilterModel_.build();

                $cookies.remove('unsignedCertFilter');

                var tpl = angular.element(
                    '<wc-utkast-filter on-search="testFilter(filter)"></wc-utkast-filter>'
                );
                $scope = $rootScope.$new();
                $scope.testFilter = function(filter) {
                };
                element = $compile(tpl)($scope);
            }
        ]);
    });

    function setupHttp(status) {
        $httpBackend.expectGET('/api/utkast/lakare/').respond(status, []);
        $scope.$digest();
        $httpBackend.flush();
    }

    describe('startup', function() {
        it('should show error message if loading /api/utkast/lakare/ fails', function() {
            setupHttp(500);

            expect(element.isolateScope().widgetState.savedByList.length).toBe(1);
            expect(element.isolateScope().widgetState.savedByList[0].hsaId).toBe(undefined);
            expect(element.isolateScope().widgetState.savedByList[0].name).toBe('<Kunde inte hämta lista>');
        });
    });

    describe('search button', function() {

        it('should call search function and save cookie', function() {

            setupHttp(200);

            spyOn($scope, 'testFilter');
            spyOn($cookies, 'putObject');

            var completeYes = $(element).find('#completeYes').eq(0);
            completeYes.click();

            var button = $(element).find('#uc-filter-btn').eq(0);
            button.click();

            var response = utkastFilterModel.build();
            response.complete = true;
            expect($cookies.putObject).toHaveBeenCalledWith('unsignedCertFilter',response);
            expect($scope.testFilter).toHaveBeenCalledWith(response);
        });
    });

    describe('reset filter button', function() {

        it('should reset filter parameters and delete saved cookie', function() {

            setupHttp(200);

            spyOn($scope, 'testFilter');
            spyOn($cookies, 'remove');
            element.isolateScope().resetFilter();
            $scope.$digest();
            expect($cookies.remove).toHaveBeenCalledWith('unsignedCertFilter');
            expect($scope.testFilter).toHaveBeenCalledWith(emptyFilter);
        });
    });
});
