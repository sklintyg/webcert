describe('ChooseCertTypeCtrl', function() {
    'use strict';

    var $controller;
    var ManageCertificate;
    var $scope;

    beforeEach(function(){

        module('webcert', function($provide) {
            var statService = jasmine.createSpyObj('common.statService', [ 'refreshStat' ]);
            ManageCertificate = jasmine.createSpyObj('webcert.ManageCertificate', [ 'getCertTypes', 'getCertificatesForPerson', 'initCopyDialog' ]);

            $provide.value('common.statService', statService);
            $provide.value('webcert.ManageCertificate', ManageCertificate);
        });

        inject(function($rootScope, _$controller_) {
            $scope = $rootScope.$new();
            $controller = _$controller_;
        });
    });

    describe('constructor', function() {
        it('should set focusFirstInput to true', function() {
            var controller = $controller('webcert.ChooseCertTypeCtrl', { $scope: $scope });
            expect($scope.focusFirstInput).toBe(true);
        });
    }) ;
});
