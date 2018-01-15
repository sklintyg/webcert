/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
describe('SokSkrivValjUtkastService', function() {
    'use strict';

    var SokSkrivValjUtkastService;
    var PatientProxy;
    var basePerson;
    var $scope;
    var PatientModel;

    beforeEach(function() {

        module('webcert', function($provide) {
            var personIdValidator = {
                validateSamordningsnummer: function(number) {
                    if (number === '195401875760') {
                        return number;
                    }
                    return null;
                },
                validResult: function(result) {
                    return result !== undefined && result !== null;
                },
                validate: function(pnr) {
                    return pnr;
                }
            };

            $provide.value('common.PersonIdValidatorService', personIdValidator);

            PatientProxy = {
                getPatient: function() {
                }
            };
            $provide.value('common.PatientProxy', PatientProxy);
            $provide.value('common.ObjectHelper', jasmine.createSpyObj('common.ObjectHelper', ['isEmpty']));
        });

        inject(['$rootScope', 'webcert.SokSkrivValjUtkastService', function(_$rootScope_, _SokSkrivValjUtkastService_) {
            SokSkrivValjUtkastService = _SokSkrivValjUtkastService_;
            $scope = _$rootScope_.$new();
        }]);
    });

    describe('setupPatientModel', function() {

        beforeEach(function() {
            PatientModel = {
                personnummer: null,
                build: function() {
                    this.personnummer = null;
                    return this;
                },
                isValid: function() {
                    return this.personnummer !== null && typeof this.personnummer !== undefined;
                }
            };
        });

        it('should return a model with a personnummer set if sent a correct patientId',
            function() {
                var testpnr = '191212121212';
                var result = SokSkrivValjUtkastService.setupPatientModel(PatientModel, testpnr);
                expect(result.personnummer).toBe(testpnr);
            });

        it('should return a model with a personnummer set if patientId is "default" and PatientModel already has a id',
            function() {
                var testpnr = '191212121212';
                PatientModel.personnummer = testpnr;
                var patientIdParam = 'default';
                var result = SokSkrivValjUtkastService.setupPatientModel(PatientModel, patientIdParam);
                expect(result.personnummer).toBe(testpnr);
            });

        it('should return a model with a personnummer set if patientId is null and PatientModel already has a id',
            function() {
                var testpnr = '191212121212';
                PatientModel.personnummer = testpnr;
                var patientIdParam = null;
                var result = SokSkrivValjUtkastService.setupPatientModel(PatientModel, patientIdParam);
                expect(result.personnummer).toBe(testpnr);
            });

        // Negative
        it('should return a model with a personnummer set to null if patientId is invalid and PatientModel doesnt have id',
            function() {
                var patientIdParam = null;
                var result = SokSkrivValjUtkastService.setupPatientModel(PatientModel, patientIdParam);
                expect(result.personnummer).toBe(null);
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

        });

        it('should resolve with a patientResponse on a correct personnummer accepted by the PU-tjanst',
            function() {

                spyOn(PatientProxy, 'getPatient').and.callFake(successResult);

                var resolveResponse = null;
                var rejectResponse = null;

                SokSkrivValjUtkastService.lookupPatient('191212121212').then(function(patientResponse) {
                    resolveResponse = patientResponse;
                }, function(errorId) {
                    rejectResponse = errorId;
                });
                $scope.$digest();

                expect(PatientProxy.getPatient).toHaveBeenCalled();
                expect(resolveResponse.personnummer).toBe('191212121212');
                expect(rejectResponse).toBe(null);
            });

        it('should resolve with a patientResponse with a correct samordningsnummer accepted by the PU-tjanst',
            function() {

                spyOn(PatientProxy, 'getPatient').and.callFake(successResult);

                var resolveResponse = null;
                var rejectResponse = null;

                SokSkrivValjUtkastService.lookupPatient('195401875769').then(function(patientResponse) {
                    resolveResponse = patientResponse;
                }, function(errorId) {
                    rejectResponse = errorId;
                });
                $scope.$digest();

                expect(PatientProxy.getPatient).toHaveBeenCalled();
                expect(resolveResponse.personnummer).toBe('195401875769');
                expect(rejectResponse).toBe(null);
            });

        it('should reject with an error id on a personnummer that does not exist', function() {

            spyOn(PatientProxy, 'getPatient').and.callFake(function(personnummer, onSuccess, onNotFound, onError) {
                onNotFound();
            });

            var resolveResponse = null;
            var rejectResponse = null;

            SokSkrivValjUtkastService.lookupPatient('191212121213').then(function(patientResponse) {
                resolveResponse = patientResponse;
            }, function(errorId) {
                rejectResponse = errorId;
            });
            $scope.$digest();

            expect(PatientProxy.getPatient).toHaveBeenCalled();
            expect(resolveResponse).toBe(null);
            expect(rejectResponse).toBe('error.pu.namenotfound');
        });

        it('should call onNotFound on samordningsnummer that does not exist', function() {

            spyOn(PatientProxy, 'getPatient').and.callFake(function(personnummer, onSuccess, onNotFound, onError) {
                onNotFound();
            });

            var resolveResponse = null;
            var rejectResponse = null;

            SokSkrivValjUtkastService.lookupPatient('195401875760').then(function(patientResponse) {
                resolveResponse = patientResponse;
            }, function(errorId) {
                rejectResponse = errorId;
            });
            $scope.$digest();

            expect(PatientProxy.getPatient).toHaveBeenCalled();
            expect(resolveResponse).toBe(null);
            expect(rejectResponse).toBe('error.pu.samordningsnummernotfound');
        });

        it('should redirect to edit-patient-name if the pu-service isnt available', function() {

            spyOn(PatientProxy, 'getPatient').and.callFake(function(personnummer, onSuccess, onNotFound, onError) {
                onError('error.pu_problem');
            });

            var resolveResponse = null;
            var rejectResponse = null;

            SokSkrivValjUtkastService.lookupPatient('195401875760').then(function(patientResponse) {
                resolveResponse = patientResponse;
            }, function(errorId) {
                rejectResponse = errorId;
            });
            $scope.$digest();

            expect(PatientProxy.getPatient).toHaveBeenCalled();
            expect(resolveResponse).toBe(null);
            expect(rejectResponse).toBe('error.pu_problem');
        });

        it('should reject if the response does not contain personnummer',
            function() {
                spyOn(PatientProxy, 'getPatient').and.callFake(function(personnummer, onSuccess, onNotFound, onError) {
                    basePerson.personnummer = '';
                    onSuccess(basePerson);
                });

                var resolveResponse = null;
                var rejectResponse = null;

                SokSkrivValjUtkastService.lookupPatient('195401875760').then(function(patientResponse) {
                    resolveResponse = patientResponse;
                }, function(errorId) {
                    rejectResponse = errorId;
                });
                $scope.$digest();

                expect(PatientProxy.getPatient).toHaveBeenCalled();
                expect(resolveResponse).toBe(null);
                expect(rejectResponse).toBe('error.pu.nopersonnummer');
            });

        it('should reject if the response does not contain fornamn or efternamn',
            function() {
                spyOn(PatientProxy, 'getPatient').and.callFake(function(personnummer, onSuccess, onNotFound, onError) {
                    basePerson.personnummer = personnummer;
                    basePerson.fornamn = '';
                    basePerson.efternamn = '';
                    onSuccess(basePerson);
                });

                var resolveResponse = null;
                var rejectResponse = null;

                SokSkrivValjUtkastService.lookupPatient('195401875760').then(function(patientResponse) {
                    resolveResponse = patientResponse;
                }, function(errorId) {
                    rejectResponse = errorId;
                });
                $scope.$digest();

                expect(PatientProxy.getPatient).toHaveBeenCalled();
                expect(resolveResponse).toBe(null);
                expect(rejectResponse).toBe('error.pu.noname');
            });
    });

    describe('hasUnsigned', function() {
        it('should return null if list is invalid', function() {
            var result = SokSkrivValjUtkastService.hasUnsigned(null);
            expect(result).toBe(null);
        });

        it('should return empty if list is empty', function() {
            var result = SokSkrivValjUtkastService.hasUnsigned([]);
            expect(result).toBe('intyglist-empty');
        });

        it('should return unsigned if any items in the list is unsigned', function() {

            var intygList = [
                {status: 'DRAFT_COMPLETE'},
                {status: 'SIGNED'}
            ];

            var result = SokSkrivValjUtkastService.hasUnsigned(intygList);
            expect(result).toBe('signed');
        });

        it('should return signed if no items list in the list is unsigned', function() {

            var intygList = [
                {status: 'RECIEVED'},
                {status: 'SIGNED'}
            ];

            var result = SokSkrivValjUtkastService.hasUnsigned(intygList);
            expect(result).toBe('unsigned');
        });
    });

});
