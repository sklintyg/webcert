describe('UtkastProxy', function() {
    'use strict';

    var UtkastProxy;
    var $httpBackend;
    var featureService;
    var dialogService;
    var $cookieStore;
    var $location;
    var $timeout;
    var $q;
    var statService;

    var createDraftRequestPayload = {
        'intygType':'fk7263','patientPersonnummer':'19121212-1212','patientFornamn':'Test',
        'patientMellannamn':'Svensson','patientEfternamn':'Testsson','patientPostadress':'Storgatan 23',
        'patientPostnummer':'12345','patientPostort':'Staden'
    };

    // Load the webcert module and mock away everything that is not necessary.
    beforeEach(angular.mock.module('webcert', function($provide) {
        featureService = {
            features: {
                HANTERA_INTYGSUTKAST: 'hanteraIntygsutkast'
            },
            isFeatureActive: jasmine.createSpy('isFeatureActive')
        };

        var User = {
            getValdVardenhet: function() {
                return {
                    id: 'enhet1',
                    namn: 'Vårdenheten'
                };
            }
        };

        dialogService = {
            showDialog: function($scope, options) {

            }
        };

        $provide.value('common.featureService', featureService);
        $provide.value('common.dialogService', dialogService);
        statService = jasmine.createSpyObj('common.statService', [ 'refreshStat' ]);
        $provide.value('common.statService', statService);
        $provide.value('common.User', User);
        $provide.value('common.CertificateService', {});
        $provide.value('common.messageService', {});

    }));

    // Get references to the object we want to test from the context.
    beforeEach(angular.mock.inject(['webcert.UtkastProxy', '$httpBackend', '$cookieStore',
        '$q', '$location',
        '$timeout', 'common.messageService',
        function(_UtkastProxy_, _$httpBackend_, _$cookieStore_,
            _$q_,
            _$location_, _$timeout_, _messageService_) {
            UtkastProxy = _UtkastProxy_;
            $httpBackend = _$httpBackend_;
            $cookieStore = _$cookieStore_;
            $location = _$location_;
            $timeout = _$timeout_;
            $q = _$q_;
            _messageService_.getProperty = function() {
                return 'Välj typ av intyg';
            };
        }]));

    describe('#createUtkast', function() {

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

            UtkastProxy.createUtkast(createDraftRequestPayload, onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).toHaveBeenCalledWith('12345');
            expect(onError).not.toHaveBeenCalled();
            expect(statService.refreshStat).toHaveBeenCalled();
        });

        it('should call onError if the server cannot create a draft', function() {

            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');
            $httpBackend.expectPOST('/api/utkast/fk7263').respond(500);

            UtkastProxy.createUtkast(createDraftRequestPayload, onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).not.toHaveBeenCalled();
            expect(onError).toHaveBeenCalled();
            expect(statService.refreshStat).not.toHaveBeenCalled();
        });
    });

    describe('#getUtkastTypes', function() {

        it('should call onSuccess callback with list of cert types from the server', function() {
            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');

            featureService.isFeatureActive.and.returnValue(true);

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

            UtkastProxy.getUtkastTypes(onSuccess, onError);
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

            UtkastProxy.getUtkastTypes(onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).not.toHaveBeenCalled();
            expect(onError).toHaveBeenCalled();
        });
    });
});
