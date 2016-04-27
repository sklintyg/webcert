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
var UtkastPage = wcTestTools.pages.intyg.luae_fs.utkast;
var IntygPage = wcTestTools.pages.intyg.luae_fs.intyg;

fdescribe('Create and Sign luae_fs utkast', function() {

    var utkastId = null;

    beforeAll(function() {
        specHelper.login();
        specHelper.createUtkastForPatient('191212121212', 'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång');
    });

    describe('Skapa luae_fs utkatst', function() {

        describe('Interagera med utkastet', function() {

            it('Spara undan intygsId från URL', function() {
            //
                // Save id so it can be removed in cleanup stage.
                browser.getCurrentUrl().then(function(url) {
                    utkastId = url.split('/').pop();
                });
            });

            describe('Fyll i luae_fs intyg', function() {

                it('Grund - baserat på', function() {
                    UtkastPage.angeIntygetBaserasPa( {
                        minUndersokning : {
                            datum: '2016-04-22'
                        }});

                    UtkastPage.angeIntygetBaserasPa( {
                        kannedomOmPatient : {
                            datum: '2016-04-21'
                        }});

                    expect(UtkastPage.baseratPa.kannedomOmPatient.datum.getAttribute('value')).toBe('2016-04-21');
                });

                it('Andra medicinska utredningar eller underlag', function() {
                    var utredningar = [{
                        underlag: 'Neuropsykiatriskt utlåtande',
                        datum: '2016-04-16',
                        infoOmUtredningen: 'Hämtas hos posten'
                    }];
                    UtkastPage.angeAndraMedicinskaUtredningar(utredningar);
                });

                it('Ange diagnoser', function() {
                    browser.ignoreSynchronization = false;
                    var diagnosObj = {
                        diagnoser: [{'kod':'J21'},{'kod':'J22'},{'kod':'A21'}]
                    };
                    UtkastPage.angeDiagnos(diagnosObj);
                    browser.ignoreSynchronization = true;
                    expect(UtkastPage.getNumberOfDiagnosRows()).toBe(3)
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
                    UtkastPage.tillaggsfragor1svar.sendKeys('Likheten på en struts? Båda benen är lika långa, särskilt det vänstra.');
                });

                it('Signera intyget', function() {

                    // reset
                    browser.ignoreSynchronization = false;

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
