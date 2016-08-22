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

describe('ViewCertCtrl', function() {
    'use strict';

    var UtkastProxy;
    var $state;
    var $stateParams;
    var dialogService;
    var fragaSvarCommonService;
    var $scope;
    var $q;
    var $rootScope;
    var $location;
    var $window;
    var modalMock;
    var newUrl = 'http://server/#/app/new';
    var currentUrl = '/app/new';
    var UserPreferencesService;
    var $controller;
    var qas = [{}];
    var userModel;

    function MockDeferreds($q){
        this.$q = $q;
        this.defs = [];
        this.lastPopped = null;
        this.getDeferred = function(){
            var def = this.$q.defer();
            this.defs.push(def);
            return def;
        };
        this.popDeferred = function(){
            this.lastPopped = this.defs.pop();
            return this.lastPopped;
        };
        this.getLast = function(){
            var index = 0;
            if(this.defs.length > 0 ){
                index = this.defs.length - 1;
            }
            var def = this.defs[index];
            return def;
        };
        this.resolveLast = function(value){
            this.getLast().resolve(value);
        };
        this.getLastPopped = function(){
            return this.lastPopped;
        };
    }

    var mockDeferreds;

    // Load the webcert module and mock away everything that is not necessary.
    beforeEach(angular.mock.module('webcert', function($provide) {
        dialogService = jasmine.createSpyObj('common.dialogService', [ 'showDialog' ]);
        modalMock = jasmine.createSpyObj('modal', [ 'close' ]);
        modalMock.result = {then:function(){}};
        dialogService.showDialog.and.callFake(function(){
            return modalMock;
        });
        $provide.value('common.dialogService', dialogService);

        fragaSvarCommonService  = jasmine.createSpyObj('common.fragaSvarCommonService', [ 'checkQAonlyDialog' ]);
        $provide.value('common.fragaSvarCommonService', fragaSvarCommonService);

        $provide.value('$window', {location:{href:currentUrl}});
        UtkastProxy = jasmine.createSpyObj('webcert.UtkastProxy', [ 'getUtkastType' ]);
        $provide.value('webcert.UtkastProxy', UtkastProxy);

        UserPreferencesService = jasmine.createSpyObj('UserPreferencesService', [ 'isSkipShowUnhandledDialogSet' ]);
        $provide.value('common.UserPreferencesService', UserPreferencesService);

        $stateParams = {qaOnly:false};
        $provide.value('$stateParams', $stateParams);
        $state = {current:{}};
        $provide.value('$state', $state);

        $provide.value('common.featureService', {
            features: {
                'HANTERA_FRAGOR': 'hanteraFragor'
            },
            isFeatureActive: function() { return true; }
        });
        $provide.value('common.UserModel', getTestUser({'LAKARE': {'name': 'Läkare', 'desc': 'Läkare'}}, 'UTHOPP'));
    }));

    // Get references to the object we want to test from the context.
    beforeEach(angular.mock.inject([ '$controller', '$rootScope', '$q', '$location', '$window', 'common.featureService', 'common.UserModel',
        function( _$controller_, _$rootScope_,_$q_, _$location_, _$window_, featureService, _userModel_) {

            $rootScope = _$rootScope_;
            $scope = $rootScope.$new();
            $q = _$q_;
            $location = _$location_;
            $window = _$window_;
            mockDeferreds = new MockDeferreds($q);

            userModel = _userModel_;
            spyOn($scope, '$broadcast');

            // setup the current location
            $location.url(currentUrl);

            userModel.roles = {LAKARE: 'Läkare'};
            userModel.isLakare = function(){return true;};
            userModel.isUthopp = function(){return false;};
            $state.current.data = 'fk7263';

            $controller = _$controller_;
            $controller('webcert.ViewCertCtrl',
                { $rootScope: $rootScope, $scope: $scope });

            spyOn(featureService, 'isFeatureActive').and.callThrough();
        }])
    );


    describe('#checkSpecialQALink', function() {
        beforeEach(function(){
            $controller('webcert.ViewCertCtrl',
                { $rootScope: $rootScope, $scope: $scope });

            // ----- arrange
            expect(UtkastProxy.getUtkastType).toHaveBeenCalled();

            // kick off the window change event
            $rootScope.$broadcast('$locationChangeStart', newUrl, currentUrl);

        });

        it('Check if the user used the special qa-link to get here', function(){

            $controller('webcert.ViewCertCtrl',
                { $rootScope: $rootScope, $scope: $scope });

            // ----- arrange
            expect(UtkastProxy.getUtkastType).toHaveBeenCalled();

            // spy on the defferd
            var def = mockDeferreds.getDeferred();
            spyOn($q, 'defer').and.returnValue(def);

            // kick off the window change event
            $rootScope.$broadcast('$locationChangeStart', newUrl, currentUrl);

            mockDeferreds.getLast().resolve(false);

            // ------ act
            // promises are resolved/dispatched only on next $digest cycle
            $rootScope.$apply();

            // ------ assert
            // dialog should be opened
            expect(fragaSvarCommonService.checkQAonlyDialog).toHaveBeenCalled();

        });

    });

    describe('#checkHasNoUnhandledMessages', function() {
        it('should check that a dialog is not opened, if there are no unhandled messages, and go to then newUrl', function(){

            // ----- arrange
            expect(UtkastProxy.getUtkastType).toHaveBeenCalled();

            // spy on the defferd
            var def = mockDeferreds.getDeferred();
            spyOn($q, 'defer').and.returnValue(def);

            // kick off the window change event
            $rootScope.$broadcast('$locationChangeStart', newUrl, currentUrl);

            var areThereUnhandledMessages = false;
            mockDeferreds.getLast().resolve(areThereUnhandledMessages);

            // ------ act
            // promises are resolved/dispatched only on next $digest cycle
            $rootScope.$apply();

            // ------ assert
            expect($scope.$broadcast).toHaveBeenCalledWith('hasUnhandledQasEvent', mockDeferreds.popDeferred());

        });

    });

    describe('#checkUserPreferencesService', function() {
        it('should check that cookie service isSkipShowUnhandledDialog is true', function(){
            // ----- arrange
            // spy on the defferd
            var def = mockDeferreds.getDeferred($q);
            spyOn($q, 'defer').and.returnValue(def);
            UserPreferencesService.isSkipShowUnhandledDialogSet.and.returnValue(true);

            // ------ act
            // kick off the window change event

            // promises are resolved/dispatched only on next $digest cycle
            $rootScope.$apply();

            $rootScope.$broadcast('$locationChangeStart', newUrl, currentUrl);

            mockDeferreds.getLast().resolve(false);


            // ------ assert
            expect(UtkastProxy.getUtkastType).toHaveBeenCalled();

            expect($scope.$broadcast).not.toHaveBeenCalledWith('hasUnhandledQasEvent', mockDeferreds.popDeferred());

        });

    });

    describe('#checkHasUnhandledMessages', function() {

        beforeEach(function(){
            // the below is run before each sub test as a means to fire a location change event and so opening the dialog.
            // ----- arrange
            // setup 3 deferreds, for some weird reason we have to do this
            mockDeferreds.getDeferred();
            mockDeferreds.getDeferred();
            mockDeferreds.getDeferred();

            spyOn($q, 'defer').and.callFake(function() {
                return mockDeferreds.popDeferred();
            });

            UserPreferencesService.isSkipShowUnhandledDialogSet.and.returnValue(false);

            // ------ act


            mockDeferreds.getLast().resolve(qas);

            // promises are resolved/dispatched only on next $digest cycle
            $rootScope.$apply();


            $rootScope.$broadcast('$locationChangeStart', newUrl, currentUrl);


            // ------ assert
            expect(UtkastProxy.getUtkastType).toHaveBeenCalled();
            expect(UserPreferencesService.isSkipShowUnhandledDialogSet).toHaveBeenCalled();
            expect($scope.$broadcast).toHaveBeenCalledWith('hasUnhandledQasEvent', mockDeferreds.getLastPopped());

            // dialog should be opened
            expect(dialogService.showDialog).toHaveBeenCalled();

        });

        it('just check that the dialog is opened and the url is the same', function(){
            // the url wont be changed until a button is pressed!!
            expect($window.location.href).toEqual(currentUrl);
        });

        xdescribe('#buttonHandle', function() {
            it('handle button click', function(){

                // This test is not QA only.
                fragaSvarCommonService.checkQAonlyDialog.and.callFake(function($scope, $event, newUrl, currentUrl, unbindLocationChange){
                    $window.location.href = newUrl;
                });

                // inside the handled button click, test that :
                var args = dialogService.showDialog.calls.mostRecent().args;
                var dialogOptions = args[0];
                // press the handled button
                dialogOptions.button1click();


                mockDeferreds.getLastPopped().resolve(qas);
                //resolve the deffereds, because the secound  is within deferred 1 we need to call apply on root, again to resolve further deferreds ..
                $rootScope.$apply();

                // markAnsweredAsHandledEvent is broadcast
                expect($scope.$broadcast).toHaveBeenCalledWith('markAnsweredAsHandledEvent', mockDeferreds.getLastPopped(), qas);

                // modal is closed
                expect(modalMock.close).toHaveBeenCalled();
                // the url wont be changed until a button is pressed!!
                expect($window.location.href).toEqual(newUrl);
            });
        });

        xdescribe('#buttonUnHandle', function() {
            it('un handled button click', function(){

                // This test is not QA only.
                fragaSvarCommonService.checkQAonlyDialog.and.callFake(function($scope, $event, newUrl, currentUrl, unbindLocationChange){
                    $window.location.href = newUrl;
                });

                // inside the handled button click, test that :
                var args = dialogService.showDialog.calls.mostRecent().args;
                var dialogOptions = args[0];
                // press the not handled button
                dialogOptions.button2click();
                // no action is taken, just close the dialog
                // modal is closed
                expect(modalMock.close).toHaveBeenCalled();
                // the url wont be changed until a button is pressed!!
                expect($window.location.href).toEqual(newUrl);
            });
        });

        xdescribe('#buttonBack', function() {
            it('back button click', function(){
                // inside the handled button click, test that :
                var args = dialogService.showDialog.calls.mostRecent().args;
                var dialogOptions = args[0];
                // press the back button
                dialogOptions.button3click();
                // no action is taken, just close the dialog
                // modal is closed
                expect(modalMock.close).toHaveBeenCalled();
                // the url should be the same no changes
                expect($window.location.href).toEqual(currentUrl);
            });
        });


    });

    function getTestUser (role, origin){
        return {
            'hsaId': 'eva',
            'namn': 'Eva Holgersson',
            'lakare': true,
            'forskrivarkod': '2481632',
            'authenticationScheme': 'urn:inera:webcert:fake',
            'vardgivare': [
                {
                    'id': 'vastmanland', 'namn': 'Landstinget Västmanland', 'vardenheter': [
                    {
                        'id': 'centrum-vast', 'namn': 'Vårdcentrum i Väst', 'arbetsplatskod': '0000000', 'mottagningar': [
                        {'id': 'akuten', 'namn': 'Akuten', 'arbetsplatskod': '0000000'},
                        {'id': 'dialys', 'namn': 'Dialys', 'arbetsplatskod': '0000000'}
                    ]
                    }
                ]
                },
                {
                    'id': 'ostergotland', 'namn': 'Landstinget Östergötland', 'vardenheter': [
                    {
                        'id': 'linkoping',
                        'namn': 'Linköpings Universitetssjukhus',
                        'arbetsplatskod': '0000000',
                        'mottagningar': [
                            {'id': 'lkpg-akuten', 'namn': 'Akuten', 'arbetsplatskod': '0000000'},
                            {'id': 'lkpg-ogon', 'namn': 'Ögonmottagningen', 'arbetsplatskod': '0000000'}
                        ]
                    }
                ]
                }
            ],
            'specialiseringar': ['Kirurgi', 'Oftalmologi'],
            'titel': 'Leg. Ögonläkare',
            'legitimeradeYrkesgrupper': ['Läkare'],
            'valdVardenhet': {
                'id': 'centrum-vast', 'namn': 'Vårdcentrum i Väst', 'arbetsplatskod': '0000000', 'mottagningar': [
                    {'id': 'akuten', 'namn': 'Akuten', 'arbetsplatskod': '0000000'},
                    {'id': 'dialys', 'namn': 'Dialys', 'arbetsplatskod': '0000000'}
                ]
            },
            'valdVardgivare': {
                'id': 'vastmanland', 'namn': 'Landstinget Västmanland', 'vardenheter': [
                    {
                        'id': 'centrum-vast', 'namn': 'Vårdcentrum i Väst', 'arbetsplatskod': '0000000', 'mottagningar': [
                        {'id': 'akuten', 'namn': 'Akuten', 'arbetsplatskod': '0000000'},
                        {'id': 'dialys', 'namn': 'Dialys', 'arbetsplatskod': '0000000'}
                    ]
                    }
                ]
            },
            'roles': role,
            'features': ['hanteraFragor', 'hanteraFragor.fk7263'],
            'totaltAntalVardenheter': 6,
            'origin': origin
        };

    }

});
