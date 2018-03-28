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

describe('EjSigneradeUtkastCtrlSpec', function() {
    'use strict';

    var $controller;
    var $scope;
    var $timeout;
    var $httpBackend;
    var mockResponse;
    var mockFactory;
    var utkastNotifyService;
    var utkastFilterModel;
    var emptyFilter;

    beforeEach(function() {

        module('htmlTemplates');
        module('webcertTest');
        module('webcert', ['$provide', function($provide) {

            var webcertTest = angular.injector(['webcertTest', 'ng']);
            mockFactory = webcertTest.get('mockFactory');

            var statService = jasmine.createSpyObj('common.statService', [ 'refreshStat' ]);
            $provide.value('common.statService', statService);

            var User = mockFactory.buildUserMinimal();
            $provide.value('common.User', User);
            $provide.value('common.UserModel', { userContext: { authenticationScheme: null }, getActiveFeatures: function() {},
                hasIntygsTyp: function() {return true;}, isLakare: function() {return true;} });
            $provide.value('common.dialogService', mockFactory.buildDialogService());
            utkastNotifyService = jasmine.createSpyObj('common.UtkastNotifyService', [ 'onNotifyChange', 'notifyUtkast' ]);
            $provide.value('common.UtkastNotifyService', utkastNotifyService);
            var featureService = jasmine.createSpyObj('common.featureService', [ 'isFeatureActive' ]);
            featureService.features = {};
            $provide.value('common.featureService', featureService);
            $provide.value('common.authorityService', jasmine.createSpyObj('common.authorityService', [ 'isAuthorityActive' ]));
            $provide.value('common.messageService', {});
            $provide.value('common.DateUtilsService', { addStrictDateParser: function(){} });
        }]);

        inject(['$rootScope', '$httpBackend', '$controller', '$timeout', 'mockResponse', 'webcert.UtkastFilterModel', '$templateCache',
            function($rootScope, _$httpBackend_, _$controller_, _$timeout_, _mockResponse_, _utkastFilterModel_, $templateCache) {

                $templateCache.put('/web/webjars/common/webcert/components/headers/wcHeader.partial.html', '');

                $scope = $rootScope.$new();
                $scope.filterFormElement = {
                    'filter-changedate-from': { $error: {}},
                    'filter-changedate-to': { $error: {}}
                };
                $scope.viewState = {
                    activeErrorMessageKey: null,
                    inlineErrorMessageKey: null
                };

                $httpBackend = _$httpBackend_;
                $controller = _$controller_;
                $timeout = _$timeout_;
                mockResponse = _mockResponse_;
                utkastFilterModel = _utkastFilterModel_;
                emptyFilter = _utkastFilterModel_.build();

                $httpBackend.expectGET('/api/utkast/').respond(200, mockResponse.utkastList);
                $controller('webcert.EjSigneradeUtkastCtrl', { $scope: $scope });
                $httpBackend.flush();
                $timeout.flush();
            }]);
    });

    describe('ejSigneradeUtkast controller startup', function() {

        it('should load utkast list on valid response', function() {
            $httpBackend.expectGET('/api/utkast/').respond(200, mockResponse.utkastList);
            $controller('webcert.EjSigneradeUtkastCtrl', { $scope: $scope });
            $httpBackend.flush();
            $timeout.flush();
        });

        it('should update error message if loading fails', function() {
            $httpBackend.expectGET('/api/utkast/').respond(500);
            $controller('webcert.EjSigneradeUtkastCtrl', { $scope: $scope });
            $httpBackend.flush();
            expect($scope.widgetState.activeErrorMessageKey).not.toBeNull();
        });
    });

    describe('ejSigneradeUtkast controller filter', function() {

        it('should update error message if loading fails', function() {
            $httpBackend.expectGET('/api/utkast/').respond(500);
            $controller('webcert.EjSigneradeUtkastCtrl', { $scope: $scope });
            $httpBackend.flush();
            expect($scope.widgetState.activeErrorMessageKey).not.toBeNull();
        });

        it('should get and fill currentList with 1 entry when unsignedCertFilter is not set', function() {
            $httpBackend.expectGET('/api/utkast?pageSize=10&startFrom=0').respond(200, mockResponse.utkastList);
            $scope.filterDrafts(emptyFilter);
            $httpBackend.flush();
            expect($scope.widgetState.currentList.length).toBe(1);
        });

        it('should get utkast list based on date filter', function() {
            $httpBackend.expectGET('/api/utkast?pageSize=10&savedFrom=2015-10-10&savedTo=2015-01-11&startFrom=0').respond(200, {results: [], totalCount: 0});
            var filter = utkastFilterModel.build();
            filter.savedTo = '2015-01-10';
            filter.savedFrom = '2015-10-10';
            $scope.filterDrafts(filter);
            $httpBackend.flush();
            expect($scope.widgetState.currentList.length).toBe(0);
        });

        it('should handle error if list could not be fetched from server', function() {
            $httpBackend.expectGET('/api/utkast?pageSize=10&startFrom=0').respond(500);
            $scope.filterDrafts(emptyFilter);
            $httpBackend.flush();
            expect($scope.widgetState.activeErrorMessageKey).not.toBeNull();
        });
    });

    describe('fetch more button', function() {
        it('should fetch PAGE_SIZE more results if user clicks', function() {
            $httpBackend.expectGET('/api/utkast?pageSize=10&startFrom=10').respond(200, {results:[]});
            $scope.fetchMore();
            $httpBackend.flush();
            expect($scope.widgetState.startFrom).toBe(10);
            expect($scope.widgetState.activeErrorMessageKey).toBeNull();
        });

        it('should update error message if fetch failed', function() {
            $httpBackend.expectGET('/api/utkast?pageSize=10&startFrom=10').respond(500);
            $scope.fetchMore();
            $httpBackend.flush();
            expect($scope.widgetState.startFrom).toBe(10);
            expect($scope.widgetState.activeErrorMessageKey).not.toBeNull();
        });
    });

});
