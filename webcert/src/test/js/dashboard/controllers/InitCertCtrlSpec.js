define([ 'angular', 'angularMocks', 'controllers' ], function(angular, mocks) {
    'use strict';

    describe('InitCertCtrl', function() {
        beforeEach(mocks.module('wc.dashboard.controllers'));

        var InitCertCtrl;
        var CreateCertificateDraft;
        var $scope;
        var $location;

        beforeEach(mocks.inject(function($controller, _$location_) {
            $location = _$location_;
            CreateCertificateDraft = jasmine.createSpyObj('CreateCertificateDraft', [
                'reset', 'getNameAndAddress', 'createDraft'
            ]);
            $scope = {};

            InitCertCtrl = $controller('InitCertCtrl', {
                $scope: $scope,
                CreateCertificateDraft: CreateCertificateDraft
            });
        }));

        it('should reset certificate draft and redirect to choose patient', function() {
            expect(CreateCertificateDraft.reset).toHaveBeenCalled();
            expect($location.path()).toEqual('/create/choose-patient/index');
        });
    });
});
