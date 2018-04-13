
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
                hasIntygsTyp: function() {return true;}, isLakare: function() {return true;} });
            $provide.value('common.dialogService', mockFactory.buildDialogService());
            var featureService = jasmine.createSpyObj('common.featureService', [ 'isFeatureActive' ]);
            featureService.features = {};
            $provide.value('common.featureService', featureService);
            $provide.value('common.authorityService', jasmine.createSpyObj('common.authorityService', [ 'isAuthorityActive' ]));
            $provide.value('common.messageService', {});
            $provide.value('common.statService', jasmine.createSpyObj('common.statService', [ 'refreshStat' ]));
        }]);

        inject(['$rootScope', '$compile', '$httpBackend', 'webcert.UtkastFilterModel', '$templateCache',
            function($rootScope, $compile, _$httpBackend_, _utkastFilterModel_, $templateCache) {

                $templateCache.put('/web/webjars/common/webcert/components/headers/wcHeader.partial.html', '');

                $httpBackend = _$httpBackend_;
                utkastFilterModel = _utkastFilterModel_;
                emptyFilter = _utkastFilterModel_.build();

                var tpl = angular.element(
                    '<wc-utkast-filter on-search="testFilter()" filter="filter"></wc-utkast-filter>'
                );
                $scope = $rootScope.$new();
                $scope.filter = emptyFilter;
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

        it('should call search function ', function() {

            setupHttp(200);

            spyOn($scope, 'testFilter');
            $scope.filter.status = 'DRAFT_COMPLETE';
            $scope.$digest();

            var button = element.find('#uc-filter-btn')[0];
            button.click();

            expect($scope.testFilter).toHaveBeenCalled();
        });
    });

    describe('reset filter button', function() {

        it('should reset filter parameters', function() {

            setupHttp(200);

            spyOn($scope, 'testFilter');
            element.isolateScope().resetFilter();
            $scope.$digest();
            expect($scope.testFilter).toHaveBeenCalled();
        });
    });
});
