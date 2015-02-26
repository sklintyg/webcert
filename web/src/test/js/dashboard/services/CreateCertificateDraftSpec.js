describe('CreateCertificateDraft', function() {
    'use strict';

    var CreateCertificateDraft;
    var $httpBackend;
    var statService;

    var createDraftRequestPayload = {
        'intygType':'fk7263','patientPersonnummer':'19121212-1212','patientFornamn':'Test',
        'patientMellannamn':'Svensson','patientEfternamn':'Testsson','patientPostadress':'Storgatan 23',
        'patientPostnummer':'12345','patientPostort':'Staden'
    };

    beforeEach(angular.mock.module('webcert', function($provide) {
        statService = jasmine.createSpyObj('common.statService', [ 'refreshStat' ]);
        $provide.value('common.statService', statService);
    }));

    beforeEach(angular.mock.inject([
        'webcert.CreateCertificateDraft', '$httpBackend',
        function(_CreateCertificateDraft_, _$httpBackend_) {
            CreateCertificateDraft = _CreateCertificateDraft_;
            $httpBackend = _$httpBackend_;
        }]));

    describe('#reset', function() {

        it('should create an empty object', function() {
            CreateCertificateDraft.reset();

            expect(CreateCertificateDraft.personnummer).toBeNull();
            expect(CreateCertificateDraft.intygType).toEqual('default');
            expect(CreateCertificateDraft.fornamn).toBeNull();
            expect(CreateCertificateDraft.mellannamn).toBeNull();
            expect(CreateCertificateDraft.efternamn).toBeNull();
            expect(CreateCertificateDraft.postadress).toBeNull();
            expect(CreateCertificateDraft.postnummer).toBeNull();
            expect(CreateCertificateDraft.postort).toBeNull();
        });
    });

    describe('#getNameAndAddress', function() {

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

            CreateCertificateDraft.reset();
            CreateCertificateDraft.getNameAndAddress(personnummer, onSuccess, null);
            $httpBackend.flush();

            expect(onSuccess).toHaveBeenCalled();
            expect(CreateCertificateDraft.personnummer).toEqual(personnummer);
            expect(CreateCertificateDraft.fornamn).toEqual('Test');
            expect(CreateCertificateDraft.mellannamn).toEqual('Svensson');
            expect(CreateCertificateDraft.efternamn).toEqual('Testsson');
            expect(CreateCertificateDraft.postadress).toEqual('Storgatan 23');
            expect(CreateCertificateDraft.postnummer).toEqual('12345');
            expect(CreateCertificateDraft.postort).toEqual('Staden');
        });

        it('should call onNotFound if the patient is unknown', function() {
            var onSuccess = jasmine.createSpy('onSuccess');
            var onNotFound = jasmine.createSpy('onNotFound');

            $httpBackend.expectGET('/api/person/' + personnummer).
                respond(200, {
                    status: 'NOT_FOUND',
                    person: null
                });

            CreateCertificateDraft.reset();
            CreateCertificateDraft.getNameAndAddress(personnummer, onSuccess, onNotFound);
            $httpBackend.flush();

            expect(onSuccess).not.toHaveBeenCalled();
            expect(onNotFound).toHaveBeenCalled();
            expect(CreateCertificateDraft.personnummer).toEqual(personnummer);
            expect(CreateCertificateDraft.fornamn).toBeNull();
            expect(CreateCertificateDraft.mellannamn).toBeNull();
            expect(CreateCertificateDraft.efternamn).toBeNull();
            expect(CreateCertificateDraft.postadress).toBeNull();
        });

        it('should call onError if the request fails', function() {
            var onSuccess = jasmine.createSpy('onSuccess');
            var onNotFound = jasmine.createSpy('onNotFound');
            var onError = jasmine.createSpy('onError');

            $httpBackend.expectGET('/api/person/' + personnummer).
                respond(500);

            CreateCertificateDraft.reset();
            CreateCertificateDraft.getNameAndAddress(personnummer, onSuccess, onNotFound, onError);
            $httpBackend.flush();

            expect(onSuccess).not.toHaveBeenCalled();
            expect(onNotFound).not.toHaveBeenCalled();
            expect(onError).toHaveBeenCalled();
        });
    });

    describe('#createDraft', function() {

        it('should create a draft if the payload is correct', function() {

            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');
            $httpBackend.
                expectPOST('/api/utkast/fk7263', {
                    intygType: 'fk7263',
                    patientPersonnummer: '19121212-1212',
                    patientFornamn: 'Test',
                    patientMellannamn: 'Svensson',
                    patientEfternamn: 'Testsson',
                    patientPostadress: 'Storgatan 23',
                    patientPostnummer: '12345',
                    patientPostort: 'Staden'
                }).
                respond(200, '12345');

            CreateCertificateDraft.createDraft(createDraftRequestPayload, onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).toHaveBeenCalledWith('12345');
            expect(onError).not.toHaveBeenCalled();
            expect(statService.refreshStat).toHaveBeenCalled();
        });

        it('should call onError if the server cannot create a draft', function() {
            
            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');
            $httpBackend.expectPOST('/api/utkast/fk7263').respond(500);

            CreateCertificateDraft.createDraft(createDraftRequestPayload, onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).not.toHaveBeenCalled();
            expect(onError).toHaveBeenCalled();
            expect(statService.refreshStat).not.toHaveBeenCalled();
        });
    });
});
