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
var UtkastPage = wcTestTools.pages.intyg.luaeFS.utkast;
var IntygPage = wcTestTools.pages.intyg.luaeFS.intyg;

// Use fdescribe to run in isolation.
describe('Create and Sign luae_fs utkast', function() {

    var utkastId = null;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
        specHelper.createUtkastForPatient('191212121212', 'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång');
    });

    describe('Skapa luae_fs utkatst', function() {

        describe('Interagera med utkastet', function() {

            it('Spara undan intygsId från URL', function() {

                // Save id so it can be removed in cleanup stage.
                browser.getCurrentUrl().then(function(url) {
                    utkastId = url.split('/').pop();
                });
            });

            describe('Fyll i luae_fs intyg', function() {

                it('tomt utkast skall visa lista med fel efter klick på "Visa vad som saknas"', function() {

                    UtkastPage.disableAutosave();

                    UtkastPage.showMissingInfoButtonClick();

                    expect(UtkastPage.getMissingInfoMessagesCount()).toBe(4);

                });

                it('Grund - baserat på', function() {

                    var promiseArr = [];

                    promiseArr.push(UtkastPage.angeIntygetBaserasPa({
                        minUndersokningAvPatienten: {
                            datum: '2016-04-22'
                        }
                    }));

                    promiseArr.push(UtkastPage.angeIntygetBaserasPa({
                        kannedomOmPatient: {
                            datum: '2016-04-21'
                        }
                    }));

                    promiseArr.push(UtkastPage.angeIntygetBaserasPa({
                        annat: {
                            datum: '2016-04-23',
                            beskrivning: 'Utlåtande från skolledningen'
                        }
                    }));

                    Promise.all(promiseArr);

                    expect(UtkastPage.baseratPa.minUndersokningAvPatienten.datum.getAttribute('value')).toBe('2016-04-22');
                    expect(UtkastPage.baseratPa.kannedomOmPatient.datum.getAttribute('value')).toBe('2016-04-21');
                    expect(UtkastPage.baseratPa.annat.datum.getAttribute('value')).toBe('2016-04-23');
                    expect(UtkastPage.baseratPa.annat.beskrivning.getAttribute('value')).toBe('Utlåtande från skolledningen');
                });

                it('Andra medicinska utredningar eller underlag', function() {
                    var utredningar = [{
                        underlag: 'Neuropsykiatriskt utlåtande',
                        datum: '2016-04-16',
                        infoOmUtredningen: 'Hämtas hos posten'
                    }];
                    UtkastPage.angeAndraMedicinskaUtredningar(utredningar);

                    expect(UtkastPage.getNumberOfUnderlag()).toBe(1);
                });

                it('Lägg till ytterligare ett annat underlag, ta bort igen.', function() {
                    UtkastPage.clickCreateUnderlag();
                    expect(UtkastPage.getNumberOfUnderlag()).toBe(2);

                    UtkastPage.clickRemoveUnderlag(1);
                    expect(UtkastPage.getNumberOfUnderlag()).toBe(1);
                });

                it('Ange diagnoser', function() {
                    var diagnosObj = {
                        diagnoser: [{
                            'kod': 'J21'
                        }, {
                            'kod': 'J22'
                        }, {
                            'kod': 'A21'
                        }]
                    };
                    UtkastPage.angeDiagnos(diagnosObj);

                    expect(UtkastPage.getNumberOfDiagnosRows()).toBe(3);

                    UtkastPage.taBortDiagnos(1);
                    expect(UtkastPage.getNumberOfDiagnosRows()).toBe(2);
                });

                it('Ange funktionsnedsättningar', function() {
                    UtkastPage.funktionsnedsattningDebut.sendKeys('Komplex tango på skoldansen, knäfraktur.');
                    UtkastPage.funktionsnedsattningPaverkan.sendKeys('Dansen funkar inte längre, svårt att fullfölja baletten.');
                });

                it('Ange övrigt', function() {
                    UtkastPage.ovrigt.sendKeys('Behöver nog ett par år extra för att komma ikapp efter skadan.');
                });

                it('Ange kontakt önskas', function() {
                    var promiseArr = [];
                    promiseArr.push(UtkastPage.kontaktMedFkNo.click());
                    promiseArr.push(UtkastPage.anledningTillKontakt.sendKeys('Patienten känner att en avstämning vore bra.'));

                    Promise.all(promiseArr);
                });

                it('Ange tilläggsfrågor', function() {
                    UtkastPage.tillaggsfragor0svar.sendKeys('Vad för slags fråga är det där?!?!?');
                    UtkastPage.enableAutosave();
                    UtkastPage.tillaggsfragor1svar.sendKeys('Likheten på en struts? Båda benen är lika långa, särskilt det vänstra.');
                });

                it('Signera intyget', function() {
                    UtkastPage.whenSigneraButtonIsEnabled().then(function() {
                        UtkastPage.signeraButtonClick();
                        expect(IntygPage.isAt()).toBeTruthy();
                    });
                });
            });
        });
    });

    afterAll(function() {
        testdataHelper.deleteIntyg(utkastId);
        testdataHelper.deleteUtkast(utkastId);
        specHelper.logout();
    });

});
