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

/*globals browser,beforeAll,afterAll*/
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var restTestdataHelper = wcTestTools.helpers.restTestdata;
var LuseIntygPage = wcTestTools.pages.intyg.luse.intyg;
var LuseUtkastPage = wcTestTools.pages.intyg.luse.utkast;
var intygGenerator = wcTestTools.intygGenerator;

describe('Djupintegration on luse with updated personnummer', function() {

    var intygId = 'luse-integration-renew-1';

    var personnummer1 = '19121212-1212';
    var personnummer2 = '20121212-1212';
    var samordningsnummer = '19540187-5769';
    var reservnummer = 'AXTY11155566';

    describe('signerat intyg', function() {
        beforeEach(function() {
            browser.ignoreSynchronization = false;
            specHelper.login();
            var testData = {
                'contents': intygGenerator.getIntygJson({'intygType': 'luse', 'intygId': intygId}),
                'utkastStatus': 'SIGNED',
                'revoked': false
            };

            // If were nog ignoring sync while setting user, protractor complains that it cannot sync with angular on the testability page loaded during setUserOrigin
            browser.ignoreSynchronization = true;
            specHelper.setUserOrigin('DJUPINTEGRATION').then(function() {
                browser.ignoreSynchronization = false;
                restTestdataHelper.deleteUtkast(intygId);
                restTestdataHelper.createWebcertIntyg(testData);
            });
        });

        afterEach(function() {
            restTestdataHelper.deleteUtkast(intygId);
        });

        it('should not display new person id message', function() {
            LuseIntygPage.getIntegration(intygId, {
                alternatePatientSSn: personnummer1
            });
            expect(LuseIntygPage.isAt()).toBeTruthy();
            expect(LuseIntygPage.newPersonIdMessage.isDisplayed()).toBeFalsy();
        });

        it('should display new person id message', function() {
            LuseIntygPage.getIntegration(intygId, {
                alternatePatientSSn: personnummer2
            });
            expect(LuseIntygPage.isAt()).toBeTruthy();
            expect(LuseIntygPage.newPersonIdMessage.isDisplayed()).toBeTruthy();
            expect(LuseIntygPage.newPersonIdMessageText.getText()).toBe('Patientens personummer har ändrats');
        });

        it('should display new person id message with samordningsnummer', function() {
            LuseIntygPage.getIntegration(intygId, {
                alternatePatientSSn: samordningsnummer
            });
            expect(LuseIntygPage.isAt()).toBeTruthy();
            expect(LuseIntygPage.newPersonIdMessage.isDisplayed()).toBeTruthy();
            expect(LuseIntygPage.newPersonIdMessageText.getText()).toBe('Patientens personummer har ändrats');
        });

        it('should display new person id message with reservenumber', function() {
            LuseIntygPage.getIntegration(intygId, {
                alternatePatientSSn: reservnummer
            });
            expect(LuseIntygPage.isAt()).toBeTruthy();
            expect(LuseIntygPage.newPersonIdMessage.isDisplayed()).toBeTruthy();
            expect(LuseUtkastPage.newPersonIdMessageText.getText()).toBe('Patienten har samordningsnummer kopplat till reservnummer: ' + reservnummer + '.');
        });
    });

    describe('utkast', function() {

        var utkastId;

        beforeEach(function() {
            browser.ignoreSynchronization = false;
            specHelper.login();

            specHelper.createUtkastForPatient(personnummer1, 'Läkarutlåtande för sjukersättning');

            specHelper.getUtkastIdFromUrl().then(function(id) {
               utkastId = id;
            });

            // If were nog ignoring sync while setting user, protractor complains that it cannot sync with angular on the testability page loaded during setUserOrigin
            browser.ignoreSynchronization = true;
            specHelper.setUserOrigin('DJUPINTEGRATION').then(function() {
                browser.ignoreSynchronization = false;
            });
        });

        afterEach(function() {
            restTestdataHelper.deleteUtkast(utkastId);
        });

        it('should not display new person id message', function() {
            LuseIntygPage.getIntegration(utkastId, {
                alternatePatientSSn: personnummer1
            });
            expect(LuseUtkastPage.isAt()).toBeTruthy();
            expect(LuseUtkastPage.newPersonIdMessage.isDisplayed()).toBeFalsy();
        });

        it('should display new person id message', function() {
            LuseIntygPage.getIntegration(utkastId, {
                alternatePatientSSn: personnummer2
            });
            expect(LuseUtkastPage.isAt()).toBeTruthy();
            expect(LuseUtkastPage.newPersonIdMessage.isDisplayed()).toBeTruthy();
            expect(LuseUtkastPage.newPersonIdMessageText.getText()).toBe('Patientens personummer har ändrats');
        });

        it('should display new person id message with samordningsnummer', function() {
            LuseIntygPage.getIntegration(utkastId, {
                alternatePatientSSn: samordningsnummer
            });
            expect(LuseUtkastPage.isAt()).toBeTruthy();
            expect(LuseUtkastPage.newPersonIdMessage.isDisplayed()).toBeTruthy();
            expect(LuseUtkastPage.newPersonIdMessageText.getText()).toBe('Patientens personummer har ändrats');
        });

        it('should display new person id message with reservenumber', function() {
            LuseIntygPage.getIntegration(utkastId, {
                alternatePatientSSn: reservnummer
            });
            expect(LuseUtkastPage.isAt()).toBeTruthy();
            expect(LuseUtkastPage.newPersonIdMessage.isDisplayed()).toBeTruthy();
            expect(LuseUtkastPage.newPersonIdMessageText.getText()).toBe('Patienten har samordningsnummer kopplat till reservnummer: ' + reservnummer + '.');
        });
    });

});
