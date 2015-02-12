describe('ChooseCertTypeCtrl', function() {
    'use strict';

    var $controller;
    var ManageCertificate;
    var $scope;
    var $location;

    beforeEach(function(){

        module('webcert', function($provide) {
            var statService = jasmine.createSpyObj('common.statService', [ 'refreshStat' ]);
            $provide.value('common.statService', statService);

            ManageCertificate = jasmine.createSpyObj('webcert.ManageCertificate', [ 'getCertTypes', 'getCertificatesForPerson', 'initCopyDialog' ]);
            $provide.value('webcert.ManageCertificate', ManageCertificate);

            $provide.value('common.IntygCopyRequestModel', {});
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
