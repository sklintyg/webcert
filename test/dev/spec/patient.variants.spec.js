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

/*globals browser */
/*globals pages */
/*globals describe,it,helpers */
/*globals afterEach,beforeEach */
'use strict';

var specHelper = wcTestTools.helpers.spec;
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;
var SokSkrivValjUtkastType = wcTestTools.pages.sokSkrivIntyg.valjUtkastType;

var HttpBackend = require('http-backend-proxy');
var proxy = new HttpBackend(browser);

describe('Patient lookup variants', function() {
    it('Login through the welcome page with user', function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
    });

    describe('mock backend responses block', function() {

        var patientId = '19121212-1212';

        afterEach(function () {
            browser.clearMockModules();
        });

        beforeEach(function () {
            specHelper.loadHttpBackendMock();
        });

        describe('When patient lookup receives error', function() {
            it('should give option to enter name manually', function () {
                SokSkrivIntygPage.get();
                proxy.whenGET(new RegExp('/api\/person\/' + patientId + '\?.*')).respond(500);
                proxy.whenPOST(/.*/).passThrough();
                proxy.whenGET(/.*/).passThrough();
                SokSkrivIntygPage.selectPersonnummer(patientId);
                expect(SokSkrivValjUtkastType.intygTypeSelector.isPresent()).toBe(false);
                expect(SokSkrivValjUtkastType.intygTypeButton.isPresent()).toBe(false);
                expect(SokSkrivIntygPage.fornamn.isDisplayed()).toBe(true);
                expect(SokSkrivIntygPage.efternamn.isDisplayed()).toBe(true);
            });

            it('should go to intygtype selector after name is entered', function () {
                SokSkrivIntygPage.fornamn.sendKeys('Test');
                SokSkrivIntygPage.efternamn.sendKeys('Testsson');
                SokSkrivIntygPage.namnFortsatt.click();
                expect(SokSkrivValjUtkastType.intygTypeSelector.isDisplayed()).toBe(true);
                expect(SokSkrivValjUtkastType.intygTypeButton.isDisplayed()).toBe(true);
            });
        });

        it('should not be possible to select intygtype when patient is not found', function () {
            SokSkrivIntygPage.get();
            proxy.whenGET(new RegExp('/api\/person\/' + patientId + '\?.*')).respond(200, '{"status":"NOT_FOUND"}');
            proxy.whenPOST(/.*/).passThrough();
            proxy.whenGET(/.*/).passThrough();
            SokSkrivIntygPage.selectPersonnummer(patientId);
            expect(SokSkrivIntygPage.puerror.isDisplayed()).toBe(true);
            expect(SokSkrivValjUtkastType.intygTypeSelector.isPresent()).toBe(false);
            expect(SokSkrivValjUtkastType.intygTypeButton.isPresent()).toBe(false);
        });

        it('should not be possible to create utkast when patient has sekretessmarkering', function () {
            SokSkrivIntygPage.get();
            proxy.whenGET(new RegExp('/api\/person\/' + patientId + '\?.*')).respond(200, '{"person":{"personnummer":"19121212-1212","sekretessmarkering":true,"fornamn":"Tolvan","efternamn":"Tolvansson","postadress":"Svensson, Storgatan 1, PL 1234","postnummer":"12345","postort":"Småmåla"},"status":"FOUND"}');
            proxy.whenPOST(/.*/).passThrough();
            proxy.whenGET(/.*/).passThrough();
            SokSkrivIntygPage.selectPersonnummer(patientId);
            expect(SokSkrivIntygPage.sekretessmarkering.isDisplayed()).toBe(true);
            expect(SokSkrivValjUtkastType.intygTypeSelector.isDisplayed()).toBe(false);
            expect(SokSkrivValjUtkastType.intygTypeButton.isDisplayed()).toBe(false);
        });

        it('should be possible to select intygtype when patient exists without sekretessmarkering', function () {
            SokSkrivIntygPage.get();
            proxy.whenGET(new RegExp('/api\/person\/' + patientId + '\?.*')).respond(200, '{"person":{"personnummer":"19121212-1212","sekretessmarkering":false,"fornamn":"Tolvan","efternamn":"Tolvansson","postadress":"Svensson, Storgatan 1, PL 1234","postnummer":"12345","postort":"Småmåla"},"status":"FOUND"}');
            proxy.whenPOST(/.*/).passThrough();
            proxy.whenGET(/.*/).passThrough();
            SokSkrivIntygPage.selectPersonnummer(patientId);
            expect(SokSkrivIntygPage.puerror.isPresent()).toBe(false);
            expect(SokSkrivIntygPage.sekretessmarkering.isPresent()).toBe(false);
            expect(SokSkrivValjUtkastType.intygTypeSelector.isDisplayed()).toBe(true);
            expect(SokSkrivValjUtkastType.intygTypeButton.isDisplayed()).toBe(true);
        });
    });
});
