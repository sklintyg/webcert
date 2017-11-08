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

describe('SokSkrivValjUtkastTypeCtrl', function() {
    'use strict';

    var $controller;
    var UtkastProxy;
    var IntygProxy;
    var $scope;
    var $location;
    var CommonIntygHelperSpy;
    var CommonIntygCopyActionsSpy;
    var IntygFornyaRequestModelSpy;
    var IntygFornyaRequestInstanceMock;
    var PatientModelMock;
    var $stateParamsMock;
    var CommonMessageServiceSpy;
    var SokSkrivValjUtkastService = {};
    var AuthorityService = {};
    var UserModel;
    var $q;

    beforeEach(function() {

        module('webcert', function($provide) {
            var statService = jasmine.createSpyObj('common.statService', ['refreshStat']);
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
                postort: 'Skogen',
                build: function() {
                },
                isValid: function() {
                },
                update: function() {
                }
            };
            CommonMessageServiceSpy.getProperty.and.returnValue('Test text');
            IntygFornyaRequestModelSpy.build.and.returnValue(IntygFornyaRequestInstanceMock);
            $provide.value('common.statService', statService);

            UtkastProxy = jasmine.createSpyObj('webcert.UtkastProxy', ['getUtkastTypes', 'initCopyDialog']);
            $provide.value('webcert.UtkastProxy', UtkastProxy);

            IntygProxy = jasmine.createSpyObj('webcert.IntygProxy', ['getIntygForPatient']);
            $provide.value('webcert.IntygProxy', IntygProxy);

            CommonIntygCopyActionsSpy = jasmine.createSpyObj('common.IntygCopyActions', ['fornya']);
            $provide.value('common.IntygCopyActions', CommonIntygCopyActionsSpy);
            $provide.value('common.IntygCopyRequestModel', {});
            $provide.value('common.IntygFornyaRequestModel', IntygFornyaRequestModelSpy);
            $provide.value('common.IntygHelper', CommonIntygHelperSpy);
            $provide.value('common.PatientModel', PatientModelMock);
            $provide.value('common.PatientProxy', {});
            $provide.value('common.messageService', CommonMessageServiceSpy);
            $provide.value('$stateParams', $stateParamsMock);
            $provide.value('common.ObjectHelper', jasmine.createSpyObj('common.ObjectHelper', ['isEmpty']));
            UserModel = jasmine.createSpyObj('common.UserModel', ['isNormalOrigin']);
            UserModel.user = {
                origin: 'NORMAL'
            };
            UserModel.privileges = {
                FORNYA_INTYG: {}
            };
            $provide.value('common.UserModel', UserModel);

            SokSkrivValjUtkastService = {
                setupPatientModel: function setupPatientModel(PatientModel, patientId) {
                },
                lookupPatient: function lookupPatient(personnummer) {
                    var deferred = $q.defer();
                    PatientModelMock.personnummer = personnummer;
                    deferred.resolve(PatientModelMock);
                    return deferred.promise;
                }
            };
            $provide.value('webcert.SokSkrivValjUtkastService', SokSkrivValjUtkastService);

            AuthorityService = {
                isAuthorityActive: function(options) {
                    // Mock the behaviour when ts-intyg are checked for renewal.
                    return !(options.intygstyp === 'ts-bas' || options.intygstyp === 'ts-diabetes');
                }
            };
            $provide.value('common.authorityService', AuthorityService);
        });

        inject(function($rootScope, _$location_, _$controller_, _$q_) {
            $scope = $rootScope.$new();
            $location = _$location_;
            $controller = _$controller_;
            $q = _$q_;
        });
    });

    describe('förnya intyg', function() {
        var intyg;

        beforeEach(function() {
            $controller('webcert.SokSkrivValjUtkastTypeCtrl', {$scope: $scope});
            intyg = {
                intygType: 'fk7263',
                intygId: 'abc123',
                status: 'SIGNED',
                relations: {
                    latestChildRelations: {}
                }
            };

            $scope.patientModel = {
                sekretessmarkering: false
            };
        });

        it('is förnya allowed', function() {

            intyg.intygType = 'ts-bas';
            expect($scope.isRenewalAllowed(intyg)).toBeFalsy();

            intyg.intygType = 'fk7263';
            expect($scope.isRenewalAllowed(intyg)).toBeTruthy();

            expect($scope.isRenewalAllowed(intyg)).toBeTruthy();

            intyg.status = 'DRAFT_INCOMPLETE';
            expect($scope.isRenewalAllowed(intyg)).toBeFalsy();

            intyg.status = 'CANCELLED';
            expect($scope.isRenewalAllowed(intyg)).toBeFalsy();


            $scope.patientModel.sekretessmarkering = true;
            expect($scope.isRenewalAllowed(intyg)).toBeFalsy();

            $scope.patientModel.sekretessmarkering = false;
            intyg.relations.latestChildRelations.replacedByIntyg = true;
            expect($scope.isRenewalAllowed(intyg)).toBeFalsy();

            intyg.relations.latestChildRelations.replacedByIntyg = false;
            intyg.relations.latestChildRelations.complementedByIntyg = true;
            expect($scope.isRenewalAllowed(intyg)).toBeFalsy();
        });

        it('should förnya intyg', function() {
            $scope.fornyaIntyg(intyg);
            expect(IntygFornyaRequestModelSpy.build).toHaveBeenCalledWith({
                intygType: 'fk7263',
                intygId: 'abc123',
                patientPersonnummer: '19121212-1212',
                nyttPatientPersonnummer: null
            });
            expect(CommonIntygCopyActionsSpy.fornya).toHaveBeenCalledWith(
                $scope.viewState,
                IntygFornyaRequestInstanceMock,
                true
            );
        });
    });


    describe('openIntyg', function() {

        var intyg;

        beforeEach(function() {
            $controller('webcert.SokSkrivValjUtkastTypeCtrl', {$scope: $scope});
            intyg = {
                source: 'WC',
                intygType: 'fk7263',
                intygId: 'abc123'
            };
        });

        it('should set utkast path', function() {
            intyg.status = 'DRAFT_COMPLETE';
            $scope.openIntyg(intyg);
            expect($location.path()).toBe('/' + intyg.intygType + '/edit/' + intyg.intygId + '/');
        });

        it('should set utkast path', function() {
            intyg.status = 'DRAFT_INCOMPLETE';
            $scope.openIntyg(intyg);
            expect($location.path()).toBe('/' + intyg.intygType + '/edit/' + intyg.intygId + '/');
        });

        it('should set signed path', function() {
            intyg.status = 'RECEIVED';
            $scope.openIntyg(intyg);
            expect($location.path()).toBe('/intyg/' + intyg.intygType + '/' + intyg.intygId + '/');
        });
    });
});
