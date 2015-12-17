/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

describe('IntygProxy', function() {
    'use strict';

    var IntygProxy;
    var $httpBackend;
    var featureService;
    var dialogService;

    // Load the webcert module and mock away everything that is not necessary.
    beforeEach(angular.mock.module('webcert', function($provide) {
        featureService = {
            features: {
                HANTERA_INTYGSUTKAST: 'hanteraIntygsutkast'
            },
            isFeatureActive: jasmine.createSpy('isFeatureActive')
        };

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
        $provide.value('common.dialogService', dialogService);
        $provide.value('common.statService', jasmine.createSpyObj('common.statService', ['refreshStat']));
        $provide.value('common.User', User);
        $provide.value('common.messageService', {});

        //$provide.value('webcert.TermsState', {termsAccepted:true, transitioning:false, reset: function(){}});


    }));

    // Get references to the object we want to test from the context.
    beforeEach(angular.mock.inject(['webcert.IntygProxy', '$httpBackend',
        function(_IntygProxy_, _$httpBackend_) {
            $httpBackend = _$httpBackend_;
            IntygProxy = _IntygProxy_;
        }]));

    describe('#getUtkastForPatient', function() {

        var personId;
        beforeEach(function() {
            personId = '19121212-1212';
        });

        it('should call onSuccess callback with list of certificates for person from the server', function() {
            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');

            featureService.isFeatureActive.and.returnValue(true);

            $httpBackend.expectGET('/api/intyg/person/' + personId).respond([
                { 'intygId': 'intyg-1', 'source': 'IT', 'intygType': 'fk7263', 'status': 'SENT',
                    'lastUpdatedSigned': '2011-03-23T09:29:15.000', 'updatedSignedBy': 'Eva Holgersson', 'vidarebefordrad': false }
            ]);

            IntygProxy.getIntygForPatient(personId, onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).toHaveBeenCalledWith([
                { 'intygId': 'intyg-1', 'source': 'IT', 'intygType': 'fk7263', 'status': 'SENT',
                    'lastUpdatedSigned': '2011-03-23T09:29:15.000', 'updatedSignedBy': 'Eva Holgersson', 'vidarebefordrad': false }
            ]);
            expect(onError).not.toHaveBeenCalled();
        });

        it('should call onError if the list cannot be fetched from the server', function() {
            var onSuccess = jasmine.createSpy('onSuccess');
            var onError = jasmine.createSpy('onError');
            $httpBackend.expectGET('/api/intyg/person/' + personId).respond(500);

            IntygProxy.getIntygForPatient(personId, onSuccess, onError);
            $httpBackend.flush();

            expect(onSuccess).not.toHaveBeenCalled();
            expect(onError).toHaveBeenCalled();
        });
    });
});
