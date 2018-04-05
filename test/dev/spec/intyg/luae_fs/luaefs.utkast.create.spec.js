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

describe('Create and Sign luae_fs utkast', function() {

    var utkastId = null;
    var data = null;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
        specHelper.createUtkastForPatient('191212121212',
            'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång');
    });

    describe('Fyll i intyget', function() {

        it('Spara undan intygsId från URL', function() {
            UtkastPage.disableAutosave();

            // Save id so it can be removed in cleanup stage.
            specHelper.getUtkastIdFromUrl().then(function(id) {
                utkastId = id;
            });
            data = wcTestTools.testdata.fk.LUAE_FS.get(utkastId);
        });

        it('tomt utkast skall visa lista med fel efter klick på Signera', function() {
            UtkastPage.signeraButtonClick();

            expect(UtkastPage.getMissingInfoMessagesCount()).toBe(3);
        });

        it('angeBaseratPa', function() {
            UtkastPage.angeBaseratPa(data.baseratPa);
        });

        it('angeAndraMedicinskaUtredningar', function() {
            UtkastPage.angeAndraMedicinskaUtredningar(data.andraMedicinskaUtredningar);
        });

        it('angeDiagnos', function() {
            UtkastPage.angeDiagnos(data.diagnos);
        });

        it('angeFunktionsnedsattning', function() {
            UtkastPage.angeFunktionsnedsattning(data.funktionsnedsattning);
        });

        it('angeOvrigaUpplysningar', function() {
            UtkastPage.enableAutosave();
            UtkastPage.angeOvrigaUpplysningar(data.ovrigt);
        });

        it('angeKontaktMedFK', function() {
            UtkastPage.angeKontaktMedFK(data.kontaktMedFk);
        });
/* no tilläggsfrågor atm. activate if we get such in the future
        it('Ange tilläggsfrågor', function() {
            UtkastPage.tillaggsfragor0svar.sendKeys('Vad för slags fråga är det där?!?!?');
            UtkastPage.enableAutosave();
            UtkastPage.tillaggsfragor1svar.sendKeys(
                'Likheten på en struts? Båda benen är lika långa, särskilt det vänstra.');
        });
*/
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

            IntygPage.whenCertificateLoaded();

            IntygPage.verify(data);
        });
    });

    afterAll(function() {
        testdataHelper.deleteIntyg(utkastId);
        testdataHelper.deleteUtkast(utkastId);
    });

});
