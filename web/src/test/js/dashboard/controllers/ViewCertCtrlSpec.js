describe('ViewCertCtrl', function() {
    'use strict';

    var manageCertificateSpy;
    var $httpBackend;
    var dialogService;
    var $scope;
    var $qaCtrlScope;
    var $q;
    var $rootScope;
    var $location;
    var $window;
    var modalMock;
    var newUrl = '/url/a';
    var currentUrl = '/url/b';

    // Load the webcert module and mock away everything that is not necessary.
    beforeEach(angular.mock.module('webcert', function($provide) {
        dialogService = jasmine.createSpyObj('common.dialogService', [ 'showDialog' ]);
        modalMock = jasmine.createSpyObj('modal', [ 'close' ]);
        dialogService.showDialog.andCallFake(function(){
            return modalMock;
        });
        $provide.value('common.dialogService', dialogService);
        $provide.value('$window', {location:{href:currentUrl}});
        manageCertificateSpy = jasmine.createSpyObj('webcert.ManageCertificate', [ 'getCertType' ]);
        $provide.value('webcert.ManageCertificate', manageCertificateSpy);
    }));

    // Get references to the object we want to test from the context.
    beforeEach(angular.mock.inject([ '$controller', '$rootScope', '$q', '$httpBackend', '$location', '$window',
        function( $controller, _$rootScope_, _$q_,_$httpBackend_, _$location_, _$window_) {

            $rootScope = _$rootScope_;
            $scope = $rootScope.$new();
            $qaCtrlScope = $rootScope.$new();
            $q = _$q_;
            $httpBackend = _$httpBackend_;
            $location = _$location_;
            $window = _$window_;
            $controller('webcert.ViewCertCtrl',
                { $rootScope: $rootScope, $scope: $scope });

            spyOn($scope, '$broadcast');
        }])
    );

    describe('#checkHasNoUnhandledMessages', function() {
        it('should check that a dialog is not opened, if there are no unhandled messages, and go to then newUrl', function(){

            expect(manageCertificateSpy.getCertType).toHaveBeenCalled();

            // spy on the defferd
            var deferred = $q.defer();
            spyOn($q, 'defer').andReturn(deferred);

            // kick off the window change event
            $rootScope.$broadcast('$locationChangeStart', newUrl, currentUrl);

            expect($scope.$broadcast).toHaveBeenCalledWith('hasUnhandledQasEvent', deferred);

            var areThereUnhandledMessages = false;
            deferred.resolve(areThereUnhandledMessages);

            // promises are resolved/dispatched only on next $digest cycle
            $rootScope.$apply();

            expect($window.location.href).toEqual(newUrl);

        });

    });

    describe('#checkHasUnhandledMessages', function() {

        var newUrl = '/url/a';
        var currentUrl = '/url/b';

        beforeEach(function(){
            // the below is run before each sub test as a means to fire a location change event and so opening the dialog.
            // ----- arrange
            // spy on the defferd
            var deferred = $q.defer();
            //noinspection JSUnresolvedFunction
            spyOn($q, 'defer').andReturn(deferred);

            // ------ act
            // kick off the window change event

            $rootScope.$broadcast('$locationChangeStart', newUrl, currentUrl);

            var areThereUnhandledMessages = true;
            deferred.resolve(areThereUnhandledMessages);

            // promises are resolved/dispatched only on next $digest cycle
            $rootScope.$apply();

            // ------ assert
            expect(manageCertificateSpy.getCertType).toHaveBeenCalled();

            expect($scope.$broadcast).toHaveBeenCalledWith('hasUnhandledQasEvent', deferred);

            // dialog should be opened
            expect(dialogService.showDialog).toHaveBeenCalled();

        });

        it('just check that the dialog is opened and the url is the same', function(){
            // the url wont be changed until a button is pressed!!
            expect($window.location.href).toEqual(currentUrl);
        });

        describe('#buttonHandle', function() {
            it('handle button click', function(){
                // inside the handled button click, test that :
                var args = dialogService.showDialog.mostRecentCall.args;
                var dialogOptions = args[1];
                // press the handled button
                dialogOptions.button1click();
                // markAllAsHandledEvent is broadcast
                expect($scope.$broadcast).toHaveBeenCalledWith('markAllAsHandledEvent');
                // modal is closed
                expect(modalMock.close).toHaveBeenCalled();
                // the url wont be changed until a button is pressed!!
                expect($window.location.href).toEqual(newUrl);
            });
        });

        describe('#buttonUnHandle', function() {
            it('un handled button click', function(){
                // inside the handled button click, test that :
                var args = dialogService.showDialog.mostRecentCall.args;
                var dialogOptions = args[1];
                // press the handled button
                dialogOptions.button2click();
                // no action is taken, just close the dialog
                // modal is closed
                expect(modalMock.close).toHaveBeenCalled();
                // the url wont be changed until a button is pressed!!
                expect($window.location.href).toEqual(newUrl);
            });
        });

        describe('#buttonBack', function() {
            it('back button click', function(){
                // inside the handled button click, test that :
                var args = dialogService.showDialog.mostRecentCall.args;
                var dialogOptions = args[1];
                // press the handled button
                dialogOptions.button3click();
                // no action is taken, just close the dialog
                // modal is closed
                expect(modalMock.close).toHaveBeenCalled();
                // the url should be the same no changes
                expect($window.location.href).toEqual(currentUrl);
            });
        });


    });

});