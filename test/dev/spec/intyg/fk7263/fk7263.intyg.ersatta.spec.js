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
    var utkastId;

    describe('prepare test with intyg', function() {
        it('should generate fk max intyg', function() {
            browser.ignoreSynchronization = false;
            var intyg = intygFromJsonFactory.defaultFK7263();
            intygId = intyg.id;
            restUtil.createIntyg(intyg).then(function(response) {
                var intyg = JSON.parse(response.request.body);
                expect(intyg.id).not.toBeNull();
            }, function(error) {
                logger.error('Error calling createIntyg');
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
            expect(element(by.id('ersattUtkastPaborjat')).isPresent()).toBe(false);
            expect(FkIntygPage.replaceDialogContinueBtn().isPresent()).toBe(false);
            FkIntygPage.replaceDialogConfirmBtn().click();
            expect(FkUtkastPage.isAt()).toBeTruthy();
            browser.getCurrentUrl().then(function(url) {
                utkastId = url.split('/').pop();
            });
        });

        it('should view original replaced fk intyg', function() {
            FkIntygPage.get(intygId);
            expect(FkIntygPage.isAt()).toBeTruthy();
        });

        describe('Expected functionality should still be available', function() {
            it('should still show send/copy/renew/replace buttons, since the replacing utkast has not been signed', function() {
                FkIntygPage.get(intygId);
                expect(FkIntygPage.isAt()).toBeTruthy();
                expect(FkIntygPage.skicka.knapp.isPresent()).toBe(true);
                expect(FkIntygPage.replaceBtn().isPresent()).toBe(true);

                //fornya exists for fk7263 (and SMI)
                expect(FkIntygPage.fornya.button.isPresent()).toBe(true);
            });

            it('should send without error, since the replacing utkast has not been signed', function() {
                FkIntygPage.skicka.knapp.click();
                FkIntygPage.skicka.dialogKnapp.click();
            });

            it('should renew without error, since the replacing utkast has not been signed', function() {
                 FkIntygPage.fornya.button.click();
                 FkIntygPage.fornya.dialogConfirmButton.click();
                 expect(FkUtkastPage.isAt()).toBeTruthy();
                 FkIntygPage.get(intygId);
            });
        });



        it('should still not show warning message with link to replacing utkast, since utkast has not been signed',
            function() {
                expect(element(by.id('wc-intyg-related-other-intyg-message')).isPresent()).toBe(false);
            });

        it('should display warning that replacement utkast exists when clicking on replace', function() {
            FkIntygPage.replaceBtn().click();
            expect(element(by.id('ersattUtkastPaborjat')).isDisplayed()).toBe(true);
            expect(FkIntygPage.replaceDialogContinueBtn().isPresent()).toBe(true);
        });

        it('should take us to the existing utkast when clicking the continue button', function() {
            FkIntygPage.replaceDialogContinueBtn().click();
            expect(FkUtkastPage.isAt()).toBeTruthy();
            browser.getCurrentUrl().then(function(url) {
                var replacementUtkastId = url.split('/').pop();
                expect(replacementUtkastId === utkastId).toBe(true);
            });
        });
    });

    afterAll(function() {
        if (intygId) {
            restTestdataHelper.deleteIntyg(intygId);
        }
    });

});
