describe('UnsignedCertCtrlSpec', function() {
    'use strict';

    var $controller;
    var $cookies;
    var $scope;
    var $location;
    var $timeout;
    var $httpBackend;
    var controller;
    var mockResponse;
    var mockFactory;
    var utkastNotifyService;

    beforeEach(function() {

        module('webcertTest');
        module('webcert', ['$provide', function($provide) {

            var webcertTest = angular.injector(['webcertTest', 'ng']);
            mockFactory = webcertTest.get('mockFactory');

            var statService = jasmine.createSpyObj('common.statService', [ 'refreshStat' ]);
            $provide.value('common.statService', statService);

            var User = mockFactory.buildUserMinimal();
            $provide.value('common.User', User);
            $provide.value('common.UserModel', { userContext: { authenticationScheme: null }, getActiveFeatures: function() {},
                hasIntygsTyp: function() {return true;} });
            $provide.value('common.dialogService', mockFactory.buildDialogService());
            utkastNotifyService = jasmine.createSpyObj('common.UtkastNotifyService', [ 'onNotifyChange', 'notifyUtkast' ]);
            $provide.value('common.UtkastNotifyService', utkastNotifyService);
            $provide.value('common.featureService', jasmine.createSpyObj('common.featureService', [ 'isFeatureActive' ]));
            $provide.value('common.messageService', {});
            $provide.value('common.DateUtilsService', { addStrictDateParser: function(){} });
        }]);

        inject(['$rootScope', '$location', '$httpBackend', '$controller', '$cookies', '$timeout', 'mockResponse',
            function($rootScope, _$location_, _$httpBackend_, _$controller_, _$cookies_, _$timeout_, _mockResponse_) {
                $scope = $rootScope.$new();
                $scope.filterFormElement = {
                    'filter-changedate-from': { $error: {}},
                    'filter-changedate-to': { $error: {}}
                };
                $scope.viewState = {
                    activeErrorMessageKey: null,
                    inlineErrorMessageKey: null
                };

                $location = _$location_;
                $httpBackend = _$httpBackend_;
                $controller = _$controller_;
                $cookies = _$cookies_;
                $timeout = _$timeout_;
                mockResponse = _mockResponse_;

                $httpBackend.expectGET('/api/utkast/lakare/').respond(200, {});
                $httpBackend.expectGET('/api/utkast/').respond(200, mockResponse.utkastList);
                controller = $controller('webcert.UnsignedCertCtrl', { $scope: $scope });
                $httpBackend.flush();
                $timeout.flush();
            }]);
    });

    describe('ejSigneradeUtkast controller startup', function() {

        it('should load utkast list on valid response', function() {
            $httpBackend.expectGET('/api/utkast/lakare/').respond(200, {});
            $httpBackend.expectGET('/api/utkast/').respond(200, mockResponse.utkastList);
            $controller('webcert.UnsignedCertCtrl', { $scope: $scope });
            $httpBackend.flush();
            $timeout.flush();
        });

        it('should update error message if loading fails', function() {
            $httpBackend.expectGET('/api/utkast/lakare/').respond(500);
            $httpBackend.expectGET('/api/utkast/').respond(500);
            $controller('webcert.UnsignedCertCtrl', { $scope: $scope });
            $httpBackend.flush();
            expect($scope.widgetState.activeErrorMessageKey).not.toBeNull();
        });
    });

    describe('ejSigneradeUtkast controller filter', function() {

        it('should update error message if loading fails', function() {

            // first run a filter to save a cookie
            $httpBackend.expectGET('/api/utkast?enhetsId=enhet1&pageSize=10&startFrom=0').respond(200, mockResponse.utkastList);
            $scope.filterDrafts();
            $httpBackend.flush();

            // then update filter filter savedBy info
            var filter = $cookies.getObject('unsignedCertFilter');
            filter.filter.savedBy = {
                name: 'Visa alla',
                hsaId: 'hsaIdFromHell'
            };
            $cookies.putObject('unsignedCertFilter', filter);

            // finally make sure successful call runs through savedBy functions
            $httpBackend.expectGET('/api/utkast/lakare/').respond(200, {});
            $httpBackend.expectGET('/api/utkast/').respond(200, mockResponse.utkastList);
            $controller('webcert.UnsignedCertCtrl', { $scope: $scope });
            $httpBackend.flush();
            $timeout.flush();
            expect($scope.widgetState.currentList.length).toBe(1);
        });

        it('should update error message if loading fails', function() {
            $httpBackend.expectGET('/api/utkast/lakare/').respond(500);
            $httpBackend.expectGET('/api/utkast/').respond(500);
            $controller('webcert.UnsignedCertCtrl', { $scope: $scope });
            $httpBackend.flush();
            expect($scope.widgetState.activeErrorMessageKey).not.toBeNull();
        });

        it('should get and fill currentList with 1 entry when unsignedCertFilter is not set', function() {
            $cookies.remove('unsignedCertFilter');
            $httpBackend.expectGET('/api/utkast?enhetsId=enhet1&pageSize=10&startFrom=0').respond(200, mockResponse.utkastList);
            $scope.filterDrafts();
            $httpBackend.flush();
            expect($scope.widgetState.currentList.length).toBe(1);
        });

        it('should get utkast list based on date filter', function() {
            $cookies.remove('unsignedCertFilter');
            $scope.filterForm.lastFilterQuery.filter.savedTo = '2015-01-10';
            $scope.filterForm.lastFilterQuery.filter.savedFrom = '2015-10-10';
            $httpBackend.expectGET('/api/utkast?enhetsId=enhet1&pageSize=10&savedFrom=2015-10-10&savedTo=2015-01-11&startFrom=0').respond(200, {results: [], totalCount: 0});
            $scope.filterDrafts();
            $httpBackend.flush();
            expect($scope.widgetState.currentList.length).toBe(0);
        });

        it('should handle error if list could not be fetched from server', function() {
            $cookies.remove('unsignedCertFilter');
            $httpBackend.expectGET('/api/utkast?enhetsId=enhet1&pageSize=10&startFrom=0').respond(500);
            $scope.filterDrafts();
            $httpBackend.flush();
            expect($scope.widgetState.activeErrorMessageKey).not.toBeNull();
        });
    });

    describe('reset filter button', function() {
        it('should reset filter parameters and delete saved cookie', function() {
            $scope.filterForm = {};
            $scope.resetFilter();
            expect($scope.filterForm).toEqual(mockResponse.utkastDefaultFilterFormData);
        });
    });

    describe('visa intyg button', function() {
        it('should try to view an intyg', function() {
            spyOn($location, 'path').and.callThrough();
            $scope.openIntyg(mockResponse.utkast);
            expect($location.path).toHaveBeenCalledWith('/' + mockResponse.utkast.intygType.toLowerCase() +
                '/edit/' + mockResponse.utkast.intygId);
        });
    });

    describe('fetch more button', function() {
        it('should fetch PAGE_SIZE more results if user clicks', function() {
            $scope.filterForm.lastFilterQuery.startFrom = 0;
            $httpBackend.expectGET('/api/utkast?enhetsId=enhet1&pageSize=10&startFrom=10').respond(200, {results:[]});
            $scope.fetchMore();
            $httpBackend.flush();
            expect($scope.filterForm.lastFilterQuery.startFrom).toBe($scope.filterForm.lastFilterQuery.pageSize);
        });

        it('should update error message if fetch failed', function() {
            $scope.filterForm.lastFilterQuery.startFrom = 0;
            $httpBackend.expectGET('/api/utkast?enhetsId=enhet1&pageSize=10&startFrom=10').respond(500);
            $scope.fetchMore();
            $httpBackend.flush();
            expect($scope.filterForm.lastFilterQuery.startFrom).toBe($scope.filterForm.lastFilterQuery.pageSize);
        });
    });

    describe('UnsignedCertCtrl QA forwarding', function() {

        it('should change forwarded state on a utkast when clicking a forward checkbox', function() {
            $scope.onNotifyChange(mockResponse.utkast);
            expect(utkastNotifyService.onNotifyChange).toHaveBeenCalled();
        });

        it('should open external mail client when user wants to forward a utkast', function() {
            $scope.openMailDialog(mockResponse.utkast);
            expect(utkastNotifyService.notifyUtkast).toHaveBeenCalled();
        });
    });

});
