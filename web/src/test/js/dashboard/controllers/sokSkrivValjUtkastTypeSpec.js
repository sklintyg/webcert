describe('ChooseCertTypeCtrl', function() {
    'use strict';

    var $controller;
    var UtkastProxy;
    var IntygProxy;
    var $scope;
    var $location;

    beforeEach(function(){

        module('webcert', function($provide) {
            var statService = jasmine.createSpyObj('common.statService', [ 'refreshStat' ]);
            $provide.value('common.statService', statService);

            UtkastProxy = jasmine.createSpyObj('webcert.UtkastProxy', [ 'getUtkastTypes', 'initCopyDialog' ]);
            $provide.value('webcert.UtkastProxy', UtkastProxy);

            IntygProxy = jasmine.createSpyObj('webcert.IntygProxy', [ 'getIntygForPatient' ]);
            $provide.value('webcert.IntygProxy', IntygProxy);

            $provide.value('common.IntygCopyRequestModel', {});
            $provide.value('common.IntygService', {});
            $provide.value('common.PatientModel', {});
            $provide.value('common.PatientProxy', {});
        });

        inject(function($rootScope, _$location_, _$controller_) {
            $scope = $rootScope.$new();
            $location = _$location_;
            $controller = _$controller_;
        });
    });

    describe('openIntyg', function() {

        var controller;
        var cert;

        beforeEach(function() {
            controller = $controller('webcert.ChooseCertTypeCtrl', { $scope: $scope });
            cert = {
                source: 'WC',
                intygType: 'fk7263',
                intygId: 'abc123'
            };
        });

        it('should set utkast path', function() {
            $scope.openIntyg(cert);
            expect($location.path()).toBe('/' + cert.intygType + '/edit/' + cert.intygId);
        });

        it('should set signed path', function() {
            cert.source = 'IT';
            $scope.openIntyg(cert);
            expect($location.path()).toBe('/intyg/' + cert.intygType + '/' + cert.intygId);
        });
    }) ;
});
