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

/*globals describe,it,beforeAll,afterAll,browser */
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.restTestdata;
var UtkastPage = wcTestTools.pages.intyg.fk['7263'].utkast;

describe('Create a FK7263 draft and verify behaviour of diagnosis fields', function() {

    var utkastId = null;

    describe('Login through the welcome page', function() {

        it('with user', function() {
            browser.ignoreSynchronization = false;
            specHelper.login();
            specHelper.createUtkastForPatient('191212121212', 'Läkarintyg FK 7263');
        });

    });

    describe('Interact with utkast', function() {

        // Save id so it can be removed in cleanup stage.
        browser.getCurrentUrl().then(function(url) {
            utkastId = url.split('/').pop();
        });

        it('Verify the initial status of diagnosis fields', function() {
            expect(UtkastPage.diagnosKod.isEnabled()).toBe(true);
            expect(UtkastPage.diagnosKod2.isEnabled()).toBe(false);
            expect(UtkastPage.diagnosKod3.isEnabled()).toBe(false);
        });

        describe('populate FK7263 diagnosis fields', function() {

            // speeds up utkast filling by not waiting for angular events, promises etc.
            browser.ignoreSynchronization = true;

            it('add first diagnosis field', function() {
                UtkastPage.angeDiagnosKod('F220', UtkastPage.diagnosKod);
                expect(UtkastPage.diagnosKod2.isEnabled()).toBe(true);
                expect(UtkastPage.diagnosBeskrivning2.isEnabled()).toBe(true);
                expect(UtkastPage.diagnosKod3.isEnabled()).toBe(false);
                expect(UtkastPage.diagnosBeskrivning3.isEnabled()).toBe(false);
            });

            it('add second diagnosis field', function() {
                UtkastPage.angeDiagnosKod('A37', UtkastPage.diagnosKod2);
                expect(UtkastPage.diagnosKod2.isEnabled()).toBe(true);
                expect(UtkastPage.diagnosBeskrivning2.isEnabled()).toBe(true);
                expect(UtkastPage.diagnosKod3.isEnabled()).toBe(true);
                expect(UtkastPage.diagnosBeskrivning3.isEnabled()).toBe(true);
            });

            it('add third diagnosis field', function() {
                UtkastPage.angeDiagnosKod('B222', UtkastPage.diagnosKod3);
                expect(UtkastPage.diagnosKod2.isEnabled()).toBe(true);
                expect(UtkastPage.diagnosBeskrivning2.isEnabled()).toBe(true);
                expect(UtkastPage.diagnosKod3.isEnabled()).toBe(true);
                expect(UtkastPage.diagnosBeskrivning3.isEnabled()).toBe(true);
            });

            it('clear second diagnosis field', function() {
                UtkastPage.diagnosKod2.clear();
                expect(UtkastPage.diagnosKod2.isEnabled()).toBe(true);
                expect(UtkastPage.diagnosBeskrivning2.isEnabled()).toBe(true);
                expect(UtkastPage.diagnosKod3.isEnabled()).toBe(true);
                expect(UtkastPage.diagnosBeskrivning3.isEnabled()).toBe(true);
            });

            it('clear first diagnosis field', function() {
                UtkastPage.diagnosKod.clear();
                expect(UtkastPage.diagnosKod2.isEnabled()).toBe(false);
                expect(UtkastPage.diagnosBeskrivning2.isEnabled()).toBe(false);
                expect(UtkastPage.diagnosKod3.isEnabled()).toBe(true);
                expect(UtkastPage.diagnosBeskrivning3.isEnabled()).toBe(true);
            });

            it('clear third diagnosis field', function() {
                UtkastPage.diagnosKod3.clear();
                expect(UtkastPage.diagnosKod2.isEnabled()).toBe(false);
                expect(UtkastPage.diagnosBeskrivning2.isEnabled()).toBe(false);
                expect(UtkastPage.diagnosKod3.isEnabled()).toBe(false);
                expect(UtkastPage.diagnosBeskrivning3.isEnabled()).toBe(false);
            });

        });

        describe('populate FK7263 diagnosis fields with long descriptions', function() {

            // speeds up utkast filling by not waiting for angular events, promises etc.
            browser.ignoreSynchronization = true;

            it('add first and second diagnosis fields', function() {
                UtkastPage.angeDiagnosKod('F220', UtkastPage.diagnosKod);
                UtkastPage.angeDiagnosKod('Y1100', UtkastPage.diagnosKod2);
            });

            it('verify that third diagnosis field are disabled', function() {
                expect(UtkastPage.diagnosKod3.isEnabled()).toBe(false);
                expect(UtkastPage.diagnosBeskrivning3.isEnabled()).toBe(false);
            });

            it('verify that clarification field is enabled but input is disallowed', function() {
                expect(UtkastPage.diagnos.fortydligande.isEnabled()).toBe(true);
                UtkastPage.angeDiagnosFortydligande('Lägg till förtydligande');
                expect(UtkastPage.diagnos.fortydligande.getText()).toBe('');
            });

        });

    });

    describe('remove test intyg', function() {
        it('should clean up all utkast after the test', function() {
            testdataHelper.deleteIntyg(utkastId);
            testdataHelper.deleteUtkast(utkastId);
        });
    });

});
