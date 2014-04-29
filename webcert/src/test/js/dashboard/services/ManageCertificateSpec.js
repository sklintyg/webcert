'use strict';
define([ 'angular', 'angularMocks', 'angularSanitize', 'angularUiBootstrap', 'services'], function(angular, mocks) {

	describe('ManageCertificate', function() {
        var ManageCertificate;
        var $httpBackend;

		beforeEach(mocks.module('ui.bootstrap'));
		beforeEach(mocks.module('wc.dashboard.services'));

		beforeEach(mocks.inject(function(_ManageCertificate_, _$httpBackend_) {
			ManageCertificate = _ManageCertificate_;
			$httpBackend = _$httpBackend_;
		}));

		describe('#getCertTypes', function() {

			it('should call onSuccess callback with list of cert types from the server', function() {
				var onSuccess = jasmine.createSpy('onSuccess');
				var onError = jasmine.createSpy('onError');
				$httpBackend.expectGET('/api/modules/map').respond([ {
					sortValue : 1,
					id : 'fk7263',
					label : 'Läkarintyg FK 7263',
					url : 'fk7263'
				}, {
					sortValue : 2,
					id : 'ts-bas',
					label : 'Transportstyrelsens läkarintyg, bas',
					url : 'ts-bas'
				} ]);

				ManageCertificate.getCertTypes(onSuccess, onError);
				$httpBackend.flush();

				expect(onSuccess).toHaveBeenCalledWith([ {
					sortValue : 0,
					id : 'default',
					label : 'Välj intygstyp'
				}, {
					sortValue : 1,
					id : 'fk7263',
					label : 'Läkarintyg FK 7263'
				}, {
					sortValue : 2,
					id : 'ts-bas',
					label : 'Transportstyrelsens läkarintyg, bas'
				} ]);
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
});