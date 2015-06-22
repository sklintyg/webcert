describe('UnsignedCertCtrlSpec', function() {
    'use strict';

    var $controller;
    var $cookieStore;
    var $scope;
    var $location;
    var $httpBackend;
    var controller;
    var mockResponse;
    var mockFactory;
    var intygNotifyService;

    beforeEach(function() {

        module('webcertTest');
        module('webcert', ['$provide', function($provide) {

            var webcertTest = angular.injector(['webcertTest', 'ng']);
            mockFactory = webcertTest.get('mockFactory');

            var statService = jasmine.createSpyObj('common.statService', [ 'refreshStat' ]);
            $provide.value('common.statService', statService);

            var User = mockFactory.buildUserMinimal();
            $provide.value('common.User', User);
            $provide.value('common.dialogService', mockFactory.buildDialogService());
            intygNotifyService = jasmine.createSpyObj('common.intygNotifyService', [ 'onForwardedChange', 'forwardIntyg' ]);
            $provide.value('common.intygNotifyService', intygNotifyService);
            $provide.value('common.featureService', jasmine.createSpyObj('common.featureService', [ 'isFeatureActive' ]));
            $provide.value('common.messageService', {});
            $provide.value('common.CertificateService', {});
            $provide.value('common.DateUtilsService', { addStrictDateParser: function(){} });
        }]);

        inject(['$rootScope', '$location', '$httpBackend', '$controller', '$cookieStore', 'mockResponse',
            function($rootScope, _$location_, _$httpBackend_, _$controller_, _$cookieStore_, _mockResponse_) {
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
                $cookieStore = _$cookieStore_;
                mockResponse = _mockResponse_;
                controller = $controller('webcert.UnsignedCertCtrl', { $scope: $scope });
            }]);
    });

    describe('UnsignedCertCtrl listing and filtering of QAs ', function() {

        it('should get and fill currentList with 1 entry when unsignedCertFilter is not set', function() {
            $cookieStore.remove('unsignedCertFilter');
            $httpBackend.expectGET('/api/utkast/lakare/').respond(200, {});
            $httpBackend.expectGET('/api/utkast/').respond(200, mockResponse.utkastList);
            $httpBackend.expectGET('/api/utkast?enhetsId=enhet1&pageSize=10&startFrom=0').respond(200, mockResponse.utkastList);
            $scope.filterDrafts();
            $httpBackend.flush();
            expect($scope.widgetState.currentList.length).toBe(1);
        });

        it('should try to view an intyg when user clicks Visa on a intyg in the list', function() {
            spyOn($location, 'path').and.callThrough();
            $scope.openIntyg(mockResponse.utkast);
            expect($location.path).toHaveBeenCalledWith('/' + mockResponse.utkast.intygType.toLowerCase() +
                '/edit/' + mockResponse.utkast.intygId);
        });

        it('should fetch PAGE_SIZE more results if user clicks fetch more button', function() {
            $scope.filterForm.lastFilterQuery.startFrom = 0;
            $scope.fetchMore();
            expect($scope.filterForm.lastFilterQuery.startFrom).toBe($scope.filterForm.lastFilterQuery.pageSize);
        });

        it('should reset filter parameters and delete saved cookie if user clicks reset', function() {
            $scope.filterForm = {};
            $scope.resetFilter();
            expect($scope.filterForm).toEqual(mockResponse.utkastDefaultFilterFormData);
        });
    });

    describe('UnsignedCertCtrl QA forwarding', function() {

        it('should change forwarded state on a utkast when clicking a forward checkbox', function() {
            $scope.onForwardedChange(mockResponse.utkast);
            expect(intygNotifyService.onForwardedChange).toHaveBeenCalled();
        });

        it('should open external mail client when user wants to forward a utkast', function() {
            $scope.openMailDialog(mockResponse.utkast);
            expect(intygNotifyService.forwardIntyg).toHaveBeenCalled();
        });
    });

});
