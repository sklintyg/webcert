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
var EC = protractor.ExpectedConditions;

// Validera att man med roll Tandläkare ej kan se Ärende tillhörande luae_fs intyg samt
// att Ärendet ej inkluderas i summeringssiffran i Fråga-svar tabben
xdescribe('fragasvar.intygstyp.filtrering', function() {

    var intygsId;
    var arendeId = 'luaefs-arende-avstmn';

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
        //testdataHelper.deleteAllUtkast();
        //testdataHelper.deleteAllIntyg();
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
                .then(IntygPage.skicka.dialogKnapp.sendKeys(protractor.Key.SPACE));


            element.all(by.id('#sendBtn')).then(function(items) {
                expect(items.length).toBe(0);
            });

            // Add a small artificial wait so the send can be processed asynchronously by Intygstjänsten. Not pretty...
            browser.sleep(500);
            expect(isIntygSent(intygsId)).toBeTruthy();
        });

        it('Skapa ärende på intyget', function() {
            testdataHelper.createArendeFromTemplate('luae_fs', intygsId, arendeId, 'Hur är det med arbetstiden?',
                'AVSTMN', 'PENDING_INTERNAL_ACTION');
        });

        it('Klicka på tabben för Fråga/svar', function() {
            element(by.css('a[ng-href="/web/dashboard#/unhandled-qa"]')).click();
            expect(element(by.id('stat-unitstat-unhandled-question-count')).getText()).toBe('1');
            expect(element(by.css('.table-qa tr td button')).getText()).toBe('Visa');
        });

        it('Byt läkarens roll till TANDLAKARE mha testbarhets-API, klicka på tabben igen', function() {
            browser.getCurrentUrl().then(function(url) {
                browser.driver.get(browser.baseUrl + '/authtestability/user/role/TANDLAKARE').then(function() {
                    browser.get(url);
                });
            });
        });

        it('Verifiera att tandläkaren INTE ser något ärende/fråga längre och att ingen siffra visas på tabben', function() {
            browser.wait(EC.invisibilityOf(element(by.id('stat-unitstat-unhandled-question-count'))), 5000);
            browser.wait(EC.invisibilityOf(element(by.id('.table-qa tr td'))), 5000);
        });
    });


    afterAll(function() {
        testdataHelper.deleteUtkast(intygsId);
        testdataHelper.deleteIntyg(intygsId);
        testdataHelper.deleteAllArenden();
        specHelper.logout();
        browser.ignoreSynchronization = false;
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
