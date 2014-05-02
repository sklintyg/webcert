define([ 'angular', 'angularMocks', 'services'], function(angular, mocks) {
    'use strict';

    describe('CreateCertificateDraft', function() {

        var CreateCertificateDraft;
        var $httpBackend;
        var statService;

        beforeEach(mocks.module('wc.dashboard.services', function($provide) {
            statService = jasmine.createSpyObj('statService', [ 'refreshStat' ]);
            $provide.value('statService', statService);
        }));


        beforeEach(mocks.inject(function(_CreateCertificateDraft_, _$httpBackend_) {
            CreateCertificateDraft = _CreateCertificateDraft_;
            $httpBackend = _$httpBackend_;
        }));

        describe('#reset', function() {

            it('should create an empty object', function() {
                CreateCertificateDraft.reset();

                expect(CreateCertificateDraft.personnummer).toBeNull();
                expect(CreateCertificateDraft.intygType).toEqual('default');
                expect(CreateCertificateDraft.firstname).toBeNull();
                expect(CreateCertificateDraft.lastname).toBeNull();
                expect(CreateCertificateDraft.address).toBeNull();
                expect(CreateCertificateDraft.vardEnhetHsaId).toBeNull();
                expect(CreateCertificateDraft.vardEnhetNamn).toBeNull();
                expect(CreateCertificateDraft.vardGivareHsaId).toBeNull();
                expect(CreateCertificateDraft.vardGivareHsaNamn).toBeNull();
            });
        });

        describe('#getNameAndAddress', function() {

            it('should set name and address to null for unknown patients', function() {
                var onSuccess = jasmine.createSpy('onSuccess');

                CreateCertificateDraft.getNameAndAddress('19401010-1014', onSuccess);

                expect(CreateCertificateDraft.firstname).toBeNull();
                expect(CreateCertificateDraft.lastname).toBeNull();
                expect(CreateCertificateDraft.address).toBeNull();
                expect(onSuccess).toHaveBeenCalled();
            });

            it('should set name and address for known patients', function() {
                var onSuccess = jasmine.createSpy('onSuccess');

                CreateCertificateDraft.getNameAndAddress('19121212-1212', onSuccess);

                expect(CreateCertificateDraft.firstname).toEqual('Test');
                expect(CreateCertificateDraft.lastname).toEqual('Testsson');
                expect(CreateCertificateDraft.address).toEqual('Storgatan 23');
                expect(onSuccess).toHaveBeenCalled();
            });
        });

        describe('#createDraft', function() {

            it('should create a draft if the payload is correct', function() {
                CreateCertificateDraft.personnummer = '19121212-1212';
                CreateCertificateDraft.firstname = 'Test';
                CreateCertificateDraft.lastname = 'Testsson';
                CreateCertificateDraft.intygType = 'fk7263';
                CreateCertificateDraft.address = 'Storgatan 23';
                CreateCertificateDraft.vardEnhetHsaId = '1';
                CreateCertificateDraft.vardEnhetNamn = 'A';
                CreateCertificateDraft.vardGivareHsaId = '2';
                CreateCertificateDraft.vardGivareNamn = 'B';

                var onSuccess = jasmine.createSpy('onSuccess');
                var onError = jasmine.createSpy('onError');
                $httpBackend.
                    expectPOST('/api/intyg/create', {
                        patientPersonnummer: '19121212-1212',
                        patientFornamn: 'Test',
                        patientEfternamn: 'Testsson',
                        intygType: 'fk7263',
                        postadress: 'Storgatan 23',
                        vardEnhetHsaId: '1',
                        vardEnhetNamn: 'A',
                        vardGivareHsaId: '2',
                        vardGivareNamn: 'B'
                    }).
                    respond(200, '12345');

                CreateCertificateDraft.createDraft(onSuccess, onError);
                $httpBackend.flush();

                expect(onSuccess).toHaveBeenCalledWith('12345');
                expect(onError).not.toHaveBeenCalled();
                expect(statService.refreshStat).toHaveBeenCalled();
            });

            it('should call onError if the server cannot create a draft', function() {
                var onSuccess = jasmine.createSpy('onSuccess');
                var onError = jasmine.createSpy('onError');
                $httpBackend.expectPOST('/api/intyg/create', {}).respond(500);

                CreateCertificateDraft.createDraft(onSuccess, onError);
                $httpBackend.flush();

                expect(onSuccess).not.toHaveBeenCalled();
                expect(onError).toHaveBeenCalled();
                expect(statService.refreshStat).not.toHaveBeenCalled();
            });
        });
    });
});
