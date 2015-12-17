/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

/*globals pages */
/*globals describe,it,helpers */
'use strict';

var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.testdata;
var UtkastPage = wcTestTools.pages.intygpages.fk7263Utkast;
var IntygPage = wcTestTools.pages.intygpages.fkIntyg;

describe('Create and Sign FK utkast', function() {

    var utkastId = null;

    describe('Login through the welcome page', function() {
        it('with user', function() {
            browser.ignoreSynchronization = false;
            specHelper.login();
            specHelper.createUtkastForPatient('191212121212', 'fk7263');
        });
    });

    describe('create fk', function(){

        describe('interact with utkast', function() {

            it('check that smittskydd is displayed', function() {

                // Save id so it can be removed in cleanup stage.
                browser.getCurrentUrl().then(function(url) {
                    utkastId = url.split('/').pop();
                });

                UtkastPage.whenSmittskyddIsDisplayed().then(function() {
                    expect(UtkastPage.getSmittskyddLabelText()).toContain('Avstängning enligt smittskyddslagen på grund av smitta');
                });

            });

            describe('fill in fk intyg', function() {

                // speeds up utkast filling by not waiting for angular events, promises etc.
                browser.ignoreSynchronization = true;

                it('nedsatt form8b', function() {
                    UtkastPage.smittskyddCheckboxClick();
                    UtkastPage.nedsattMed25CheckboxClick();
                });

                it('resor form 6a', function() {
                    UtkastPage.travelRadioButtonJaClick();
                    var val = UtkastPage.getCheckedTravelRadioButtonValue();
                    expect(val).toBe('JA');
                });

                it('can sign', function() {

                    // reset
                    browser.ignoreSynchronization = false;

                    UtkastPage.whenSigneraButtonIsEnabled().then(function() {
                        UtkastPage.signeraButtonClick();
                        expect(IntygPage.isAt()).toBeTruthy();
                    });
                });
            });
        });
    });

    describe('remove test intyg', function() {
        it('should clean up all utkast after the test', function() {
            testdataHelper.deleteIntyg(utkastId);
            testdataHelper.deleteUtkast(utkastId);
        });
    });

});
