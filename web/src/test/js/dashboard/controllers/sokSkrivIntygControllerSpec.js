describe('ChoosePatientCtrl', function() {
    'use strict';

    var $controller;
    var PatientProxy;
    var $scope;
    var $location;
    var controller;

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

            PatientProxy = {
                getPatient: function(personnummer, onSuccess, onNotFound, onError) {
                    if(personnummer === '191212121212') {
                        onSuccess ({
                            status: 'FOUND',
                            person: {
                                personnummer: personnummer,
                                sekretessmarkering: false,
                                fornamn: 'Test',
                                mellannamn: 'Svensson',
                                efternamn: 'Testsson',
                                postadress: 'Storgatan 23',
                                postnummer: '12345',
                                postort: 'Staden'
                            }
                        });
                    } else if(personnummer === '195401875769') {
                        onSuccess({
                            status: 'FOUND',
                            person: {
                                personnummer: personnummer,
                                sekretessmarkering: false,
                                fornamn: 'Test',
                                mellannamn: 'Svensson',
                                efternamn: 'Testsson',
                                postadress: 'Storgatan 23',
                                postnummer: '12345',
                                postort: 'Staden'
                            }
                        });
                    } else {
                        onNotFound({
                            status: 'NOT_FOUND',
                            person: {
                                personnummer: personnummer,
                                sekretessmarkering: false,
                                fornamn: 'Test',
                                mellannamn: 'Svensson',
                                efternamn: 'Testsson',
                                postadress: 'Storgatan 23',
                                postnummer: '12345',
                                postort: 'Staden'
                            }
                        });
                    }
                }
            };
            $provide.value('webcert.PatientProxy', PatientProxy);
        });

        inject(function($rootScope, _$location_, _$controller_) {
            $scope = $rootScope.$new();
            $location = _$location_;
            $controller = _$controller_;
            controller = $controller('webcert.ChoosePatientCtrl', { $scope: $scope });
        });
    });

    describe('lookupPatient', function() {

        beforeEach(function() {
            spyOn(PatientProxy, 'getPatient').and.callThrough();
        });

        it('should call onSuccess on a correct personnummer accepted by the PU-tjanst', function() {

            $scope.personnummer = '191212121212';
            $scope.lookupPatient();

            expect(PatientProxy.getPatient).toHaveBeenCalled();
            expect($scope.widgetState.waiting).toBe(false);
            expect($scope.widgetState.errorid).toBe(undefined);
        });

        it('should call onSuccess on a correct samordningsnummer accepted by the PU-tjanst', function() {

            $scope.personnummer = '195401875769';
            $scope.lookupPatient();

            expect(PatientProxy.getPatient).toHaveBeenCalled();
            expect($scope.widgetState.waiting).toBe(false);
            expect($scope.widgetState.errorid).toBe(undefined);
        });

        it('should call onNotFound on an invalid personnummer', function() {

            $scope.personnummer = '191212121213';
            $scope.lookupPatient();

            expect(PatientProxy.getPatient).toHaveBeenCalled();
            expect($scope.widgetState.waiting).toBe(false);
            expect($scope.widgetState.errorid).toBe('error.pu.namenotfound');
        });

        it('should call onNotFound on an invalid samordningsnummer', function() {

            $scope.personnummer = '195401875760';
            $scope.lookupPatient();

            expect(PatientProxy.getPatient).toHaveBeenCalled();
            expect($scope.widgetState.waiting).toBe(false);
            expect($scope.widgetState.errorid).toBe('error.pu.samordningsnummernotfound');
        });

        it('should set signed path', function() {
            $scope.personnummer = '191010101010';
            $scope.lookupPatient();
            expect(PatientProxy.getPatient).toHaveBeenCalled();
            expect($scope.widgetState.waiting).toBe(false);
            expect($scope.widgetState.errorid).toBe('error.pu.namenotfound');
        });
    }) ;
});
