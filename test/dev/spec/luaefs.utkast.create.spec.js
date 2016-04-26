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
var UtkastPage = wcTestTools.pages.intyg.luae_fs.utkast;
// var IntygPage = wcTestTools.pages.intyg.fk['luae_fs'].intyg;

fdescribe('Create and Sign luae_fs utkast', function() {

    var utkastId = null;

    describe('Login through the welcome page', function() {
        it('with user', function() {
            browser.ignoreSynchronization = false;
            specHelper.login();
            specHelper.createUtkastForPatient('191212121212', 'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång');
        });
    });

    describe('create luae_fs', function() {

        describe('interact with utkast', function() {

            it('check that smittskydd is displayed', function() {
            //
                // Save id so it can be removed in cleanup stage.
                browser.getCurrentUrl().then(function(url) {
                    utkastId = url.split('/').pop();
                });
            //
            //    UtkastPage.whenSmittskyddIsDisplayed().then(function() {
            //        expect(UtkastPage.getSmittskyddLabelText()).toContain('Avstängning enligt smittskyddslagen på grund av smitta');
            //    });
            //
            });

            describe('fill in luae_fs intyg', function() {

                // speeds up utkast filling by not waiting for angular events, promises etc.
                browser.ignoreSynchronization = true;

                it('Grund - baserat på', function() {
                    var baserasPa = {
                        minUndersokning : {
                            datum: '2016-04-22'
                        }
                    };
                    UtkastPage.angeIntygetBaserasPa(baserasPa);
                });

                it('Andra medicinska utredningar eller underlag', function() {
                    var utredningar = [{
                        underlag: 'Neuropsykiatriskt utlåtande',
                        datum: '2016-04-16',
                        infoOmUtredningen: 'Hämtas hos posten'
                    }];
                    UtkastPage.angeAndraMedicinskaUtredningar(utredningar);
                });



                //it('resor form 6a', function() {
                //    UtkastPage.travelRadioButtonJaClick();
                //    var val = UtkastPage.getCheckedTravelRadioButtonValue();
                //    expect(val).toBe('JA');
                //});
                //
                //it('can sign', function() {
                //
                //    // reset
                //    browser.ignoreSynchronization = false;
                //
                //    UtkastPage.whenSigneraButtonIsEnabled().then(function() {
                //        UtkastPage.signeraButtonClick();
                //        expect(IntygPage.isAt()).toBeTruthy();
                //    });
                //});
            });
        });
    });

    describe('remove test intyg', function() {
        it('should clean up all utkast after the test', function() {
            //testdataHelper.deleteIntyg(utkastId);
            testdataHelper.deleteUtkast(utkastId);
        });
    });

});
