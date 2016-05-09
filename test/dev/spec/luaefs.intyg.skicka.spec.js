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
var IntygPage = wcTestTools.pages.intyg.luaeFS.intyg;
var SokSkrivValjIntyg = wcTestTools.pages.sokSkrivIntyg.visaIntyg;
var intygFromJsonFactory = wcTestTools.intygFromJsonFactory;
var restUtil = wcTestTools.restUtil;
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;

// Use fdescribe to run in isolation.
describe('Validera sändning av luae_fs Intyg', function() {

    var intygsId;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
    });

    describe('Visa signerat luae_fs intyg', function() {

        it('Skapa intyget och gå till intygssidan', function() {
            var intyg = intygFromJsonFactory.defaultLuaefs();
            intygsId = intyg.id;
            restUtil.createIntyg(intyg);
            SokSkrivIntygPage.selectPersonnummer('19121212-1212');
            SokSkrivValjIntyg.selectIntygById(intygsId);

            expect(IntygPage.isAt()).toBeTruthy();
        });

        it('Skicka intyget', function() {
            IntygPage.skicka.knapp.sendKeys(protractor.Key.SPACE);
            browser.wait(IntygPage.skicka.dialogKnapp.isDisplayed())
                .then(IntygPage.skicka.samtyckeCheckbox.sendKeys(protractor.Key.SPACE))
                .then(IntygPage.skicka.dialogKnapp.sendKeys(protractor.Key.SPACE));


            element.all(by.id('#sendBtn')).then(function(items) {
                expect(items.length).toBe(0);
            });

            // Add a small artificial wait so the revoke can be processed asynchronously by Intygstjänsten. Not pretty...
            browser.sleep(500);
            expect(isIntygSent(intygsId)).toBeTruthy();
        });

    });

    afterAll(function() {
        testdataHelper.deleteIntyg(intygsId);
        specHelper.logout();
        browser.ignoreSynchronization = true;
    });

    function isIntygSent(intygsId) {
        var innerDefer = protractor.promise.defer();
        restUtil.getIntyg(intygsId).then(function(intygBody) {
            var result = IntygPage.hasState(intygBody.body.states, 'SENT');
            innerDefer.fulfill(result);
        });
        return innerDefer.promise;
    }

});
