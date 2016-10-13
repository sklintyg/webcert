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
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;
var SokSkrivValjIntyg = wcTestTools.pages.sokSkrivIntyg.visaIntyg;
var intygFromJsonFactory = wcTestTools.intygFromJsonFactory;
var restUtil = wcTestTools.restUtil;

// used for some assertions of non-transformed values.
var luaefsTemplate = require('webcert-testtools/testdata/intyg.luae_fs.minimal.json');

describe('Validera visning av Intyg', function() {

    var intygsId;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
    });

    describe('Visa signerat luae_fs intyg', function() {

        it('Something...', function() {
            var intyg = intygFromJsonFactory.defaultLuaefs();
            intygsId = intyg.id;
            restUtil.createIntyg(intyg);
            SokSkrivIntygPage.selectPersonnummer('19121212-1212');
            SokSkrivValjIntyg.selectIntygById(intygsId);

            expect(IntygPage.isAt()).toBeTruthy();
        });

        it('Verifiera Grund för medicinskt underlag', function() {
            expect(IntygPage.undersokningAvPatienten.getText()).toBe('9 september 2015');
            expect(IntygPage.journaluppgifter.getText()).toBe('10 september 2015');
            expect(IntygPage.anhorigsBeskrivningAvPatienten.getText()).toBe('11 september 2015');
            expect(IntygPage.annatGrundForMU.getText()).toBe('12 september 2015');
            expect(IntygPage.annatGrundForMUBeskrivning.getText()).toBe(luaefsTemplate.annatGrundForMUBeskrivning);
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

        it('Verifiera diagnos', function() {
            expect(IntygPage.diagnos0Kod.getText()).toBe('S47');
            expect(IntygPage.diagnos0Beskrivning.getText()).toBe('Klämskada skuldra');
            expect(IntygPage.diagnos1Kod.getText()).toBe('J22');
            expect(IntygPage.diagnos1Beskrivning.getText()).toBe('Icke specificerad akut infektion i nedre luftvägarna');
        });

        it('Verifiera funktionsnedsättning', function() {
            expect(IntygPage.funktionsnedsattningDebut.getText()).toBe('Skoldansen');
            expect(IntygPage.funktionsnedsattningPaverkan.getText()).toBe('Haltar när han dansar');
        });

        it('Verifiera övrigt, kontakt och tilläggsfrågor', function() {
            expect(IntygPage.ovrigt.getText()).toBe('Detta skulle kunna innebära sämre möjlighet att få ställa upp i danstävlingar');
            expect(IntygPage.kontaktMedFkJa.getText()).toBe('Ja');
            expect(IntygPage.anledningTillKontakt.getText()).toBe('Vill stämma av ersättningen');
            expect(IntygPage.tillagsFraga1.getText()).toBe('Tämligen');
            expect(IntygPage.tillagsFraga2.getText()).toBe('Minst 3 fot');
        });

    });

    afterAll(function() {
        testdataHelper.deleteIntyg(intygsId);
        specHelper.logout();
        browser.ignoreSynchronization = false;
    });

});
