describe('ChoosePatientCtrl', function() {
    'use strict';

    var $controller;
    var PatientProxy;
    var $scope;
    var $location;
    var controller;
    var basePerson;

    beforeEach(function(){

        module('webcert', function($provide) {
            var personIdValidator = {};// jasmine.createSpyObj('common.PersonIdValidatorService', ['validateSamordningsnummer']);

            personIdValidator.validateSamordningsnummer = function(number) {
                if (number === '195401875760') {
                    return number;
                }

                return null;
            };

            personIdValidator.validResult = function(result) {
                return result !== undefined && result !== null;
            };

            $provide.value('common.PersonIdValidatorService', personIdValidator);

            PatientProxy = { getPatient: function() {} };
            $provide.value('common.PatientProxy', PatientProxy);
            $provide.value('common.PatientModel', {});
        });

        inject(function($rootScope, _$location_, _$controller_) {
            $scope = $rootScope.$new();
            $location = _$location_;
            $controller = _$controller_;
            controller = $controller('webcert.ChoosePatientCtrl', { $scope: $scope });
        });
    });

    describe('lookupPatient', function() {

        var successResult = function(personnummer, onSuccess, onNotFound, onError) {
            basePerson.personnummer = personnummer;
            onSuccess(basePerson);
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

            spyOn($location, 'path').and.callThrough();
        });

        it('should redirect to choose-cert-type without errors on a correct personnummer accepted by the PU-tjanst', function() {

            spyOn(PatientProxy, 'getPatient').and.callFake(successResult);

            $scope.personnummer = '191212121212';
            $scope.lookupPatient();

            expect(PatientProxy.getPatient).toHaveBeenCalled();
            expect($location.path).toHaveBeenCalledWith('/create/choose-cert-type/index');
            expect($scope.widgetState.waiting).toBe(false);
            expect($scope.widgetState.errorid).toBe(undefined);
        });

        it('should redirect to choose-cert-type without errors on a correct samordningsnummer accepted by the PU-tjanst', function() {

            spyOn(PatientProxy, 'getPatient').and.callFake(successResult);

            $scope.personnummer = '195401875769';
            $scope.lookupPatient();

            expect(PatientProxy.getPatient).toHaveBeenCalled();
            expect($location.path).toHaveBeenCalledWith('/create/choose-cert-type/index');
            expect($scope.widgetState.waiting).toBe(false);
            expect($scope.widgetState.errorid).toBe(undefined);
        });

        it('should call onNotFound on a personnummer that does not exist', function() {

            spyOn(PatientProxy, 'getPatient').and.callFake(function(personnummer, onSuccess, onNotFound, onError) {
                onNotFound();
            });

            $scope.personnummer = '191212121213';
            $scope.lookupPatient();

            expect(PatientProxy.getPatient).toHaveBeenCalled();
            expect($location.path).not.toHaveBeenCalled();
            expect($scope.widgetState.waiting).toBe(false);
            expect($scope.widgetState.errorid).toBe('error.pu.namenotfound');
        });

        it('should call onNotFound on samordningsnummer that does not exist', function() {

            spyOn(PatientProxy, 'getPatient').and.callFake(function(personnummer, onSuccess, onNotFound, onError) {
                onNotFound();
            });

            $scope.personnummer = '195401875760';
            $scope.lookupPatient();

            expect(PatientProxy.getPatient).toHaveBeenCalled();
            expect($location.path).not.toHaveBeenCalled();
            expect($scope.widgetState.waiting).toBe(false);
            expect($scope.widgetState.errorid).toBe('error.pu.samordningsnummernotfound');
        });

        it('should redirect to edit-patient-name if the pu-service isnt available', function() {
            spyOn(PatientProxy, 'getPatient').and.callFake(function(personnummer, onSuccess, onNotFound, onError) {
                onError();
            });

            $scope.personnummer = '195401875760';
            $scope.lookupPatient();

            expect(PatientProxy.getPatient).toHaveBeenCalled();
            expect($location.path).toHaveBeenCalledWith('/create/edit-patient-name/errorOccured');
            expect($scope.widgetState.waiting).toBe(false);
            expect($scope.widgetState.errorid).toBeUndefined();
        });

        it('should present an error if the request was successful but the response does not contain personnummer', function() {
            spyOn(PatientProxy, 'getPatient').and.callFake(function(personnummer, onSuccess, onNotFound, onError) {
                basePerson.personnummer = '';
                onSuccess(basePerson);
            });

            $scope.personnummer = '195401875760';
            $scope.lookupPatient();

            expect(PatientProxy.getPatient).toHaveBeenCalled();
            expect($location.path).not.toHaveBeenCalled();
            expect($scope.widgetState.waiting).toBe(false);
            expect($scope.widgetState.errorid).toBe('error.pu.nopersonnummer');
        });

        it('should present an error if the request was successful but the response does not contain fornamn or efternamn', function() {
            spyOn(PatientProxy, 'getPatient').and.callFake(function(personnummer, onSuccess, onNotFound, onError) {
                basePerson.personnummer = personnummer;
                basePerson.fornamn = '';
                basePerson.efternamn = '';
                onSuccess(basePerson);
            });

            $scope.personnummer = '195401875760';
            $scope.lookupPatient();

            expect(PatientProxy.getPatient).toHaveBeenCalled();
            expect($location.path).not.toHaveBeenCalled();
            expect($scope.widgetState.waiting).toBe(false);
            expect($scope.widgetState.errorid).toBe('error.pu.noname');
        });
    });
});
