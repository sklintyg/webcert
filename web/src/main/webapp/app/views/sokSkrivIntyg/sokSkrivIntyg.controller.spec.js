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

describe('ChoosePatientCtrl', function() {
    'use strict';

    var $controller;
    var $rootScope;
    var $scope;
    var $q;
    var $state;
    var basePerson;
    var SokSkrivValjUtkastService;

    beforeEach(function() {

        //module('htmlTemplates');
        module('webcert', function($provide) {
            /*            var PatientProxy = {
             getPatient: function(personnummer, onSuccess, onNotFound, onError) {
             }
             };
             $provide.value('common.PatientProxy', PatientProxy);
             $provide.value('common.PersonIdValidatorService', {});*/
            SokSkrivValjUtkastService = {
                lookupPatient: function() {
                }
            };
            $provide.value('$state', jasmine.createSpyObj('$state', ['go']));
            $provide.value('webcert.SokSkrivValjUtkastService', SokSkrivValjUtkastService);
        });

        inject(function(_$rootScope_, _$controller_, _$state_, _$q_) {
            $rootScope = _$rootScope_;
            $scope = $rootScope.$new();
            $q = _$q_;
            $state = _$state_;
            $controller = _$controller_;
            $controller('webcert.ChoosePatientCtrl', {$scope: $scope});
        });
    });

    describe('loadPatient', function() {

        var successResult = function(personnummer) {
            basePerson.personnummer = personnummer;
            var promise = $q.defer();
            promise.resolve(basePerson);
            return promise.promise;
        };

        var notFoundResult = function(personnummer) {
            var promise = $q.defer();
            promise.reject('error.pu.namenotfound');
            return promise.promise;
        };
        var notFoundSamordningResult = function(personnummer) {
            var promise = $q.defer();
            promise.reject('error.pu.samordningsnummernotfound');
            return promise.promise;
        };
        var noConnectionResult = function(personnummer) {
            var promise = $q.defer();
            promise.reject('error.pu_problem');
            return promise.promise;
        };

        beforeEach(function() {

            basePerson = {
                personnummer: '',
                sekretessmarkering: false,
                fornamn: 'Test',
                mellannamn: 'Svensson',
                efternamn: 'Testsson',
                postadress: 'Storgatan 23',
                postnummer: '12345',
                postort: 'Staden'
            };
        });

        it('should redirect to choose-intyg-type without errors on a correct personnummer accepted by the PU-tjanst',
            function() {

                spyOn(SokSkrivValjUtkastService, 'lookupPatient').and.callFake(successResult);

                $scope.personnummer = '191212121212';
                $scope.$digest();
                $scope.loadPatient();
                $scope.$digest();

                expect(SokSkrivValjUtkastService.lookupPatient).toHaveBeenCalled();
                expect($state.go).toHaveBeenCalledWith('webcert.create-choose-certtype-index',
                    {'patientId': '191212121212'});
                expect($scope.viewState.loading).toBe(false);
                expect($scope.viewState.errorid).toBe(null);
            });

        it('should redirect to choose-intyg-type without errors on a correct samordningsnummer accepted by the PU-tjanst',
            function() {

                spyOn(SokSkrivValjUtkastService, 'lookupPatient').and.callFake(successResult);

                $scope.personnummer = '195401875769';
                $scope.$digest();
                $scope.loadPatient();
                $scope.$digest();

                expect(SokSkrivValjUtkastService.lookupPatient).toHaveBeenCalled();
                expect($state.go).toHaveBeenCalledWith('webcert.create-choose-certtype-index',
                    {'patientId': '195401875769'});
                expect($scope.viewState.loading).toBe(false);
                expect($scope.viewState.errorid).toBe(null);
            });

        it('should call onNotFound on a personnummer that does not exist', function() {

            spyOn(SokSkrivValjUtkastService, 'lookupPatient').and.callFake(notFoundResult);

            $scope.personnummer = '191212121213';
            $scope.$digest();
            $scope.loadPatient();
            $scope.$digest();

            expect(SokSkrivValjUtkastService.lookupPatient).toHaveBeenCalled();
            expect($state.go).not.toHaveBeenCalled();
            expect($scope.viewState.loading).toBe(false);
            expect($scope.viewState.errorid).toBe('error.pu.namenotfound');
        });

        it('should call onNotFound on samordningsnummer that does not exist', function() {

            spyOn(SokSkrivValjUtkastService, 'lookupPatient').and.callFake(notFoundSamordningResult);

            $scope.personnummer = '195401875760';
            $scope.$digest();
            $scope.loadPatient();
            $scope.$digest();

            expect(SokSkrivValjUtkastService.lookupPatient).toHaveBeenCalled();
            expect($state.go).not.toHaveBeenCalled();
            expect($scope.viewState.loading).toBe(false);
            expect($scope.viewState.errorid).toBe('error.pu.samordningsnummernotfound');
        });

        it('should redirect to edit-patient-name if the pu-service isnt available', function() {
            spyOn(SokSkrivValjUtkastService, 'lookupPatient').and.callFake(noConnectionResult);

            $scope.personnummer = '195401875760';
            $scope.$digest();
            $scope.loadPatient();
            $scope.$digest();

            expect(SokSkrivValjUtkastService.lookupPatient).toHaveBeenCalled();
            expect($scope.viewState.loading).toBe(false);
            expect($scope.viewState.errorid).toBe('error.pu_problem');
        });

    });
});
