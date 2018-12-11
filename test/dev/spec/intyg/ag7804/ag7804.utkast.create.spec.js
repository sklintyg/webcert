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

/*globals afterAll,beforeAll,describe,it,browser */
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.restTestdata;
var UtkastPage = wcTestTools.pages.intyg.ag.ag7804.utkast;
var IntygPage = wcTestTools.pages.intyg.ag.ag7804.intyg;
var ValjIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;

describe('Create and Sign an AG7804 utkast', function() {

    var utkastId = null,
        data = null;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
    });

    describe('smittskydd', function() {
        beforeAll(function() {
            ValjIntygPage.get();
            specHelper.createUtkastForPatient('191212121212', 'ag7804');
        });

        describe('Fyll i intyget', function() {

            it('Spara undan intygsId från URL', function() {
                UtkastPage.disableAutosave();

                specHelper.getUtkastIdFromUrl().then(function(id) {
                    utkastId = id;
                });
                data = wcTestTools.testdata.ag.ag7804.get(utkastId, true);
            });

            it('angeSmittskydd', function() {
                UtkastPage.angeSmittskydd(data.smittskydd);
            });
            it('angeDiagnos', function() {
                UtkastPage.angeDiagnos(data.diagnos);
            });
            it('angeArbetsformaga', function() {
                UtkastPage.angeArbetsformaga(data.arbetsformaga);
                UtkastPage.enableAutosave();
            });
            it('angeOvrigaUpplysningar', function() {
                UtkastPage.angeOvrigt(data.ovrigt);
            });
            it('angeKontaktMedAg', function() {
                UtkastPage.angeKontakt(data.kontaktMedAg, data.anledningTillKontakt);
            });
        });

        it('Signera intyget', function() {
            UtkastPage.whenSigneraButtonIsEnabled();

            UtkastPage.signeraButtonClick();

            expect(IntygPage.isAt()).toBeTruthy();

        });

        it('Verifiera intyg', function() {
            // Om intyget inte hunnit processas av IT så hämtas det från WC. Då är inte uppgifter flyttade till övriga
            // upplysningar ännu.
            // Vänta tills intyget tagits emot av IT. Ladda därefter om sidan så datan säkert kommer från IT.
            IntygPage.waitUntilIntygInIT(utkastId);
            browser.refresh();

            IntygPage.whenCertificateLoaded().then(function() {
		        IntygPage.verify(data);
		    });

            expect(IntygPage.skrivUtBtn.isDisplayed()).toBeTruthy();
        });

        afterAll(function() {
            testdataHelper.deleteIntyg(utkastId);
            testdataHelper.deleteUtkast(utkastId);
        });
    });

    describe('normalt intyg', function() {
        beforeAll(function() {
            ValjIntygPage.get();
            specHelper.createUtkastForPatient('191212121212', 'ag7804');
        });

        describe('Fyll i intyget', function() {

            it('Spara undan intygsId från URL', function() {
                UtkastPage.disableAutosave();

                specHelper.getUtkastIdFromUrl().then(function(id) {
                    utkastId = id;
                });
                data = wcTestTools.testdata.ag.ag7804.get(utkastId);
            });


            it('angeBaseratPa', function() {
                UtkastPage.angeBaseratPa(data.baseratPa);
            });
            it('angeSysselsättning', function() {
                UtkastPage.angeSysselsattning(data.sysselsattning);
            });
            it('angeDiagnos', function() {
                UtkastPage.angeDiagnos(data.diagnos);
            });
            it('angeKonsekvenser', function() {
                UtkastPage.angeKonsekvenser(data);
            });
            it('angeMedicinskBehandling', function() {
                UtkastPage.angeMedicinskBehandling(data.medicinskbehandling);
            });
            it('angeArbetsformaga', function() {
                UtkastPage.angeArbetsformaga(data.arbetsformaga);
            });
            it('angeArbetstidsforlaggning', function() {
                UtkastPage.angeArbetstidsforlaggning(data.arbetstidsforlaggning);
            });
            it('angeResorTillArbete', function() {
                UtkastPage.angeArbetsformaga(data.resorTillArbete);
            });
            it('angePrognosForArbetsformaga', function() {
                UtkastPage.angePrognosForArbetsformaga(data.prognosForArbetsformaga);
            });
            it('angeAtgarder', function() {
                UtkastPage.angeAtgarder(data.atgarder);
            });
            it('angeOvrigaUpplysningar', function() {
                UtkastPage.angeOvrigt(data.ovrigt);
            });

            it('angeKontaktMedAG', function() {
                UtkastPage.enableAutosave();
                UtkastPage.angeKontakt(data.kontaktMedAg, data.anledningTillKontakt);
            });

        });

        it('Signera intyget', function() {
            UtkastPage.whenSigneraButtonIsEnabled();

            UtkastPage.signeraButtonClick();

            expect(IntygPage.isAt()).toBeTruthy();
        });

        it('Wait until intyg in IT', function() {
            // Om intyget inte hunnit processas av IT så hämtas det från WC. Då är inte uppgifter flyttade till övriga
            // upplysningar ännu.
            // Vänta tills intyget tagits emot av IT. Ladda därefter om sidan så datan säkert kommer från IT.
            IntygPage.waitUntilIntygInIT(utkastId);
            browser.refresh();
        });

        it('Verifiera intyg', function() {
            IntygPage.whenCertificateLoaded().then(function() {
		        IntygPage.verify(data);
		    });
        });

        afterAll(function() {
            testdataHelper.deleteIntyg(utkastId);
            testdataHelper.deleteUtkast(utkastId);
        });
    });

});
