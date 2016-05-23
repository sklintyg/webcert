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

/*globals browser */
/*globals describe,it */
/*globals beforeAll,afterAll */
/*globals protractor */
'use strict';

var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var LuseUtkastPage = wcTestTools.pages.intyg.luse.utkast;
var testdataHelper = wcTestTools.helpers.restTestdata;
var intygGenerator = wcTestTools.intygGenerator;

describe('Luse attic tests', function() {

    var intygsId;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();

        testdataHelper.createUtkast('luse').then(function(response){
            var utkast = response.body;
            intygsId = utkast.intygsId;

            var utkastData = JSON.parse(intygGenerator.buildIntyg({
                intygType: 'luse',
                intygId: intygsId,
                personnr: utkast.patientPersonnummer
            }).document);

            testdataHelper.saveUtkast('luse', intygsId, utkast.version, utkastData, function(){
            });
        });
    });

    afterAll(function() {
        testdataHelper.deleteUtkast(intygsId);
    });

    it('should load utkast', function () {
        LuseUtkastPage.get(intygsId);
        LuseUtkastPage.showMissingInfoButtonClick();

        expect(LuseUtkastPage.getMissingInfoMessagesCount()).toBe(0);
    });

    describe('annat', function() {
        it('should still be valid if annat is empty', function() {
            LuseUtkastPage.baseratPa.annat.checkbox.sendKeys(protractor.Key.SPACE);
            LuseUtkastPage.showMissingInfoButtonClick(true);

            expect(LuseUtkastPage.baseratPa.annat.datum.getAttribute('value')).toBe('');
            expect(LuseUtkastPage.baseratPa.annat.beskrivning.isPresent()).toBeFalsy();
            // annatBeskrivning should be removed from the model sent to the server
            // if it is still present we should get a validationerror here.
            expect(LuseUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });

        it ('should restore annatBeskrivning if annat is specified again', function() {
            LuseUtkastPage.baseratPa.annat.datum.sendKeys('2016-12-12');
            LuseUtkastPage.showMissingInfoButtonClick(true);

            expect(LuseUtkastPage.baseratPa.annat.datum.getAttribute('value')).toBe('2016-12-12');
            expect(LuseUtkastPage.baseratPa.annat.beskrivning.getAttribute('value')).toBe('Annat underlag.');
            expect(LuseUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });
    });

    describe('underlag', function() {
        it('should still be valid if underlagFinns is set to no', function() {
            LuseUtkastPage.andraMedicinskaUtredningar.finns.NEJ.sendKeys(protractor.Key.SPACE);
            LuseUtkastPage.showMissingInfoButtonClick(true);

            expect(LuseUtkastPage.andraMedicinskaUtredningar.underlagRow(0).underlag.isPresent()).toBeFalsy();
            expect(LuseUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });

        it('should restore underlag if underlagFinns is set to yes again', function() {
            LuseUtkastPage.andraMedicinskaUtredningar.finns.JA.sendKeys(protractor.Key.SPACE);
            LuseUtkastPage.showMissingInfoButtonClick(true);

            expect(LuseUtkastPage.andraMedicinskaUtredningar.underlagRow(0).underlag.getAttribute('value')).toBe('string:HABILITERING');
            expect(LuseUtkastPage.andraMedicinskaUtredningar.underlagRow(0).datum.getAttribute('value')).toBe('2016-04-07');
            expect(LuseUtkastPage.andraMedicinskaUtredningar.underlagRow(0).information.getAttribute('value')).toBe('Information om utredning.');
            expect(LuseUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });
    });

    describe('skalTillNyBedomning', function() {
        it('should still be valid if skalTillNyBedomning is set to no', function() {
            LuseUtkastPage.diagnos.skalTillNyBedomning.NEJ.sendKeys(protractor.Key.SPACE);
            LuseUtkastPage.showMissingInfoButtonClick(true);

            expect(LuseUtkastPage.diagnos.diagnosForNyBedomning.isPresent()).toBeFalsy();
            expect(LuseUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });

        it('should restore diagnosForNyBedomning if skalTillNyBedomning is set to yes again', function() {
            LuseUtkastPage.diagnos.skalTillNyBedomning.JA.sendKeys(protractor.Key.SPACE);
            LuseUtkastPage.showMissingInfoButtonClick(true);

            expect(LuseUtkastPage.diagnos.diagnosForNyBedomning.getAttribute('value')).toBe('Hela diagnosen kan vara trasig');
            expect(LuseUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });
    });

    describe('kontaktMedFk', function() {
        it('should still be valid if kontaktMedFk is set to no', function() {
            LuseUtkastPage.kontaktMedFK.sendKeys(protractor.Key.SPACE);
            LuseUtkastPage.showMissingInfoButtonClick(true);

            expect(LuseUtkastPage.anledningTillKontakt.isPresent()).toBeFalsy();
            expect(LuseUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });

        it('should restore anledningTillKontakt if kontaktMedFk is set to yes again', function() {
            LuseUtkastPage.kontaktMedFK.sendKeys(protractor.Key.SPACE);
            LuseUtkastPage.showMissingInfoButtonClick(true);

            expect(LuseUtkastPage.anledningTillKontakt.getAttribute('value')).toBe('Kontaktinfo.');
            expect(LuseUtkastPage.getMissingInfoMessagesCount()).toBe(0);
        });
    });
});
