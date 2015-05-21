describe('PatientModel', function() {
    'use strict';

    var PatientModel;
    var $httpBackend;

    beforeEach(angular.mock.module('webcert', function($provide) {
    }));

    beforeEach(angular.mock.inject([
        'webcert.PatientModel', '$httpBackend',
        function(_PatientModel_, _$httpBackend_) {
            PatientModel = _PatientModel_;
            $httpBackend = _$httpBackend_;
        }]));

    describe('#reset', function() {

        it('should create an empty object', function() {
            PatientModel.reset();

            expect(PatientModel.personnummer).toBeNull();
            expect(PatientModel.intygType).toEqual('default');
            expect(PatientModel.fornamn).toBeNull();
            expect(PatientModel.mellannamn).toBeNull();
            expect(PatientModel.efternamn).toBeNull();
            expect(PatientModel.postadress).toBeNull();
            expect(PatientModel.postnummer).toBeNull();
            expect(PatientModel.postort).toBeNull();
        });
    });
});
