/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

describe('ChooseCertTypeCtrl', function() {
    'use strict';

    var $controller;
    var UtkastProxy;
    var IntygProxy;
    var $scope;
    var $location;
    var CommonIntygServiceSpy;
    var IntygFornyaRequestModelSpy;
    var IntygFornyaRequestInstanceMock;
    var PatientModelMock;
    var $stateParamsMock;
    var CommonMessageServiceSpy;

    beforeEach(function(){

        module('webcert', function($provide) {
            var statService = jasmine.createSpyObj('common.statService', [ 'refreshStat' ]);
            CommonIntygServiceSpy = jasmine.createSpyObj('common.IntygService', ['fornya']);
            IntygFornyaRequestModelSpy = jasmine.createSpyObj('common.IntygFornyaRequestModel', ['build']);
            CommonMessageServiceSpy = jasmine.createSpyObj('common.messageService', ['getProperty']);
            IntygFornyaRequestInstanceMock = {};
            $stateParamsMock = {
                patientId: 'PAT-ID-TEST'
            };
            PatientModelMock = {
                sekretessmarkering: '',
                fornamn: 'Kalle',
                mellannamn: 'Kula',
                efternamn: 'Håkansson',
                personnummer: '19121212-1212',
                intygType: 'fk7263',
                postadress: 'Skogsvägen 1',
                postnummer: '111 22',
                postort: 'Skogen'
            };
            CommonMessageServiceSpy.getProperty.and.returnValue('Test text');
            IntygFornyaRequestModelSpy.build.and.returnValue(IntygFornyaRequestInstanceMock);
            $provide.value('common.statService', statService);

            UtkastProxy = jasmine.createSpyObj('webcert.UtkastProxy', [ 'getUtkastTypes', 'initCopyDialog' ]);
            $provide.value('webcert.UtkastProxy', UtkastProxy);

            IntygProxy = jasmine.createSpyObj('webcert.IntygProxy', [ 'getIntygForPatient' ]);
            $provide.value('webcert.IntygProxy', IntygProxy);

            $provide.value('common.IntygCopyRequestModel', {});
            $provide.value('common.IntygFornyaRequestModel', IntygFornyaRequestModelSpy);
            $provide.value('common.IntygService', CommonIntygServiceSpy);
            $provide.value('common.PatientModel', PatientModelMock);
            $provide.value('common.PatientProxy', {});
            $provide.value('common.messageService', CommonMessageServiceSpy);
            $provide.value('$stateParams', $stateParamsMock);
        });

        inject(function($rootScope, _$location_, _$controller_) {
            $scope = $rootScope.$new();
            $location = _$location_;
            $controller = _$controller_;
        });
    });

    describe('förnya intyg', function () {
        var controller;
        var cert;

        beforeEach(function() {
            controller = $controller('webcert.ChooseCertTypeCtrl', { $scope: $scope });
            cert = {
                intygType: 'fk7263',
                intygId: 'abc123'
            };
        });

        it('should förnya intyg', function () {
            $scope.fornyaIntyg(cert);
            expect(IntygFornyaRequestModelSpy.build).toHaveBeenCalledWith({
                intygType: 'fk7263',
                intygId: 'abc123',
                patientPersonnummer: '19121212-1212',
                nyttPatientPersonnummer: 'PAT-ID-TEST'
            });
            expect(CommonIntygServiceSpy.fornya).toHaveBeenCalledWith(
                $scope.viewState,
                IntygFornyaRequestInstanceMock,
                true
            );
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
            cert.status = 'DRAFT_COMPLETE';
            $scope.openIntyg(cert);
            expect($location.path()).toBe('/' + cert.intygType + '/edit/' + cert.intygId);
        });

        it('should set utkast path', function() {
            cert.status = 'DRAFT_INCOMPLETE';
            $scope.openIntyg(cert);
            expect($location.path()).toBe('/' + cert.intygType + '/edit/' + cert.intygId);
        });

        it('should set signed path', function() {
            cert.status = 'RECEIVED';
            $scope.openIntyg(cert);
            expect($location.path()).toBe('/intyg/' + cert.intygType + '/' + cert.intygId);
        });
    });
});
