/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

/*globals browser */
/*globals describe,it */
/*globals beforeAll,afterAll */
/*globals protractor */
'use strict';

var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var UtkastPage = wcTestTools.pages.intyg.luaeNA.utkast;
var testdataHelper = wcTestTools.helpers.restTestdata;
var intygGenerator = wcTestTools.intygGenerator;

describe('luae_na attic tests', function() {

    var intygsId;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();

        testdataHelper.createUtkast('luae_na').then(function(response) {
            var utkast = response.body;
            intygsId = utkast.intygsId;

            var utkastData = JSON.parse(intygGenerator.buildIntyg({
                intygType: 'luae_na',
                intygId: intygsId,
                personnr: utkast.patientPersonnummer
            }).document);

            testdataHelper.saveUtkast('luae_na', intygsId, utkast.version, utkastData, function() {});
        });
    });

    afterAll(function() {
        testdataHelper.deleteUtkast(intygsId);
    });

    it('should load utkast', function() {
        UtkastPage.get(intygsId);
        UtkastPage.disableAutosave();
    });

    describe('annat', function() {
        it('should still be valid if annat is empty', function() {
            UtkastPage.baseratPa.annat.checkbox.sendKeys(protractor.Key.SPACE);

            expect(UtkastPage.baseratPa.annat.datum.getAttribute('value')).toBe('');
            expect(UtkastPage.baseratPa.annat.beskrivning.isPresent()).toBeFalsy();
            // annatBeskrivning should be removed from the model sent to the server
            // if it is still present we should get a validationerror here.
        });

        it('should restore annatBeskrivning if annat is specified again', function() {
            UtkastPage.baseratPa.annat.datum.sendKeys('2016-12-12');

            expect(UtkastPage.baseratPa.annat.datum.getAttribute('value')).toBe('2016-12-12');
            expect(UtkastPage.baseratPa.annat.beskrivning.getAttribute('value')).toBe('Annat');
        });
    });

    describe('underlag', function() {
        it('should still be valid if underlagFinns is set to no', function() {
            UtkastPage.andraMedicinskaUtredningar.finns.NEJ.sendKeys(protractor.Key.SPACE);

            expect(UtkastPage.andraMedicinskaUtredningar.underlagRow(0).underlag.isPresent()).toBeFalsy();
        });

        it('should restore underlag if underlagFinns is set to yes again', function() {
            UtkastPage.andraMedicinskaUtredningar.finns.JA.sendKeys(protractor.Key.SPACE);

            expect(UtkastPage.andraMedicinskaUtredningar.underlagRow(0).underlag.element(by.css('.ui-select-match-text')).getText()).toBe('Underlag från habiliteringen');
            expect(UtkastPage.andraMedicinskaUtredningar.underlagRow(0).datum.getAttribute('value')).toBe('2016-04-26');
            expect(UtkastPage.andraMedicinskaUtredningar.underlagRow(0).information.getAttribute('value')).toBe('vårdgivare');
        });
    });

    describe('skalTillNyBedomning', function() {
        it('should still be valid if skalTillNyBedomning is set to no', function() {
            UtkastPage.diagnos.skalTillNyBedomning.NEJ.sendKeys(protractor.Key.SPACE);

            expect(UtkastPage.diagnos.diagnosForNyBedomning.isPresent()).toBeFalsy();
        });

        it('should restore diagnosForNyBedomning if skalTillNyBedomning is set to yes again', function() {
            UtkastPage.diagnos.skalTillNyBedomning.JA.sendKeys(protractor.Key.SPACE);

            expect(UtkastPage.diagnos.diagnosForNyBedomning.getAttribute('value')).toBe('Diagnos 2');
        });
    });

    describe('kontaktMedFk', function() {
        it('should still be valid if kontaktMedFk is set to no', function() {
            UtkastPage.kontaktMedFK.sendKeys(protractor.Key.SPACE);

            expect(UtkastPage.anledningTillKontakt.isPresent()).toBeFalsy();
        });

        it('should restore anledningTillKontakt if kontaktMedFk is set to yes again', function() {
            UtkastPage.enableAutosave();
            UtkastPage.kontaktMedFK.sendKeys(protractor.Key.SPACE);

            expect(UtkastPage.anledningTillKontakt.getAttribute('value')).toBe('Kontaktorsak');
        });
    });
});
