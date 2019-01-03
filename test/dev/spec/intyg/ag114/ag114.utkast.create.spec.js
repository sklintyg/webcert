/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
var UtkastPage = wcTestTools.pages.intyg.ag['ag114'].utkast;
var IntygPage = wcTestTools.pages.intyg.ag['ag114'].intyg;

describe('Create and Sign ag114 utkast', function() {

    var utkastId = null,
        data = null;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
        testdataHelper.deleteAllUtkast();
        testdataHelper.deleteAllIntyg();
        specHelper.createUtkastForPatient('191212121212', 'ag114');
    });

    it('Spara undan intygsId från URL', function() {
        UtkastPage.disableAutosave();

        specHelper.getUtkastIdFromUrl().then(function(id) {
            utkastId = id;
        });

        data = wcTestTools.testdata.ag.ag114.get(utkastId);
    });

    describe('Skapa ag114', function() {

        describe('Fyll i intyget', function() {

            it('angeSysselsattning', function() {
                UtkastPage.angeSysselsattning(data.sysselsattning);
            });
            it('angeOnskarFormedlaDiagnos', function () {
                UtkastPage.angeOnskarFormedlaDiagnos(data.onskarFormedlaDiagnos)
            });
            it('angeNedsattArbetsformaga', function () {
                UtkastPage.angeNedsattArbetsformaga(data.nedsattArbetsformaga)
            });
            it('angeBedomning', function () {
                UtkastPage.angeBedomning(data.bedomning)
            });
            it('angeOvrigt', function () {
                UtkastPage.angeOvrigt(data.ovrigt)
                UtkastPage.enableAutosave();
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
            IntygPage.verify(data);
        });
    });

    afterAll(function() {
        testdataHelper.deleteIntyg(utkastId);
        testdataHelper.deleteUtkast(utkastId);
    });

});
