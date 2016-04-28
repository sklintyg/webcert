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

/*globals describe,it,browser */
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.restTestdata;
var IntygPage = wcTestTools.pages.intyg.luae_fs.intyg;
var SokSkrivValjIntyg = wcTestTools.pages.sokSkrivIntyg.visaIntyg;
var luaefsTemplate = require('webcert-testtools/testdata/luae_fs-minimal.json');
var restUtil = require('webcert-testtools/util/rest.util.js');
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;

// Use fdescribe to run in isolation.
describe('Validera visning av Intyg', function() {

    var intygsId = 'intyg-visa-test-1';

    function buildLuaefsIntyg() {
        var certHolder = {
            id: intygsId,
            document: JSON.stringify(luaefsTemplate),
            originalCertificate: '',
            type: 'luae_fs',
            signingDoctorName: 'Jan Nilsson',
            careUnitId: 'IFV1239877878-1042',
            careUnitName: 'WebCert Enhet 1',
            careGiverId: 'IFV1239877878-1041',
            civicRegistrationNumber: '19121212-1212',
            signedDate: '2016-04-28',
            validFromDate: null,
            validToDate: null,
            additionalInfo: '',
            deleted: false,
            deletedByCareGiver: false,
            certificateStates: [{
                target: 'HV',
                state: 'RECEIVED',
                timestamp: '2016-04-28T14:00:00.000'
            }],
            revoked: false
        };
        return certHolder;
    }

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
    });

    describe('Visa signerat luae_fs intyg', function() {

        it('Something...', function() {
            restUtil.createIntyg(buildLuaefsIntyg());
            SokSkrivIntygPage.selectPersonnummer('19121212-1212');
            SokSkrivValjIntyg.selectIntygById(intygsId);

            expect(IntygPage.isAt()).toBeTruthy();
        });

        it('Verifiera Grund för medicinskt underlag', function() {
            expect(IntygPage.undersokningAvPatienten.getText()).toBe('9 september 2015');
            expect(IntygPage.journaluppgifter.getText()).toBe('10 september 2015');
            expect(IntygPage.anhorigsBeskrivningAvPatienten.getText()).toBe('11 september 2015');
            expect(IntygPage.annatGrundForMU.getText()).toBe('12 september 2015');
            expect(IntygPage.annatGrundForMUBeskrivning.getText()).toBe('Detta är en annan beskrivning');
            expect(IntygPage.kannedomOmPatient.getText()).toBe('7 januari 2015');
        });

        it('Verifiera underlag', function() {
            expect(IntygPage.underlagFinnsJa.getText()).toBe('Ja');

            expect(IntygPage.underlag0Typ.getText()).toBe('Underlag från psykolog');
            expect(IntygPage.underlag0Datum.getText()).toBe('3 september 2015');
            expect(IntygPage.underlag0HamtasFran.getText()).toBe('Skickas med posten');

            expect(IntygPage.underlag1Typ.getText()).toBe('Underlag från habiliteringen');
            expect(IntygPage.underlag1Datum.getText()).toBe('4 september 2015');
            expect(IntygPage.underlag1HamtasFran.getText()).toBe('Arkivet');

        });

    });

    afterAll(function() {
        testdataHelper.deleteIntyg(intygsId);
        specHelper.logout();
        browser.ignoreSynchronization = true;
    });

});
