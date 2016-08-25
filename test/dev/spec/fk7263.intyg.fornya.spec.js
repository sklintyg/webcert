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

/**
 * Created by bennysce on 09/06/15.
 */
/*globals browser,JSON,protractor*/
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.testdata;
var restTestdataHelper = wcTestTools.helpers.restTestdata;
var FkIntygPage = wcTestTools.pages.intyg.fk['7263'].intyg;
var FkUtkastPage = wcTestTools.pages.intyg.fk['7263'].utkast;
var SokSkrivIntyg = wcTestTools.pages.sokSkrivIntyg.pickPatient;
var SokSkrivValjUtkastTyp = wcTestTools.pages.sokSkrivIntyg.valjUtkastType;
var restUtil = wcTestTools.restUtil;
var intygFromJsonFactory = wcTestTools.intygFromJsonFactory;

describe('Generate fk intyg', function() {

    var intygId;
    //var intyg;

    describe('prepare test with intyg', function() {
        it('should generate fk max intyg', function() {
            browser.ignoreSynchronization = false;

            var intyg = intygFromJsonFactory.defaultFK7263();
            intygId = intyg.id;
            restUtil.createIntyg(intyg).then(function(response) {
                intyg = JSON.parse(response.request.body);
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

    var utkastIds = [];

    describe('fornya fk intyg to new utkast', function() {
        it('should view fk intyg', function() {
            FkIntygPage.get(intygId);
            expect(FkIntygPage.isAt()).toBeTruthy();
        });

        it('should fornya intyg and view resulting utkast', function() {
            FkIntygPage.fornyaBtn.click();
            FkIntygPage.fornyaDialogConfirmBtn().click();
            expect(FkUtkastPage.isAt()).toBeTruthy();
        });

        it('should set information based on date', function() {
            // Save id so it can be removed in cleanup stage.
            browser.getCurrentUrl().then(function(url) {
                utkastIds.push(url.split('/').pop());
            });

            browser.ignoreSynchronization = true;
            FkUtkastPage.angeIntygetBaserasPa({
                minUndersokning: {
                    datum:'2015-01-01'
                }
            }).then(function () {
                expect(FkUtkastPage.baserasPa.minUndersokning.input.getAttribute('value'))
                    .toEqual('2015-01-01');
            });
        });

        it('should set capacity for work based on previous intyg last effective date', function () {
            FkUtkastPage.nedsattMed25CheckboxClick()
                .then(function () {
                    expect(FkUtkastPage.nedsatt.med25.alert.isPresent()).toBe(true);
                    return protractor.promise.all([
                        FkUtkastPage.nedsatt.med25.alert.evaluate('lastEffectiveDate').then(),
                        FkUtkastPage.nedsatt.med25.from.getAttribute('value').then()
                    ]);
                })
                .then(function (dateStrings) {
                    var lastEffectiveDate = new Date(dateStrings[0]);
                    var med25FromDate = new Date(dateStrings[1]);
                    expect(lastEffectiveDate.getTime() + 1000 * 60 * 60 * 24)
                        .toEqual(med25FromDate.getTime());
                });
        });

        it('should sign copy', function() {
            browser.ignoreSynchronization = false;
            FkUtkastPage.whenSigneraButtonIsEnabled().then(function() {
                FkUtkastPage.signeraButtonClick();
                expect(FkIntygPage.isAt()).toBeTruthy();
            });
        });
    });

    describe('Förnya through search page', function () {
        it('should go to search page', function () {
            SokSkrivIntyg.get();
            expect(SokSkrivIntyg.isAt()).toBe(true);
        });

        it('finds patient with intyg', function () {
            SokSkrivIntyg.selectPersonnummer('19121212-1212').then(function () {
                expect(SokSkrivValjUtkastTyp.isAt()).toBe(true);
            });
        });

        it('should click fornya button', function () {
            SokSkrivValjUtkastTyp.clickFornyaBtnById(intygId);
            FkIntygPage.fornyaDialogConfirmBtn().click();
            expect(FkUtkastPage.isAt()).toBeTruthy();
            // Save id so it can be removed in cleanup stage.
            browser.getCurrentUrl().then(function(url) {
                utkastIds.push(url.split('/').pop());
            });
        });
    });

    describe('remove test intyg', function() {
        it('should clean up all utkast after the test', function() {
            restTestdataHelper.deleteIntyg(intygId);
            utkastIds.forEach(function (id) {
                restTestdataHelper.deleteUtkast(id); // in case the test breaks before it is signed..
                restTestdataHelper.deleteIntyg(id);
            });
        });
    });
});
