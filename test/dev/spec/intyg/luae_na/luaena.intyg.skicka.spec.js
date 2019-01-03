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

/*globals afterAll,beforeAll,describe,it,browser,protractor */
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.restTestdata;
var IntygPage = wcTestTools.pages.intyg.luaeNA.intyg;
var SokSkrivValjIntyg = wcTestTools.pages.sokSkrivIntyg.visaIntyg;
var intygFromJsonFactory = wcTestTools.intygFromJsonFactory;
var restUtil = wcTestTools.restUtil;
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;

describe('Validera sändning av luae_na Intyg', function() {

    var intyg = intygFromJsonFactory.defaultLuaena();
    var intygsId = intyg.id;

    beforeAll(function() {
        testdataHelper.deleteIntyg(intygsId);
        browser.ignoreSynchronization = false;
        specHelper.login();
    });

    describe('Visa signerat luae_na intyg', function() {

        it('Skapa intyget och gå till intygssidan', function() {
            restUtil.createIntyg(intyg);
            SokSkrivIntygPage.selectPersonnummer('19121212-1212');
            SokSkrivValjIntyg.selectIntygById(intygsId);

            expect(IntygPage.isAt()).toBeTruthy();
            expect(IntygPage.skicka.statusSent.isPresent()).toBeFalsy();
        });

        it('Skicka intyget', function() {
            IntygPage.skicka.knapp.sendKeys(protractor.Key.SPACE);
            browser.wait(IntygPage.skicka.dialogKnapp.isDisplayed())
                .then(IntygPage.skicka.dialogKnapp.sendKeys(protractor.Key.SPACE));


            element.all(by.id('#sendBtn')).then(function(items) {
                expect(items.length).toBe(0);
            });

            expect(IntygPage.skicka.statusSent.isDisplayed()).toBeTruthy();
        });
    });

    afterAll(function() {
        testdataHelper.deleteIntyg(intygsId);
        specHelper.logout();
        browser.ignoreSynchronization = false;
    });

});
