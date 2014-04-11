'use strict';

describe('InitCertCtrl', function () {
    beforeEach(module('wc.dashboard.controllers'));

    var InitCertCtrl;
    var CertificateDraft;
    var $scope;
    var $location;

    beforeEach(inject(function ($controller, _$location_) {
        $location = _$location_;
        CertificateDraft = jasmine.createSpyObj('CertificateDraft',
            ['reset', 'getNameAndAddress', 'createDraft']);
        $scope = {};

        InitCertCtrl = $controller('InitCertCtrl', {
            $scope : $scope,
            CertificateDraft : CertificateDraft
        });
    }));

    it('should reset certificate draft and redirect to choose patient', function () {
        expect(CertificateDraft.reset).toHaveBeenCalled();
        expect($location.path()).toEqual('/create/choose-patient/index');
    });
});
