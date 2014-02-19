'use strict';

describe('CertificateDraft', function () {
    beforeEach(module('wc.dashboard.services'));

    var CertificateDraft;
    var $httpBackend;

    beforeEach(inject(function (_CertificateDraft_, _$httpBackend_) {
        CertificateDraft = _CertificateDraft_;
        $httpBackend = _$httpBackend_;
    }));

    describe('#reset', function () {

        it('should create an empty object', function () {
            CertificateDraft.reset();

            expect(CertificateDraft.personnummer).toBeNull();
            expect(CertificateDraft.intygType).toEqual('default');
            expect(CertificateDraft.firstname).toBeNull();
            expect(CertificateDraft.lastname).toBeNull();
            expect(CertificateDraft.address).toBeNull();
            expect(CertificateDraft.vardEnhetHsaId).toBeNull();
            expect(CertificateDraft.vardEnhetNamn).toBeNull();
            expect(CertificateDraft.vardGivareHsaId).toBeNull();
            expect(CertificateDraft.vardGivareHsaNamn).toBeNull();
        });
    });

    describe('#getNameAndAddress', function () {

        it('should set name and address to null for unknown patients', function () {
            var onSuccess = jasmine.createSpy('onSuccess');

            CertificateDraft.getNameAndAddress('19401010-1014', onSuccess);

            expect(CertificateDraft.firstname).toBeNull();
            expect(CertificateDraft.lastname).toBeNull();
            expect(CertificateDraft.address).toBeNull();
            expect(onSuccess).toHaveBeenCalled();
        });

        it('should set name and address for known patients', function () {
            var onSuccess = jasmine.createSpy('onSuccess');

            CertificateDraft.getNameAndAddress('19121212-1212', onSuccess);

            expect(CertificateDraft.firstname).toEqual('Test');
            expect(CertificateDraft.lastname).toEqual('Testsson');
            expect(CertificateDraft.address).toEqual('Storgatan 23');
            expect(onSuccess).toHaveBeenCalled();
        });
    });

    describe('#getCertTypes', function () {

        it('should call onSuccess callback with list of cert types from the server', function () {
            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');
            $httpBackend.
                expectGET('/api/intyg/types').
                respond([
                    { sortValue : 1, id : 'fk7263', label : 'Läkarintyg FK 7263', url : 'fk7263' },
                    { sortValue : 2, id : 'ts-bas', label : 'Transportstyrelsens läkarintyg, bas', url : 'ts-bas' }
                ]);

            CertificateDraft.getCertTypes(onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).toHaveBeenCalledWith([
                { sortValue : 0, id : 'default', label : 'Välj intygstyp' },
                { sortValue : 1, id : 'fk7263', label : 'Läkarintyg FK 7263', url : 'fk7263' },
                { sortValue : 2, id : 'ts-bas', label : 'Transportstyrelsens läkarintyg, bas', url : 'ts-bas' }
            ]);
            expect(onError).not.toHaveBeenCalled();
        });

        it('should call onError if the list cannot be fetched from the server', function () {
            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');
            $httpBackend.
                expectGET('/api/intyg/types').
                respond(500);

            CertificateDraft.getCertTypes(onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).not.toHaveBeenCalled();
            expect(onError).toHaveBeenCalled();
        });
    });

    describe('#createDraft', function () {

        it('should create a draft if the payload is correct', function () {
            CertificateDraft.personnummer = '19121212-1212';
            CertificateDraft.firstname = 'Test';
            CertificateDraft.lastname = 'Testsson';
            CertificateDraft.intygType = 'fk7263';
            CertificateDraft.address = 'Storgatan 23';
            CertificateDraft.vardEnhetHsaId = '1';
            CertificateDraft.vardEnhetNamn = 'A';
            CertificateDraft.vardGivareHsaId = '2';
            CertificateDraft.vardGivareNamn = 'B';

            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');
            $httpBackend.
                expectPOST('/api/intyg/create', {
                    patientPersonnummer : '19121212-1212',
                    patientFornamn : 'Test',
                    patientEfternamn : 'Testsson',
                    intygType : 'fk7263',
                    address : 'Storgatan 23',
                    vardEnhetHsaId : '1',
                    vardEnhetNamn : 'A',
                    vardGivareHsaId : '2',
                    vardGivareNamn : 'B'
                }).
                respond(200, '12345');

            CertificateDraft.createDraft(onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).toHaveBeenCalledWith('12345');
            expect(onError).not.toHaveBeenCalled();
        });

        it('should call onError if the server cannot create a draft', function () {
            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');
            $httpBackend.
                expectPOST('/api/intyg/create', {}).
                respond(500);

            CertificateDraft.createDraft(onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).not.toHaveBeenCalled();
            expect(onError).toHaveBeenCalled();
        });
    });
});
