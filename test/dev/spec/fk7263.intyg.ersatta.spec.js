/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

/**
 * Created by marced on 2017-03-27.
 */
/*globals browser,JSON*/
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var restTestdataHelper = wcTestTools.helpers.restTestdata;
var FkIntygPage = wcTestTools.pages.intyg.fk['7263'].intyg;
var FkUtkastPage = wcTestTools.pages.intyg.fk['7263'].utkast;
var restUtil = wcTestTools.restUtil;
var intygFromJsonFactory = wcTestTools.intygFromJsonFactory;

describe('Verify replace intyg ', function() {

    var intygId;

    describe('prepare test with intyg', function() {
        it('should generate fk max intyg', function() {
            browser.ignoreSynchronization = false;
            var intyg = intygFromJsonFactory.defaultFK7263();
            intygId = intyg.id;
            restUtil.createIntyg(intyg).then(function(response) {
                var intyg = JSON.parse(response.request.body);
                expect(intyg.id).not.toBeNull();
            }, function(error) {
                console.log('Error calling createIntyg');
            });
        });
    });

    describe('Login through the welcome page', function() {
        it('with default user', function() {
            specHelper.login();
        });
    });

    describe('replace a signed intyg with new utkast', function() {
        it('should view fk intyg', function() {
            FkIntygPage.get(intygId);
            expect(FkIntygPage.isAt()).toBeTruthy();
        });

        it('should replace intyg and view resulting utkast', function() {
            expect(FkIntygPage.replaceBtn().isPresent()).toBe(true);
            FkIntygPage.replaceBtn().click();
            FkIntygPage.replaceDialogConfirmBtn().click();
            expect(FkUtkastPage.isAt()).toBeTruthy();
        });

        it('should view original replaced fk intyg', function() {
            FkIntygPage.get(intygId);
            expect(FkIntygPage.isAt()).toBeTruthy();
        });

        it('should no longer show send/copy/renew/replace buttons', function() {
            FkIntygPage.get(intygId);
            expect(FkIntygPage.isAt()).toBeTruthy();
            expect(FkIntygPage.copy.button.isPresent()).toBe(false);
            expect(FkIntygPage.skicka.knapp.isPresent()).toBe(false);
            expect(FkIntygPage.replaceBtn().isPresent()).toBe(false);

            //fornya exists for fk7263
            expect(FkIntygPage.fornya.button.isPresent()).toBe(false);

        });

        it('should show warning message with link to replacing utkast', function() {
            expect(element(by.id('wc-intyg-related-other-intyg-message')).isDisplayed()).toBe(true);
            element(by.css('#wc-intyg-related-other-intyg-message a')).click();
            expect(FkUtkastPage.isAt()).toBeTruthy();
        });
    });

    afterAll(function() {
        if (intygId) {
            restTestdataHelper.deleteIntyg(intygId);
        }
    });

});
