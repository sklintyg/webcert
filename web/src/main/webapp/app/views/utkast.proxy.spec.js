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

describe('UtkastProxy', function() {
    'use strict';

    var UtkastProxy;
    var $httpBackend;
    var featureService;
    var authorityService;
    var dialogService;
    var statService;

    var createDraftRequestPayload = {
        'intygType':'fk7263','patientPersonnummer':'19121212-1212','patientFornamn':'Test',
        'patientMellannamn':'Svensson','patientEfternamn':'Testsson','patientPostadress':'Storgatan 23',
        'patientPostnummer':'12345','patientPostort':'Staden'
    };

    // Load the webcert module and mock away everything that is not necessary.
    beforeEach(angular.mock.module('webcert', function($provide) {
        featureService = {
            features: {
                HANTERA_INTYGSUTKAST: 'HANTERA_INTYGSUTKAST'
            },
            isFeatureActive: jasmine.createSpy('isFeatureActive')
        };
        featureService.isFeatureActive.and.returnValue(true);

        authorityService = {
            isAuthorityActive: jasmine.createSpy('isAuthorityActive')
        };
        authorityService.isAuthorityActive.and.returnValue(true);

        var User = {
            getValdVardenhet: function() {
                return {
                    id: 'enhet1',
                    namn: 'Vårdenheten'
                };
            }
        };

        dialogService = {
            showDialog: function($scope, options) {

            }
        };


        $provide.value('common.featureService', featureService);
        $provide.value('common.authorityService', authorityService);
        $provide.value('common.dialogService', dialogService);
        statService = jasmine.createSpyObj('common.statService', [ 'refreshStat' ]);
        $provide.value('common.statService', statService);
        $provide.value('common.User', User);
        $provide.value('common.messageService', {});
        $provide.value('common.UserModel', {
            userContext: {authenticationScheme: null},
            user: {origin: 'NORMAL'},
            privileges: {SKRIVA_INTYG: {}},
            getActiveFeatures: function() {
            },
            hasIntygsTyp: function() {return true;},
            isLakare: function() {return true;}});

    }));

    // Get references to the object we want to test from the context.
    beforeEach(angular.mock.inject(['webcert.UtkastProxy', '$httpBackend', 'common.messageService',
        function(_UtkastProxy_, _$httpBackend_, _messageService_) {
            UtkastProxy = _UtkastProxy_;
            $httpBackend = _$httpBackend_;
            _messageService_.getProperty = function() {
                return 'Välj typ av intyg';
            };
        }]));

    describe('#createUtkast', function() {

        it('should create a draft if the payload is correct', function() {

            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');
            $httpBackend.
                expectPOST('/api/utkast/fk7263', {
                    intygType: 'fk7263',
                    patientPersonnummer: '19121212-1212',
                    patientFornamn: 'Test',
                    patientMellannamn: 'Svensson',
                    patientEfternamn: 'Testsson',
                    patientPostadress: 'Storgatan 23',
                    patientPostnummer: '12345',
                    patientPostort: 'Staden'
                }).
                respond(200, '12345');

            UtkastProxy.createUtkast(createDraftRequestPayload, onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).toHaveBeenCalledWith('12345');
            expect(onError).not.toHaveBeenCalled();
            expect(statService.refreshStat).toHaveBeenCalled();
        });

        it('should call onError if the server cannot create a draft', function() {

            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');
            $httpBackend.expectPOST('/api/utkast/fk7263').respond(500);

            UtkastProxy.createUtkast(createDraftRequestPayload, onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).not.toHaveBeenCalled();
            expect(onError).toHaveBeenCalled();
            expect(statService.refreshStat).not.toHaveBeenCalled();
        });
    });

    describe('#getUtkastTypes', function() {

        it('should call onSuccess callback with list of intyg types from the server', function() {
            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');

            $httpBackend.expectGET('/api/modules/map').respond([
                {
                    sortValue: 1,
                    id: 'fk7263',
                    label: 'Läkarintyg FK 7263',
                    detailedDescription: 'beskrivning',
                    url: 'fk7263',
                    fragaSvarAvailable: true
                },
                {
                    sortValue: 2,
                    id: 'ts-bas',
                    label: 'Transportstyrelsens läkarintyg, bas',
                    detailedDescription: 'beskrivning2',
                    url: 'ts-bas',
                    fragaSvarAvailable: false
                }
            ]);

            UtkastProxy.getUtkastTypes(onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).toHaveBeenCalledWith([
                {
                    sortValue: 0,
                    id: 'default',
                    label: 'Välj typ av intyg'
                },
                {
                    sortValue: 1,
                    id: 'fk7263',
                    label: 'Läkarintyg FK 7263',
                    detailedDescription: 'beskrivning',
                    fragaSvarAvailable: true
                },
                {
                    sortValue: 2,
                    id: 'ts-bas',
                    label: 'Transportstyrelsens läkarintyg, bas',
                    detailedDescription: 'beskrivning2',
                    fragaSvarAvailable: false
                }
            ]);
            expect(onError).not.toHaveBeenCalled();
        });

        it('should call onError if the list cannot be fetched from the server', function() {
            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');
            $httpBackend.expectGET('/api/modules/map').respond(500);

            UtkastProxy.getUtkastTypes(onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).not.toHaveBeenCalled();
            expect(onError).toHaveBeenCalled();
        });
    });
});
