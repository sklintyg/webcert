describe('PatientProxy', function() {
    'use strict';

    var PatientProxy;
    var PatientModel;
    var $httpBackend;
    var statService;

    beforeEach(angular.mock.module('webcert', function($provide) {
        statService = jasmine.createSpyObj('common.statService', [ 'refreshStat' ]);
        $provide.value('common.statService', statService);
    }));

    beforeEach(angular.mock.inject([
        'webcert.PatientProxy', 'webcert.PatientModel', '$httpBackend',
        function(_PatientProxy_, _PatientModel_, _$httpBackend_) {
            PatientProxy = _PatientProxy_;
            PatientModel = _PatientModel_;
            $httpBackend = _$httpBackend_;
        }]));

    describe('#getPatient', function() {

        var personnummer = '19121212-1212';

        it('should call onSuccess with correct data if patient is known', function() {
            var onSuccess = jasmine.createSpy('onSuccess');

            $httpBackend.expectGET('/api/person/' + personnummer).
                respond(200, {
                    status: 'FOUND',
                    person: {
                        personnummer: personnummer,
                        fornamn: 'Test',
                        mellannamn: 'Svensson',
                        efternamn: 'Testsson',
                        postadress: 'Storgatan 23',
                        postnummer: '12345',
                        postort: 'Staden'
                    }
                });

            PatientModel.reset();
            PatientProxy.getPatient(personnummer, onSuccess, null);
            $httpBackend.flush();

            expect(onSuccess).toHaveBeenCalled();
            expect(PatientModel.personnummer).toEqual(personnummer);
            expect(PatientModel.fornamn).toEqual('Test');
            expect(PatientModel.mellannamn).toEqual('Svensson');
            expect(PatientModel.efternamn).toEqual('Testsson');
            expect(PatientModel.postadress).toEqual('Storgatan 23');
            expect(PatientModel.postnummer).toEqual('12345');
            expect(PatientModel.postort).toEqual('Staden');
        });

        it('should call onNotFound if the patient is unknown', function() {
            var onSuccess = jasmine.createSpy('onSuccess');
            var onNotFound = jasmine.createSpy('onNotFound');

            $httpBackend.expectGET('/api/person/' + personnummer).
                respond(200, {
                    status: 'NOT_FOUND',
                    person: null
                });

            PatientModel.reset();
            PatientProxy.getPatient(personnummer, onSuccess, onNotFound);
            $httpBackend.flush();

            expect(onSuccess).not.toHaveBeenCalled();
            expect(onNotFound).toHaveBeenCalled();
            expect(PatientModel.personnummer).toEqual(personnummer);
            expect(PatientModel.fornamn).toBeNull();
            expect(PatientModel.mellannamn).toBeNull();
            expect(PatientModel.efternamn).toBeNull();
            expect(PatientModel.postadress).toBeNull();
        });

        it('should call onError if the request fails', function() {
            var onSuccess = jasmine.createSpy('onSuccess');
            var onNotFound = jasmine.createSpy('onNotFound');
            var onError = jasmine.createSpy('onError');

            $httpBackend.expectGET('/api/person/' + personnummer).
                respond(500);

            PatientModel.reset();
            PatientProxy.getPatient(personnummer, onSuccess, onNotFound, onError);
            $httpBackend.flush();

            expect(onSuccess).not.toHaveBeenCalled();
            expect(onNotFound).not.toHaveBeenCalled();
            expect(onError).toHaveBeenCalled();
        });
    });
});
