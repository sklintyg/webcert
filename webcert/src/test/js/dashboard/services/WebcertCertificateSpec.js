'use strict';

describe('WebcertCertificate', function () {
    beforeEach(module('wc.dashboard.services'));

    var WebcertCertificate;
    var $httpBackend;

    beforeEach(inject(function (_WebcertCertificate_, _$httpBackend_) {
        WebcertCertificate = _WebcertCertificate_;
        $httpBackend = _$httpBackend_;
    }));

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

            WebcertCertificate.getCertTypes(onSuccess, onError);
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

            WebcertCertificate.getCertTypes(onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).not.toHaveBeenCalled();
            expect(onError).toHaveBeenCalled();
        });
    });
});
