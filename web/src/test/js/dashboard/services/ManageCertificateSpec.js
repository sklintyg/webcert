describe('ManageCertificate', function() {
    'use strict';

    var ManageCertificate;
    var $httpBackend;
    var featureService;
    var dialogService;
    var $cookieStore;
    var $location;
    var $timeout;
    var $q;

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
        $provide.value('common.statService', jasmine.createSpyObj('common.statService', ['refreshStat']));
        $provide.value('common.User', User);
        $provide.value('common.CertificateService', {});
        $provide.value('common.messageService', {});

    }));

    // Get references to the object we want to test from the context.
    beforeEach(angular.mock.inject(['webcert.ManageCertificate', '$httpBackend', '$cookieStore',
        '$q', '$location',
        '$timeout', 'common.messageService',
        function(_ManageCertificate_, _$httpBackend_, _$cookieStore_,
            _$q_,
            _$location_, _$timeout_, _messageService_) {
            ManageCertificate = _ManageCertificate_;
            $httpBackend = _$httpBackend_;
            $cookieStore = _$cookieStore_;
            $location = _$location_;
            $timeout = _$timeout_;
            $q = _$q_;
            _messageService_.getProperty = function() {
                return 'Välj typ av intyg';
            };
        }]));

    describe('#getCertTypes', function() {

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

    describe('#getCertificatesForPerson', function() {

        var personId;
        beforeEach(function() {
            personId = '19121212-1212';
        });

        it('should call onSuccess callback with list of certificates for person from the server', function() {
            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');

            featureService.isFeatureActive.and.returnValue(true);

            $httpBackend.expectGET('/api/intyg/person/' + personId).respond([
                { 'intygId': 'intyg-1', 'source': 'IT', 'intygType': 'fk7263', 'status': 'SENT', 'lastUpdatedSigned': '2011-03-23T09:29:15.000', 'updatedSignedBy': 'Eva Holgersson', 'vidarebefordrad': false }
            ]);

            ManageCertificate.getCertificatesForPerson(personId, onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).toHaveBeenCalledWith([
                { 'intygId': 'intyg-1', 'source': 'IT', 'intygType': 'fk7263', 'status': 'SENT', 'lastUpdatedSigned': '2011-03-23T09:29:15.000', 'updatedSignedBy': 'Eva Holgersson', 'vidarebefordrad': false }
            ]);
            expect(onError).not.toHaveBeenCalled();
        });

        it('should call onError if the list cannot be fetched from the server', function() {
            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');
            $httpBackend.expectGET('/api/intyg/person/' + personId).respond(500);

            ManageCertificate.getCertificatesForPerson(personId, onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).not.toHaveBeenCalled();
            expect(onError).toHaveBeenCalled();
        });
    });

    describe('#copy', function() {

        var $scope;
        var cert;

        beforeEach(function() {
            $scope = {
                viewState: {
                    activeErrorMessageKey: null,
                    inlineErrorMessageKey: null
                },
                dialog: {
                    showerror: false,
                    acceptprogressdone: false,
                    errormessageid: null
                }
            };
            cert = {
                'intygId': 'intyg-1', 'source': 'IT', 'intygType': 'fk7263', 'status': 'SENT', 'lastUpdatedSigned': '2011-03-23T09:29:15.000', 'updatedSignedBy': 'Eva Holgersson', 'vidarebefordrad': false,
                'grundData' : { 'patient' : { 'personId': '19121212-1212'}, 'skapadAv' : {'vardenhet' : {'enhetsid' : '1234'} } }
            };

            spyOn(dialogService, 'showDialog').and.callFake(function(options) {
                options.button1click();

                return {
                    opened: { then: function() {} },
                    close: function() {}
                };
            });

            spyOn($location, 'path').and.callThrough();

        });

        it('should immediately request a utkast copy of cert if the copy cookie is set', function() {

            $cookieStore.put(ManageCertificate.COPY_DIALOG_COOKIE, true);

            $httpBackend.expectPOST('/api/intyg/' + cert.intygType + '/' + cert.intygId +'/kopiera/').respond(
                {'intygsUtkastId':'nytt-utkast-id','intygsTyp':'fk7263'}
            );
            ManageCertificate.copy($scope.viewState, cert);
            $httpBackend.flush();
            $timeout.flush();
            expect(dialogService.showDialog).not.toHaveBeenCalled();
            expect($location.path).toHaveBeenCalledWith('/fk7263/edit/nytt-utkast-id', true);

            $cookieStore.remove(ManageCertificate.COPY_DIALOG_COOKIE);
        });

        it('should show the copy dialog if the copy cookie is not set', function() {

            $cookieStore.remove(ManageCertificate.COPY_DIALOG_COOKIE);
            $httpBackend.expectPOST('/api/intyg/' + cert.intygType + '/' + cert.intygId +'/kopiera/').respond(
                {'intygsUtkastId':'nytt-utkast-id','intygsTyp':'fk7263'}
            );
            ManageCertificate.copy($scope.viewState, cert);
            $httpBackend.flush();
            $timeout.flush();

            expect(dialogService.showDialog).toHaveBeenCalled();

        });
    });

    describe('_createCopyDraft', function() {

        var cert;
        beforeEach(function() {
            cert = {
                'intygId': 'intyg-1', 'source': 'IT', 'intygType': 'fk7263', 'status': 'SENT', 'lastUpdatedSigned': '2011-03-23T09:29:15.000', 'updatedSignedBy': 'Eva Holgersson', 'vidarebefordrad': false,
                'grundData' : { 'patient' : { 'personId': '19121212-1212'}}
            };
        });

        it('should request a copy and redirect to the edit page', function() {

            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');

            $httpBackend.expectPOST('/api/intyg/' + cert.intygType + '/' + cert.intygId +'/kopiera/').respond(
                {'intygsUtkastId':'nytt-utkast-id','intygsTyp':'fk7263'}
            );
            ManageCertificate.__test__.createCopyDraft(cert, onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).toHaveBeenCalledWith({'intygsUtkastId':'nytt-utkast-id','intygsTyp':'fk7263'});
            expect(onError).not.toHaveBeenCalled();
        });
    });
});
