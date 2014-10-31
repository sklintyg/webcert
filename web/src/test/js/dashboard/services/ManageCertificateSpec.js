describe('ManageCertificate', function() {
    'use strict';

    var ManageCertificate;
    var $httpBackend;
    var featureService;

    // Load the webcert module and mock away everything that is not necessary.
    beforeEach(angular.mock.module('webcert', function($provide) {
        featureService = {
            features:{
                HANTERA_INTYGSUTKAST: 'hanteraIntygsutkast'
            },
            isFeatureActive: jasmine.createSpy('isFeatureActive')
        };
        $provide.value('common.featureService', featureService);
        $provide.value('common.dialogService', {});
        $provide.value('common.statService', {});
        $provide.value('common.User', {});
        $provide.value('common.CertificateService', {});
        $provide.value('common.messageService', {});
    }));

    // Get references to the object we want to test from the context.
    beforeEach(angular.mock.inject(['webcert.ManageCertificate', '$httpBackend', 'common.messageService',
        function(_ManageCertificate_, _$httpBackend_, _messageService_) {
            ManageCertificate = _ManageCertificate_;
            $httpBackend = _$httpBackend_;
            _messageService_.getProperty = function() { return 'Välj typ av intyg'; };
        }]));

    describe('#getCertTypes', function() {

        it('should call onSuccess callback with list of cert types from the server', function() {
            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');

            featureService.isFeatureActive.andReturn(true);

            $httpBackend.expectGET('/api/modules/map').respond([
                {
                    sortValue: 1,
                    id: 'fk7263',
                    label: 'Läkarintyg FK 7263',
                    url: 'fk7263',
                    fragaSvarAvailable: true
                },
                {
                    sortValue: 2,
                    id: 'ts-bas',
                    label: 'Transportstyrelsens läkarintyg, bas',
                    url: 'ts-bas',
                    fragaSvarAvailable: false
                }
            ]);

            ManageCertificate.getCertTypes(onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).toHaveBeenCalledWith([
                {
                    sortValue: 0,
                    id: 'default',
                    label: 'Välj typ av intyg'
                },
                {
                    sortValue: 1,
                    id: 'fk7263',
                    label: 'Läkarintyg FK 7263',
                    fragaSvarAvailable: true
                },
                {
                    sortValue: 2,
                    id: 'ts-bas',
                    label: 'Transportstyrelsens läkarintyg, bas',
                    fragaSvarAvailable: false
                }
            ]);
            expect(onError).not.toHaveBeenCalled();
        });

        it('should call onError if the list cannot be fetched from the server', function() {
            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');
            $httpBackend.expectGET('/api/modules/map').respond(500);

            ManageCertificate.getCertTypes(onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).not.toHaveBeenCalled();
            expect(onError).toHaveBeenCalled();
        });
    });
});
