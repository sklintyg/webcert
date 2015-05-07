describe('ChoosePatientCtrl', function() {
    'use strict';

    var $controller;
    var PatientProxy;
    var $scope;
    var $location;
    var controller;

    beforeEach(function(){

        module('webcert', function($provide) {
/*            var statService = jasmine.createSpyObj('common.statService', [ 'refreshStat' ]);
            $provide.value('common.statService', statService);

            ManageCertificate = jasmine.createSpyObj('webcert.ManageCertificate', [ 'getUtkastTypes', 'getIntygForPatient', 'initCopyDialog' ]);
            $provide.value('webcert.ManageCertificate', ManageCertificate);

            $provide.value('common.IntygCopyRequestModel', {});*/


            PatientProxy = {
                getPatient: function(personnummer, onSuccess, onNotFound, onError) {
                    if(personnummer === '191212121212') {
                        onSuccess ({
                            status: 'FOUND',
                            person: {
                                personnummer: personnummer,
                                sekretessmarkering: false,
                                fornamn: 'Test',
                                mellannamn: 'Svensson',
                                efternamn: 'Testsson',
                                postadress: 'Storgatan 23',
                                postnummer: '12345',
                                postort: 'Staden'
                            }
                        });
                    } else {
                        onNotFound({
                            status: 'NOT_FOUND',
                                person: null
                        });
                    }
                }
            };
            $provide.value('webcert.PatientProxy', PatientProxy);
        });

        inject(function($rootScope, _$location_, _$controller_) {
            $scope = $rootScope.$new();
            $location = _$location_;
            $controller = _$controller_;
            controller = $controller('webcert.ChoosePatientCtrl', { $scope: $scope });
        });
    });

    describe('lookupPatient', function() {

        beforeEach(function() {
            spyOn(PatientProxy, 'getPatient').and.callThrough();
        });

        it('should call onSuccess on a correct personnummer accepted by the PU-tjanst', function() {

            $scope.personnummer = '191212121212';
            $scope.lookupPatient();

            expect(PatientProxy.getPatient).toHaveBeenCalled();
            expect($scope.widgetState.waiting).toBe(false);
            expect($scope.widgetState.errorid).toBe(undefined);
        });

        it('should set signed path', function() {
            $scope.personnummer = '191010101010';
            $scope.lookupPatient();
            expect(PatientProxy.getPatient).toHaveBeenCalled();
            expect($scope.widgetState.waiting).toBe(false);
            expect($scope.widgetState.errorid).toBe('error.pu.namenotfound');
        });
    }) ;
});
